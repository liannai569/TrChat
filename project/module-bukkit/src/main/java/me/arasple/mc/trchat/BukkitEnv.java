package me.arasple.mc.trchat;

import taboolib.common.env.RuntimeDependency;

/**
 * @author ItsFlicker
 * @since 2022/3/20 11:56
 */
@RuntimeDependency(
        value = "!net.kyori:adventure-api:4.12.0",
        test = "!net.kyori.adventure.Adventure",
        initiative = true
)
@RuntimeDependency(
        value = "!net.kyori:adventure-platform-bukkit:4.2.0",
        test = "!net.kyori.adventure.platform.bukkit.BukkitAudiences",
        repository = "https://repo.maven.apache.org/maven2",
        initiative = true
)
@RuntimeDependency(
        value = "!net.kyori:adventure-text-minimessage:4.12.0",
        test = "net.kyori.adventure.text.minimessage.MiniMessage"
)
public class BukkitEnv {  }