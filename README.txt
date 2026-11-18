Nearest Structures — Fabric 1.21.11

ГОТОВЫЙ ПРОЕКТ (шаблон). Единственное, чего нет — gradle-wrapper.jar (его нельзя скачать без интернета).
Выбери ОДИН из способов сборки:

СПОСОБ 1 — без установки чего-либо (GitHub):
  1. Создай репозиторий на github.com и загрузи в него содержимое этой папки
     (кнопка "Add file" -> "Upload files", включая скрытую папку .github).
  2. Вкладка Actions -> build -> дождись зелёной галочки.
  3. Внутри запуска внизу в Artifacts скачай "nearest-structures-jar" — там .jar.
     Берёшь файл БЕЗ "-sources" в названии.

СПОСОБ 2 — на своём компьютере (IntelliJ IDEA):
  1. Поставь JDK 21 и IntelliJ IDEA Community.
  2. File -> Open -> выбери эту папку (build.gradle), дождись импорта Gradle.
  3. Справа вкладка Gradle -> Tasks -> build -> build (двойной клик).
  4. Готовый .jar: папка build/libs/.

В Minecraft: положи .jar и Fabric API в папку mods (Fabric Loader 0.18.4+).

Клавиши: H — меню, J — повторить поиск, K — убрать метку. Команда: /nearest <тип>.
