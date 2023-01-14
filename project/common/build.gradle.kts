val taboolibVersion: String by project

dependencies {
    compileOnly("io.izzel.taboolib:module-lang:$taboolibVersion")
    compileOnly("io.izzel.taboolib:module-metrics:$taboolibVersion")
    compileOnly("com.eatthepath:fast-uuid:0.2.0")
    compileOnly("net.kyori:adventure-platform-api:4.2.0")
}