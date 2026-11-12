pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "OmniFile"
include(":app")
include(":core:engine-api")
include(":core:workspace")
include(":feature:filemanager")
include(":feature:toolkit")
include(":engines:apktool")
include(":engines:jadx")
include(":engines:smali")
