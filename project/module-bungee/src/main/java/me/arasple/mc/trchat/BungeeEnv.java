package me.arasple.mc.trchat;

import taboolib.common.env.RuntimeDependency;

/**
 * @author ItsFlicker
 * @since 2022/3/20 12:54
 */
@RuntimeDependency(
        value = "!net.kyori:adventure-api:4.12.0",
        test = "!net.kyori.adventure.Adventure",
        initiative = true
)
@RuntimeDependency(
        value = "!net.kyori:adventure-platform-bungeecord:4.2.0",
        test = "!net.kyori.adventure.platform.bungeecord.BungeeAudiences",
        repository = "https://repo.maven.apache.org/maven2",
        initiative = true
)
public class BungeeEnv {  }