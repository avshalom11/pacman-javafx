
import java.io.File

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.26.0"

}

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

application {
    mainModule.set("pacman")
    mainClass.set("com.pacman.App")
}

javafx {
    version = "17"
    modules = listOf(
        "javafx.controls",
        "javafx.graphics",
        "javafx.media"     // <<< חשוב לסאונד
    )
}

jlink {
    addExtraDependencies("javafx")
    options.set(listOf("--strip-debug", "--no-header-files", "--no-man-pages", "--compress=2"))

    launcher { name = "Pacman" }

    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            // משתמשים ב-MSI (נוח ויציב). ל-EXE צריך WiX תואם; MSI עובד עם WiX 3.x.
            installerType = "msi"

            // אייקון אם קיים ב-icons/game.ico
            val ico = File("icons/game.ico")
            if (ico.exists()) icon = ico.path

            installerOptions = listOf("--win-menu", "--win-shortcut")
        }
        appVersion = project.version.toString()
        vendor = "YourName"
    }
    imageName.set("Pacman")
    launcher {
        name = "Pacman"
    }
    jpackage {
        installerType = "msi"
        installerOptions = listOf("--wix-config", "installer-config.wxs")
        icon = "icons/game.ico"
    }
}


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


