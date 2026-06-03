Диаграммы и скриншоты для docs/report.tex
=========================================

1. UML-диаграммы в Mermaid:
   - source: docs/diagrams/uml_microservices.mmd
   - export target: docs/diagrams/uml_component_architecture.png
   - source: docs/diagrams/uml_gateway_auth_sequence.mmd
   - export target: docs/diagrams/uml_sequence_gateway_auth.png

2. BPMN-style диаграммы бизнес-процессов в Mermaid:
   - source: docs/diagrams/bpmn_registration_login.mmd
   - export target: docs/diagrams/bpmn_registration_login.png
   - source: docs/diagrams/bpmn_create_event.mmd
   - export target: docs/diagrams/bpmn_create_event.png
   - source: docs/diagrams/bpmn_search_event.mmd
   - export target: docs/diagrams/bpmn_search_event.png
   - source: docs/diagrams/bpmn_join_event_review.mmd
   - export target: docs/diagrams/bpmn_join_event_review.png

3. Экспорт Mermaid в PNG можно сделать любым способом:
   - через https://mermaid.live/;
   - через VS Code Mermaid Preview;
   - через mermaid-cli, например:
     mmdc -i docs/diagrams/uml_microservices.mmd -o docs/diagrams/uml_component_architecture.png -b white
     mmdc -i docs/diagrams/uml_gateway_auth_sequence.mmd -o docs/diagrams/uml_sequence_gateway_auth.png -b white
     mmdc -i docs/diagrams/bpmn_registration_login.mmd -o docs/diagrams/bpmn_registration_login.png -b white
     mmdc -i docs/diagrams/bpmn_create_event.mmd -o docs/diagrams/bpmn_create_event.png -b white
     mmdc -i docs/diagrams/bpmn_search_event.mmd -o docs/diagrams/bpmn_search_event.png -b white
     mmdc -i docs/diagrams/bpmn_join_event_review.mmd -o docs/diagrams/bpmn_join_event_review.png -b white

4. Дополнительные PNG, которые ожидает docs/report.tex:
   - er_database_schema.png
   - figma_mockups_overview.png
   - implementation_screens_overview.png
   - gradle_tests_screenshot.png

В TeX-файле используются реальные \includegraphics через макрос \reportfigure,
поэтому для финальной сборки в Overleaf нужно загрузить эти PNG в docs/diagrams.

5. Как оформить скриншоты макетов и реализации:
   - figma_mockups_overview.png: один коллаж из 3-5 экранов Figma;
   - implementation_screens_overview.png: один коллаж из 3-5 аналогичных экранов реализации;
   - минимальный набор: карта мероприятий, поиск/каталог, карточка мероприятия;
   - если есть место, добавьте профиль пользователя или сообщества и кабинет организатора;
   - порядок экранов в двух коллажах лучше сделать одинаковым, чтобы было видно соответствие макета и реализации.
