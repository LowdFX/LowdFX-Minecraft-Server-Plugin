plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0" // ShadowJar ist das gleiche wie shade in Maven.
    id("xyz.jpenilla.run-paper") version "2.2.3" // Um code zu ändern ohne den server neu zu starten.
}

group = "at.lowdfx"
version = "1.0"

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("xyz.xenondevs.invui:invui:1.43")
}

tasks {
    build {
        dependsOn(shadowJar) // ShadowJar ist das gleiche wie shade in Maven.
    }
    runServer {
        dependsOn(shadowJar)
        minecraftVersion("1.21.3")
    }
    shadowJar {
        archiveClassifier.set("") // Kein -all im .jar Namen.
        minimize()
    }
}
