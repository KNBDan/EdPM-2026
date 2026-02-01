package logic.description.rtranslator;

//здесь хранится базовый код функций для преобразования псевдокода в код для R
public class BasicFunctionCode {
    static public String returnBasicFunktionCode(){
        String code = "library(vioplot) #Для рисования скрипичной диаграммы\n" +
"library(purrr)\n" +
"\n" +
"# --- ==== [ Объявление функций ] ==== ---\n" +
"\n" +
"####### S элемент с вероятностной работой\n" +
"# P - вероятность \n" +
"# N - число элементов для заполнения\n" +
"# ID - значение с которого начинается нумерация генерируемых событий\n" +
"S_prob<-function(N, P, ID){\n" +
"  RES<-vector(mode = \"integer\", length = N)\n" +
"  RES[1:N]<-rpois(N,P)\n" +
"  \n" +
"  S1<-data.frame(S=RES, ID=vector(mode = \"numeric\", length = N))\n" +
"  ID_S<-ID\n" +
"  for (i in 1:N){\n" +
"    if (RES[i]>0){\n" +
"      Vect<-as.vector(ID_S+1:RES[i], mode = \"numeric\")\n" +
"      ID_S<-ID_S+RES[i]\n" +
"      S1$ID[i]<-list(Value=Vect)\n" +
"    }\n" +
"  }\n" +
"  S_prob<-S1\n" +
"}\n" +
"\n" +
"#######  S элемент с периодической работой\n" +
"# FP - начало  заполнения\n" +
"# P - периаодичность\n" +
"# N - число элементов для заполнения\n" +
"# ID - значение с которого начинается нумерация генерируемых событий\n" +
"S_periodic<-function(N, FP, P, ID){\n" +
"  #RES<-vector(mode = \"integer\", length = N)\n" +
"  if (length(FP) == 0) {\n" +
"    RES<-vector(mode = \"integer\", length = N)\n" +
"  } else {\n" +
"    RES1<-vector(mode = \"integer\", length = FP[1]-1)       \n" +
"    RES2<-vector(mode = \"integer\", length = N-FP[1]+1)\n" +
"    MN<-1:(N-FP[1])\n" +
"    RES2[(MN+P-1)%%P==0]<-1\n" +
"    RES<-c(RES1,RES2)\n" +
"  }\n" +
"  S1<-data.frame(S=RES, ID=vector(mode = \"numeric\", length = N))\n" +
"  ID_S<-ID\n" +
"  for (i in 1:N){\n" +
"    if (RES[i]>0){\n" +
"      Vect<-as.vector(ID_S+1:RES[i], mode = \"numeric\")\n" +
"      ID_S<-ID_S+RES[i]\n" +
"      S1$ID[i]<-list(Value=Vect)\n" +
"    }\n" +
"  }\n" +
"  S_prob<-S1\n" +
"}\n" +
"\n" +
"#V линейная сложность\n" +
"V_L<-function(N,O){\n" +
"  V_L<-N*O\n" +
"}\n" +
"\n" +
"#V сложность N^2\n" +
"V_L2<-function(N, O){\n" +
"  V_2L<-(N*N)*O\n" +
"}\n" +
"\n" +
"#V логорифмическая сложность\n" +
"V_Lg<-function(N, O){\n" +
"  if (N == 0) {V_Lg<-0} else {\n" +
"    if (N == 1) {\n" +
"      V_Lg<-1\n" +
"    } else {  \n" +
"      V_Lg<-N*log(N)*O\n" +
"    }  \n" +
"  }\n" +
"}\n" +
"\n" +
"#V экспоненциальная сложность\n" +
"V_E<-function(N, O){\n" +
"  if (N == 0) {V_E<-0} else {V_E<-exp(N)*O}\n" +
"}\n" +
"\n" +
"################# Блок обработки (V)  #######################\n" +
"# O - тип сложности (1 - линейная сложность, 2 - сложность N^2, 3 - логорифмическая сложность, 4 - експоненциальная сложность)\n" +
"# S - поток событий\n" +
"# V - название блока для записи в результирующую талицу (требуется для построения XES файла)\n" +
"\n" +
"# Формат таблицы которую получаем\n" +
"# I - номер шага\n" +
"# J - номер шага на котором последний раз освободился блок (получил статус - свободен)\n" +
"# Prj_Flow - поток событий\n" +
"# Prj_File - очередь событий\n" +
"# V_W - количество шагов обработки\n" +
"# V - название блока обработки\n" +
"# R - выходной (после обработки) поток событий \n" +
"# ID_File - идентификаторы событий в очереди \n" +
"# ID_Out - идентификаторы событий почсле обработки \n" +
"\n" +
"V<-function(m, S, V, O){\n" +
"  # расчет для логарифмической сложности\n" +
"  N<-length(S$S)\n" +
"  Df<-data.frame(I=1:N, \n" +
"                 J=vector(mode = \"numeric\", length = length(N)), \n" +
"                 Prj_Flow=S$S, \n" +
"                 Prj_File=vector(mode = \"numeric\", length = length(N)), \n" +
"                 V_W=vector(mode = \"numeric\", length = length(N)), \n" +
"                 V=V, \n" +
"                 R=vector(mode = \"numeric\", length = length(N)), \n" +
"                 ID_File=vector(mode = \"numeric\", length = length(N)), \n" +
"                 ID_Out=vector(mode = \"numeric\", length = length(N)))\n" +
"  Df$ID_File<-rep(list(0), length(N))\n" +
"  Df$ID_Out<-rep(list(0), length(N))\n" +
"  j<-1\n" +
"  L<-0\n" +
"  for (i in 1:N){\n" +
"    Df$Prj_File[i]<-sum(Df$Prj_Flow[i:j])\n" +
"    Df$ID_File[i]<-list(unique(list_c(S$ID[i:j])))\n" +
"    Df$J[i]<-j\n" +
"    if (Df$V_W[i]==0) {\n" +
"      nk<-Df$Prj_File[i]\n" +
"      #if (m == -1) {L<-ceiling(V_L001(nk))}\n" +
"      #if (m == 0) {L<-ceiling(V_L01(nk))}\n" +
"      if (m == 1) {L<-ceiling(V_L(nk, O))}\n" +
"      if (m == 2) {L<-ceiling(V_L2(nk, O))}\n" +
"      if (m == 3) {L<-ceiling(V_Lg(nk, O))}\n" +
"      if (m == 4) {L<-ceiling(V_E(nk, O))}\n" +
"      k<-min(i+L-1,N)\n" +
"      if (k>=i){\n" +
"        Df$V_W[i:k]<-L\n" +
"        Df$R[k] <- nk\n" +
"        Df$ID_Out[k]<-Df$ID_File[i]\n" +
"      }\n" +
"      j<-i+1\n" +
"    }\n" +
"    \n" +
"  }\n" +
"  \n" +
"  P1<-Df$R\n" +
"  P2<-Df$ID_Out\n" +
"  \n" +
"  Df$R[1]<-0\n" +
"  Df$R[2:N]<-P1[1:(N-1)]\n" +
"  Df$ID_Out[1]<-list(0)\n" +
"  if (N > 1) {\n" +
"    Df$ID_Out[2:N]<-P2[1:(N-1)]\n" +
"  }\n" +
"  V1<-Df\n" +
"}\n" +
"\n" +
"################# Собираем статистику в формате XES (Process Mining)  #######################\n" +
"#  R - таблица выдаваемая блоком V\n" +
"\n" +
"# Формат таблицы которую получаем\n" +
"# ID - идентификатор события\n" +
"# V - операция\n" +
"# I -	номер шага поступления\n" +
"# W - длительность обработки в шагах\n" +
"\n" +
"XES<-function(R){\n" +
"  Df<-data.frame(ID=0, V=\"V\", I=0, W=0)\n" +
"  N1<-length(R$I)\n" +
"  for (i in 1:N1){\n" +
"    N2<-length(R$ID_Out[[i]])\n" +
"    if (N2>1 & R$R[i]>0){\n" +
"      Vec<-list_c(R$ID_Out[i])\n" +
"      for (j in 1:N2){\n" +
"          if (Vec[j]>0){\n" +
"            Df_0<-data.frame(ID=Vec[j], V=R$V[i], I=i, W=R$V_W[i-1])\n" +
"            Df<-rbind(Df,Df_0)\n" +
"         }\n" +
"      }\n" +
"    }\n" +
"  }\n" +
"  Df<-Df[-1,]\n" +
"  XES<-Df\n" +
"}\n" +
"\n" +
"# Объединение двух последовательностей типа выдаваемого блоком S по аддитивному закону\n" +
"Add<-function(A1, A2){\n" +
"  N1<-length(A1$S)\n" +
"  AS3<-vector(mode = \"integer\", length = N1)\n" +
"  AS3<-A1$S+A2$S\n" +
"  A3<-data.frame(S=AS3, ID=vector(mode = \"numeric\", length = N1))\n" +
"  \n" +
"  for (i in 1:N1){\n" +
"    Vect<-as.vector(unique(c(A1$ID[[i]],A2$ID[[i]])), mode = \"numeric\")\n" +
"    A3$ID[i]<-list(Value=Vect)\n" +
"  }\n" +
"  Add<-A3\n" +
"}\n" +
"\n" +
"\n" +
"# Разделение потоков по номерам проектов \n" +
"Select<-function(A, N1, N2){\n" +
"  N<-length(A$S)\n" +
"  A3<-data.frame(S=A$S, ID=vector(mode = \"numeric\", length = N))\n" +
"  for (i in 1:N){\n" +
"    Vect<-as.vector(unique(A$ID[[i]]), mode = \"numeric\")\n" +
"    Vect1<-0\n" +
"    k<-1\n" +
"    for (j in 1:length(Vect)) {\n" +
"      if (Vect[j]>N1 &  Vect[j]<N2){\n" +
"        Vect1<-c(Vect1, Vect[j])\n" +
"      }\n" +
"    }\n" +
"    A3$ID[i]<-list(Value=Vect1)\n" +
"    A3$S[i]<-length(A3$ID[[i]])-1\n" +
"  }\n" +
"  Select<-A3\n" +
"}\n" +
"\n" +
"# --- ==== [ Основная программа ] ==== ---";
        return code;
    }
}
