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
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    implementation("net.fabricmc:fabric-language-kotlin:$language_kotlin_version")
    // Bundled so the mod loads without the player installing the Kotlin adapter separately.
    include("net.fabricmc:fabric-language-kotlin:$language_kotlin_version")
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
