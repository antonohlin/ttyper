plugins {
    application
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
}

group = "se.antonohlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.googlecode.lanterna:lanterna:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.9.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}

val generatedSrcDir = project.layout.buildDirectory.dir("generated/src/main/kotlin")

sourceSets {
    main {
        compileClasspath += project.files(generatedSrcDir)
        println(compileClasspath.files)
    }
}

tasks.register<BuiltInDictionaryGenerator>("generateDict") {
    sourceText = File(projectDir, "dictionary")
    outputPath = generatedSrcDir

    generateDictionaryClass() // take dictionary textfile and make a kotlin list of it

    sourceSets.named("main").configure {
        extensions
            .getByName<SourceDirectorySet>("kotlin")
            .srcDirs(generatedSrcDir)
    }
}

tasks.build {
    dependsOn("generateDict")
}

tasks.register<Jar>("uberJar") {
    dependsOn("build")
    archiveFileName = "ttyper.jar"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath
            .get()
            .filter {
                it.name.endsWith("jar")
            }.map { zipTree(it) }
    })
}
