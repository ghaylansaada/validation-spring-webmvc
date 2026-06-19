import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("plugin.spring") version "2.4.0"
	kotlin("jvm") version "2.4.0"
	`maven-publish`
	`java-library`
}

group = "io.github.ghaylansaada"
version = "0.0.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
	withSourcesJar()
	withJavadocJar()
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Spring dependency version alignment (BOM)
	implementation(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))

    // Spring MVC support
	implementation("org.springframework.boot:spring-boot-webmvc")
	
	// Spring autoconfiguration support
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // Jackson databinding
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // OpenAPI / Swagger annotations
	implementation("io.swagger.core.v3:swagger-annotations:2.2.50")

   // Kotlin reflection support
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // HTML parsing
	implementation("org.jsoup:jsoup:1.22.2")
	
	// Phone number parsing / validation
	implementation("com.googlecode.libphonenumber:libphonenumber:9.0.33")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		// Emit method parameter names for reflection frameworks
		javaParameters = true
		
		// Generate Java 21 bytecode
		jvmTarget.set(JvmTarget.JVM_25)
		
		// Enable latest stable Kotlin behavior immediately
        progressiveMode.set(true)
		
		// Strict handling of Java nullability annotations
        freeCompilerArgs.add("-Xjsr305=strict")
		
		// Emit type-use annotations for frameworks and tooling
        freeCompilerArgs.add("-Xemit-jvm-type-annotations")
		
		// Allow usage of APIs marked with @RequiresOptIn
        optIn.add("kotlin.RequiresOptIn")

        // Allow usage of experimental Kotlin standard library APIs
        optIn.add("kotlin.ExperimentalStdlibApi")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            setUrl("https://maven.pkg.github.com/ghaylansaada/validation-spring-webmvc")
            credentials {
                username = project.findProperty("gpr.user") as String?
                password = project.findProperty("gpr.token") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "io.github.ghaylansaada"
            artifactId = "validation-spring-webmvc"
            version = "0.0.1"
        }
    }
}