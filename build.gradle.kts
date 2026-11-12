plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

// Move build directories out of OneDrive to prevent file locking ("Unable to delete directory") on Windows
val localBuildDir = File(System.getProperty("user.home"), ".gradle-builds/${rootProject.name}")
allprojects {
    layout.buildDirectory.set(File(localBuildDir, project.name))
}
