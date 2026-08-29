package com.simibubi.create.foundation.blockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.simibubi.create.foundation.utility.ValueIOShim;

@ParametersAreNonnullByDefault
public abstract class SyncedBlockEntity extends BlockEntity {
	public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/// The registries this block entity serializes against. Loading happens
	/// before the level is attached in some paths, so fall back to the tag
	/// context Create's shim builds.
	public HolderLookup.Provider registries() {
		return level != null ? level.registryAccess() : RegistryAccess.EMPTY;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return writeClient(new CompoundTag(), registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(ValueInput input) {
		readClient(ValueIOShim.tagOf(input), registries());
	}

	@Override
	public void onDataPacket(Connection net, ValueInput input) {
		readClient(ValueIOShim.tagOf(input), registries());
	}

	// 26.x moved block entity serialization to ValueInput/ValueOutput. Create's
	// block entities and behaviours are CompoundTag-shaped all the way down, so
	// convert here and keep the tag-based overrides working.
	@Override
	protected final void loadAdditional(ValueInput input) {
		loadAdditional(ValueIOShim.tagOf(input), registries());
	}

	@Override
	protected final void saveAdditional(ValueOutput output) {
		saveAdditional(ValueIOShim.tagOf(output), registries());
	}

	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(ValueIOShim.inputOf(tag, registries));
	}

	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		TagValueOutput output = ValueIOShim.output(registries);
		super.saveAdditional(output);
		tag.merge(output.buildResult());
	}

	// Special handling for client update packets
	public void readClient(CompoundTag tag, HolderLookup.Provider registries) {
		loadAdditional(tag, registries);
	}

	// Special handling for client update packets
	public CompoundTag writeClient(CompoundTag tag, HolderLookup.Provider registries) {
		saveAdditional(tag, registries);
		return tag;
	}

	public void sendData() {
		if (level instanceof ServerLevel serverLevel)
			serverLevel.getChunkSource().blockChanged(getBlockPos());
	}

	public void notifyUpdate() {
		setChanged();
		sendData();
	}

	public HolderGetter<Block> blockHolderGetter() {
		return level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
	}
}
