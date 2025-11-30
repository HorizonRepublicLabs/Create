package com.simibubi.create.content.redstone.link;

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;

import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;

public interface IRedstoneLinkable {

    int getTransmittedStrength();

    void setReceivedStrength(int power);

    boolean isListening();

    boolean isAlive();

    Couple<Frequency> getNetworkKey();

    BlockPos getLocation();
}
