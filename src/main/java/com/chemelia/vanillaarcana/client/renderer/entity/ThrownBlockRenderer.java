package com.chemelia.vanillaarcana.client.renderer.entity;

import com.chemelia.vanillaarcana.entity.projectile.ThrownBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

public class ThrownBlockRenderer<T extends ThrownBlock & ItemSupplier> extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;

    public ThrownBlockRenderer(EntityRendererProvider.Context provider){
        super(provider);
        this.shadowRadius = 0.6F;
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public Vec3 getRenderOffset(T type, float offset) {
        return super.getRenderOffset(type, offset);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return entity.level.getLightEmission(entity.blockPosition());
    }

    @Override
	public void render(T entity, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.translate(0, entity.getBbHeight()/2f, 0);
		if (entity.tickCount >= 3 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)){
            matrixStack.pushPose();
            matrixStack.translate(0,0.25,0);
            matrixStack.mulPose(Vector3f.YN.rotationDegrees(180 - Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())));
            matrixStack.mulPose(Vector3f.ZN.rotationDegrees(Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));

            this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.NONE, packedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer, 0);
            matrixStack.popPose();
            super.render(entity, yaw, partialTicks, matrixStack, buffer, packedLight);
        }

		
	}

    @Override
    public ResourceLocation getTextureLocation(ThrownBlock pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
