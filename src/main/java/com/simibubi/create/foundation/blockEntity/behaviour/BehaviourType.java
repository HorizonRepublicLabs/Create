package com.simibubi.create.foundation.blockEntity.behaviour;

public record BehaviourType<T extends BlockEntityBehaviour>(String name) {

    public BehaviourType() {
        this("");
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 * 493286711; // Better hash table distribution
    }
}
