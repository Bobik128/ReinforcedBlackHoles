package com.mod.rbh.entity;


import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.entity.ItemEntity.SingularityRifleItemEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<EntityType<BlackHoleProjectile>> BLACK_HOLE_PROJECTILE =
            ENTITY_TYPES.register("black_hole_projectile", () -> EntityType.Builder.<BlackHoleProjectile>of(BlackHoleProjectile::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .build("black_hole_projectile"));

    public static final RegistryObject<EntityType<TestBlackHole>> TEST_BLACK_HOLE =
            ENTITY_TYPES.register("test_black_hole", () -> EntityType.Builder.<TestBlackHole>of(TestBlackHole::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .build("test_black_hole"));

    public static final RegistryObject<EntityType<SingularityRifleItemEntity>> RIFLE_ITEM =
            ENTITY_TYPES.register("rifle_item", () -> EntityType.Builder.<SingularityRifleItemEntity>of(SingularityRifleItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("rifle_item"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

