import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    `kotlin-dsl`
    id("org.jreleaser") version "1.12.0"
}

val kspVersion = "1.9.21-1.0.15"

group = "io.github.perforators"
version = "1.1-rc4"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications.withType<MavenPublication> {
        groupId = group.toString()
        artifactId = project.name

        artifact(javadocJar)
        artifact(sourcesJar)

        pom {
            name.set("proxy-use-cases")
            url.set("https://github.com/perforators/ProxyUseCase")
            description.set("KSP plugin for generation of proxy use cases.")
            inceptionYear.set("2024")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("EgorKrivochkov")
                    name.set("Egor Krivochkov")
                    email.set("krivochkov01@mail.ru")
                }
            }
            scm {
                url.set("https://github.com/perforators/ProxyUseCase")
            }
        }
    }

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("/staging-deploy"))
        }
    }
}

ext["signing.password"] = null
ext["signing.secretKey"] = null
ext["signing.openKey"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKey"] = System.getenv("SIGNING_SECRET_KEY")
    ext["signing.openKey"] = System.getenv("SIGNING_OPEN_KEY")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
        setMode("FILE")
        publicKey.set(getExtraString("signing.openKey"))
        secretKey.set(getExtraString("signing.secretKey"))
        passphrase.set(getExtraString("signing.password"))
    }
    deploy.maven.mavenCentral.register("sonatype") {
        username.set(getExtraString("ossrhUsername"))
        password.set(getExtraString("ossrhPassword"))
        setActive("ALWAYS")
        url.set("https://central.sonatype.com/api/v1/publisher")
        stagingRepository("build/staging-deploy")
    }
}

afterEvaluate {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            apiVersion = "1.9"
            languageVersion = "1.9"
        }
    }
}
