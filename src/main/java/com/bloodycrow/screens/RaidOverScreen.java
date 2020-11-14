package com.bloodycrow.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaidOverScreen extends Screen {
    private final ItemStack reward;
    private final boolean won;

    public RaidOverScreen(ItemStack reward, boolean won) {
        super(new TranslationTextComponent("event.quantia.raid_over_title"));
        this.reward = reward;
        this.won = won;
        this.passEvents = true;
        Button button = new Button(width / 2, height / 2, 150, 20, new TranslationTextComponent("quantia.close_raid_over_message").mergeStyle(TextFormatting.WHITE), b -> closeScreen());
        button.visible = true;
        button.active = true;
        addButton(button);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        int middleX = width / 2;
        FontRenderer fontRenderer = minecraft.fontRenderer;
        itemRenderer.renderItemAndEffectIntoGUI(reward, middleX, 150);
        itemRenderer.renderItemOverlays(fontRenderer, reward, middleX, 150);
        if(middleX + 16 >= mouseX && 150 + 16 >= mouseY)
            if(mouseX >= middleX && mouseY >= 150)
                renderTooltip(matrixStack, reward, mouseX, mouseY);
        /* The parameter names here will confuse you, but this is the actual use for the parameters:
        text = x pos
        x = y pos
        y = color
        No idea why the mappings messed this up, but this is how it works */
        IFormattableTextComponent component = new TranslationTextComponent("event.quantia.raid_screen_message", won ? new TranslationTextComponent("event.quantia.won").getString() : new TranslationTextComponent("event.quantia.loss").getString()).mergeStyle(TextFormatting.WHITE);
        String a = component.getString();
        String b = title.getString();
        drawString(matrixStack, fontRenderer, getTitle(), middleX - fontRenderer.getStringWidth(b), 110, 0xffffff);
        drawString(matrixStack, fontRenderer, component, middleX - fontRenderer.getStringWidth(a), 125, 0xffffff);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public static void open(ItemStack reward, boolean won) {
        Minecraft.getInstance().displayGuiScreen(new RaidOverScreen(reward, won));
    }
}
