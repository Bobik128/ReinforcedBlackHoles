package com.mod.rbh.entity;

import com.mod.rbh.ReinforcedBlackHoles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RBHEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ReinforcedBlackHoles.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleProjectile>> BLACK_HOLE_PROJECTILE =
            ENTITY_TYPES.register("black_hole_projectile", () ->
                    EntityType.Builder.<BlackHoleProjectile>of(BlackHoleProjectile::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)
                            .build("black_hole_projectile")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TestBlackHole>> TEST_BLACK_HOLE =
            ENTITY_TYPES.register("test_black_hole", () ->
                    EntityType.Builder.<TestBlackHole>of(TestBlackHole::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)
                            .build("test_black_hole")
            );

    private static ResourceKey<EntityType<?>> entityKey(String name) {
        return ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, name)
        );
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}