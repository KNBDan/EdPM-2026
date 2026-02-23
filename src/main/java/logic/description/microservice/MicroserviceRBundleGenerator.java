package logic.description.microservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicroserviceRBundleGenerator {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b[A-Za-z][A-Za-z0-9_]*\\b");

    public MicroserviceGenerationResult generateBundle(String sequentialCode, Path outputRoot) throws IOException {
        if (sequentialCode == null) {
            sequentialCode = "";
        }

        Files.createDirectories(outputRoot);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path bundleDir = outputRoot.resolve("edpm_microservice_" + stamp);
        Files.createDirectories(bundleDir);

        Map<String, String> splitCode = splitSequentialCode(sequentialCode);
        String prelude = splitCode.get("prelude");
        String mainProgram = splitCode.get("main");
        List<JobDefinition> jobs = parseJobs(mainProgram);
        String postEffectsScript = buildPostEffectsScript(mainProgram);

        writeString(bundleDir.resolve("common_prelude.R"), prelude);
        writeString(bundleDir.resolve("orchestrator.R"), buildOrchestratorScript());
        writeString(bundleDir.resolve("worker_exec.R"), buildWorkerScript());
        writeString(bundleDir.resolve("run_local.R"), buildRunLocalScript());
        writeString(bundleDir.resolve("post_effects.R"), postEffectsScript);
        writeTopologyCsv(bundleDir.resolve("topology.csv"), jobs);

        List<String> generated = List.of(
                "topology.csv",
                "common_prelude.R",
                "orchestrator.R",
                "worker_exec.R",
                "run_local.R",
                "post_effects.R"
        );
        return new MicroserviceGenerationResult(bundleDir, generated);
    }

    private void writeString(Path target, String content) throws IOException {
        Files.writeString(target, content, StandardCharsets.UTF_8);
    }

    private void writeTopologyCsv(Path target, List<JobDefinition> jobs) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("id,lhs,rhs,deps\n");
        for (JobDefinition job : jobs) {
            String deps = String.join(";", job.deps);
            sb.append(job.id).append(",")
              .append(csv(job.lhs)).append(",")
              .append(csv(job.rhs)).append(",")
              .append(csv(deps)).append("\n");
        }
        writeString(target, sb.toString());
    }

    private String csv(String value) {
        String v = value == null ? "" : value;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private Map<String, String> splitSequentialCode(String sequentialCode) {
        StringBuilder prelude = new StringBuilder();
        StringBuilder main = new StringBuilder();
        int mainStartIndex = findMainStartIndex(sequentialCode);
        int lineIndex = 0;

        for (String line : sequentialCode.split("\\R")) {
            if (lineIndex >= mainStartIndex) {
                main.append(line).append("\n");
            } else {
                prelude.append(line).append("\n");
            }
            lineIndex += 1;
        }

        // Safety net: if parser cut V*_func block out of prelude, append it explicitly.
        String preludeText = prelude.toString();
        if (!preludeText.contains("_func <- function(")) {
            String microBlock = extractMicroserviceBlock(sequentialCode);
            if (!microBlock.isEmpty()) {
                prelude.append("\n").append(microBlock).append("\n");
            }
        }

        Map<String, String> result = new HashMap<>();
        result.put("prelude", prelude.toString());
        result.put("main", main.toString());
        return result;
    }

    private int findMainStartIndex(String sequentialCode) {
        String[] lines = sequentialCode.split("\\R", -1);
        int headerIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (isMainProgramHeader(lines[i].trim())) {
                headerIndex = i + 1;
                break;
            }
        }
        if (headerIndex >= 0) {
            return headerIndex;
        }

        // Fallback 1: typical start of generated main block.
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.isEmpty() && Character.isWhitespace(line.charAt(0))) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.matches("^N\\s*<-.*")) {
                return i;
            }
        }

        // Fallback: first top-level assignment that is not a function definition.
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.isEmpty() && Character.isWhitespace(line.charAt(0))) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (!trimmed.contains("<-")) {
                continue;
            }
            if (trimmed.contains("function(")) {
                continue;
            }
            return i;
        }
        return lines.length;
    }

    private String extractMicroserviceBlock(String sequentialCode) {
        String[] lines = sequentialCode.split("\\R", -1);
        int start = -1;
        int end = lines.length;
        for (int i = 0; i < lines.length; i++) {
            String lower = lines[i].toLowerCase();
            if (lower.contains("РјРёРєСЂРѕСЃРµСЂРІРёСЃ") || lower.contains("microservice")) {
                start = i;
                break;
            }
        }
        if (start < 0) {
            return "";
        }
        for (int i = start + 1; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.startsWith("# --- ==== [") && trimmed.contains("==== ---")
                    && (trimmed.toLowerCase().contains("РѕСЃРЅРѕРІРЅР°СЏ РїСЂРѕРіСЂР°РјРјР°")
                    || trimmed.toLowerCase().contains("main program"))) {
                end = i;
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString().trim();
    }

    private boolean isMainProgramHeader(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        if (!trimmed.startsWith("# --- ==== [") || !trimmed.contains("==== ---")) {
            return false;
        }
        String lower = trimmed.toLowerCase();
        return lower.contains("РѕСЃРЅРѕРІРЅР°СЏ РїСЂРѕРіСЂР°РјРјР°")
                || lower.contains("main program");
    }

    private List<JobDefinition> parseJobs(String mainProgram) {
        List<JobDefinition> jobs = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        for (String line : mainProgram.split("\\R")) {
            if (!line.isEmpty() && Character.isWhitespace(line.charAt(0))) {
                // Only top-level statements become jobs.
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.contains("function(")) {
                continue;
            }
            if (!trimmed.contains("<-")) {
                continue;
            }
            String lhs = trimmed.split("<-", 2)[0].trim();
            if (!isCoreAssignmentLhs(lhs)) {
                continue;
            }
            lines.add(trimmed);
        }

        Set<String> producedVars = new LinkedHashSet<>();
        for (String line : lines) {
            String[] parts = line.split("<-", 2);
            if (parts.length < 2) {
                continue;
            }
            String lhs = parts[0].trim();
            if (!lhs.isEmpty()) {
                producedVars.add(lhs);
            }
        }

        int id = 1;
        for (String line : lines) {
            String lhs;
            String rhs;
            if (line.contains("<-")) {
                String[] parts = line.split("<-", 2);
                if (parts.length < 2) {
                    continue;
                }
                lhs = parts[0].trim();
                rhs = parts[1].trim();
            } else {
                lhs = "__effect_" + id;
                rhs = line;
            }
            if (lhs.isEmpty() || rhs.isEmpty()) {
                continue;
            }
            List<String> deps = extractDependencies(rhs, lhs, producedVars);
            jobs.add(new JobDefinition(id, lhs, rhs, deps));
            id += 1;
        }
        return jobs;
    }

    private boolean isCoreAssignmentLhs(String lhs) {
        if (lhs == null || lhs.isEmpty()) {
            return false;
        }
        if (lhs.equals("N") || lhs.equals("i") || lhs.equals("FP")) {
            return true;
        }
        return lhs.matches("^R\\d+$");
    }

    private String buildPostEffectsScript(String mainProgram) {
        StringBuilder body = new StringBuilder();
        for (String line : mainProgram.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                body.append("\n");
                continue;
            }
            if (trimmed.startsWith("#")) {
                body.append(line).append("\n");
                continue;
            }
            if (trimmed.contains("function(")) {
                continue;
            }
            if (trimmed.contains("<-")) {
                String lhs = trimmed.split("<-", 2)[0].trim();
                if (isCoreAssignmentLhs(lhs)) {
                    continue;
                }
            }
            body.append(line).append("\n");
        }

        return ""
                + "source('common_prelude.R')\n"
                + "if (file.exists('runtime/state.rds')) {\n"
                + "  state <- readRDS('runtime/state.rds')\n"
                + "  for (nm in names(state)) assign(nm, state[[nm]], envir = .GlobalEnv)\n"
                + "}\n"
                + "dir.create('runtime', showWarnings = FALSE, recursive = TRUE)\n"
                + "setwd(getwd())\n"
                + "\n"
                + "# --- Post-effects block from generated main program ---\n"
                + body
                + "\n";
    }

    private List<String> extractDependencies(String rhs, String lhs, Set<String> producedVars) {
        Set<String> deps = new LinkedHashSet<>();
        Matcher matcher = IDENTIFIER_PATTERN.matcher(rhs);
        while (matcher.find()) {
            String token = matcher.group();
            if (token.equals(lhs)) {
                continue;
            }
            if (isReservedToken(token)) {
                continue;
            }
            if (token.endsWith("_func")) {
                continue;
            }
            // Core EdPM runtime variables must always be tracked as dependencies.
            if (token.matches("^R\\d+$") || token.equals("N") || token.equals("i") || token.equals("FP")) {
                deps.add(token);
                continue;
            }
            if (producedVars.contains(token)) {
                deps.add(token);
            }
        }
        return new ArrayList<>(deps);
    }

    private boolean isReservedToken(String token) {
        return token.equals("if")
                || token.equals("for")
                || token.equals("while")
                || token.equals("repeat")
                || token.equals("TRUE")
                || token.equals("FALSE")
                || token.equals("NULL");
    }

    private String buildRunLocalScript() {
        return ""
                + "args <- commandArgs(trailingOnly = TRUE)\n"
                + "worker_count <- if (length(args) >= 1) as.integer(args[[1]]) else 2L\n"
                + "seed_value <- if (length(args) >= 2) as.integer(args[[2]]) else 12345L\n"
                + "strict_repro <- if (length(args) >= 3) as.integer(args[[3]]) != 0 else TRUE\n"
                + "if (is.na(worker_count) || worker_count < 1) worker_count <- 2L\n"
                + "if (is.na(seed_value)) seed_value <- 12345L\n"
                + "if (is.na(strict_repro)) strict_repro <- TRUE\n"
                + "if (strict_repro) worker_count <- 1L\n"
                + "set.seed(seed_value)\n"
                + "\n"
                + "dir.create('runtime', showWarnings = FALSE, recursive = TRUE)\n"
                + "unlink(list.files('runtime', full.names = TRUE, recursive = TRUE), recursive = TRUE, force = TRUE)\n"
                + "dir.create('runtime', showWarnings = FALSE, recursive = TRUE)\n"
                + "writeLines(c(\n"
                + "  sprintf('seed=%d', seed_value),\n"
                + "  sprintf('worker_count=%d', worker_count),\n"
                + "  sprintf('strict_repro=%s', ifelse(strict_repro, '1', '0'))\n"
                + "), 'runtime/run_config.txt')\n"
                + "\n"
                + "rscript_bin <- file.path(R.home('bin'), ifelse(.Platform$OS.type == 'windows', 'Rscript.exe', 'Rscript'))\n"
                + "for (i in seq_len(worker_count)) {\n"
                + "  out_log <- sprintf('runtime/worker_%02d.log', i)\n"
                + "  worker_seed <- as.integer(seed_value + i - 1L)\n"
                + "  system2(rscript_bin, args = c('worker_exec.R', ifelse(strict_repro, '1', '0'), as.character(worker_seed), as.character(i)), wait = FALSE, stdout = out_log, stderr = out_log)\n"
                + "}\n"
                + "\n"
                + "source('orchestrator.R')\n";
    }

    private String buildOrchestratorScript() {
        return ""
                + "jobs <- read.csv('topology.csv', stringsAsFactors = FALSE)\n"
                + "if (nrow(jobs) == 0) stop('No jobs in topology.csv')\n"
                + "if (!exists('seed_value', inherits = FALSE)) seed_value <- 12345L\n"
                + "if (!exists('worker_count', inherits = FALSE)) worker_count <- 1L\n"
                + "if (!exists('strict_repro', inherits = FALSE)) strict_repro <- TRUE\n"
                + "\n"
                + "dir.create('runtime', showWarnings = FALSE, recursive = TRUE)\n"
                + "dir.create('runtime/jobs_pending', showWarnings = FALSE, recursive = TRUE)\n"
                + "dir.create('runtime/jobs_working', showWarnings = FALSE, recursive = TRUE)\n"
                + "dir.create('runtime/results', showWarnings = FALSE, recursive = TRUE)\n"
                + "unlink('runtime/stop.signal')\n"
                + "saveRDS(list(), 'runtime/state.rds')\n"
                + "summary_csv <- 'runtime/run_summary.csv'\n"
                + "summary_txt <- 'runtime/run_summary.txt'\n"
                + "writeLines('time,event,job_id,lhs,status,message', summary_csv)\n"
                + "\n"
                + "done_vars <- character(0)\n"
                + "queued_ids <- integer(0)\n"
                + "finished_ids <- integer(0)\n"
                + "completed_order <- character(0)\n"
                + "total_jobs <- nrow(jobs)\n"
                + "idle_ticks <- 0\n"
                + "stagnation_ticks <- 0\n"
                + "\n"
                + "csv_safe <- function(x) {\n"
                + "  if (is.null(x) || length(x) == 0) return('')\n"
                + "  v <- as.character(x)\n"
                + "  v <- gsub('\"', \"'\", v, fixed = TRUE)\n"
                + "  v <- gsub(',', ';', v, fixed = TRUE)\n"
                + "  v\n"
                + "}\n"
                + "\n"
                + "summary_add <- function(event, job_id, lhs, status, message) {\n"
                + "  row <- sprintf('%s,%s,%s,%s,%s,%s',\n"
                + "    format(Sys.time(), '%Y-%m-%d %H:%M:%S'),\n"
                + "    csv_safe(event), csv_safe(job_id), csv_safe(lhs), csv_safe(status), csv_safe(message))\n"
                + "  write(row, file = summary_csv, append = TRUE)\n"
                + "}\n"
                + "summary_add('config', '', '', 'ok', sprintf('seed=%d;worker_count=%d;strict_repro=%s', as.integer(seed_value), as.integer(worker_count), ifelse(strict_repro, '1', '0')))\n"
                + "\n"
                + "if (strict_repro) {\n"
                + "  produced_lhs <- unique(as.character(jobs$lhs))\n"
                + "  known_roots <- c('N', 'i', 'FP')\n"
                + "  missing_producers <- character(0)\n"
                + "  for (i in seq_len(nrow(jobs))) {\n"
                + "    dep_str <- jobs$deps[i]\n"
                + "    if (is.na(dep_str) || dep_str == '') next\n"
                + "    deps <- unlist(strsplit(dep_str, ';', fixed = TRUE))\n"
                + "    deps <- deps[deps != '']\n"
                + "    for (d in deps) {\n"
                + "      if (!(d %in% produced_lhs) && !(d %in% known_roots)) {\n"
                + "        missing_producers <- c(missing_producers, sprintf('%s needs %s', jobs$lhs[i], d))\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "  if (length(missing_producers) > 0) {\n"
                + "    missing_producers <- unique(missing_producers)\n"
                + "    msg <- paste('Strict mode missing producer(s):', paste(missing_producers, collapse = ' | '))\n"
                + "    summary_add('model_error', '', '', 'error', msg)\n"
                + "    stop(msg)\n"
                + "  }\n"
                + "}\n"
                + "\n"
                + "deps_ready <- function(dep_str, done_set) {\n"
                + "  if (is.na(dep_str) || dep_str == '') return(TRUE)\n"
                + "  deps <- unlist(strsplit(dep_str, ';', fixed = TRUE))\n"
                + "  deps <- deps[deps != '']\n"
                + "  if (length(deps) == 0) return(TRUE)\n"
                + "  all(deps %in% done_set)\n"
                + "}\n"
                + "\n"
                + "enqueue_ready <- function() {\n"
                + "  for (i in seq_len(nrow(jobs))) {\n"
                + "    job_id <- jobs$id[i]\n"
                + "    if (job_id %in% queued_ids) next\n"
                + "    if (!deps_ready(jobs$deps[i], done_vars)) next\n"
                + "    job <- list(id = job_id, lhs = jobs$lhs[i], rhs = jobs$rhs[i], deps = jobs$deps[i])\n"
                + "    saveRDS(job, sprintf('runtime/jobs_pending/job_%04d.rds', job_id))\n"
                + "    queued_ids <<- c(queued_ids, job_id)\n"
                + "    cat(sprintf('Queued job #%d: %s <- %s\\n', job_id, job$lhs, job$rhs))\n"
                + "    summary_add('queued', job_id, job$lhs, 'ok', '')\n"
                + "  }\n"
                + "}\n"
                + "\n"
                + "enqueue_ready()\n"
                + "while (length(finished_ids) < total_jobs) {\n"
                + "  result_files <- list.files('runtime/results', pattern = '^result_.*\\\\.rds$', full.names = TRUE)\n"
                + "  if (length(result_files) == 0) {\n"
                + "    Sys.sleep(0.2)\n"
                + "    enqueue_ready()\n"
                + "    idle_ticks <- idle_ticks + 1\n"
                + "    pending_now <- list.files('runtime/jobs_pending', pattern = '^job_.*\\\\.rds$', full.names = TRUE)\n"
                + "    working_now <- list.files('runtime/jobs_working', pattern = '^job_.*\\\\.rds$', full.names = TRUE)\n"
                + "    if (length(pending_now) == 0 && length(working_now) == 0 && length(queued_ids) < total_jobs) {\n"
                + "      stagnation_ticks <- stagnation_ticks + 1\n"
                + "      if (strict_repro && stagnation_ticks >= 5) {\n"
                + "        unresolved_idx <- which(!(jobs$id %in% queued_ids))\n"
                + "        unresolved <- jobs[unresolved_idx, , drop = FALSE]\n"
                + "        unresolved_msgs <- character(0)\n"
                + "        for (k in seq_len(nrow(unresolved))) {\n"
                + "          dep_str <- unresolved$deps[k]\n"
                + "          missing <- character(0)\n"
                + "          if (!is.na(dep_str) && dep_str != '') {\n"
                + "            deps <- unlist(strsplit(dep_str, ';', fixed = TRUE))\n"
                + "            deps <- deps[deps != '']\n"
                + "            missing <- deps[!(deps %in% done_vars)]\n"
                + "          }\n"
                + "          unresolved_msgs <- c(unresolved_msgs, sprintf('%s missing [%s]', unresolved$lhs[k], paste(missing, collapse = ';')))\n"
                + "        }\n"
                + "        deadlock_msg <- paste('Strict mode deadlock/cycle detected:', paste(unresolved_msgs, collapse = ' | '))\n"
                + "        summary_add('deadlock', '', '', 'error', deadlock_msg)\n"
                + "        stop(deadlock_msg)\n"
                + "      }\n"
                + "    } else {\n"
                + "      stagnation_ticks <- 0\n"
                + "    }\n"
                + "    if (idle_ticks > 3000) stop('Timeout waiting for workers/results')\n"
                + "    next\n"
                + "  }\n"
                + "  idle_ticks <- 0\n"
                + "  stagnation_ticks <- 0\n"
                + "  for (rf in result_files) {\n"
                + "    res <- readRDS(rf)\n"
                + "    unlink(rf)\n"
                + "    if (!(res$id %in% finished_ids)) {\n"
                + "      finished_ids <- c(finished_ids, res$id)\n"
                + "      if (identical(res$status, 'ok')) {\n"
                + "        if (!startsWith(res$lhs, '__effect_')) {\n"
                + "          done_vars <- unique(c(done_vars, res$lhs))\n"
                + "        }\n"
                + "        completed_order <- c(completed_order, res$lhs)\n"
                + "        cat(sprintf('Completed: %s\\n', res$lhs))\n"
                + "        summary_add('completed', res$id, res$lhs, 'ok', '')\n"
                + "      } else {\n"
                + "        summary_add('failed', res$id, res$lhs, 'error', res$message)\n"
                + "        stop(sprintf('Worker failed for %s: %s', res$lhs, res$message))\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "  enqueue_ready()\n"
                + "}\n"
                + "\n"
                + "file.create('runtime/stop.signal')\n"
                + "state <- readRDS('runtime/state.rds')\n"
                + "cat('\\nMicroservice run completed. Produced variables:\\n')\n"
                + "print(names(state))\n"
                + "post_effects_error <- NULL\n"
                + "if (file.exists('post_effects.R')) {\n"
                + "  cat('\\nRunning post-effects (plots/xes/csv)...\\n')\n"
                + "  tryCatch({\n"
                + "    source('post_effects.R')\n"
                + "    summary_add('post_effects', '', '', 'ok', '')\n"
                + "  }, error = function(e) {\n"
                + "    post_effects_error <<- as.character(e$message)\n"
                + "    summary_add('post_effects', '', '', 'error', post_effects_error)\n"
                + "  })\n"
                + "}\n"
                + "cleanup_dir_if_empty <- function(path) {\n"
                + "  if (!dir.exists(path)) return(invisible(FALSE))\n"
                + "  files <- list.files(path, all.files = FALSE, no.. = TRUE)\n"
                + "  if (length(files) == 0) {\n"
                + "    unlink(path, recursive = TRUE, force = TRUE)\n"
                + "    return(invisible(TRUE))\n"
                + "  }\n"
                + "  invisible(FALSE)\n"
                + "}\n"
                + "cleanup_dir_if_empty('runtime/jobs_pending')\n"
                + "cleanup_dir_if_empty('runtime/jobs_working')\n"
                + "cleanup_dir_if_empty('runtime/results')\n"
                + "summary_lines <- c(\n"
                + "  sprintf('Seed: %d', as.integer(seed_value)),\n"
                + "  sprintf('Worker count: %d', as.integer(worker_count)),\n"
                + "  sprintf('Strict reproducible: %s', ifelse(strict_repro, 'TRUE', 'FALSE')),\n"
                + "  sprintf('Total jobs: %d', total_jobs),\n"
                + "  sprintf('Completed jobs: %d', length(finished_ids)),\n"
                + "  sprintf('Completion order: %s', paste(completed_order, collapse = ' -> ')),\n"
                + "  sprintf('Produced variables: %s', paste(names(state), collapse = ', ')),\n"
                + "  sprintf('Summary CSV: %s', summary_csv),\n"
                + "  sprintf('State file: %s', 'runtime/state.rds')\n"
                + ")\n"
                + "writeLines(summary_lines, summary_txt)\n"
                + "if (!is.null(post_effects_error)) {\n"
                + "  stop(post_effects_error)\n"
                + "}\n";
    }

    private String buildWorkerScript() {
        return ""
                + "source('common_prelude.R')\n"
                + "\n"
                + "# Compatibility wrapper: generated models may pass complexity codes outside 1..4.\n"
                + "if (exists('V', mode = 'function')) {\n"
                + "  V_orig_edpm <- V\n"
                + "  V <- function(m, S, V, O) {\n"
                + "    mm <- suppressWarnings(as.integer(m))\n"
                + "    if (is.na(mm)) mm <- 1L\n"
                + "    if (mm < 1L || mm > 4L) mm <- 1L\n"
                + "    V_orig_edpm(mm, S, V, O)\n"
                + "  }\n"
                + "}\n"
                + "args <- commandArgs(trailingOnly = TRUE)\n"
                + "strict_repro <- if (length(args) >= 1) as.integer(args[[1]]) != 0 else TRUE\n"
                + "worker_seed <- if (length(args) >= 2) as.integer(args[[2]]) else 12345L\n"
                + "worker_index <- if (length(args) >= 3) as.integer(args[[3]]) else 1L\n"
                + "if (is.na(strict_repro)) strict_repro <- TRUE\n"
                + "if (is.na(worker_seed)) worker_seed <- 12345L\n"
                + "if (is.na(worker_index) || worker_index < 1) worker_index <- 1L\n"
                + "set.seed(worker_seed)\n"
                + "\n"
                + "dir.create('runtime', showWarnings = FALSE, recursive = TRUE)\n"
                + "dir.create('runtime/jobs_pending', showWarnings = FALSE, recursive = TRUE)\n"
                + "dir.create('runtime/jobs_working', showWarnings = FALSE, recursive = TRUE)\n"
                + "dir.create('runtime/results', showWarnings = FALSE, recursive = TRUE)\n"
                + "\n"
                + "state_file <- 'runtime/state.rds'\n"
                + "lock_file <- paste0(state_file, '.lock')\n"
                + "\n"
                + "acquire_lock <- function(path) {\n"
                + "  repeat {\n"
                + "    ok <- file.create(path)\n"
                + "    if (ok) return(invisible(TRUE))\n"
                + "    Sys.sleep(0.03)\n"
                + "  }\n"
                + "}\n"
                + "\n"
                + "release_lock <- function(path) {\n"
                + "  if (file.exists(path)) unlink(path)\n"
                + "}\n"
                + "\n"
                + "default_value_for <- function(name) {\n"
                + "  if (grepl('^R', name)) {\n"
                + "    return(data.frame(R = 0, ID_Out = I(list(0))))\n"
                + "  }\n"
                + "  if (grepl('^NV', name)) {\n"
                + "    return(data.frame(S = 0, ID = I(list(0))))\n"
                + "  }\n"
                + "  if (name %in% c('N', 'i', 'FP')) {\n"
                + "    return(0)\n"
                + "  }\n"
                + "  return(0)\n"
                + "}\n"
                + "\n"
                + "ensure_seed <- function(name, state, env) {\n"
                + "  if (is.null(name) || is.na(name) || name == '') return(state)\n"
                + "  if (!exists(name, envir = env, inherits = FALSE)) {\n"
                + "    seed <- default_value_for(name)\n"
                + "    assign(name, seed, envir = env)\n"
                + "    state[[name]] <- seed\n"
                + "  }\n"
                + "  state\n"
                + "}\n"
                + "\n"
                + "extract_missing_name <- function(msg) {\n"
                + "  m1 <- regexec(\"object '([^']+)' not found\", msg)\n"
                + "  p1 <- regmatches(msg, m1)[[1]]\n"
                + "  if (length(p1) >= 2) return(p1[2])\n"
                + "  m2 <- regexec(\"РѕР±СЉРµРєС‚ '([^']+)' РЅРµ РЅР°Р№РґРµРЅ\", msg)\n"
                + "  p2 <- regmatches(msg, m2)[[1]]\n"
                + "  if (length(p2) >= 2) return(p2[2])\n"
                + "  q <- regexec(\"'([^']+)'\", msg)\n"
                + "  pq <- regmatches(msg, q)[[1]]\n"
                + "  if (length(pq) >= 2) return(pq[2])\n"
                + "  return(NA_character_)\n"
                + "}\n"
                + "\n"
                + "claim_job <- function() {\n"
                + "  pending <- list.files('runtime/jobs_pending', pattern = '^job_.*\\\\.rds$', full.names = TRUE)\n"
                + "  if (length(pending) == 0) return(NULL)\n"
                + "  pending <- sort(pending)\n"
                + "  for (p in pending) {\n"
                + "    w <- sub('jobs_pending', 'jobs_working', p, fixed = TRUE)\n"
                + "    ok <- file.rename(p, w)\n"
                + "    if (ok) return(w)\n"
                + "  }\n"
                + "  NULL\n"
                + "}\n"
                + "\n"
                + "cat('Worker started...\\n')\n"
                + "repeat {\n"
                + "  stop_signal <- file.exists('runtime/stop.signal')\n"
                + "  job_file <- claim_job()\n"
                + "  if (is.null(job_file)) {\n"
                + "    if (stop_signal) break\n"
                + "    Sys.sleep(0.1)\n"
                + "    next\n"
                + "  }\n"
                + "\n"
                + "  job <- readRDS(job_file)\n"
                + "  status <- 'ok'\n"
                + "  message <- ''\n"
                + "\n"
                + "  acquire_lock(lock_file)\n"
                + "  tryCatch({\n"
                + "    state <- if (file.exists(state_file)) readRDS(state_file) else list()\n"
                + "    if (length(state) > 0) {\n"
                + "      for (nm in names(state)) assign(nm, state[[nm]], envir = .GlobalEnv)\n"
                + "    }\n"
                + "    value <- NULL\n"
                + "    if (strict_repro) {\n"
                + "      value <- eval(parse(text = job$rhs), envir = .GlobalEnv)\n"
                + "    } else {\n"
                + "      # Legacy relaxed behavior for compatibility mode.\n"
                + "      for (base_nm in c('N', 'i', 'FP')) {\n"
                + "        state <- ensure_seed(base_nm, state, .GlobalEnv)\n"
                + "      }\n"
                + "      if (!is.null(job$deps) && !is.na(job$deps) && job$deps != '') {\n"
                + "        deps <- unlist(strsplit(as.character(job$deps), ';', fixed = TRUE))\n"
                + "        deps <- deps[deps != '']\n"
                + "        for (dn in deps) {\n"
                + "          state <- ensure_seed(dn, state, .GlobalEnv)\n"
                + "        }\n"
                + "      }\n"
                + "      attempt <- 1\n"
                + "      max_attempts <- 20\n"
                + "      repeat {\n"
                + "        ok <- TRUE\n"
                + "        tryCatch({\n"
                + "          value <- eval(parse(text = job$rhs), envir = .GlobalEnv)\n"
                + "        }, error = function(e) {\n"
                + "          msg <- as.character(e$message)\n"
                + "          missing_name <- extract_missing_name(msg)\n"
                + "          if (!is.na(missing_name) && attempt < max_attempts) {\n"
                + "            seed <- default_value_for(missing_name)\n"
                + "            assign(missing_name, seed, envir = .GlobalEnv)\n"
                + "            state[[missing_name]] <- seed\n"
                + "            attempt <<- attempt + 1\n"
                + "            ok <<- FALSE\n"
                + "            cat(sprintf('Auto-seeded missing symbol: %s\\n', missing_name))\n"
                + "          } else {\n"
                + "            stop(e)\n"
                + "          }\n"
                + "        })\n"
                + "        if (ok) break\n"
                + "      }\n"
                + "    }\n"
                + "    if (!startsWith(job$lhs, '__effect_')) {\n"
                + "      if (is.null(value)) {\n"
                + "        state[[job$lhs]] <- default_value_for(job$lhs)\n"
                + "      } else {\n"
                + "        state[[job$lhs]] <- value\n"
                + "      }\n"
                + "    }\n"
                + "    saveRDS(state, state_file)\n"
                + "  }, error = function(e) {\n"
                + "    status <<- 'error'\n"
                + "    message <<- as.character(e$message)\n"
                + "  })\n"
                + "  release_lock(lock_file)\n"
                + "\n"
                + "  result <- list(id = job$id, lhs = job$lhs, status = status, message = message)\n"
                + "  saveRDS(result, sprintf('runtime/results/result_%04d.rds', job$id))\n"
                + "  unlink(job_file)\n"
                + "}\n"
                + "cat('Worker stopped.\\n')\n";
    }

    private static class JobDefinition {
        private final int id;
        private final String lhs;
        private final String rhs;
        private final List<String> deps;

        private JobDefinition(int id, String lhs, String rhs, List<String> deps) {
            this.id = id;
            this.lhs = lhs;
            this.rhs = rhs;
            this.deps = deps;
        }
    }
}


