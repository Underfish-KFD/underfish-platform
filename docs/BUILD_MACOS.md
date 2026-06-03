# Сборка отчета в macOS / MacTeX

## 1. Установка окружения

Для полной сборки отчета удобнее использовать полный MacTeX, так как он уже содержит `pdflatex`, `latexmk`, `biber`, русские языковые пакеты и стиль библиографии `biblatex-gost`.

Проверка установки:

```bash
which pdflatex
which latexmk
which biber
pdflatex --version
biber --version
```

Если команды не находятся, добавьте TeX Live в `PATH` текущей оболочки:

```bash
export PATH="/Library/TeX/texbin:$PATH"
```

Для постоянного подключения можно добавить эту строку в `~/.zshrc`.

## 2. Подготовка изображений

Файл `docs/report.tex` использует реальные `\includegraphics`, поэтому до финальной сборки в `docs/diagrams` должны лежать PNG-файлы, которые вставляются в текст:

- `uml_component_architecture.png`;
- `er_database_schema.png`;
- `bpmn_registration_login.png`;
- `bpmn_create_event.png`;
- `bpmn_search_event.png`;
- `bpmn_join_event_review.png`;
- `uml_sequence_gateway_auth.png`;
- `figma_mockups_overview.png`;
- `implementation_screens_overview.png`;
- `gradle_tests_screenshot.png`.

Mermaid-исходники находятся в `docs/diagrams/*.mmd`. Их можно экспортировать через Mermaid Live Editor, VS Code Mermaid Preview или `mermaid-cli`.

Пример экспорта через `mermaid-cli`:

```bash
npx -y @mermaid-js/mermaid-cli \
  -i docs/diagrams/uml_microservices.mmd \
  -o docs/diagrams/uml_component_architecture.png \
  -b white
```

## 3. Сборка через latexmk

Запускать сборку лучше из корня репозитория с флагом `-cd`: так `latexmk` перейдет в каталог `docs`, корректно найдет `diagrams/` и положит итоговый PDF рядом с исходником.

```bash
latexmk -cd -pdf -interaction=nonstopmode -file-line-error docs/report.tex
```

`latexmk` сам выполнит нужную последовательность `pdflatex -> biber -> pdflatex -> pdflatex`. Итоговый PDF появится как `docs/report.pdf`.

## 4. Ручная сборка без latexmk

Если `latexmk` недоступен, используйте ручную последовательность:

```bash
cd docs
pdflatex -interaction=nonstopmode -file-line-error report.tex
biber report
pdflatex -interaction=nonstopmode -file-line-error report.tex
pdflatex -interaction=nonstopmode -file-line-error report.tex
```

Ручной вариант выполняется из каталога `docs`, потому что там создается `underfish_report_refs.bib` из блока `filecontents*`, лежат временные файлы сборки и доступен каталог `diagrams/`.

## 5. Очистка временных файлов

```bash
latexmk -cd -C docs/report.tex
```

После очистки можно собрать документ повторно командой из раздела 3.
