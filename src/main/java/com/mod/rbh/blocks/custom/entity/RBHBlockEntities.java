package com.mod.rbh.blocks.custom.entity;

import com.mod.rbh.ReinforcedBlackHoles;
import com.mod.rbh.blocks.RBHBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RBHBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ReinforcedBlackHoles.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HoleShowcaseBlockEntity>> HOLE_SHOWCASE_BE =
            BLOCK_ENTITIES.register("hole_showcase_be", () ->
                    BlockEntityType.Builder.of(
                            HoleShowcaseBlockEntity::new,
                            RBHBlocks.HOLE_SHOWCASE.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}