package io.github.techtastic.ntm_vehicles;

import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.items.gear.ArmorFSB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class PowerArmor {
    private PowerArmor() {}

    public static boolean isWearing(Entity entity) {
        if (!(entity instanceof EntityPlayer))
            return false;

        EntityPlayer player = (EntityPlayer) entity;
        Item chestplate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();
        if (!isPowerArmor(chestplate))
            return false;

        ArmorFSB suit = (ArmorFSB) chestplate;
        // Check locally because CE and ExtendedEdition use different helper signatures.
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR
                    || (slot == EntityEquipmentSlot.HEAD && suit.noHelmet))
                continue;

            ItemStack stack = player.getItemStackFromSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorFSB)
                    || ((ArmorFSB) stack.getItem()).getArmorMaterial() != suit.getArmorMaterial())
                return false;
        }
        return true;
    }

    private static boolean isPowerArmor(Item item) {
        if (item instanceof ArmorFSBPowered)
            return true;

        // CE adds fuel-powered suits. Avoid linking a class absent from older NTM releases.
        if (item instanceof ArmorFSB) {
            for (Class<?> type = item.getClass(); type != ArmorFSB.class; type = type.getSuperclass()) {
                if (type.getName().equals("com.hbm.items.armor.ArmorFSBFueled"))
                    return true;
            }
        }
        return false;
    }
}
