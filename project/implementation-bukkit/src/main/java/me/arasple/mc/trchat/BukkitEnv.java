package me.arasple.mc.trchat;

import taboolib.common.env.RuntimeDependency;

/**
 * @author wlys
 * @since 2022/3/20 11:56
 */
@RuntimeDependency(
        value = "!net.kyori:adventure-api:4.11.0",
        test = "!net.kyori.adventure.Adventure",
//        relocate = {"!net.kyori.adventure", "!net.kyori.adventure_4_11_0"},
        initiative = true
)
@RuntimeDependency(
        value = "!net.kyori:adventure-platform-bukkit:4.1.1",
        test = "!net.kyori.adventure.platform.bukkit.BukkitAudiences",
//        relocate = {"!net.kyori.adventure", "!net.kyori.adventure_4_11_0"},
        repository = "https://repo.maven.apache.org/maven2",
        initiative = true
)
public class BukkitEnv {  }