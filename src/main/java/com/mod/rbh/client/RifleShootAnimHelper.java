package com.mod.rbh.client;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RifleShootAnimHelper {
    public static Map<Long, Integer> shootingRifles = new HashMap<>();

    public static void addShootingRifle(long id, int chargeLevel) {
        shootingRifles.put(id, chargeLevel);
    }

    public static boolean rifleShooting(ItemStack stack) {
        return shootingRifles.containsKey(GeoItem.getId(stack));
    }

    public static int getChargeLevel(ItemStack stack) {
        return shootingRifles.get(GeoItem.getId(stack));
    }

    public static void remove(ItemStack stack) {
        shootingRifles.remove(GeoItem.getId(stack));
    }

    public static void tick() {
//        shootingRifles.clear();
    }
}
