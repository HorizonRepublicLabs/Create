package com.simibubi.create.foundation.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/// Capabilities are handed resource handlers now. NeoForge bridges one of those
/// into the old item handler shape; Create's inventories are written against
/// the old shape, so this goes the other way. The old handlers write straight
/// through, so a transaction that is rolled back is undone by restoring the
/// contents recorded before the first write.
public class ItemHandlerResourceAdapter extends SnapshotJournal<List<ItemStack>>
	implements ResourceHandler<ItemResource> {

	private final IItemHandlerModifiable handler;

	public ItemHandlerResourceAdapter(IItemHandlerModifiable handler) {
		this.handler = handler;
	}

	public static ResourceHandler<ItemResource> of(IItemHandler handler) {
		if (handler == null)
			return null;
		if (handler instanceof IItemHandlerModifiable modifiable)
			return new ItemHandlerResourceAdapter(modifiable);
		throw new IllegalArgumentException(
			"Item handler cannot be rolled back and so cannot back a resource handler: " + handler.getClass());
	}

	@Override
	protected List<ItemStack> createSnapshot() {
		List<ItemStack> snapshot = new ArrayList<>(handler.getSlots());
		for (int slot = 0; slot < handler.getSlots(); slot++)
			snapshot.add(handler.getStackInSlot(slot)
				.copy());
		return snapshot;
	}

	@Override
	protected void revertToSnapshot(List<ItemStack> snapshot) {
		for (int slot = 0; slot < snapshot.size() && slot < handler.getSlots(); slot++)
			handler.setStackInSlot(slot, snapshot.get(slot));
	}

	@Override
	public int size() {
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index) {
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index) {
		return handler.getStackInSlot(index)
			.getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource) {
		return handler.getSlotLimit(index);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return handler.isItemValid(index, resource.toStack());
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
		ItemStack remainder = handler.insertItem(index, resource.toStack(amount), true);
		int insertable = amount - remainder.getCount();
		if (insertable <= 0)
			return 0;

		updateSnapshots(transaction);
		handler.insertItem(index, resource.toStack(insertable), false);
		return insertable;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
		ItemStack present = handler.getStackInSlot(index);
		if (present.isEmpty() || !ItemResource.of(present)
			.equals(resource))
			return 0;

		int extractable = handler.extractItem(index, amount, true)
			.getCount();
		if (extractable <= 0)
			return 0;

		updateSnapshots(transaction);
		handler.extractItem(index, extractable, false);
		return extractable;
	}
}
