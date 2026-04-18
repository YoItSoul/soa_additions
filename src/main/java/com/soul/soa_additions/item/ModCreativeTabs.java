package com.soul.soa_additions.item;

import com.soul.soa_additions.SoaAdditions;
import com.soul.soa_additions.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoaAdditions.MODID);

    public static final RegistryObject<CreativeModeTab> SOA_TAB = CREATIVE_MODE_TABS.register("soa_additions_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soa_additions_tab"))
                    .icon(() -> ModItems.VOID_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Ingots
                        output.accept(ModItems.ABYSSAL_INGOT.get());
                        output.accept(ModItems.ETHER_INGOT.get());
                        output.accept(ModItems.INFERNIUM_INGOT.get());
                        output.accept(ModItems.VOID_INGOT.get());

                        // Misc
                        output.accept(ModItems.CHEATER_COIN.get());
                        output.accept(ModItems.QUEST_BOOK.get());
                        if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
                            output.accept(com.soul.soa_additions.curios.CuriosIntegration.GREEDY_BAG.get());
                        }

                        // Ores
                        output.accept(ModBlocks.ABYSSAL_ORE_BLOCK.get());
                        output.accept(ModBlocks.ETHER_ORE_BLOCK.get());
                        output.accept(ModBlocks.INFERNIUM_ORE_BLOCK.get());
                        output.accept(ModBlocks.VOID_ORE_BLOCK.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> GC_TAB = CREATIVE_MODE_TABS.register("gc_items_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.gc_items_tab"))
                    .icon(() -> ModItems.CREATIVE_SOUL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // GC Custom Metals
                        output.accept(ModItems.AEONSTEEL_INGOT.get());
                        output.accept(ModItems.AEROITE_INGOT.get());
                        output.accept(ModItems.AQUALITE_INGOT.get());
                        output.accept(ModItems.ASGARDIUM_INGOT.get());
                        output.accept(ModItems.ASTRAL_METAL_INGOT.get());
                        output.accept(ModItems.CHROMASTEEL_INGOT.get());
                        output.accept(ModItems.CHROMIUM_INGOT.get());
                        output.accept(ModItems.COSMILITE_INGOT.get());
                        output.accept(ModItems.CRIMSONITE_INGOT.get());
                        output.accept(ModItems.CRYONIUM_INGOT.get());
                        output.accept(ModItems.CYTOSINITE_INGOT.get());
                        output.accept(ModItems.DURASTEEL_INGOT.get());
                        output.accept(ModItems.ELECTRONIUM_INGOT.get());
                        output.accept(ModItems.MANGANESE_INGOT.get());
                        output.accept(ModItems.MANGANESE_STEEL_INGOT.get());
                        output.accept(ModItems.PROTONIUM_INGOT.get());
                        output.accept(ModItems.SHADOWIUM_INGOT.get());
                        output.accept(ModItems.STAINLESS_STEEL_INGOT.get());
                        output.accept(ModItems.TERRA_ALLOY_INGOT.get());
                        output.accept(ModItems.TITANIUM_INGOT.get());

                        // TCon Material Items (mods not in SOA)
                        output.accept(ModItems.ETHAXIUM_INGOT.get());
                        output.accept(ModItems.SCARLITE.get());
                        output.accept(ModItems.HEPHAESTITE.get());
                        output.accept(ModItems.RAVAGING_INGOT.get());
                        output.accept(ModItems.REMORSEFUL_GEM.get());
                        output.accept(ModItems.MITHRILLIUM_INGOT.get());
                        output.accept(ModItems.ADAMINITE_INGOT.get());
                        output.accept(ModItems.MITHMINITE_INGOT.get());
                        output.accept(ModItems.YELLORIUM_INGOT.get());
                        output.accept(ModItems.CINCINNASITE.get());
                        output.accept(ModItems.RIME_CRYSTAL.get());
                        output.accept(ModItems.TOFU_GEM.get());
                        output.accept(ModItems.STRONG_TOFU_GEM.get());
                        output.accept(ModItems.SAKURA_DIAMOND.get());
                        output.accept(ModItems.CHOCOLATE_BAR.get());
                        output.accept(ModItems.METEOR_INGOT.get());
                        output.accept(ModItems.MODULARIUM_INGOT.get());
                        output.accept(ModItems.SPECTRE_INGOT.get());
                        output.accept(ModItems.ORICHALCOS_INGOT.get());
                        output.accept(ModItems.GAIASTEEL_INGOT.get());
                        output.accept(ModItems.FUSION_MATRIX_INGOT.get());

                        // TCon ports (tconevo/plustic, mods not in SOA)
                        output.accept(ModItems.RESTONIA_CRYSTAL.get());
                        output.accept(ModItems.PALIS_CRYSTAL.get());
                        output.accept(ModItems.DIAMANTINE_CRYSTAL.get());
                        output.accept(ModItems.VOID_CRYSTAL.get());
                        output.accept(ModItems.EMERALDIC_CRYSTAL.get());
                        output.accept(ModItems.ENORI_CRYSTAL.get());
                        output.accept(ModItems.BLACK_QUARTZ.get());
                        output.accept(ModItems.GHOSTWOOD_LOG.get());
                        output.accept(ModItems.BLOODWOOD_LOG.get());
                        output.accept(ModItems.DARKWOOD_LOG.get());
                        output.accept(ModItems.FUSEWOOD_LOG.get());
                        output.accept(ModItems.FLUX_CRYSTAL.get());
                        output.accept(ModItems.AQUAMARINE.get());
                        output.accept(ModItems.STARMETAL_INGOT.get());
                        output.accept(ModItems.ESSENCE_METAL_INGOT.get());
                        output.accept(ModItems.ENERGETIC_METAL_INGOT.get());
                        output.accept(ModItems.PRIMAL_METAL_INGOT.get());
                        output.accept(ModItems.UNIVERSAL_METAL_INGOT.get());
                        output.accept(ModItems.MEAT_METAL_INGOT.get());
                        output.accept(ModItems.PINK_SLIME_CRYSTAL.get());
                        output.accept(ModItems.MIRION_INGOT.get());
                        output.accept(ModItems.ALUMITE_INGOT.get());
                        output.accept(ModItems.OSGLOGLAS_INGOT.get());
                        output.accept(ModItems.OSMIRIDIUM_INGOT.get());

                        // TCon Bowstring/Fletching materials
                        output.accept(ModItems.NYLON_STRING.get());
                        output.accept(ModItems.RUBBER_BAND.get());
                        output.accept(ModItems.NYLON_CLOTH.get());
                        output.accept(ModItems.SPECTRE_STRING.get());
                        output.accept(ModItems.FLAMESTRING.get());

                        // TCon BoP gem materials (plustic ports)
                        output.accept(ModItems.SAPPHIRE.get());
                        output.accept(ModItems.RUBY.get());
                        output.accept(ModItems.PERIDOT.get());
                        output.accept(ModItems.MALACHITE.get());
                        output.accept(ModItems.TOPAZ.get());
                        output.accept(ModItems.TANZANITE.get());
                        output.accept(ModItems.AMETHYST_GEM.get());
                        output.accept(ModItems.AMBER.get());

                        // Materials
                        output.accept(ModItems.INFERNIUM_NUGGET.get());
                        output.accept(ModItems.ANTI_ENTROPY_MATTER.get());
                        output.accept(ModItems.ENERGY_MATTER_CORE.get());

                        // Singularities
                        output.accept(ModItems.FLUX_SINGULARITY.get());
                        output.accept(ModItems.MANA_SINGULARITY.get());
                        output.accept(ModItems.EXPERIENCE_SINGULARITY.get());
                        output.accept(ModItems.MATTER_SINGULARITY.get());

                        // Medals & Certificates
                        output.accept(ModItems.ORDINARY_MEDAL.get());
                        output.accept(ModItems.PIONEER_MEDAL.get());
                        output.accept(ModItems.GREEDY_MEDAL.get());
                        output.accept(ModItems.BRAVERY_CERTIFICATE.get());
                        output.accept(ModItems.OCD_CERTIFICATE.get());

                        // Souls & Shards
                        output.accept(ModItems.WITHER_SOUL.get());
                        output.accept(ModItems.DRAGON_SOUL.get());
                        output.accept(ModItems.CREATIVE_SHARD.get());
                        output.accept(ModItems.CREATIVE_SOUL.get());

                        // Solar items
                        output.accept(ModItems.SOLARIUM_STAR.get());
                        output.accept(ModItems.BROKEN_SOLARIUM_STAR.get());
                        output.accept(ModItems.SOLAR_SEED.get());
                        output.accept(ModItems.SUN_TOTEM.get());
                        output.accept(ModItems.SHINING_STAR.get());

                        // Sigils & Rituals
                        output.accept(ModItems.BLOOD_SIGIL.get());
                        output.accept(ModItems.TRUE_BLOOD_SIGIL.get());
                        output.accept(ModItems.BLOODY_SACRIFICE.get());
                        output.accept(ModItems.FORBIDDEN_BIBLE.get());

                        // Stage unlock items
                        output.accept(ModItems.AWAKENED_EYE.get());
                        output.accept(ModItems.ARCANE_CRYSTAL_BALL.get());
                        output.accept(ModItems.ENDER_CHARM.get());

                        // Functional items
                        output.accept(ModItems.DIFFICULTY_CHANGER.get());
                        output.accept(ModItems.DEATH_COIN.get());
                        output.accept(ModItems.CREATIVE_CONTROLLER.get());
                        output.accept(ModItems.FAKE_PHILOSOPHER_STONE.get());
                        output.accept(ModItems.BEAST_HAND.get());
                        output.accept(ModItems.PURIFYING_PILL.get());
                        output.accept(ModItems.ADRENALINE.get());
                        output.accept(ModItems.GOODIE_BAG.get());

                        // Consumables
                        output.accept(ModItems.MEDKIT_BIG.get());
                        output.accept(ModItems.MEDKIT_SUPER.get());
                        output.accept(ModItems.UNDEAD_MEDKIT.get());
                        output.accept(ModItems.STRANGE_LOLIPOP.get());
                        output.accept(ModItems.SHIELD_GUM.get());

                        // NBT items
                        output.accept(ModItems.NORTH_STAR.get());
                        output.accept(ModItems.OVERFLUX_CAPACITOR.get());
                        output.accept(ModItems.GLIDER.get());

                        // Loot / Reward items
                        output.accept(ModItems.STICK_OF_KNOCKBACK.get());
                        output.accept(ModItems.SWORD_OF_THE_RNG_GOD.get());

                        // Remaining GC items
                        output.accept(ModItems.ANCIENT_TOME.get());
                        output.accept(ModItems.ANCIENT_TOME_FRAGMENT.get());
                        output.accept(ModItems.ANCIENT_TOME_PAGE.get());
                        output.accept(ModItems.AURORA_HEART.get());
                        output.accept(ModItems.BAG_OF_DYES.get());
                        output.accept(ModItems.BLACK_HOLE_REMNANT.get());
                        output.accept(ModItems.BLUEPRINT.get());
                        output.accept(ModItems.BLUEPRINT_LASER_GUN.get());
                        output.accept(ModItems.BLUEPRINT_SHURIKEN.get());
                        output.accept(ModItems.BLUEPRINT_TACTIC.get());
                        output.accept(ModItems.BLUEPRINT_WAND.get());
                        output.accept(ModItems.BOUNTY_HUNTER_MEDAL.get());
                        output.accept(ModItems.BOUNTY_HUNTER_MEDAL_BRONZE.get());
                        output.accept(ModItems.BOUNTY_HUNTER_MEDAL_EMERALD.get());
                        output.accept(ModItems.BOUNTY_HUNTER_MEDAL_SILVER.get());
                        output.accept(ModItems.CATALYST_STAR.get());
                        output.accept(ModItems.CITY_DEFENDER_MEDAL.get());
                        output.accept(ModItems.COOKED_HUMAN_MEAT.get());
                        output.accept(ModItems.CRUDE_HATCHET.get());
                        output.accept(ModItems.CRYONIC_ARTIFACT.get());
                        output.accept(ModItems.DEATH_COUNTER.get());
                        output.accept(ModItems.DELIVERY_ORDER.get());
                        output.accept(ModItems.DIFFICULTY_SYNCER.get());
                        output.accept(ModItems.ELYSIA_PROJECT_LORE.get());
                        output.accept(ModItems.EMERGENCY_BUTTON.get());
                        output.accept(ModItems.EXECUTOR_TERMINAL.get());
                        output.accept(ModItems.EXPERIENCE_INGOT.get());
                        output.accept(ModItems.EXPERIENCE_NUGGET.get());
                        output.accept(ModItems.EXPERIENCE_TRANSPORTER.get());
                        output.accept(ModItems.EXPERIMENT_NOTE.get());
                        output.accept(ModItems.FOOD_BAG.get());
                        output.accept(ModItems.FURNITURE_CRATE.get());
                        output.accept(ModItems.GIFT.get());
                        output.accept(ModItems.GRASS_STRING.get());
                        output.accept(ModItems.GUIDE_BOOK.get());
                        output.accept(ModItems.HUAJI.get());
                        output.accept(ModItems.INFINITY_STONE.get());
                        output.accept(ModItems.ITEM_PURGER.get());
                        output.accept(ModItems.LOLI_LOLIPOP.get());
                        output.accept(ModItems.LUCKY_CLOVER.get());
                        output.accept(ModItems.MEDKIT_SMALL.get());
                        output.accept(ModItems.ONE_PUNCH.get());
                        output.accept(ModItems.PASSPORT.get());
                        output.accept(ModItems.PEARL_OF_KNOWLEDGE.get());
                        output.accept(ModItems.PEBBLE.get());
                        output.accept(ModItems.PINECONE.get());
                        output.accept(ModItems.PLANT_FIBRE.get());
                        output.accept(ModItems.PLATE_OF_HONOR.get());
                        output.accept(ModItems.POOP.get());
                        output.accept(ModItems.POOPBURGER.get());
                        output.accept(ModItems.PURIFYING_DUST.get());
                        output.accept(ModItems.RAW_HUMAN_MEAT.get());
                        output.accept(ModItems.RED_PACKET.get());
                        output.accept(ModItems.RESPAWN_ANCHOR.get());
                        output.accept(ModItems.REWARD_TICKET_COMMON.get());
                        output.accept(ModItems.REWARD_TICKET_EPIC.get());
                        output.accept(ModItems.REWARD_TICKET_LEGENDARY.get());
                        output.accept(ModItems.REWARD_TICKET_RARE.get());
                        output.accept(ModItems.ROYAL_GEL.get());
                        output.accept(ModItems.SAND_OF_TIME.get());
                        output.accept(ModItems.SKILL_RESET_SCROLL.get());
                        output.accept(ModItems.SLIME_CROWN.get());
                        output.accept(ModItems.STAINLESS_STEEL_BALL.get());
                        output.accept(ModItems.STRANGE_MATTER.get());
                        output.accept(ModItems.SUNNY_DOLL.get());
                        output.accept(ModItems.TIME_FRAGMENT.get());
                        output.accept(ModItems.TIME_SHARD.get());
                        output.accept(ModItems.TITANIUM_NUGGET.get());
                        output.accept(ModItems.TOWER_CHEST_KEY.get());
                        output.accept(ModItems.TOWER_CHEST_UNLOCKED.get());
                        output.accept(ModItems.TRUE_EYE_OF_ENDER.get());
                        output.accept(ModItems.TWILIGHT_GEM.get());
                        output.accept(ModItems.TWILIGHT_SHIELD.get());
                    })
                    .build());

    private ModCreativeTabs() {}

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
