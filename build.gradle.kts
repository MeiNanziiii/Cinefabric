import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.10"
    id("fabric-loom") version "1.9.+"
}

loom {
    serverOnlyMinecraftJar()

    accessWidenerPath = file("src/main/resources/cinefabric.accesswidener")
}

val modVersion: String by project
val mavenGroup: String by project

base.archivesName.set("cinefabric")

version = "$modVersion+${libs.versions.minecraft.get()}"
group = mavenGroup

repositories {
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.yarn)

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.kotlin)
    modImplementation(libs.fabric.api)

    implementAndInclude(libs.sgui)

    implementAndInclude(libs.permissions)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks {
    processResources {
        inputs.property("version", modVersion)
        inputs.property("minecraft_version", libs.versions.minecraft.get())

        filesMatching("fabric.mod.json") {
            expand(
                "version" to modVersion,
                "minecraft_version" to libs.versions.minecraft.get()
            )
        }
    }

    jar {
        from("LICENSE")
    }
}

fun DependencyHandlerScope.implementAndInclude(dep: Any) {
    modImplementation(dep)
    include(dep)
}
