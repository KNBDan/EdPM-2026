# --- ==== [ Library Loading ] ==== ---
# Auto-install missing packages (binary to avoid compilation issues)
if (!requireNamespace('utils', quietly = TRUE)) install.packages('utils')
options(repos = c(CRAN = 'https://cloud.r-project.org'), install.packages.compile.from.source = 'never')
if (!requireNamespace('parallelly', quietly = TRUE)) {
  tryCatch(install.packages('parallelly', type = 'binary', quiet = TRUE), error = function(e) NULL)
}
if (!requireNamespace('future', quietly = TRUE)) {
  tryCatch(install.packages('future', type = 'binary', quiet = TRUE), error = function(e) NULL)
}
if (!requireNamespace('future.apply', quietly = TRUE)) {
  tryCatch(install.packages('future.apply', type = 'binary', quiet = TRUE), error = function(e) NULL)
}
library(future)
library(future.apply)

# --- ==== [ Configuration ] ==== ---
if (!exists('worker_count')) worker_count <- 1L

if (worker_count > 1) {
  plan(multisession, workers = worker_count)
  cat(sprintf('Using multisession plan with %d workers\n', worker_count))
} else {
  plan(sequential)
  cat('Using sequential plan (single worker)\n')
}

# --- ==== [ Base Functions (from prelude) ] ==== ---
library(vioplot) #Для рисования скрипичной диаграммы
library(purrr)

# --- ==== [ Объявление функций ] ==== ---

####### S элемент с вероятностной работой
# P - вероятность 
# N - число элементов для заполнения
# ID - значение с которого начинается нумерация генерируемых событий
S_prob<-function(N, P, ID){
  RES<-vector(mode = "integer", length = N)
  RES[1:N]<-rpois(N,P)
  
  S1<-data.frame(S=RES, ID=vector(mode = "numeric", length = N))
  ID_S<-ID
  for (i in 1:N){
    if (RES[i]>0){
      Vect<-as.vector(ID_S+1:RES[i], mode = "numeric")
      ID_S<-ID_S+RES[i]
      S1$ID[i]<-list(Value=Vect)
    }
  }
  S_prob<-S1
}

#######  S элемент с периодической работой
# FP - начало  заполнения
# P - периаодичность
# N - число элементов для заполнения
# ID - значение с которого начинается нумерация генерируемых событий
S_periodic<-function(N, FP, P, ID){
  #RES<-vector(mode = "integer", length = N)
  if (length(FP) == 0) {
    RES<-vector(mode = "integer", length = N)
  } else {
    RES1<-vector(mode = "integer", length = FP[1]-1)       
    RES2<-vector(mode = "integer", length = N-FP[1]+1)
    MN<-1:(N-FP[1])
    RES2[(MN+P-1)%%P==0]<-1
    RES<-c(RES1,RES2)
  }
  S1<-data.frame(S=RES, ID=vector(mode = "numeric", length = N))
  ID_S<-ID
  for (i in 1:N){
    if (RES[i]>0){
      Vect<-as.vector(ID_S+1:RES[i], mode = "numeric")
      ID_S<-ID_S+RES[i]
      S1$ID[i]<-list(Value=Vect)
    }
  }
  S_prob<-S1
}

#V линейная сложность
V_L<-function(N,O){
  V_L<-N*O
}

#V сложность N^2
V_L2<-function(N, O){
  V_2L<-(N*N)*O
}

#V логорифмическая сложность
V_Lg<-function(N, O){
  if (N == 0) {V_Lg<-0} else {
    if (N == 1) {
      V_Lg<-1
    } else {  
      V_Lg<-N*log(N)*O
    }  
  }
}

#V экспоненциальная сложность
V_E<-function(N, O){
  if (N == 0) {V_E<-0} else {V_E<-exp(N)*O}
}

################# Блок обработки (V)  #######################
# O - тип сложности (1 - линейная сложность, 2 - сложность N^2, 3 - логорифмическая сложность, 4 - експоненциальная сложность)
# S - поток событий
# V - название блока для записи в результирующую талицу (требуется для построения XES файла)

# Формат таблицы которую получаем
# I - номер шага
# J - номер шага на котором последний раз освободился блок (получил статус - свободен)
# Prj_Flow - поток событий
# Prj_File - очередь событий
# V_W - количество шагов обработки
# V - название блока обработки
# R - выходной (после обработки) поток событий 
# ID_File - идентификаторы событий в очереди 
# ID_Out - идентификаторы событий почсле обработки 

V<-function(m, S, V, O){
  # расчет для логарифмической сложности
  N<-length(S$S)
  Df<-data.frame(I=1:N, 
                 J=vector(mode = "numeric", length = length(N)), 
                 Prj_Flow=S$S, 
                 Prj_File=vector(mode = "numeric", length = length(N)), 
                 V_W=vector(mode = "numeric", length = length(N)), 
                 V=V, 
                 R=vector(mode = "numeric", length = length(N)), 
                 ID_File=vector(mode = "numeric", length = length(N)), 
                 ID_Out=vector(mode = "numeric", length = length(N)))
  Df$ID_File<-rep(list(0), length(N))
  Df$ID_Out<-rep(list(0), length(N))
  j<-1
  L<-0
  for (i in 1:N){
    Df$Prj_File[i]<-sum(Df$Prj_Flow[i:j])
    Df$ID_File[i]<-list(unique(list_c(S$ID[i:j])))
    Df$J[i]<-j
    if (Df$V_W[i]==0) {
      nk<-Df$Prj_File[i]
      #if (m == -1) {L<-ceiling(V_L001(nk))}
      #if (m == 0) {L<-ceiling(V_L01(nk))}
      if (m == 1) {L<-ceiling(V_L(nk, O))}
      if (m == 2) {L<-ceiling(V_L2(nk, O))}
      if (m == 3) {L<-ceiling(V_Lg(nk, O))}
      if (m == 4) {L<-ceiling(V_E(nk, O))}
      k<-min(i+L-1,N)
      if (k>=i){
        Df$V_W[i:k]<-L
        Df$R[k] <- nk
        Df$ID_Out[k]<-Df$ID_File[i]
      }
      j<-i+1
    }
    
  }
  
  P1<-Df$R
  P2<-Df$ID_Out
  
  Df$R[1]<-0
  Df$R[2:N]<-P1[1:(N-1)]
  Df$ID_Out[1]<-list(0)
  if (N > 1) {
    Df$ID_Out[2:N]<-P2[1:(N-1)]
  }
  V1<-Df
}

################# Собираем статистику в формате XES (Process Mining)  #######################
#  R - таблица выдаваемая блоком V

# Формат таблицы которую получаем
# ID - идентификатор события
# V - операция
# I -	номер шага поступления
# W - длительность обработки в шагах

XES<-function(R){
  Df<-data.frame(ID=0, V="V", I=0, W=0)
  N1<-length(R$I)
  for (i in 1:N1){
    N2<-length(R$ID_Out[[i]])
    if (N2>1 & R$R[i]>0){
      Vec<-list_c(R$ID_Out[i])
      for (j in 1:N2){
          if (Vec[j]>0){
            Df_0<-data.frame(ID=Vec[j], V=R$V[i], I=i, W=R$V_W[i-1])
            Df<-rbind(Df,Df_0)
         }
      }
    }
  }
  Df<-Df[-1,]
  XES<-Df
}

# Объединение двух последовательностей типа выдаваемого блоком S по аддитивному закону
Add<-function(A1, A2){
  N1<-length(A1$S)
  AS3<-vector(mode = "integer", length = N1)
  AS3<-A1$S+A2$S
  A3<-data.frame(S=AS3, ID=vector(mode = "numeric", length = N1))
  
  for (i in 1:N1){
    Vect<-as.vector(unique(c(A1$ID[[i]],A2$ID[[i]])), mode = "numeric")
    A3$ID[i]<-list(Value=Vect)
  }
  Add<-A3
}


# Разделение потоков по номерам проектов 
Select<-function(A, N1, N2){
  N<-length(A$S)
  A3<-data.frame(S=A$S, ID=vector(mode = "numeric", length = N))
  for (i in 1:N){
    Vect<-as.vector(unique(A$ID[[i]]), mode = "numeric")
    Vect1<-0
    k<-1
    for (j in 1:length(Vect)) {
      if (Vect[j]>N1 &  Vect[j]<N2){
        Vect1<-c(Vect1, Vect[j])
      }
    }
    A3$ID[i]<-list(Value=Vect1)
    A3$S[i]<-length(A3$ID[[i]])-1
  }
  Select<-A3
}

# --- ==== [ Основная программа ] ==== ---

# --- ==== [ V Functions ] ==== ---
V1_func <- function(){
  S1<-S_prob(N, 0.9, 68)
  return(V(1, S1, "V1", 1))
}

V2_func <- function(R_in){
  NV_in_V2<-subset( R_in, select=c(R, ID_Out))
  colnames( NV_in_V2 ) <- c('S', 'ID')
  return(V(1, NV_in_V2, "V2", 1))
}

V3_func <- function(R_in){
  NV_in_V3<-subset( R_in, select=c(R, ID_Out))
  colnames( NV_in_V3 ) <- c('S', 'ID')
  return(V(1, NV_in_V3, "V3", 1))
}

V4_func <- function(R_in){
  NV_in_V4<-subset( R_in, select=c(R, ID_Out))
  colnames( NV_in_V4 ) <- c('S', 'ID')
  return(V(1, NV_in_V4, "V4", 1))
}




# --- ==== [ Microservice Job Execution ] ==== ---
# Jobs are organized by dependency levels for parallel execution

# LLM simulation mode selected at generation time
llm_simulation_mode <- "DISABLED"
has_llm_blocks <- FALSE
if (!has_llm_blocks) llm_simulation_mode <- "DISABLED"

# Runtime flags inherited from linear generation settings
is_plot_active <- TRUE
is_xes_active <- TRUE
script_path <- NULL
try({
  ofile <- sys.frame(1)$ofile
  if (!is.null(ofile) && length(ofile) > 0) script_path <- ofile
}, silent = TRUE)
if (is.null(script_path)) {
  full_args <- commandArgs(trailingOnly = FALSE)
  file_arg <- grep('^--file=', full_args, value = TRUE)
  if (length(file_arg) > 0) script_path <- sub('^--file=', '', file_arg[1])
}
output_dir <- if (!is.null(script_path)) dirname(normalizePath(script_path, winslash='/', mustWork=FALSE)) else getwd()
xes_file_name <- file.path(output_dir, basename("xesik.csv"))
plots_pdf_file <- file.path(output_dir, "microservice_plots.pdf")

# Core scalar parameters (initialized once)
N <- 1000
i <- 1
FP <- 1

first_r_name <- "R1"

if (is_plot_active) {
  if (capabilities("cairo")) {
    cairo_pdf(plots_pdf_file, width = 12, height = 8, family = "Arial", onefile = TRUE)
  } else {
    pdf(plots_pdf_file, width = 12, height = 8, onefile = TRUE)
  }
}

# Environment to store results
results_env <- new.env()

empty_r_stream <- function() {
  id_out <- vector("list", N)
  id_out[] <- list(0)
  data.frame(R = rep(0, N), ID_Out = I(id_out))
}

# Accumulators for inline post-effects
X <- NULL
X_list <- list()

process_r_result <- function(r_name, r_value) {
  if (is.null(r_value) || !is.data.frame(r_value)) return(invisible(NULL))
  if (!all(c("R", "Prj_File") %in% names(r_value))) return(invisible(NULL))
  assign(r_name, r_value, envir = .GlobalEnv)
  if (is_plot_active) {
    if (nzchar(first_r_name) && identical(r_name, first_r_name) && "Prj_Flow" %in% names(r_value)) {
      plot(1:N, r_value$Prj_Flow, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,6), main = "Element S1")
    }
    plot(1:N, r_value$R, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = paste("Element", r_name, "Output"))
    plot(1:N, r_value$Prj_File, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = paste("Element", r_name, "Queue"))
  }
  if (is_xes_active) {
    x_cur <- tryCatch(XES(r_value), error = function(e) NULL)
    if (is.null(x_cur) || !is.data.frame(x_cur)) return(invisible(NULL))
    if (is.null(X)) {
      X <<- x_cur
    } else {
      X <<- rbind(X, x_cur)
    }
    if (length(x_cur$W) > 0) {
      X_list[[length(X_list) + 1]] <<- x_cur$W
      vioplot(x_cur$W, col = "lightgray", panel.first=grid(), main = paste("Element", r_name))
    }
    if (!is.null(X) && length(X$W) > 0) {
      vioplot(X$W, col = "lightgray", panel.first=grid(), main = paste("All elements before", r_name))
    }
  }
}

# --- Dynamic DAG Scheduler ---
job_defs <- list(
  list(name="R1", deps=character(0), run=function(){ V1_func() }),
  list(name="R2", deps=c("R1"), run=function(){ V2_func(R1) }),
  list(name="R3", deps=c("R2"), run=function(){ V3_func(R2) }),
  list(name="R4", deps=c("R3"), run=function(){ V4_func(R3) })
)

active_futures <- list()
completed_jobs <- character(0)
total_jobs <- length(job_defs)

is_job_ready <- function(job) {
  if (job$name %in% completed_jobs) return(FALSE)
  if (job$name %in% names(active_futures)) return(FALSE)
  if (length(job$deps) == 0) return(TRUE)
  all(job$deps %in% completed_jobs)
}

while (length(completed_jobs) < total_jobs) {
  ready_idx <- which(vapply(job_defs, is_job_ready, logical(1)))
  while (length(ready_idx) > 0 && length(active_futures) < worker_count) {
    jidx <- ready_idx[1]
    ready_idx <- ready_idx[-1]
    job <- job_defs[[jidx]]
    dep_vals <- list()
    if (length(job$deps) > 0) {
      for (dep in job$deps) {
        dep_val <- results_env[[dep]]
        if (is.null(dep_val) && grepl('^R[0-9]+$', dep)) dep_val <- empty_r_stream()
        dep_vals[[dep]] <- dep_val
      }
    }
    cat(sprintf('Starting: %s (active=%d)\n', job$name, length(active_futures) + 1L))
    active_futures[[job$name]] <- future({
      if (length(dep_vals) > 0) {
        for (dep_name in names(dep_vals)) {
          assign(dep_name, dep_vals[[dep_name]], envir = .GlobalEnv)
        }
      }
      job$run()
    }, seed = NULL)
  }
  if (length(active_futures) == 0) {
    stop('Deadlock detected: no active futures and no ready jobs. Check DAG dependencies.')
  }
  resolved_name <- NULL
  for (nm in names(active_futures)) {
    if (future::resolved(active_futures[[nm]])) {
      resolved_name <- nm
      break
    }
  }
  if (is.null(resolved_name)) {
    Sys.sleep(0.01)
    next
  }
  result_val <- value(active_futures[[resolved_name]])
  active_futures[[resolved_name]] <- NULL
  results_env[[resolved_name]] <- result_val
  completed_jobs <- c(completed_jobs, resolved_name)
  cat(sprintf('Completed: %s (%d/%d)\n', resolved_name, length(completed_jobs), total_jobs))
  if (grepl('^R[0-9]+$', resolved_name)) {
    process_r_result(resolved_name, results_env[[resolved_name]])
  }
}

# --- ==== [ Collect All Results ] ==== ---
cat('\n=== Collecting Results ===\n')
all_results <- list(
  R1 = results_env$R1,
  R2 = results_env$R2,
  R3 = results_env$R3,
  R4 = results_env$R4
)

# --- ==== [ Summary ] ==== ---
cat('\n=== Execution Summary ===\n')
cat('Total jobs:', length(all_results), '\n')
cat('Execution mode: dynamic DAG scheduler\n')
cat('Produced variables:\n')
print(names(all_results))

# --- ==== [ Finalize XES ] ==== ---
if (is_xes_active && !is.null(X)) {
  if (length(X_list) > 0) {
    do.call(vioplot, c(X_list, list(col = "lightgray", panel.first=grid())))
  }
  l <- unique(X$ID)
  s_last <- NA
  for (i in 1:length(l)) {
    s_last[i] <- sum(X$W[X$ID==l[i]])
  }
  if (length(s_last) > 0) vioplot(s_last, col = "lightgray", panel.first=grid())
  write.csv(X, file=xes_file_name)
}

if (is_plot_active) {
  try(dev.off(), silent = TRUE)
}

# --- ==== [ Cleanup ] ==== ---
plan(sequential)
cat('\nMicroservice execution completed.\n')
