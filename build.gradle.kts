plugins {
    id("net.fabricmc.fabric-loom") version "1.19.4"
    kotlin("jvm") version "2.4.21"
    java
}

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project
val minecraft_version: String by project
val loader_version: String by project
val fabric_api_version: String by project
val language_kotlin_version: String by project
val devauth_version: String by project
val resourcefulconfig_version: String by project
val resourcefullib_version: String by project
val resourcefulconfigkt_version: String by project
val skyblockapi_version: String by project

group = "com.$mod_id"
version = mod_version

base {
    archivesName = mod_id
}

// Minecraft 26.1 and later ship unobfuscated, so Loom needs no mappings and this project
// uses the non-remapping plugin id. Nothing here is obfuscated on the way out.
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://maven.teamresourceful.com/repository/maven-public/") {
        name = "TeamResourceful"
    }
    // DevAuth is a development-only login helper; it never ships in the mod jar.
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        name = "DevAuth"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    implementation("net.fabricmc:fabric-language-kotlin:$language_kotlin_version")

    // Hypixel data layer: location, dungeon, sacks and item attributes, maintained against
    // the live game rather than parsed here.
    implementation("tech.thatgravyboat:skyblock-api:$skyblockapi_version") {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-$minecraft_version") }
    }
    include("tech.thatgravyboat:skyblock-api:$skyblockapi_version") {
        capabilities { requireCapability("tech.thatgravyboat:skyblock-api-$minecraft_version") }
    }

    // The Kotlin DSL over Resourceful Config, so the config can stay in Kotlin.
    implementation("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-26.1-rc-1:$resourcefulconfigkt_version")
    include("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-26.1-rc-1:$resourcefulconfigkt_version")

    // Config screens and their widget library. Bundled, because players should not have to
    // install a config library to run the mod.
    implementation("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-$minecraft_version:$resourcefulconfig_version")
    include("com.teamresourceful.resourcefulconfig:resourcefulconfig-fabric-$minecraft_version:$resourcefulconfig_version")
    implementation("com.teamresourceful.resourcefullib:resourcefullib-fabric-$minecraft_version:$resourcefullib_version")
    include("com.teamresourceful.resourcefullib:resourcefullib-fabric-$minecraft_version:$resourcefullib_version")

    // Signs the development client into a real Minecraft account, so dev runs see the same
    // account data a release does. Enabled for runClient below; the tokens stay in .devauth.
    runtimeOnly("me.djtheredstoner:DevAuth-fabric:$devauth_version")
}

loom {
    runs {
        named("client") {
            property("devauth.enabled", "true")
            property("devauth.configDir", file(".devauth").absolutePath)
        }
    }
}

tasks.processResources {
    inputs.property("version", mod_version)
    inputs.property("mod_id", mod_id)
    inputs.property("mod_name", mod_name)
    inputs.property("minecraft_version", minecraft_version)
    inputs.property("loader_version", loader_version)

    filesMatching("fabric.mod.json") {
        expand(
            "mod_id" to mod_id,
            "mod_name" to mod_name,
            "version" to mod_version,
            "minecraft_version" to minecraft_version,
            "loader_version" to loader_version,
        )
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}
