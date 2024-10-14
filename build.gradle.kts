plugins {
    `maven-publish`
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.entiv"
version = "1.8"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "Paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        name = "PlaceholderAPI"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "bStats"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "Vault"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "purpur"
        url = uri("https://repo.purpurmc.org/snapshots")
    }

    maven {
        name = "spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "jitpack-repo"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "enginehub-maven"
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        name = "coreprotect-repo"
        url = uri("https://maven.playpro.com/")
    }

    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.org/repository/maven-snapshots/")
    }
    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
//    implementation("org.bstats:bstats-bukkit:3.0.0")

    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))
//    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    compileOnly("me.clip:placeholderapi:2.11.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.LoneDev6:api-itemsadder:3.0.0")
//    compileOnly("org.purpurmc.purpur:purpur-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.1-SNAPSHOT")
//    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.0.1-SNAPSHOT")
    compileOnly("net.coreprotect:coreprotect:2.14.2")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation(project(":nms"))
    implementation(project(":nms-1.8"))
    implementation(project(":nms-1.12.2"))
    implementation(project(":nms-1.18.1"))
    compileOnly(fileTree("libs"))
}

tasks {
    shadowJar {
        project.findProperty("outputPath")?.let {
            destinationDirectory.set(file(it.toString()))
        }
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    jar {
        project.findProperty("outputPath")?.let {
            destinationDirectory.set(file(it.toString()))
        }
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filesMatching("plugin.yml") {
            expand(
                "name" to rootProject.name,
                "version" to project.version,
                "author" to "SakuraTown"
            )
        }
        filteringCharset = Charsets.UTF_8.name()
    }

    test {
        useJUnitPlatform()
    }

}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sakuratown/insekicore")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            artifactId = project.name.toLowerCase()
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}