<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20Platform-2.18.1-000000?style=for-the-badge&logo=intellijidea&logoColor=white)](https://github.com/JetBrains/intellij-platform-gradle-plugin)
[![IDEA](https://img.shields.io/badge/IDEA-2025.2%E2%80%932025.3-FE315D?style=for-the-badge&logo=intellijidea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-4B5563?style=for-the-badge)](LICENSE)
[![Sacred](https://img.shields.io/badge/Sacred-Community-8B1A1A?style=for-the-badge&labelColor=1C1410)](https://ancaria.dev)

[English](README.EN.md)

</div>

# Sacred Mod Development

Sacred Mod Development: плагин для разработки модов Sacred Gold в IntelliJ
IDEA. В нём есть мастер создания проекта, конфигурация Run Sacred, две иконки на
полях редактора и страница настроек.

Мастер и `coderpack new` обращаются к одному генератору проектов. Run Sacred
вызывает Gradle Wrapper из проекта и запускает Sacred Mod Loader. Иконки
работают с API из `coderpack`, а шаблоны остаются в репозитории `build`, поэтому
плагин не хранит их копию.

## Возможности

### File | New | Project | Sacred Mod

Мастер запрашивает группу, имя и идентификатор мода, описание, шаблон, язык мода
и язык сценария сборки. Идентификатор по умолчанию выводится из имени. Флажок
**Create a Git repository** создаёт репозиторий Git и добавляет адрес проекта
как `origin`, но не создаёт коммит. Флажок **Create an SRML mod repository**
добавляет `registry.toml` и workflow для публикации новых версий, которые затем
может устанавливать лаунчер. Адрес репозитория вводится в поле **Repository
URL**. Оба флажка включены по умолчанию. Поля автора, пакета и версии мода
находятся в разделе **Advanced**.

Поля **Language** и **Build script** относятся к разным частям проекта:

- **Language** задаёт язык мода: Java, Kotlin или Groovy. Для Kotlin и Groovy
  среда выполнения добавляется в JAR. Для Java отдельная среда не нужна.
- **Build script** выбирает `build.gradle.kts` или `build.gradle`. Язык
  сценария сборки не обязан совпадать с языком мода.

Списки шаблонов, языков и вариантов сценария сборки поступают из генератора.
После следующей сборки плагина новый шаблон из `build` появляется в мастере без
изменений в его исходном коде. Для каждого шаблона показываются только
поддерживаемые им языки.

Мастер передаёт генератору те же параметры, что и `coderpack new`, поэтому оба
способа создают одинаковую структуру проекта. Полученный проект собирается и
проходит проверку без ручных исправлений.

### Run Sacred

Плагин добавляет конфигурацию **Run Sacred** при открытии проекта, если
`dev.ancaria.coderpack` найден в сценарии сборки корня или его непосредственного
подкаталога. Существующая конфигурация не дублируется. Если с Gradle ещё не
связан ни один проект, плагин также связывает корневой проект.

По умолчанию Run Sacred запускает
`gradlew installSacredMod -PsacredDir=<папка игры> --console=plain`,
устанавливает выбранный релиз Sacred Mod Loader в папку игры и запускает его.
В Windows используется `gradlew.bat`. Если нужного релиза нет в локальном кеше,
плагин скачивает его. Перед копированием установленный файл сравнивается с
кешированной версией по SHA-256.

Если папка Sacred Gold не настроена, Run Sacred открывает страницу настроек.

При включённом флажке **Build and install the mod first** консоль запуска
показывает вывод Gradle. После успешной сборки лаунчер запускается отдельно,
поэтому кнопка Stop не завершает игру. Если флажок выключен или в проекте нет
Gradle Wrapper, консоль подключается к лаунчеру и Stop завершает его процесс.
Флажок **Show the loader console** добавляет параметр `--debug`.

В конфигурации запуска сохраняются только эти два флажка. Путь к игре и релиз
лаунчера хранятся в настройках IDE на уровне приложения, отдельно от проекта,
поэтому они не попадают в общий файл конфигурации запуска.

### Иконки на полях

Плагин показывает иконки возле классов, реализующих `SacredMod`, и возле
корректных обработчиков событий с `@Subscribe`. Одна реализация обслуживает
Java, Kotlin и Groovy через UAST.

Иконка точки входа появляется у каждого конкретного неабстрактного класса,
который реализует `SacredMod`. Подсказка сообщает, совпадает ли класс с
`entrypoint` из сценария сборки. Нажатие на иконку открывает место объявления
`entrypoint`, если плагин его нашёл.

Иконка события появляется у метода с `@Subscribe`, одним параметром и типом
параметра из иерархии событий Sacred. До первой синхронизации Gradle также
распознаются классы в пакете событий, кроме `Guard`. В подсказке указаны событие
и нестандартный приоритет обработчика. Нажатие открывает класс события.

### Settings | Tools | Sacred Mod Development

На странице задаются папка Sacred Gold и релиз Sacred Mod Loader. В выбранной
папке должен находиться `pureHD.exe`, `Sacred.exe` или `Game.exe`.

Список стабильных релизов загружается из GitHub. Черновики, предварительные
релизы и записи без файла `Sacred Mod Loader.exe` в список не попадают. До
завершения запроса доступны версии из локального кеша, а кнопка **Refresh**
повторно запрашивает список. Пустое значение означает последний релиз и после
загрузки списка отображается как `Latest (<версия>)`. Если GitHub недоступен,
плагин выбирает самую новую версию из кеша. Выбор сохраняется на уровне IDE.
Нажатие Apply скачивает релиз, если его ещё нет.

Файлы находятся в
`<gradle user home>/caches/ancaria/launcher/<версия>/Sacred Mod Loader.exe`.
Плагин не удаляет старые версии автоматически.

## Установка

Через Marketplace: **Settings | Plugins | Marketplace**, затем найдите
**Sacred Mod Development**.

Из файла: скачайте `sacred-idea-<версия>.zip` из релиза этого репозитория или
возьмите локальный файл
`build/distributions/sacred-idea-<версия>.zip`. Затем откройте
**Settings | Plugins | ⚙ | Install Plugin from Disk**.

Поддерживаются IntelliJ IDEA 2025.2 и 2025.3, Community и Ultimate.

## Сборка

Для запуска Gradle нужен JDK в `PATH`. Сборка использует Java 21. Если подходящей
версии нет локально, toolchain resolver скачает Temurin 21.

```
./gradlew build          # собрать, прогнать тесты, собрать zip
./gradlew runIde         # песочница с плагином внутри
./gradlew verifyPlugin   # то, что Marketplace прогоняет при загрузке
```

Первая сборка скачивает дистрибутив IDE объёмом около гигабайта в кеш Gradle.

Если репозиторий `build` находится рядом с `idea`, каталог `../build/gradle`
подключается как composite build. Изменения шаблонов тогда видны в мастере без
публикации. Без соседнего репозитория зависимость
`dev.ancaria.coderpack:templates` разрешается через Maven Local или Maven
Central. CI проверяет именно этот вариант, предварительно публикуя генератор в
Maven Local.

## Релиз

Версия задаётся свойством `pluginVersion` в `gradle.properties`. На ветке
`master`, если тега `v<версия>` ещё нет, CI публикует плагин в JetBrains
Marketplace и создаёт релиз GitHub с тем же ZIP-файлом. При существующем теге CI
только собирает и проверяет плагин.

Для публикации нужен один секрет — `PUBLISH_TOKEN`. Своим сертификатом плагин
не подписывается: Marketplace подписывает сам, а начав подписывать своим,
перестать уже нельзя.

## Лицензия

Проект распространяется по лицензии MIT. См. [LICENSE](LICENSE). Плагин не
содержит файлы игры, для работы нужна установленная копия Sacred Gold.
