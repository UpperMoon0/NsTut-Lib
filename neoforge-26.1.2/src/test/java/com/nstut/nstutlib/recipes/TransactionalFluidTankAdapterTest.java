package com.nstut.nstutlib.recipes;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionalFluidTankAdapterTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Fluids.WATER.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
    }

    @Test
    void adaptsNonFluidTankHandlerToRestorableTransactionContract() {
        FluidTank backing = new FluidTank(4000);
        IFluidHandler delegate = new DelegatingHandler(backing);
        TransactionalFluidTankAdapter adapter = new TransactionalFluidTankAdapter(
                delegate,
                4000,
                backing::setFluid);

        assertTrue(adapter instanceof FluidTank);
        ModRecipe.requireRestorableStorage(null, List.of(adapter), "input");

        assertEquals(1000, adapter.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE));
        FluidStack snapshot = adapter.getFluid().copy();
        assertEquals(500, adapter.drain(500, IFluidHandler.FluidAction.EXECUTE).getAmount());
        assertEquals(500, backing.getFluidAmount());

        adapter.setFluid(snapshot);
        assertEquals(1000, backing.getFluidAmount());
    }

    private static final class DelegatingHandler implements IFluidHandler {
        private final IFluidHandler delegate;

        private DelegatingHandler(IFluidHandler delegate) {
            this.delegate = delegate;
        }

        @Override public int getTanks() { return delegate.getTanks(); }
        @Override public FluidStack getFluidInTank(int tank) { return delegate.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return delegate.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return delegate.isFluidValid(tank, stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return delegate.fill(resource, action); }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return delegate.drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return delegate.drain(maxDrain, action); }
    }
}
