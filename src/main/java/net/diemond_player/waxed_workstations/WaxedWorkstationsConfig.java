package net.diemond_player.waxed_workstations;


import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;


public class WaxedWorkstationsConfig extends MidnightConfig {
    @Entry public static boolean enableWaxingWorkstations = true;
    @Entry public static boolean enableWaxingBeds = true;
    @Entry public static boolean enableWaxingBells = true;
    @Entry public static boolean enableWaxingLodestones = false;
    @Entry public static boolean enableWaxingLightningRods = false;
    @Entry public static boolean enableWaxingBeehives = false;
    @Entry public static boolean enableWaxingNetherPortals = false;
    @Entry public static boolean enableWaxingEtc = false;
    @Entry public static int waxingConsumeAmount = 1;
    @Entry public static int unwaxingConsumeAmount = 1;
    @Entry public static List<String> waxingItems = Lists.newArrayList(Registries.ITEM.getId(Items.HONEYCOMB).toString());
    @Entry public static List<String> unwaxingItems = Lists.newArrayList("tag minecraft:axes");
}
