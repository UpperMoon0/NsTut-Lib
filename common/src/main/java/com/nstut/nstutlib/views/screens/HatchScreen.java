package com.nstut.nstutlib.views.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.menu.HatchMenu;
import com.nstut.nstutlib.blocks.hatch.ItemHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.FluidHatchBlockEntity;
import com.nstut.nstutlib.blocks.hatch.EnergyHatchBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HatchScreen extends AbstractContainerScreen<HatchMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NsTutLib.MOD_ID, "textures/gui/blank.png");
    private static final ResourceLocation ITEM_HATCH_TEXTURE = new ResourceLocation(NsTutLib.MOD_ID, "textures/gui/item_hatch.png");
    private static final ResourceLocation FLUID_HATCH_TEXTURE = new ResourceLocation(NsTutLib.MOD_ID, "textures/gui/fluid_hatch.png");
    private static final ResourceLocation ENERGY_HATCH_TEXTURE = new ResourceLocation(NsTutLib.MOD_ID, "textures/gui/energy_hatch.png");

    private final ResourceLocation currentTexture;

    public HatchScreen(HatchMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;

        if (pMenu.getHatchBlockEntity() instanceof ItemHatchBlockEntity) {
            this.currentTexture = ITEM_HATCH_TEXTURE;
        } else if (pMenu.getHatchBlockEntity() instanceof FluidHatchBlockEntity) {
            this.currentTexture = FLUID_HATCH_TEXTURE;
        } else if (pMenu.getHatchBlockEntity() instanceof EnergyHatchBlockEntity) {
            this.currentTexture = ENERGY_HATCH_TEXTURE;
        } else {
            this.currentTexture = TEXTURE; 
        }
    }

    @Override
    protected void init() {
        super.init();
        // Initialize widgets here if needed
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderTexture(0, this.currentTexture);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(this.currentTexture, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
