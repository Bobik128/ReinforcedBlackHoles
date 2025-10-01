package com.mod.rbh.entity;


import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<EntityType<BlackHoleProjectile>> BLACK_HOLE =
            ENTITY_TYPES.register("black_hole_projectile", () -> EntityType.Builder.<BlackHoleProjectile>of(BlackHoleProjectile::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .build("black_hole_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

