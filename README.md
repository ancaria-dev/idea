<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20Platform-2.18.1-000000?style=for-the-badge&logo=intellijidea&logoColor=white)](https://github.com/JetBrains/intellij-platform-gradle-plugin)
[![IDEA](https://img.shields.io/badge/IDEA-2025.2%E2%80%932025.3-FE315D?style=for-the-badge&logo=intellijidea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-4B5563?style=for-the-badge)](LICENSE)
[![Sacred](https://img.shields.io/badge/Sacred-Community-8B1A1A?style=for-the-badge&labelColor=1C1410)](https://ancaria.dev)

[English](README.EN.md) · [Deutsch](README.DE.md)

</div>

# Sacred Mod Development

Плагин для IntelliJ IDEA, с которым мод для Sacred Gold создаётся и
запускается прямо из IDE.

Мастер создаёт готовый Gradle-проект мода. Кнопка Run Sacred собирает мод,
ставит его в игру и запускает лаунчер. Иконки на полях показывают точку входа
мода и его обработчики событий.

Плагин не хранит своих шаблонов. Мастер берёт их из того же генератора, что и
`coderpack new`, поэтому оба способа дают одинаковый проект.

## Как начать

1. Установите плагин: **Settings | Plugins | Marketplace**, найдите
   **Sacred Mod Development** или откройте
   [страницу плагина](https://plugins.jetbrains.com/plugin/34165-sacred-mod-development).
2. Создайте проект: **File | New | Project | Sacred Mod**.
3. Укажите папку игры: **Settings | Tools | Sacred Mod Development**.
4. Нажмите **Run Sacred**.

Плагин работает в IntelliJ IDEA 2025.2 и 2025.3, Community и Ultimate. Чтобы
поставить его из файла, скачайте `sacred-idea-<версия>.zip` из релизов и
выберите **Settings | Plugins | ⚙ | Install Plugin from Disk**.

## Возможности

### Мастер нового проекта

Мастер спрашивает группу, имя мода, описание, шаблон, язык мода и язык
сценария сборки. Идентификатор мода он выводит из имени. Автор, пакет и версия
лежат в разделе **Advanced**.

Язык мода и язык сборки выбираются независимо:

- **Language** — язык мода: Java, Kotlin или Groovy. Рантайм Kotlin и Groovy
  попадает в jar мода, Java свой рантайм не нужен.
- **Build script** — `build.gradle.kts` или `build.gradle`.

Два флажка включены по умолчанию. **Create a Git repository** создаёт
репозиторий и добавляет **Repository URL** как `origin`, но не делает коммит.
**Create an SRML mod repository** добавляет `registry.toml` и workflow, который
выпускает новые версии мода. После этого игроки могут ставить мод из лаунчера.

Шаблоны и языки приходят из генератора в `build`. Новый шаблон появится в
мастере после пересборки плагина, и для каждого шаблона видны только его
языки.

### Run Sacred

Плагин сам добавляет конфигурацию **Run Sacred**, если находит
`dev.ancaria.coderpack` в сценарии сборки корня или вложенной папки первого
уровня. Если проект ещё не связан с Gradle, плагин свяжет его.

При запуске Run Sacred:

1. Собирает мод и ставит его в игру через
   `gradlew installSacredMod -PsacredDir=<папка игры> --console=plain`.
2. Скачивает выбранный релиз Sacred Mod Loader, если его нет в кеше.
3. Копирует лаунчер в папку игры, если он отличается от кешированного по
   SHA-256.
4. Запускает лаунчер.

Если папка игры не задана, Run Sacred откроет настройки.

У конфигурации два флажка. **Build and install the mod first** включает
сборку: консоль показывает вывод Gradle, а лаунчер запускается отдельно, так
что Stop не закрывает игру. Без сборки или без Gradle Wrapper консоль следит
за лаунчером, и Stop его завершает. **Show the loader console** добавляет
`--debug`.

Путь к игре и версия лаунчера хранятся в настройках IDE, а не в конфигурации.
Поэтому общий файл конфигурации не содержит путей с вашей машины.

### Иконки на полях

Иконка точки входа стоит у каждого неабстрактного класса, который наследует
`SacredMod` напрямую или через свой абстрактный класс. Подсказка говорит,
совпадает ли класс с `entrypoint` из сценария сборки. Щелчок открывает это
объявление.

Иконка события стоит у метода с `@Subscribe` и одним параметром-событием.
Подсказка называет событие и приоритет, если он не стандартный. У обработчика
с `MONITOR` иконка серая. Щелчок открывает класс события. Тип результата
иконка не проверяет: это делает линтер при сборке.

Иконки работают в Java, Kotlin и Groovy.

### Настройки

**Settings | Tools | Sacred Mod Development** хранит папку Sacred Gold и
релиз Sacred Mod Loader. В папке должен лежать `pureHD.exe`, `Sacred.exe` или
`Game.exe`.

Список релизов плагин берёт из GitHub и показывает только стабильные с файлом
`Sacred Mod Loader.exe`. Пока список грузится, доступны версии из кеша.
**Refresh** запрашивает его заново. Пустое значение означает последний релиз и
после загрузки выглядит как `Latest (<версия>)`. Без доступа к GitHub плагин
берёт самую новую версию из кеша. **Apply** скачивает выбранный релиз.

Лаунчеры хранятся в
`<gradle user home>/caches/ancaria/launcher/<версия>/Sacred Mod Loader.exe`.
Старые версии плагин не удаляет.

## Сборка

Для Gradle нужен JDK в `PATH`. Плагин собирается под Java 21. Если её нет,
Gradle скачает Temurin 21 сам.

```
./gradlew build          # сборка, тесты и zip
./gradlew runIde         # IDE-песочница с плагином
./gradlew verifyPlugin   # та же проверка, что в Marketplace
```

Первая сборка скачивает в кеш Gradle дистрибутив IDE размером около гигабайта.

Шаблоны приходят из Maven Central как `dev.ancaria.coderpack:templates`.
Чтобы попробовать ещё не выпущенный шаблон, выполните `publishToMavenLocal` в
репозитории `build`: Maven Local проверяется первым.

## Релизы

Версия плагина задана в `pluginVersion` в `gradle.properties`. Когда на
`master` появляется версия без тега `v<версия>`, CI публикует плагин в
JetBrains Marketplace и выпускает GitHub-релиз с тем же zip. Если тег уже
есть, CI только собирает и проверяет плагин.

Для публикации нужен один секрет — `PUBLISH_TOKEN`. Своим сертификатом плагин
не подписывается: это делает Marketplace. Если начать подписывать самому,
отказаться от этого уже нельзя.

## Лицензия

MIT, см. [LICENSE](LICENSE). Плагин не содержит файлов игры, нужна своя копия
Sacred Gold.
