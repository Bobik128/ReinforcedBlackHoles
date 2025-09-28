package com.mod.rbh.datagen.recipe;

import com.mod.rbh.items.RBHItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.SequencedAssemblyRecipeGen;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * Create's own Data Generation for Sequenced Assembly recipes
 * @see SequencedAssemblyRecipeGen
 */
@SuppressWarnings("unused")
public class RBHSequencedAssemblyRecipeGen extends SequencedAssemblyRecipeGen {

    GeneratedRecipe

    SINGULARITY_BATTERY = create("singularity_battery.json", b -> b.require(RBHItems.SINGULARITY_BATTERY_EMPTY.get())
            .transitionTo(RBHItems.SINGULARITY_BATTERY_INCOMPLETE.get())
            .addOutput(RBHItems.SINGULARITY_BATTERY.get(), 10)
		.loops(5)
		.addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.NETHER_STAR))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.EXPERIENCE_BOTTLE)))

            ;

    public RBHSequencedAssemblyRecipeGen(PackOutput output, String defaultNamespace) {
        super(output, defaultNamespace);
    }
}
