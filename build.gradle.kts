import org.springframework.boot.gradle.tasks.bundling.BootJar

// 版本全局定义
val springCloudVersion = "Hoxton.SR1"
val springBootVersion = "2.2.0.RELEASE"
val springCloudAlibabaVersion = "2.2.0.RELEASE"
val lombokVersion = "1.18.16"

ext["springCloudVersion"] = "Hoxton.SR1"
ext["springCloudAlibabaVersion"] = "2.2.0.RELEASE"
ext["lombokVersion"] = "1.18.16"
ext["jibVersion"] = "3.2.1"
ext["springBootVersion"] = "2.2.0.RELEASE"

plugins {
    id("base")
    id("idea")
    id("java")
    id("org.springframework.boot") version "2.2.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "3.2.1"
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

buildscript {
    repositories {
        maven { url = uri("https://maven.aliyun.com/nexus/content/groups/public") }
        maven { url = uri("https://repo.spring.io/plugins-release") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.2.0.RELEASE")
        classpath("gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:3.2.1")
    }
}

tasks.bootJar { enabled = false }
tasks.build { enabled = false }
tasks.assemble { enabled = false }

allprojects {
    repositories {
        mavenCentral()
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/groups/public")
            setUrl("https://repo.spring.io/release")
            setUrl("https://repo.spring.io/milestone")
        }
    }
}

subprojects {
    apply(plugin = "base")
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR1")
            mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2.2.0.RELEASE")
        }
    }
    dependencies {
        implementation("org.projectlombok:lombok:1.18.16")
        implementation("org.apache.skywalking:apm-toolkit-logback-1.x:8.6.0")
        implementation("org.apache.skywalking:apm-toolkit-trace:8.6.0")
        testImplementation("junit", "junit", "4.12")
    }
}

project("backend-gateway") {
    apply(plugin = "com.google.cloud.tools.jib")

    group = "backend.gateway"
    version = "1.0"
    description = "SpringCloud gateway project"

    tasks.named<BootJar>("bootJar") {
        archiveClassifier.set("boot")
        destinationDirectory.set(file("../build"))
        mainClassName = "backend.router.BackendRouterApp"
    }

    jib {
        from {
            image = "openjdk:alpine"
        }
        container {
            mainClass = "backend.router.BackendRouterApp"
            ports = mutableListOf("8080")
            jvmFlags = mutableListOf("-Xms512m", "-Xdebug")
            format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.OCI
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.cloud:spring-cloud-starter-gateway")
        implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
        implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config")
        implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
    }
}
