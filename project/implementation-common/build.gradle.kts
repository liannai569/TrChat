val taboolibVersion: String by project

plugins {
    id("io.izzel.taboolib") version "1.40"
}

taboolib {
    install("common", "common-5")
    install("module-kether")
    options("skip-minimize", "keep-kotlin-module")
    classifier = null
    version = taboolibVersion
}

dependencies {
    compileOnly(project(":project:common"))
}