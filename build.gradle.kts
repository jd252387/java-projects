plugins {
    application
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "com.example.projects.App"
}

dependencies {
    implementation("org.apache.solr:solr-core:10.0.0")
    testImplementation(platform("org.junit:junit-bom:5.13.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
