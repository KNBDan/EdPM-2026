package logic.description.microservice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Injects runtime LLM API support into generated R script and rewires
 * *_llm_complexity helpers to call real API at R execution time.
 */
public class RRuntimeLlmApiInjector {

    private static final Pattern LLM_HELPER_RETURN_PATTERN =
            Pattern.compile("(?m)^(\\s*)as\\.numeric\\(O_default\\)\\s*$");

    public String patchPreludeForRuntime(String prelude) {
        if (prelude == null || prelude.isBlank()) {
            return prelude;
        }
        if (!prelude.contains("_llm_complexity <- function(")) {
            return prelude;
        }

        String patched = prelude;
        Matcher m = LLM_HELPER_RETURN_PATTERN.matcher(patched);
        patched = m.replaceAll("$1llm_runtime_complexity(prompt, O_default)");
        return patched;
    }

    public String buildRuntimeApiBlock() {
        return buildRuntimeApiBlock("");
    }

    public String buildRuntimeApiBlock(String llmToken) {
        StringBuilder bootstrap = new StringBuilder();
        if (llmToken != null && !llmToken.isBlank()) {
            bootstrap.append("if (Sys.getenv(\"GIGACHAT_TOKEN\", unset = \"\") == \"\") {\n")
                    .append("  Sys.setenv(GIGACHAT_TOKEN = \"")
                    .append(escapeRString(llmToken))
                    .append("\")\n")
                    .append("}\n\n");
        }
        return """
# --- ==== [ Runtime LLM API ] ==== ---
if (!requireNamespace('httr2', quietly = TRUE)) {
  tryCatch(install.packages('httr2', type = 'binary', quiet = TRUE), error = function(e) NULL)
}
if (!requireNamespace('jsonlite', quietly = TRUE)) {
  tryCatch(install.packages('jsonlite', type = 'binary', quiet = TRUE), error = function(e) NULL)
}
library(httr2)
library(jsonlite)

""" + bootstrap + """
llm_runtime_complexity <- function(prompt, O_default) {
  token <- Sys.getenv("GIGACHAT_TOKEN", unset = "")
  token_type <- tolower(Sys.getenv("GIGACHAT_TOKEN_TYPE", unset = "auto"))
  oauth_endpoint <- Sys.getenv("GIGACHAT_AUTH_URL", unset = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
  oauth_scope <- Sys.getenv("GIGACHAT_SCOPE", unset = "GIGACHAT_API_PERS")
  endpoint <- Sys.getenv("GIGACHAT_API_URL", unset = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions")
  model_name <- Sys.getenv("GIGACHAT_MODEL", unset = "GigaChat")
  insecure_ssl <- tolower(Sys.getenv("GIGACHAT_INSECURE_SSL", unset = "1"))
  insecure_ssl <- insecure_ssl %in% c("1", "true", "yes", "on")
  timeout_sec <- suppressWarnings(as.numeric(Sys.getenv("GIGACHAT_TIMEOUT_SEC", unset = "60")))
  if (is.na(timeout_sec) || timeout_sec <= 0) timeout_sec <- 60

  if (is.null(prompt) || nchar(prompt) == 0) {
    cat("[LLM RUNTIME] Empty prompt, fallback O_default\\n")
    return(as.numeric(O_default))
  }
  if (nchar(token) == 0) {
    cat("[LLM RUNTIME] Missing GIGACHAT_TOKEN, fallback O_default\\n")
    return(as.numeric(O_default))
  }

  req_body <- list(
    model = model_name,
    messages = list(list(role = "user", content = prompt)),
    temperature = 0.2,
    max_tokens = 128
  )

  build_req <- function(url) {
    req <- request(url) |>
      req_timeout(seconds = timeout_sec)
    if (isTRUE(insecure_ssl)) {
      req <- req |>
        req_options(ssl_verifypeer = 0L, ssl_verifyhost = 0L)
    }
    req
  }

  make_uuid_v4 <- function() {
    hex <- c(0:9, letters[1:6])
    rnd <- function(n) paste0(sample(hex, n, replace = TRUE), collapse = "")
    paste0(
      rnd(8), "-", rnd(4), "-4", rnd(3), "-",
      sample(c("8", "9", "a", "b"), 1), rnd(3), "-", rnd(12)
    )
  }

  exchange_auth_key <- function(auth_key) {
    scopes_to_try <- unique(c(oauth_scope, "GIGACHAT_API_B2B"))
    last_err <- ""
    for (sc in scopes_to_try) {
      req <- build_req(oauth_endpoint) |>
        req_headers(
          Authorization = paste("Basic", auth_key),
          `Content-Type` = "application/x-www-form-urlencoded",
          Accept = "application/json",
          RqUID = make_uuid_v4()
        ) |>
        req_body_form(scope = sc) |>
        req_error(is_error = function(resp) FALSE)
      resp <- req |> req_perform()
      st <- resp_status(resp)
      if (st >= 200 && st < 300) {
        parsed <- resp_body_json(resp)
        at <- tryCatch(parsed$access_token, error = function(e) "")
        if (!is.null(at) && nchar(at) > 0) {
          cat("[LLM RUNTIME] OAuth OK with scope=", sc, "\\n", sep = "")
          return(at)
        }
        last_err <- paste0("OAuth 2xx but empty access_token for scope=", sc)
      } else {
        body_txt <- tryCatch(resp_body_string(resp), error = function(e) "")
        last_err <- paste0("HTTP ", st, " | scope=", sc, " | body=", body_txt)
      }
    }
    stop(paste0("OAuth exchange failed: ", last_err))
  }

  call_chat <- function(access_token) {
    req <- build_req(endpoint) |>
      req_headers(
        Authorization = paste("Bearer", access_token),
        `Content-Type` = "application/json",
        Accept = "application/json"
      ) |>
      req_body_json(req_body, auto_unbox = TRUE) |>
      req_error(is_error = function(resp) FALSE)
    if (isTRUE(insecure_ssl)) {
      cat("[LLM RUNTIME] SSL verification is DISABLED\\n")
    }
    resp <- req |> req_perform()
    st <- resp_status(resp)
    if (st < 200 || st >= 300) {
      body_txt <- tryCatch(resp_body_string(resp), error = function(e) "")
      stop(paste0("Chat request failed: HTTP ", st, " | body=", body_txt))
    }
    resp
  }

  started <- Sys.time()
  response_text <- ""
  ok <- TRUE

  tryCatch({
    token_preview <- if (nchar(token) > 12) paste0(substr(token, 1, 6), "...", substr(token, nchar(token)-3, nchar(token))) else "<short>"
    cat("[LLM RUNTIME] auth debug: token_type=", token_type,
        ", token_len=", nchar(token),
        ", token_preview=", token_preview,
        ", oauth_endpoint=", oauth_endpoint,
        ", scope=", oauth_scope, "\\n", sep = "")
    access_token <- token
    if (token_type %in% c("auth_key", "authkey")) {
      access_token <- exchange_auth_key(token)
      cat("[LLM RUNTIME] token_type=auth_key, exchanged to access_token\\n")
    }
    resp <- NULL
    if (token_type == "auto") {
      first_try <- tryCatch(call_chat(access_token), error = function(e) e)
      if (inherits(first_try, "error")) {
        msg <- conditionMessage(first_try)
        if (grepl("HTTP 4", msg, fixed = TRUE) || grepl("Unauthorized", msg, ignore.case = TRUE)) {
          access_token <- exchange_auth_key(token)
          cat("[LLM RUNTIME] token_type=auto, bearer failed with 4xx, exchanged auth_key to access_token\\n")
          resp <- call_chat(access_token)
        } else {
          stop(first_try)
        }
      } else {
        resp <- first_try
      }
    } else {
      resp <- call_chat(access_token)
    }
    parsed <- resp_body_json(resp)
    response_text <- tryCatch(parsed$choices[[1]]$message$content, error = function(e) "")
  }, error = function(e) {
    ok <<- FALSE
    cat("[LLM RUNTIME] API error:", conditionMessage(e), "\\n")
  })

  elapsed_ms <- as.numeric(difftime(Sys.time(), started, units = "secs")) * 1000
  if (!is.finite(elapsed_ms) || elapsed_ms <= 0) elapsed_ms <- 1
  complexity_o <- max(1, ceiling(elapsed_ms / 1000))

  preview <- if (nchar(response_text) > 140) substr(response_text, 1, 140) else response_text
  if (!ok) {
    cat("[LLM RUNTIME] fallback O_default due to API error\\n")
    return(as.numeric(O_default))
  }
  cat(sprintf("[LLM RUNTIME] latencyMs=%d O=%d\\n", as.integer(elapsed_ms), as.integer(complexity_o)))
  if (nchar(preview) > 0) cat("[LLM RUNTIME] response=", preview, "\\n", sep = "")
  as.numeric(complexity_o)
}
""";
    }

    private String escapeRString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
