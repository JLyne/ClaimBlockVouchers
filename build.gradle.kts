import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.pluginYml)
}

group = "uk.co.notnull"
version = "1.0-SNAPSHOT"
description = "Voucher items for redeeming claim blocks."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.not-null.co.uk/snapshots/")
    }
    mavenLocal()
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.griefPrevention)
    compileOnly(libs.customItems)
    paperLibrary(libs.messagesHelper)
}

paper {
    main = "uk.co.notnull.claimblockvouchers.ClaimBlockVouchers"
    loader = "uk.co.notnull.claimblockvouchers.ClaimBlockVouchersLoader"
    apiVersion = libs.versions.paperApi.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
    authors = listOf("Jim (AnEnragedPigeon)")
    generateLibrariesJson = true
    description = "Voucher items for redeeming claim blocks."

    serverDependencies {
        register("GriefPrevention") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("CustomItems") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
        }
    }

    permissions {
        register("claimblockvouchers.give") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
        options.encoding = "UTF-8"
    }
}
