# EdPM-Modeler-2025

Этот файл фиксирует изменения рефакторинга, сделанные ассистентом.

Изменения:
- Added `src/main/java/logic/pagerank/PageRankService.java` and moved PageRank + matrix helpers out of `src/main/java/jMDIForm/jMDIFrame.java`.
- `src/main/java/jMDIForm/jMDIFrame.java` now delegates PageRank calculation to the new service.
- Added `src/main/java/logic/graph/ConnectionPolicy.java` and moved connection validation logic out of `src/main/java/jMDIForm/jMDIFrame.java`.
- `src/main/java/jMDIForm/jMDIFrame.java` now delegates arrow validation to the new policy.
- Added `src/main/java/logic/serialization/DiagramSerializer.java` and moved JSON read/write + figure/line conversion out of `src/main/java/jMDIForm/jMDIFrame.java`.
- `src/main/java/jMDIForm/jMDIFrame.java` now delegates JSON save/load and object conversion to the serializer.
- Added `src/main/java/logic/description/DescriptionService.java` and moved description/R‑code generation logic out of `src/main/java/jMDIForm/jMDIFrame.java`.
- `src/main/java/jMDIForm/jMDIFrame.java` now delegates description and R‑code generation/saving to the service.
- Added `src/main/java/logic/history/HistoryService.java` and moved undo/history stack logic out of `src/main/java/jMDIForm/jMDIFrame.java`.
- `src/main/java/jMDIForm/jMDIFrame.java` now delegates history save/undo/clear to the service.
- Added `src/main/java/domain/DiagramLoadResult.java` as a domain data holder for loaded diagram state.
- Added `clearHistory()` in `src/main/java/jMDIForm/jMDIFrame.java` and updated `src/main/java/EPM/mdi.java` to use it.
- Adjusted `src/main/java/logic/description/DescriptionService.java` to wrap `List` into `ArrayList` for `newPrecodeGenerator`.
- Cleaned unused imports and removed unused `OutOfBounds()` in `src/main/java/jMDIForm/jMDIFrame.java`.
- Removed large obsolete commented block and unused `getLines` from `src/main/java/jMDIForm/readSaveData.java`.
- PageRank now uses a stable figure order (by id/name) inside `src/main/java/logic/pagerank/PageRankService.java` to avoid z-order affecting results.
- Added null/empty guards in `src/main/java/logic/graph/ConnectionPolicy.java`, `src/main/java/logic/serialization/DiagramSerializer.java`, and `src/main/java/logic/description/DescriptionService.java`.
- Added LLM prompt storage for V (`llmPrompt`) and wired the new LLM complexity tab in `src/main/java/jMDIForm/PropertiesDialog.java`.
- Приведена структура пакетов в порядок: часть логики перенесена под `logic`, неиспользуемые/устаревшие классы перенесены под `legacy`.
- Переносы пакетов:
  - `converter` -> `logic.serialization.model`
  - `descritGen` -> `logic.description.gen`
  - `rtranslator` -> `logic.description.rtranslator`
  - `line` -> `legacy.line`
  - `point` -> `legacy.point`
  - `JGraphFrame` -> `legacy.jgraph`
  - `EPM/S.java` -> `legacy.ui`
  - `jMDIForm/Gson.java`, `jMDIForm/g2d.java`, `jMDIForm/FileUtils.java`, `jMDIForm/Figure_s.java`, `jMDIForm/CustomVerticalScrollbar.java` -> `legacy.misc`
- Фигуры разнесены по подпакетам `objects`:
  - `figure` -> `objects.figure`
  - линии из `figure` -> `objects.line`
  - точки из `figure` -> `objects.point`
  - рендер‑утилиты из `figure` -> `objects.render`

Текущая структура (высокий уровень):
- `src/main/java/EPM` — основной UI (NetBeans MDI)
- `src/main/java/jMDIForm` — формы/панели UI и диаграмма
- `src/main/java/objects` — фигуры/линии/точки/рендер, разнесенные по подпакетам
- `src/main/java/logic` — чистая логика/сервисы
- `src/main/java/domain` — доменные структуры (минимально)
- `src/main/java/legacy` — устаревшие/неиспользуемые классы
