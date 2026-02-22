# EdPM-2026

Изменения (кратко):
- Вынесен PageRank и матричные операции из UI в `src/main/java/logic/pagerank/PageRankService.java`.
- Вынесена проверка связей/стрелок из UI в `src/main/java/logic/graph/ConnectionPolicy.java`.
- Вынесено сохранение/загрузка JSON из UI в `src/main/java/logic/serialization/DiagramSerializer.java`.
- Вынесена генерация описаний и R-кода из UI в `src/main/java/logic/description/DescriptionService.java`.
- Вынесена история/undo из UI в `src/main/java/logic/history/HistoryService.java`.
- Добавлен доменный класс `src/main/java/domain/DiagramLoadResult.java`.
- Почищены неиспользуемые импорты и большие устаревшие комментарии в UI/serialization.
- В фигуру V добавлен `llmPrompt` и вкладка LLM в `src/main/java/jMDIForm/PropertiesDialog.java`.
- Упорядочены пакеты и разнесены классы по логике:
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
  - линии -> `objects.line`
  - точки -> `objects.point`
  - рендер-утилиты -> `objects.render`

Текущая структура (верхний уровень):
- `src/main/java/EPM` — основной UI (NetBeans MDI)
- `src/main/java/jMDIForm` — формы/панели UI и диаграмма
- `src/main/java/objects` — фигуры/линии/точки/рендер
- `src/main/java/logic` — чистая логика/сервисы
- `src/main/java/domain` — доменные структуры
- `src/main/java/legacy` — устаревшие/неиспользуемые классы
