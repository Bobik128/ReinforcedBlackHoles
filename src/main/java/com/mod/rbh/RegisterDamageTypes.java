package com.mod.rbh;

import com.mod.rbh.entity.BlackHoleProjectile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class RegisterDamageTypes {

    public static final ResourceKey<DamageType> HOLE_HIT = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ReinforcedBlackHoles.MODID, "hole_hit")
    );

    private RegisterDamageTypes() {
    }

    public static DamageSource causeHoleHitDamage(BlackHoleProjectile entity) {
        Holder<DamageType> holder = entity.level()
                .registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolder(HOLE_HIT)
                .orElseThrow(() -> new IllegalStateException("Missing damage type: " + HOLE_HIT.location()));

        return new HoleHitDamageSource(holder, entity, entity.getOwner());
    }

    private static class HoleHitDamageSource extends DamageSource {
        public HoleHitDamageSource(
                Holder<DamageType> damageType,
                @Nullable Entity directEntity,
                @Nullable Entity causingEntity
        ) {
            super(damageType, directEntity, causingEntity);
        }

        @Override
        public @NotNull Component getLocalizedDeathMessage(LivingEntity victim) {
            LivingEntity killCredit = victim.getKillCredit();
            String base = "death.attack." + this.getMsgId();

            // There are three translation variants (0, 1, 2).
            int index = victim.getRandom().nextInt(3);

            if (killCredit != null) {
                return Component.translatable(
                        base + ".attacker_" + index,
                        victim.getDisplayName(),
                        killCredit.getDisplayName()
                );
            }

            return Component.translatable(base + "." + index, victim.getDisplayName());
        }
    }
}
