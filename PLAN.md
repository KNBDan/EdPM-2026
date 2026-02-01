# План рефакторинга (черновик)

Цель: вынести не‑UI логику из Swing‑классов с минимальными изменениями поведения.

Планируемая структура (целевая):
- src/main/java/app/                // UI wiring (existing Swing frames stay)
- src/main/java/domain/             // Domain entities for diagram (optional later)
- src/main/java/logic/              // Pure logic/services (no Swing)
- src/main/java/logic/pagerank/      // PageRank calculations
- src/main/java/logic/graph/         // Connection rules, graph helpers
- src/main/java/logic/serialization/ // JSON load/save helpers
- src/main/java/logic/history/       // Undo/redo state management
- src/main/java/logic/description/   // Pseudocode/R-code generation adapters

Шаг 1 (старт): вынести PageRank из UI
- Create: src/main/java/logic/pagerank/PageRankService.java
- Move from: src/main/java/jMDIForm/jMDIFrame.java
  - PageRank(...)
  - MergeMatrix(...)
  - MultiplyMatrixIteration(...)
  - MultiplyMatrixUntilDelta(...)
  - Determinant(...)
  - MatrixMinor(...)
  - Transposition(...)
  - InverseMatrix(...)
  - DFS/Component (if needed by metrics)
- Update callers:
  - src/main/java/jMDIForm/PageRankFrame.java
  - src/main/java/jMDIForm/jMDIFrame.java

Шаг 2: вынести правила соединений (валидация стрелок)
- Create: src/main/java/logic/graph/ConnectionPolicy.java
- Move from: src/main/java/jMDIForm/jMDIFrame.java
  - findLinkedFigToFig(...)
  - validation logic in jPanel1MouseReleased (connection checks)

Шаг 3: вынести сериализацию
- Create: src/main/java/logic/serialization/DiagramSerializer.java
- Move from: src/main/java/jMDIForm/jMDIFrame.java
  - CreatorConvertObject()
  - SaveInJSON(...)
  - LoadFromJSON(...)
- Keep UI file chooser in UI classes.

Шаг 4: вынести генерацию описаний/R‑кода
- Create: src/main/java/logic/description/DescriptionService.java
- Move from: src/main/java/jMDIForm/jMDIFrame.java
  - GenerateDescription()
  - SaveInRFile()

Шаг 5: вынести историю/undo
- Create: src/main/java/logic/history/HistoryService.java
- Move from: src/main/java/jMDIForm/jMDIFrame.java
  - saveState()
  - undo()
- Keep DiagramState in jMDIForm or move to logic/history later.

Примечания:
- Поведение менять не планировалось; код переносился как есть.
- Сравнения через `==` пока оставлены без изменений.
