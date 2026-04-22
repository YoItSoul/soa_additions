package com.soul.soa_additions.nyx;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.nyx.ench.LunarEdgeEnchantment;
import com.soul.soa_additions.nyx.ench.LunarShieldEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NyxEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, SoaAdditions.MODID);

    public static final RegistryObject<Enchantment> LUNAR_EDGE =
            ENCHANTMENTS.register("lunar_edge", LunarEdgeEnchantment::new);
    public static final RegistryObject<Enchantment> LUNAR_SHIELD =
            ENCHANTMENTS.register("lunar_shield", LunarShieldEnchantment::new);

    public static void register(IEventBus bus) { ENCHANTMENTS.register(bus); }
    private NyxEnchantments() {}
}
