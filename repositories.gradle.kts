dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

pluginManagement {
    repositories {maven { url = uri("https://raw.githubusercontent.com/tachiyomiorg/tachiyomi/master/repository") }
maven { url = uri("https://raw.githubusercontent.com/inorichi/injekt/master") }

        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
