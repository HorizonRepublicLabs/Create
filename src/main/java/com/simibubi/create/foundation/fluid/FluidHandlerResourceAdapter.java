package com.simibubi.create.foundation.fluid;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/// The fluid side of the same bridge as ItemHandlerResourceAdapter: Create's
/// tanks are fluid handlers, and capabilities want resource handlers. Fluid
/// handlers do not address a tank when filling, so a rollback restores the
/// contents recorded before the first write.
public class FluidHandlerResourceAdapter extends SnapshotJournal<List<FluidStack>>
	implements ResourceHandler<FluidResource> {

	private final IFluidHandler handler;

	public FluidHandlerResourceAdapter(IFluidHandler handler) {
		this.handler = handler;
	}

	public static ResourceHandler<FluidResource> of(IFluidHandler handler) {
		return handler == null ? null : new FluidHandlerResourceAdapter(handler);
	}

	@Override
	protected List<FluidStack> createSnapshot() {
		List<FluidStack> snapshot = new ArrayList<>(handler.getTanks());
		for (int tank = 0; tank < handler.getTanks(); tank++)
			snapshot.add(handler.getFluidInTank(tank)
				.copy());
		return snapshot;
	}

	@Override
	protected void revertToSnapshot(List<FluidStack> snapshot) {
		// Drain whatever is there and put the recorded contents back.
		for (FluidStack present : List.copyOf(snapshot))
			if (!present.isEmpty())
				handler.drain(present, FluidAction.EXECUTE);
		for (int tank = 0; tank < handler.getTanks(); tank++) {
			FluidStack contents = handler.getFluidInTank(tank);
			if (!contents.isEmpty())
				handler.drain(contents, FluidAction.EXECUTE);
		}
		for (FluidStack recorded : snapshot)
			if (!recorded.isEmpty())
				handler.fill(recorded, FluidAction.EXECUTE);
	}

	@Override
	public int size() {
		return handler.getTanks();
	}

	@Override
	public FluidResource getResource(int index) {
		return FluidResource.of(handler.getFluidInTank(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getFluidInTank(index)
			.getAmount();
	}

	@Override
	public long getCapacityAsLong(int index, FluidResource resource) {
		return handler.getTankCapacity(index);
	}

	@Override
	public boolean isValid(int index, FluidResource resource) {
		return handler.isFluidValid(index, resource.toStack(1));
	}

	@Override
	public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
		FluidStack offered = resource.toStack(amount);
		int fillable = handler.fill(offered, FluidAction.SIMULATE);
		if (fillable <= 0)
			return 0;

		updateSnapshots(transaction);
		return handler.fill(resource.toStack(fillable), FluidAction.EXECUTE);
	}

	@Override
	public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
		FluidStack wanted = resource.toStack(amount);
		FluidStack drainable = handler.drain(wanted, FluidAction.SIMULATE);
		if (drainable.isEmpty())
			return 0;

		updateSnapshots(transaction);
		return handler.drain(drainable, FluidAction.EXECUTE)
			.getAmount();
	}
}
