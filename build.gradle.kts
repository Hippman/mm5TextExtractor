plugins {
    id("java")
    id("io.freefair.lombok") version "8.13.1"
}

group = "vsurjenko"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.projectlombok:lombok:1.18.38")
    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.google.code.gson:gson:2.13.1")

}

tasks.jar {
    manifest.attributes["Main-Class"] = "Application"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


