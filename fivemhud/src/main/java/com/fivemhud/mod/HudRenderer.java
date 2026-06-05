package com.fivemhud.mod;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HudRenderer extends AbstractGui {

    // Dimensiuni
    private static final int BAR_WIDTH  = 160;
    private static final int BAR_HEIGHT = 10;
    private static final int BAR_MARGIN = 6;
    private static final int ICON_SIZE  = 14;
    private static final int PADDING    = 10;

    // Health - rosu
    private static final int COLOR_HEALTH_BG    = 0xAA1a0a0a;
    private static final int COLOR_HEALTH_FILL  = 0xFFe8263a;
    private static final int COLOR_HEALTH_SHINE = 0x33ffffff;

    // Food - portocaliu
    private static final int COLOR_FOOD_BG      = 0xAA1a120a;
    private static final int COLOR_FOOD_FILL    = 0xFFf5a623;
    private static final int COLOR_FOOD_SHINE   = 0x33ffffff;

    // Armor - albastru
    private static final int COLOR_ARMOR_BG     = 0xAA0a0e1a;
    private static final int COLOR_ARMOR_FILL   = 0xFF4a9eff;
    private static final int COLOR_ARMOR_SHINE  = 0x33ffffff;

    // Panel
    private static final int COLOR_PANEL_BG     = 0xBB0d0d0d;
    private static final int COLOR_BORDER       = 0xFF222222;

    // Smooth animation state
    private float smoothHealth = 20f;
    private float smoothFood   = 20f;
    private float smoothArmor  = 0f;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if (mc.gameMode != null && !mc.gameMode.isSurvivalOrAdventure()) return;

        PlayerEntity player = mc.player;

        float maxHealth    = player.getMaxHealth();
        float curHealth    = player.getHealth();
        int   foodLevel    = player.getFoodData().getFoodLevel();
        int   armorValue   = player.getArmorValue(); // 0-20

        // Smooth lerp
        smoothHealth = MathHelper.lerp(0.15f, smoothHealth, curHealth);
        smoothFood   = MathHelper.lerp(0.15f, smoothFood,   (float) foodLevel);
        smoothArmor  = MathHelper.lerp(0.15f, smoothArmor,  (float) armorValue);

        float healthPct = MathHelper.clamp(smoothHealth / maxHealth, 0f, 1f);
        float foodPct   = MathHelper.clamp(smoothFood   / 20f,       0f, 1f);
        float armorPct  = MathHelper.clamp(smoothArmor  / 20f,       0f, 1f);

        MatrixStack ms = event.getMatrixStack();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // 3 bare: health + food + armor
        int rows    = 3;
        int panelW  = PADDING + ICON_SIZE + 6 + BAR_WIDTH + 45 + PADDING;
        int panelH  = PADDING + rows * ICON_SIZE + (rows - 1) * BAR_MARGIN + PADDING;

        // Pozitie: stanga-jos
        int startX = 10;
        int startY = screenH - panelH - 10;

        drawPanel(ms, startX, startY, panelW, panelH);

        int contentX = startX + PADDING;
        int row1Y    = startY + PADDING;
        int row2Y    = row1Y + ICON_SIZE + BAR_MARGIN;
        int row3Y    = row2Y + ICON_SIZE + BAR_MARGIN;

        // Health
        drawBar(ms, mc, contentX, row1Y,
                healthPct, curHealth, maxHealth,
                COLOR_HEALTH_BG, COLOR_HEALTH_FILL, COLOR_HEALTH_SHINE,
                0xFFff6b7a, true, false);

        // Food
        drawBar(ms, mc, contentX, row2Y,
                foodPct, foodLevel, 20,
                COLOR_FOOD_BG, COLOR_FOOD_FILL, COLOR_FOOD_SHINE,
                0xFFf5c842, false, false);

        // Armor
        drawBar(ms, mc, contentX, row3Y,
                armorPct, armorValue, 20,
                COLOR_ARMOR_BG, COLOR_ARMOR_FILL, COLOR_ARMOR_SHINE,
                0xFF7ec8ff, false, true);

        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
    }

    private void drawPanel(MatrixStack ms, int x, int y, int w, int h) {
        // Umbra
        fill(ms, x + 3, y + 3, x + w + 3, y + h + 3, 0x55000000);
        // Border
        fill(ms, x - 1, y - 1, x + w + 1, y + h + 1, COLOR_BORDER);
        // Background
        fill(ms, x, y, x + w, y + h, COLOR_PANEL_BG);
        // Linie accent stanga
        fill(ms, x, y + 2, x + 2, y + h - 2, 0xFF444444);
    }

    private void drawBar(MatrixStack ms, Minecraft mc,
                         int x, int y,
                         float percent, float current, float max,
                         int bgColor, int fillColor, int shineColor,
                         int textColor,
                         boolean isHealth, boolean isArmor) {

        int barX = x + ICON_SIZE + 6;
        int barY = y + (ICON_SIZE - BAR_HEIGHT) / 2;

        drawSquareIcon(ms, x, y, fillColor);

        // BG bara
        fill(ms, barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, bgColor);

        // Fill
        int fillW = (int)(BAR_WIDTH * percent);
        if (fillW > 0) {
            fill(ms, barX, barY, barX + fillW, barY + BAR_HEIGHT, fillColor);
            fill(ms, barX, barY, barX + fillW, barY + 2,          shineColor);
        }

        // Border linii
        fill(ms, barX, barY,                  barX + BAR_WIDTH, barY + 1,              0x55ffffff);
        fill(ms, barX, barY + BAR_HEIGHT - 1, barX + BAR_WIDTH, barY + BAR_HEIGHT,     0x33000000);

        // Label
        String label;
        if (isHealth) {
            label = String.format("%.0f / %.0f", current, max);
        } else if (isArmor) {
            label = String.format("%d / 20", (int) current);
        } else {
            label = String.format("%d / 20", (int) current);
        }

        mc.font.drawShadow(ms, label, barX + BAR_WIDTH + 5, barY + 1, textColor);
    }

    private void drawSquareIcon(MatrixStack ms, int x, int y, int color) {
        int s = ICON_SIZE;
        fill(ms, x + 1, y,     x + s - 1, y + s,     color);
        fill(ms, x,     y + 1, x + s,     y + s - 1, color);
        fill(ms, x + 1, y + 1, x + s - 1, y + 3,     0x44ffffff);
    }
}
