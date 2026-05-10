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
  plot(1:N, S1$S, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,6), main = "Элемент S1")
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

N <- 1000
i <- 1
FP <- 1
R1<- V1_func()

plot(1:N, R1$R, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R1")
plot(1:N, R1$Prj_File, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R1")
X1<-XES(R1)
X<-XES(R1)
if (length(X1$W) > 0) vioplot(X1$W, col = "lightgray", panel.first=grid(), main = "Element R1")
if (length(X$W) > 0) vioplot(X$W, col = "lightgray", panel.first=grid(), main = "All elements before R1")
R2<- V2_func(R1)

plot(1:N, R2$R, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R2")
plot(1:N, R2$Prj_File, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R2")
X2<-XES(R2)
X<-rbind(X,X2)
if (length(X2$W) > 0) vioplot(X2$W, col = "lightgray", panel.first=grid(), main = "Element R2")
if (length(X$W) > 0) vioplot(X$W, col = "lightgray", panel.first=grid(), main = "All elements before R2")
R3<- V3_func(R2)

plot(1:N, R3$R, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R3")
plot(1:N, R3$Prj_File, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R3")
X3<-XES(R3)
X<-rbind(X,X3)
if (length(X3$W) > 0) vioplot(X3$W, col = "lightgray", panel.first=grid(), main = "Element R3")
if (length(X$W) > 0) vioplot(X$W, col = "lightgray", panel.first=grid(), main = "All elements before R3")
R4<- V4_func(R3)

plot(1:N, R4$R, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R4")
plot(1:N, R4$Prj_File, type="s", col="black", panel.first=grid(), ylab='S', xlab='i', ylim = c(0,15), main = "Element R4")
X4<-XES(R4)
X<-rbind(X,X4)
if (length(X4$W) > 0) vioplot(X4$W, col = "lightgray", panel.first=grid(), main = "Element R4")
if (length(X$W) > 0) vioplot(X$W, col = "lightgray", panel.first=grid(), main = "All elements before R4")
X_list <- list(X1$W,X2$W,X3$W,X4$W)
X_list <- X_list[sapply(X_list, length) > 0]
if (length(X_list) > 0) do.call(vioplot, c(X_list, list(col = "lightgray", panel.first=grid())))
l<-unique(X$ID)
s_last<-NA
for (i in 1:length(l)){
  s_last[i]<-sum(X$W[X$ID==l[i]])
}
if (length(s_last) > 0) vioplot(s_last, col = "lightgray", panel.first=grid())
write.csv(X, file="xesik.csv")
