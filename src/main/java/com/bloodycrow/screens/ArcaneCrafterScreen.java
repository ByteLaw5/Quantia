package com.bloodycrow.screens;

import com.bloodycrow.containers.ArcaneCrafterContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.bloodycrow.Quantia;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("deprecation")
public class ArcaneCrafterScreen extends ContainerScreen<ArcaneCrafterContainer> {
    public ArcaneCrafterScreen(ArcaneCrafterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        minecraft.getTextureManager().bindTexture(new ResourceLocation(Quantia.MOD_ID, "textures/gui/arcane_crafter.png"));
        int relX = (width - xSize) / 2;
        int relY = (height - ySize) / 2;
        blit(matrixStack, relX, relY, 0, 0, xSize, ySize + 54);
    }
}
