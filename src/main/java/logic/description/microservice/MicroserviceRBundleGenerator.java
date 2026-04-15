package logic.description.microservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicroserviceRBundleGenerator {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b[A-Za-z][A-Za-z0-9_]*\\b");
    private static final Pattern WRITE_CSV_X_PATTERN = Pattern.compile("write\\.csv\\(\\s*X\\s*,\\s*file\\s*=\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern INT_ASSIGNMENT_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*<-\\s*([0-9]+)\\s*$");

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
        List<JobDefinition> jobs = parseJobs(mainProgram, prelude);
        
        List<List<JobDefinition>> levels = computeTopologicalLevels(jobs);
        RuntimeConfig runtimeConfig = parseRuntimeConfig(mainProgram);

        String futureBasedCode = buildFutureBasedCode(prelude, jobs, levels, runtimeConfig);

        writeString(bundleDir.resolve("run_microservice.R"), futureBasedCode);
        writeString(bundleDir.resolve("run_local.R"), buildRunLocalWrapper());

        List<String> generated = List.of("run_microservice.R", "run_local.R");
        return new MicroserviceGenerationResult(bundleDir, generated);
    }

    private void writeString(Path target, String content) throws IOException {
        Files.writeString(target, content, StandardCharsets.UTF_8);
    }

    private List<List<JobDefinition>> computeTopologicalLevels(List<JobDefinition> jobs) {
        List<List<JobDefinition>> levels = new ArrayList<>();
        
        Set<String> completed = new HashSet<>();
        Set<JobDefinition> remaining = new HashSet<>(jobs);
        
        while (!remaining.isEmpty()) {
            List<JobDefinition> currentLevel = new ArrayList<>();
            
            for (JobDefinition job : remaining) {
                boolean depsSatisfied = true;
                for (String dep : job.deps) {
                    if (!completed.contains(dep)) {
                        depsSatisfied = false;
                        break;
                    }
                }
                if (depsSatisfied) {
                    currentLevel.add(job);
                }
            }
            
            if (currentLevel.isEmpty() && !remaining.isEmpty()) {
                currentLevel.addAll(remaining);
            }
            
            levels.add(currentLevel);
            for (JobDefinition job : currentLevel) {
                remaining.remove(job);
                completed.add(job.lhs);
            }
        }
        
        return levels;
    }

    private String buildFutureBasedCode(String prelude, List<JobDefinition> jobs,
            List<List<JobDefinition>> levels, RuntimeConfig runtimeConfig) {
        StringBuilder code = new StringBuilder();
        String sanitizedPrelude = stripInlinePlotsFromPrelude(prelude);
        sanitizedPrelude = normalizePlotLabelsToEnglish(sanitizedPrelude);
        String firstRName = "";
        for (JobDefinition job : jobs) {
            if (job.lhs != null && job.lhs.matches("^R\\d+$")) {
                firstRName = job.lhs;
                break;
            }
        }

        code.append("# =============================================================================\n");
        code.append("# EdPM Model - Microservice Execution using futures\n");
        code.append("# Generated: ").append(LocalDateTime.now()).append("\n");
        code.append("# =============================================================================\n\n");

        // Library loading
        code.append("# --- ==== [ Library Loading ] ==== ---\n");
        code.append("# Auto-install missing packages (binary to avoid compilation issues)\n");
        code.append("if (!requireNamespace('utils', quietly = TRUE)) install.packages('utils')\n");
        code.append("options(repos = c(CRAN = 'https://cloud.r-project.org'), install.packages.compile.from.source = 'never')\n");
        code.append("if (!requireNamespace('parallelly', quietly = TRUE)) {\n");
        code.append("  tryCatch(install.packages('parallelly', type = 'binary', quiet = TRUE), error = function(e) NULL)\n");
        code.append("}\n");
        code.append("if (!requireNamespace('future', quietly = TRUE)) {\n");
        code.append("  tryCatch(install.packages('future', type = 'binary', quiet = TRUE), error = function(e) NULL)\n");
        code.append("}\n");
        code.append("if (!requireNamespace('future.apply', quietly = TRUE)) {\n");
        code.append("  tryCatch(install.packages('future.apply', type = 'binary', quiet = TRUE), error = function(e) NULL)\n");
        code.append("}\n");
        code.append("library(future)\n");
        code.append("library(future.apply)\n\n");

        // Configuration
        code.append("# --- ==== [ Configuration ] ==== ---\n");
        code.append("if (!exists('seed_value')) seed_value <- 12345L\n");
        code.append("if (!exists('worker_count')) worker_count <- future::availableCores()\n");
        code.append("set.seed(seed_value)\n\n");

        // Plan selection
        code.append("if (worker_count > 1) {\n");
        code.append("  plan(multisession, workers = worker_count)\n");
        code.append("  cat(sprintf('Using multisession plan with %d workers\\n', worker_count))\n");
        code.append("} else {\n");
        code.append("  plan(sequential)\n");
        code.append("  cat('Using sequential plan (single worker)\\n')\n");
        code.append("}\n\n");

        // Base functions from prelude
        code.append("# --- ==== [ Base Functions (from prelude) ] ==== ---\n");
        code.append(sanitizedPrelude);
        code.append("\n\n");

        // Job execution with levels
        code.append("# --- ==== [ Microservice Job Execution ] ==== ---\n");
        code.append("# Jobs are organized by dependency levels for parallel execution\n\n");

        code.append("# Runtime flags inherited from linear generation settings\n");
        code.append("is_plot_active <- ").append(runtimeConfig.plotEnabled ? "TRUE" : "FALSE").append("\n");
        code.append("is_xes_active <- ").append(runtimeConfig.xesEnabled ? "TRUE" : "FALSE").append("\n");
        code.append("script_path <- NULL\n");
        code.append("try({\n");
        code.append("  ofile <- sys.frame(1)$ofile\n");
        code.append("  if (!is.null(ofile) && length(ofile) > 0) script_path <- ofile\n");
        code.append("}, silent = TRUE)\n");
        code.append("if (is.null(script_path)) {\n");
        code.append("  full_args <- commandArgs(trailingOnly = FALSE)\n");
        code.append("  file_arg <- grep('^--file=', full_args, value = TRUE)\n");
        code.append("  if (length(file_arg) > 0) script_path <- sub('^--file=', '', file_arg[1])\n");
        code.append("}\n");
        code.append("output_dir <- if (!is.null(script_path)) dirname(normalizePath(script_path, winslash='/', mustWork=FALSE)) else getwd()\n");
        code.append("xes_file_name <- file.path(output_dir, basename(\"")
            .append(escapeRString(runtimeConfig.xesFileName)).append("\"))\n");
        code.append("plots_pdf_file <- file.path(output_dir, \"microservice_plots.pdf\")\n\n");
        code.append("# Core scalar parameters (initialized once)\n");
        code.append("N <- ").append(runtimeConfig.nValue).append("\n");
        code.append("i <- ").append(runtimeConfig.iValue).append("\n");
        code.append("FP <- ").append(runtimeConfig.fpValue).append("\n\n");
        code.append("first_r_name <- \"").append(escapeRString(firstRName)).append("\"\n\n");
        code.append("if (is_plot_active) {\n");
        code.append("  if (capabilities(\"cairo\")) {\n");
        code.append("    cairo_pdf(plots_pdf_file, width = 12, height = 8, family = \"Arial\", onefile = TRUE)\n");
        code.append("  } else {\n");
        code.append("    pdf(plots_pdf_file, width = 12, height = 8, onefile = TRUE)\n");
        code.append("  }\n");
        code.append("}\n\n");

        code.append("# Environment to store results\n");
        code.append("results_env <- new.env()\n\n");
        code.append("empty_r_stream <- function() {\n");
        code.append("  id_out <- vector(\"list\", N)\n");
        code.append("  id_out[] <- list(0)\n");
        code.append("  data.frame(R = rep(0, N), ID_Out = I(id_out))\n");
        code.append("}\n\n");
        code.append("# Accumulators for inline post-effects\n");
        code.append("X <- NULL\n");
        code.append("X_list <- list()\n\n");
        code.append("process_r_result <- function(r_name, r_value) {\n");
        code.append("  if (is.null(r_value) || !is.data.frame(r_value)) return(invisible(NULL))\n");
        code.append("  if (!all(c(\"R\", \"Prj_File\") %in% names(r_value))) return(invisible(NULL))\n");
        code.append("  assign(r_name, r_value, envir = .GlobalEnv)\n");
        code.append("  if (is_plot_active) {\n");
        code.append("    if (nzchar(first_r_name) && identical(r_name, first_r_name) && \"Prj_Flow\" %in% names(r_value)) {\n");
        code.append("      plot(1:N, r_value$Prj_Flow, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,6), main = \"Element S1\")\n");
        code.append("    }\n");
        code.append("    plot(1:N, r_value$R, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = paste(\"Element\", r_name, \"Output\"))\n");
        code.append("    plot(1:N, r_value$Prj_File, type=\"s\", col=\"black\", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = paste(\"Element\", r_name, \"Queue\"))\n");
        code.append("  }\n");
        code.append("  if (is_xes_active) {\n");
        code.append("    x_cur <- tryCatch(XES(r_value), error = function(e) NULL)\n");
        code.append("    if (is.null(x_cur) || !is.data.frame(x_cur)) return(invisible(NULL))\n");
        code.append("    if (is.null(X)) {\n");
        code.append("      X <<- x_cur\n");
        code.append("    } else {\n");
        code.append("      X <<- rbind(X, x_cur)\n");
        code.append("    }\n");
        code.append("    if (length(x_cur$W) > 0) {\n");
        code.append("      X_list[[length(X_list) + 1]] <<- x_cur$W\n");
        code.append("      vioplot(x_cur$W, col = \"lightgray\", panel.first=grid(), main = paste(\"Element\", r_name))\n");
        code.append("    }\n");
        code.append("    if (!is.null(X) && length(X$W) > 0) {\n");
        code.append("      vioplot(X$W, col = \"lightgray\", panel.first=grid(), main = paste(\"All elements before\", r_name))\n");
        code.append("    }\n");
        code.append("  }\n");
        code.append("}\n\n");

        for (int levelIdx = 0; levelIdx < levels.size(); levelIdx++) {
            List<JobDefinition> level = levels.get(levelIdx);
            code.append("# --- Level ").append(levelIdx + 1).append(" (").append(level.size()).append(" jobs) ---\n");
            
            if (level.size() == 1) {
                JobDefinition job = level.get(0);
                code.append("cat('Executing: ").append(job.lhs).append(" (level ").append(levelIdx + 1).append(")\\n');\n");
                for (String dep : job.deps) {
                    code.append("dep_").append(job.id).append("_").append(dep).append(" <- results_env$").append(dep).append("\n");
                    if (dep.matches("^R\\d+$")) {
                        code.append("if (is.null(dep_").append(job.id).append("_").append(dep).append(")) dep_")
                                .append(job.id).append("_").append(dep).append(" <- empty_r_stream()\n");
                    }
                }
                code.append("future_").append(job.id).append(" <- future({\n");
                for (String dep : job.deps) {
                    code.append("  ").append(dep).append(" <- dep_").append(job.id).append("_").append(dep).append("\n");
                    code.append("  assign(\"").append(dep).append("\", ").append(dep).append(", envir = .GlobalEnv)\n");
                }
                code.append("  result <- ").append(job.rhs).append("\n");
                code.append("  result\n");
                code.append("}, seed = seed_value + ").append(job.id).append(")\n");
                code.append("results_env$").append(job.lhs).append(" <- value(future_").append(job.id).append(")\n\n");
                if (job.lhs.matches("^R\\d+$")) {
                    code.append("process_r_result(\"").append(job.lhs).append("\", results_env$").append(job.lhs).append(")\n\n");
                }
            } else {
                code.append("cat('Executing level ").append(levelIdx + 1).append(" in parallel (").append(level.size()).append(" jobs)...\\n');\n");
                
                code.append("# Define job functions for parallel execution\n");
                for (JobDefinition job : level) {
                    code.append("# Job ").append(job.id).append(": ").append(job.lhs).append("\n");
                    for (String dep : job.deps) {
                        code.append("dep_").append(job.id).append("_").append(dep).append(" <- results_env$").append(dep).append("\n");
                        if (dep.matches("^R\\d+$")) {
                            code.append("if (is.null(dep_").append(job.id).append("_").append(dep).append(")) dep_")
                                    .append(job.id).append("_").append(dep).append(" <- empty_r_stream()\n");
                        }
                    }
                    code.append("job_func_").append(job.id).append(" <- function() {\n");
                    for (String dep : job.deps) {
                        code.append("  ").append(dep).append(" <- dep_").append(job.id).append("_").append(dep).append("\n");
                        code.append("  assign(\"").append(dep).append("\", ").append(dep).append(", envir = .GlobalEnv)\n");
                    }
                    code.append("  ").append(job.lhs).append(" <- ").append(job.rhs).append("\n");
                    code.append("  return(").append(job.lhs).append(")\n");
                    code.append("}\n");
                }
                
                code.append("\n# Execute all jobs in this level in parallel\n");
                code.append("job_functions <- list(\n");
                for (int i = 0; i < level.size(); i++) {
                    JobDefinition job = level.get(i);
                    code.append("  function() job_func_").append(job.id).append("()");
                    if (i < level.size() - 1) code.append(",");
                    code.append(" # ").append(job.lhs).append("\n");
                }
                code.append(")\n\n");
                
                code.append("# Run all jobs in parallel using future_lapply\n");
                code.append("level_results <- future_lapply(job_functions, function(f) f(), future.seed = TRUE)\n");
                code.append("# Store results in environment\n");
                for (int i = 0; i < level.size(); i++) {
                    JobDefinition job = level.get(i);
                    code.append("results_env$").append(job.lhs).append(" <- level_results[[").append(i + 1).append("]]\n");
                }
                for (JobDefinition job : level) {
                    if (job.lhs.matches("^R\\d+$")) {
                        code.append("process_r_result(\"").append(job.lhs).append("\", results_env$").append(job.lhs).append(")\n");
                    }
                }
                code.append("\n");
            }
        }

        code.append("# --- ==== [ Collect All Results ] ==== ---\n");
        code.append("cat('\\n=== Collecting Results ===\\n')\n");
        code.append("all_results <- list(\n");
        for (int i = 0; i < jobs.size(); i++) {
            JobDefinition job = jobs.get(i);
            code.append("  ").append(job.lhs).append(" = results_env$").append(job.lhs);
            if (i < jobs.size() - 1) code.append(",");
            code.append("\n");
        }
        code.append(")\n\n");

        code.append("# --- ==== [ Summary ] ==== ---\n");
        code.append("cat('\\n=== Execution Summary ===\\n')\n");
        code.append("cat('Total jobs:', length(all_results), '\\n')\n");
        code.append("cat('Total levels:', ").append(levels.size()).append(", '\\n')\n");
        code.append("cat('Produced variables:\\n')\n");
        code.append("print(names(all_results))\n\n");
        code.append("# --- ==== [ Finalize XES ] ==== ---\n");
        code.append("if (is_xes_active && !is.null(X)) {\n");
        code.append("  if (length(X_list) > 0) {\n");
        code.append("    do.call(vioplot, c(X_list, list(col = \"lightgray\", panel.first=grid())))\n");
        code.append("  }\n");
        code.append("  l <- unique(X$ID)\n");
        code.append("  s_last <- NA\n");
        code.append("  for (i in 1:length(l)) {\n");
        code.append("    s_last[i] <- sum(X$W[X$ID==l[i]])\n");
        code.append("  }\n");
        code.append("  if (length(s_last) > 0) vioplot(s_last, col = \"lightgray\", panel.first=grid())\n");
        code.append("  write.csv(X, file=xes_file_name)\n");
        code.append("}\n\n");
        code.append("if (is_plot_active) {\n");
        code.append("  try(dev.off(), silent = TRUE)\n");
        code.append("}\n\n");

        code.append("# --- ==== [ Cleanup ] ==== ---\n");
        code.append("plan(sequential)\n");
        code.append("cat('\\nMicroservice execution completed.\\n')\n");

        return code.toString();
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
        for (int i = 0; i < lines.length; i++) {
            if (isMainProgramHeader(lines[i].trim())) {
                return i + 1;
            }
        }
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
            if (lines[i].toLowerCase().contains("microservice")) {
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
                    && trimmed.toLowerCase().contains("main program")) {
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
        return trimmed.toLowerCase().contains("main program");
    }

    private List<JobDefinition> parseJobs(String mainProgram, String prelude) {
        List<JobDefinition> jobs = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        
        for (String line : mainProgram.split("\\R")) {
            if (!line.isEmpty() && Character.isWhitespace(line.charAt(0))) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
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
            if (parts.length >= 2) {
                String lhs = parts[0].trim();
                if (!lhs.isEmpty()) {
                    producedVars.add(lhs);
                }
            }
        }
        Map<String, Set<String>> functionDependencies = extractFunctionDependencies(prelude, producedVars);

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
            List<String> deps = extractDependencies(rhs, lhs, producedVars, functionDependencies);
            jobs.add(new JobDefinition(id, lhs, rhs, deps));
            id += 1;
        }
        return jobs;
    }

    private boolean isCoreAssignmentLhs(String lhs) {
        if (lhs == null || lhs.isEmpty()) {
            return false;
        }
        return lhs.matches("^R\\d+$");
    }

    private RuntimeConfig parseRuntimeConfig(String mainProgram) {
        boolean plotEnabled = mainProgram.contains("plot(");
        boolean xesEnabled = mainProgram.contains("XES(") || mainProgram.contains("write.csv(X");
        String xesFileName = "xesik.csv";
        int nValue = 30;
        int iValue = 1;
        int fpValue = 1;
        for (String line : mainProgram.split("\\R")) {
            Matcher m = INT_ASSIGNMENT_PATTERN.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            String var = m.group(1);
            int value = Integer.parseInt(m.group(2));
            if (var.equals("N")) {
                nValue = value;
            } else if (var.equals("i")) {
                iValue = value;
            } else if (var.equals("FP")) {
                fpValue = value;
            }
        }
        Matcher matcher = WRITE_CSV_X_PATTERN.matcher(mainProgram);
        if (matcher.find()) {
            xesFileName = matcher.group(1);
        }
        return new RuntimeConfig(plotEnabled, xesEnabled, xesFileName, nValue, iValue, fpValue);
    }

    private String buildRunLocalWrapper() {
        return """
args <- commandArgs(trailingOnly = TRUE)
worker_count <- if (length(args) >= 1) as.integer(args[[1]]) else 1L
seed_value <- if (length(args) >= 2) as.integer(args[[2]]) else 12345L
if (is.na(worker_count) || worker_count < 1) worker_count <- 1L
if (is.na(seed_value)) seed_value <- 12345L
source("run_microservice.R")
""";
    }

    private String escapeRString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String stripInlinePlotsFromPrelude(String prelude) {
        if (prelude == null || prelude.isBlank()) {
            return prelude;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : prelude.split("\\R", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("plot(") || trimmed.startsWith("vioplot(")) {
                continue;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private String normalizePlotLabelsToEnglish(String code) {
        if (code == null || code.isBlank()) {
            return code;
        }
        return code
            .replace("Элемент ", "Element ")
            .replace("Все элементы до ", "All elements before ");
    }

    private List<String> extractDependencies(String rhs, String lhs, Set<String> producedVars,
            Map<String, Set<String>> functionDependencies) {
        List<String> deps = new ArrayList<>();
        Matcher matcher = IDENTIFIER_PATTERN.matcher(rhs);
        while (matcher.find()) {
            String ident = matcher.group();
            if (producedVars.contains(ident) && !ident.equals(lhs)) {
                if (!deps.contains(ident)) {
                    deps.add(ident);
                }
            }
        }
        Matcher funcCallMatcher = Pattern.compile("\\b([A-Za-z][A-Za-z0-9_]*)\\s*\\(").matcher(rhs);
        while (funcCallMatcher.find()) {
            String functionName = funcCallMatcher.group(1);
            Set<String> fnDeps = functionDependencies.get(functionName);
            if (fnDeps == null) {
                continue;
            }
            for (String dep : fnDeps) {
                if (!dep.equals(lhs) && !deps.contains(dep)) {
                    deps.add(dep);
                }
            }
        }
        return deps;
    }

    private Map<String, Set<String>> extractFunctionDependencies(String prelude, Set<String> producedVars) {
        Map<String, Set<String>> result = new HashMap<>();
        if (prelude == null || prelude.isBlank()) {
            return result;
        }
        String[] lines = prelude.split("\\R", -1);
        Pattern functionHeader = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*<-\\s*function\\s*\\(");
        for (int i = 0; i < lines.length; i++) {
            Matcher headerMatcher = functionHeader.matcher(lines[i]);
            if (!headerMatcher.find()) {
                continue;
            }
            String functionName = headerMatcher.group(1);
            StringBuilder body = new StringBuilder();
            int balance = 0;
            boolean started = false;
            for (int j = i; j < lines.length; j++) {
                String ln = lines[j];
                for (int k = 0; k < ln.length(); k++) {
                    char ch = ln.charAt(k);
                    if (ch == '{') {
                        balance++;
                        started = true;
                    } else if (ch == '}') {
                        balance--;
                    }
                }
                body.append(ln).append("\n");
                if (started && balance <= 0) {
                    i = j;
                    break;
                }
            }
            Set<String> deps = new LinkedHashSet<>();
            Matcher identMatcher = IDENTIFIER_PATTERN.matcher(body.toString());
            while (identMatcher.find()) {
                String ident = identMatcher.group();
                if (producedVars.contains(ident)) {
                    deps.add(ident);
                }
            }
            result.put(functionName, deps);
        }
        return result;
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

    private static class RuntimeConfig {
        private final boolean plotEnabled;
        private final boolean xesEnabled;
        private final String xesFileName;
        private final int nValue;
        private final int iValue;
        private final int fpValue;

        private RuntimeConfig(boolean plotEnabled, boolean xesEnabled, String xesFileName,
                int nValue, int iValue, int fpValue) {
            this.plotEnabled = plotEnabled;
            this.xesEnabled = xesEnabled;
            this.xesFileName = xesFileName;
            this.nValue = nValue;
            this.iValue = iValue;
            this.fpValue = fpValue;
        }
    }
}
