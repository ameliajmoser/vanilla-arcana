package com.chemelia.vanillaarcana.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.world.entity.monster.Ghast;

public class BabyGhastRenderer extends GhastRenderer {

    public BabyGhastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Ghast pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        // float f = 1.0F;
        // float f1 = 1.5F;
        // float f2 = 1.5F;
        pMatrixStack.scale(1.5F, 1.5F, 1.5F);
     }
    
}
