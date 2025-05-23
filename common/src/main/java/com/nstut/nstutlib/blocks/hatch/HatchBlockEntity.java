package com.nstut.nstutlib.blocks.hatch;

import com.nstut.nstutlib.blocks.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.nstut.nstutlib.menu.HatchMenu; 
import dev.architectury.registry.menu.ExtendedMenuProvider; 
import net.minecraft.network.FriendlyByteBuf; 

public abstract class HatchBlockEntity extends BlockEntity implements ExtendedMenuProvider { 

    @Nullable
    protected MachineBlockEntity controller;
    // TODO: Consider how the controller is linked. Maybe via NBT or searching on load?

    public HatchBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public void setController(@Nullable MachineBlockEntity controller) {
        this.controller = controller;
        // TODO: Mark dirty and save controller\'s position if needed for persistence
        setChanged();
    }

    @Nullable
    public MachineBlockEntity getController() {
        // TODO: Add logic to find controller if null and part of a multiblock
        return controller;
    }

    // Added for MenuProvider
    @Override
    public Component getDisplayName() {
        // You can customize this to display a more specific name if needed
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer);

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(worldPosition);
    }

    // Added for HatchMenu stillValid
    public boolean stillValid(Player pPlayer) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return pPlayer.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }
}
