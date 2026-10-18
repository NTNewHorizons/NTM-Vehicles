package io.github.techtastic.ntm_vehicles.mixin;

import mcinterface1122.WrapperEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WrapperEntity.class, remap = false)
public interface WrapperEntityAccessor {
    @Accessor("entity")
    Entity ntmVehicles$getEntity();
}
