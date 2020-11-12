package com.bloodycrow.entity.client;

import com.bloodycrow.Quantia;
import com.bloodycrow.entity.TestInvaderEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TestInvaderEntityRenderer extends MobRenderer<TestInvaderEntity, BipedModel<TestInvaderEntity>> {
    public TestInvaderEntityRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new BipedModel<>(1F), 0.45F);
    }

    @Override
    public ResourceLocation getEntityTexture(TestInvaderEntity entity) {
        return new ResourceLocation(Quantia.MOD_ID, "textures/entity/test_invader.png");
    }
}
