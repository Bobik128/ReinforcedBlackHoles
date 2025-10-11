package com.mod.rbh.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TestBlackHole extends BlackHole{
    public TestBlackHole(Vec3 pos, Level level, float size, float effectSize) {
        super(pos, level, size, effectSize, RBHEntityTypes.TEST_BLACK_HOLE.get());
    }

    public TestBlackHole(Vec3 pos, Level level, float size, float effectSize, boolean rainbow) {
        super(pos, level, size, effectSize, rainbow, RBHEntityTypes.TEST_BLACK_HOLE.get());
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return super.shouldRender(pX, pY, pZ);
    }

    public TestBlackHole(EntityType<? extends BlackHole> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}
