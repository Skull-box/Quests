import xyz.wagyourtail.jvmdg.gradle.task.DowngradeJar
import xyz.wagyourtail.jvmdg.gradle.task.ShadeJar

plugins {
    java
    `maven-publish`
    id("xyz.wagyourtail.jvmdowngrader")
}

allprojects {
    apply(plugin = "java")

    group = "com.leonardobishop"
    // La CI injecte la version de release avec -PreleaseVersion=x.y.z (build.sh) et neutralise le suffixe du hash de
    // commit (allJar) avec -Pgitversion=false : le jar et plugin.yml portent alors exactement cette version.
    // Sans propriété : 3.15.2 (suffixé du hash, comme en amont).
    version = findProperty("releaseVersion") ?: "3.15.2"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.compilerArgs = listOf("-Xlint:deprecation", "-Xlint:unchecked")
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }

    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
}

defaultTasks = mutableListOf("clean", "allJar")

tasks.register<Jar>("allJar") {
    subprojects {
        dependsOn.add(tasks.build)
    }

    if (project.findProperty("gitversion") == null || project.findProperty("gitversion") == "true") {
        val gitCommitHash = gitCommitHash()

        allprojects {
            version = "${version}-${gitCommitHash}"
        }
    }

    subprojects {
        configurations.archives {
            allArtifacts.files.forEach {
                from(zipTree(it))
            }
        }
    }

    archiveBaseName = "Quests"
}

fun gitCommitHash(): String {
    val execOutput = providers.exec {
        commandLine = "git rev-parse --verify --short HEAD".split(" ")
    }

    val gitCommitHashProvider = execOutput.standardOutput.asText
    val gitCommitHashString = gitCommitHashProvider.getOrElse("unknown")

    return gitCommitHashString.trim()
}

val javaVersions = listOf(
    // from 1.12 to 1.16.5
    JavaVersion.VERSION_1_8,

    // just because it's a LTS version and a lot of servers use it
    // https://en.wikipedia.org/wiki/Java_version_history
    // https://bstats.org/global/bukkit#javaVersion
    //
    // also Paper recommends it
    // https://docs.papermc.io/paper/getting-started#requirements
    JavaVersion.VERSION_11,

    // from 1.17 to 1.17.1
    JavaVersion.VERSION_16,

    // from 1.18 to 1.20.4
    JavaVersion.VERSION_17
)

for (javaVersion in javaVersions) {
    val allJarTask = tasks.getByName<Jar>("allJar")

    // we use this hacky solution to improve display and sort order in IntelliJ Gradle tab
    val majorVersion = javaVersion.ordinal + 1
    val majorVersionFormatted = String.format("%02d", majorVersion)
    val downgradeTaskName = "downgrade${majorVersionFormatted}AllJar"

    tasks.register<DowngradeJar>(downgradeTaskName) {
        inputFile = allJarTask.archiveFile
        downgradeTo = javaVersion
        quiet = true

        archiveBaseName = "Quests"
        archiveClassifier = "downgraded-${majorVersion}"
    }

    val downgradeJarTask = tasks.getByName<DowngradeJar>(downgradeTaskName)
    val shadeTaskName = "shade${majorVersionFormatted}Downgrade"

    tasks.register<ShadeJar>(shadeTaskName) {
        inputFile = downgradeJarTask.archiveFile
        downgradeTo = javaVersion
        quiet = true

        archiveBaseName = "Quests"
        archiveClassifier = "downgraded-${majorVersion}-shaded"

        shadePath = { _ -> "com/leonardobishop/quests/jvmdg" }
    }

    defaultTasks.add(shadeTaskName)
}

artifacts {
    val allJarTask = tasks.named("allJar")
    archives(allJarTask)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.leonardobishop"
            artifactId = "quests"
            version = project.version.toString()

            val allJarTask = tasks.named("allJar")
            artifact(allJarTask)

            pom {
                dependencies {
                    clear()
                }
            }
        }
    }

    repositories {
        // SNAPSHOT mobile com.leonardobishop:quests:3.15.2-SNAPSHOT sur les GitHub Packages de CE dépôt
        // (publish-snapshot.sh, à chaque push sur master). Un seul artefact : le jar du plugin (allJar), sans
        // dépendances dans le pom. Identifiants par l'environnement (MAVEN_USERNAME / MAVEN_TOKEN).
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Skull-box/Quests")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_TOKEN")
            }
        }
    }
}
