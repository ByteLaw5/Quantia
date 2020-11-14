package com.bloodycrow.entity.client;

import com.bloodycrow.Quantia;
import com.bloodycrow.entity.TestInvaderEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.bloodycrow.entity.AbstractInvaderRaiderEntity.RESIZE_FACTOR;

@OnlyIn(Dist.CLIENT)
public class TestInvaderEntityRenderer extends BipedRenderer<TestInvaderEntity, BipedResizableModel<TestInvaderEntity>> {
    public TestInvaderEntityRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new BipedResizableModel<>(1.0F), 0.45F);
    }

    @Override
    protected void preRenderCallback(TestInvaderEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
        if(entitylivingbaseIn.isLeader())
            getEntityModel().resize(RESIZE_FACTOR);
    }

    @Override
    public ResourceLocation getEntityTexture(TestInvaderEntity entity) {
        return new ResourceLocation(Quantia.MOD_ID, "textures/entity/test_invader.png");
    }
}
