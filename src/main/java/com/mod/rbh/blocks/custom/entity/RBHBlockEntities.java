package com.mod.rbh.blocks.custom.entity;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.RBHBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RBHBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ReinforcedBlackHoles.MODID);

    public static final RegistryObject<BlockEntityType<HoleShowcaseBlockEntity>> HOLE_SHOWCASE_BE =
            BLOCK_ENTITIES.register("hole_showcase_be", () ->
                    BlockEntityType.Builder.of(HoleShowcaseBlockEntity::new, RBHBlocks.HOLE_SHOWCASE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
