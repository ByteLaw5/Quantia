package com.bloodycrow.containers;

import com.bloodycrow.Quantia;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("deprecation")
public class EnergyProducerScreen extends ContainerScreen<EnergyProducerContainer> {
    public EnergyProducerScreen(EnergyProducerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        drawString(matrixStack, minecraft.fontRenderer, "Energy: " + container.getEnergy(), 10, 10, 0xffffff);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        minecraft.getTextureManager().bindTexture(new ResourceLocation(Quantia.MOD_ID, "textures/gui/energy_producer.png"));
        int relX = (this.width - this.xSize) / 2;
        int relY = (this.height - this.ySize) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.xSize, this.ySize);
    }
}
