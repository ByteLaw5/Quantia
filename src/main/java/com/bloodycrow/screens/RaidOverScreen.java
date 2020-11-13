package com.bloodycrow.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaidOverScreen extends Screen {
    private final ItemStack reward;
    private final boolean won;

    public RaidOverScreen(ItemStack reward, boolean won) {
        super(new TranslationTextComponent("quantia.raid_over_title"));
        this.reward = reward;
        this.won = won;
        this.passEvents = true;
        addButton(new Button(125 + width / 2, 175 + height / 2, 95, 20, new TranslationTextComponent("quantia.close_raid_over_message").mergeStyle(TextFormatting.WHITE), (button) -> closeScreen()));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, minecraft.fontRenderer, new TranslationTextComponent("quantia.raid_screen_message", won), 0xffffff, 125, 125);
        itemRenderer.renderItemAndEffectIntoGUI(reward, 125, 150);
        drawCenteredString(matrixStack, minecraft.fontRenderer, Integer.toString(reward.getCount()), 0xffffff, 125, 125);
        drawString(matrixStack, minecraft.fontRenderer, getTitle(), 0xffffff, 200, 200);
    }

    public static void open(ItemStack reward, boolean won) {
        Minecraft.getInstance().displayGuiScreen(new RaidOverScreen(reward, won));
    }
}
