package com.soul.soa_additions.nyx;

import com.soul.soa_additions.nyx.item.LunarWaterBottleItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NyxItems {

    public static final String NYX_ID = "nyx";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NYX_ID);

    public static final RegistryObject<Item> METEOR_INGOT = ITEMS.register("meteor_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> METEOR_DUST = ITEMS.register("meteor_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> METEOR_SHARD = ITEMS.register("meteor_shard",
            () -> new Item(new Item.Properties()));
    // 1.12 FallenStar.onEntityItemUpdate: the dropped star leaves a moving light-15
    // star_air block at its position so the landing spot is findable at night.
    public static final RegistryObject<Item> FALLEN_STAR = ITEMS.register("fallen_star",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)) {
                @Override
                public boolean onEntityItemUpdate(net.minecraft.world.item.ItemStack stack,
                        net.minecraft.world.entity.item.ItemEntity entity) {
                    var level = entity.level();
                    if (level.isClientSide()) return false;
                    var cur = entity.blockPosition();
                    var data = entity.getPersistentData();
                    long prev = data.getLong("nyx:star_air_pos");
                    var prevPos = prev == 0L ? null : net.minecraft.core.BlockPos.of(prev);
                    if (prevPos != null && prevPos.equals(cur)) return false;
                    if (prevPos != null
                            && level.getBlockState(prevPos).is(NyxBlocks.STAR_AIR.get())) {
                        level.setBlockAndUpdate(prevPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                    }
                    if (level.getBlockState(cur).isAir()
                            && !level.getBlockState(cur).is(NyxBlocks.STAR_AIR.get())) {
                        level.setBlockAndUpdate(cur, NyxBlocks.STAR_AIR.get().defaultBlockState());
                        data.putLong("nyx:star_air_pos", cur.asLong());
                    }
                    return false;
                }
            });
    public static final RegistryObject<Item> UNREFINED_CRYSTAL = ITEMS.register("unrefined_crystal",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> METEOR_FINDER = ITEMS.register("meteor_finder",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SCYTHE = ITEMS.register("scythe",
            () -> new ScytheItem(NyxMaterials.METEOR_TIER, 0, -3.0f,
                    new Item.Properties().durability(3450)));

    public static final RegistryObject<Item> METEOR_PICKAXE = ITEMS.register("meteor_pickaxe",
            () -> new PickaxeItem(NyxMaterials.METEOR_TIER, 1, -2.8f, new Item.Properties()) {
                @Override
                public float getDestroySpeed(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
                    float speed = super.getDestroySpeed(stack, state);
                    // 1.12: 2x speed on obsidian and meteor rock
                    if (state.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)
                            || state.is(net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN)
                            || state.getBlock() instanceof com.soul.soa_additions.nyx.block.MeteorRockBlock) {
                        speed *= 2.0f;
                    }
                    return speed;
                }
            });
    public static final RegistryObject<Item> METEOR_AXE = ITEMS.register("meteor_axe",
            () -> new AxeItem(NyxMaterials.METEOR_TIER, 5.0f, -3.2f, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_SHOVEL = ITEMS.register("meteor_shovel",
            () -> new ShovelItem(NyxMaterials.METEOR_TIER, 1.5f, -3.0f, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_HOE = ITEMS.register("meteor_hoe",
            () -> new HoeItem(NyxMaterials.METEOR_TIER, -1, 0.0f, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_SWORD = ITEMS.register("meteor_sword",
            () -> new SwordItem(NyxMaterials.METEOR_TIER, 3, -2.4f, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_HAMMER = ITEMS.register("meteor_hammer",
            () -> new HammerItem(NyxMaterials.METEOR_TIER, 1, -3.2f, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_BOW = ITEMS.register("meteor_bow",
            () -> new com.soul.soa_additions.nyx.item.MeteorBowItem(new Item.Properties().durability(2250)));

    public static final RegistryObject<Item> METEOR_HELM = ITEMS.register("meteor_helm",
            () -> new MeteorArmorItem(NyxMaterials.METEOR_ARMOR, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_CHEST = ITEMS.register("meteor_chest",
            () -> new MeteorArmorItem(NyxMaterials.METEOR_ARMOR, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_PANTS = ITEMS.register("meteor_pants",
            () -> new MeteorArmorItem(NyxMaterials.METEOR_ARMOR, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> METEOR_BOOTS = ITEMS.register("meteor_boots",
            () -> new MeteorArmorItem(NyxMaterials.METEOR_ARMOR, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> LUNAR_WATER_BOTTLE = ITEMS.register("lunar_water_bottle",
            () -> new LunarWaterBottleItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    private NyxItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
