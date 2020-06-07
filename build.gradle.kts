plugins {
    java
    application
    id("io.freefair.lombok") version "5.1.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val javaVersion: JavaVersion by extra { JavaVersion.VERSION_1_8 }

application {
    applicationName = "Einsatzauftragsparser"
    mainClassName = "org.jackl.ffw.einsatzprotokollparser.Einsatzprotokollparser"
    version = "1.0-SNAPSHOT"
    group = "org.jackl.ffw"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Vendor"] = project.group
        attributes["Title"] = application.applicationName
        attributes["AppVersion"] = project.version
        attributes["CreatedBy"] = "Gradle ${gradle.gradleVersion}"
        attributes["OS"] = "${System.getProperty("os.name")} (${System.getProperty("os.version")})"
        attributes["BuildTarget"] = javaVersion
        attributes["JDK"] = System.getProperty("java.version")
        attributes["Multi-Release"] = true
    }
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        mergeServiceFiles()
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.13.+")
    implementation("org.apache.logging.log4j:log4j-core:2.13.+")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.+")
    implementation("org.apache.pdfbox:pdfbox:2.0.+")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.+")
    implementation("com.fasterxml.jackson:jackson-base:2.10.+")
    implementation("com.google.guava:guava:29.0-jre")
}
