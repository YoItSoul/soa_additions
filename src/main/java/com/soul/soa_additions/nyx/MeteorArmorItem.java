package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class MeteorArmorItem extends ArmorItem {

    public MeteorArmorItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public String getArmorTexture(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.Entity entity,
                                   EquipmentSlot slot, String type) {
        int layer = (slot == EquipmentSlot.LEGS) ? 2 : 1;
        return SoaAdditions.MODID + ":textures/models/armor/meteor_layer_" + layer + ".png";
    }
}
