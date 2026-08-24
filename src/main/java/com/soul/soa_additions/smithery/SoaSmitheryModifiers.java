package com.soul.soa_additions.smithery;

import com.soul.smithery.api.SmitheryAPI;
import com.soul.smithery.api.modifier.Modifier;
import com.soul.smithery.item.tool.SmitheryArmorItem;
import com.soul.smithery.item.tool.SmitheryToolData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class SoaSmitheryModifiers {

    public static ResourceLocation ABSORBENT_ARMOR;
    public static ResourceLocation ALIEN_ARMOR;
    public static ResourceLocation AMBITIOUS_ARMOR;
    public static ResourceLocation AMPHIBIOUS_ARMOR;
    public static ResourceLocation ANALYSING;
    public static ResourceLocation ANTICORROSION;
    public static ResourceLocation ANTIGRAV;
    public static ResourceLocation AOA_THRESHOLD;
    public static ResourceLocation APOCALYPSE;
    public static ResourceLocation AQUADYNAMIC;
    public static ResourceLocation AQUASPEED_ARMOR;
    public static ResourceLocation ARCANE;
    public static ResourceLocation ARIDICULOUS;
    public static ResourceLocation ARIDICULOUS_ARMOR;
    public static ResourceLocation ASSASSINTRAIT;
    public static ResourceLocation AUTOFORGE_ARMOR;
    public static ResourceLocation AUTOREPAIR;
    public static ResourceLocation AUTOSMELT;
    public static ResourceLocation BACONLICIOUS;
    public static ResourceLocation BACONLICIOUS_ARMOR;
    public static ResourceLocation BANE_OF_ARTHOPODS;
    public static ResourceLocation BANE_OF_NIGHT;
    public static ResourceLocation BARRETT;
    public static ResourceLocation BEHEADING;
    public static ResourceLocation BERSERK;
    public static ResourceLocation BLAST_RESISTANT_ARMOR;
    public static ResourceLocation BLASTING;
    /** Tinkers' mining "Blasting": full speed on any block, at the cost of its drops. */
    public static ResourceLocation BLASTING_MINING;
    public static ResourceLocation BLESSED_ARMOR;
    public static ResourceLocation BLIND;
    public static ResourceLocation BLINDBANDIT;
    public static ResourceLocation BLOODLUSTTRAIT;
    public static ResourceLocation BODY;
    public static ResourceLocation BOTANICAL;
    public static ResourceLocation BOUNCY_ARMOR;
    public static ResourceLocation BREAKABLE;
    public static ResourceLocation BRIGHT;
    public static ResourceLocation BRITTLE;
    public static ResourceLocation BROWNMAGIC;
    public static ResourceLocation CALCIC_ARMOR;
    public static ResourceLocation CAMDAIBAY_ARMOR;
    public static ResourceLocation CASCADE;
    public static ResourceLocation CATCHER;
    public static ResourceLocation CHADTHUNDER;
    public static ResourceLocation CHEAP;
    public static ResourceLocation CHEAP_ARMOR;
    public static ResourceLocation CHEAPSKATE;
    public static ResourceLocation CHEAPSKATE_ARMOR;
    public static ResourceLocation CHUNKY;
    public static ResourceLocation COLDBLOODED;
    public static ResourceLocation COMBUSTIBLE_ARMOR;
    public static ResourceLocation CONCEALED_ARMOR;
    public static ResourceLocation CONGENIAL;
    public static ResourceLocation CORALIUMPLAGUE;
    public static ResourceLocation COTLIFESTEAL;
    public static ResourceLocation COTLIFESTEALTRAIT;
    public static ResourceLocation CRUDE;
    public static ResourceLocation CRUMBLING;
    public static ResourceLocation CRUSHING;
    public static ResourceLocation CRYONICTRAIT;
    public static ResourceLocation CRYONICTRAIT_ARMOR;
    public static ResourceLocation CRYSTALTRAIT;
    public static ResourceLocation CRYSTALTRAIT_ARMOR;
    public static ResourceLocation CURSED;
    public static ResourceLocation CURVATURE;
    public static ResourceLocation CUSHY;
    public static ResourceLocation DARK;
    public static ResourceLocation DARKTRAVELER;
    public static ResourceLocation DECAY;
    public static ResourceLocation DEFILED;
    public static ResourceLocation DENSE;
    public static ResourceLocation DENSE_ARMOR;
    public static ResourceLocation DEPTHDIGGER;
    public static ResourceLocation DEVILSSTRENGTH;
    public static ResourceLocation DEXTEROUS_ARMOR;
    public static ResourceLocation DIAMOND;
    public static ResourceLocation DIAMOND_ARMOR;
    public static ResourceLocation DIFFUSE;
    public static ResourceLocation DISSOLVING;
    public static ResourceLocation DIVINESHIELD;
    public static ResourceLocation DPRK;
    public static ResourceLocation DRAMATIC_ARMOR;
    public static ResourceLocation DREADPLAGUE;
    public static ResourceLocation DREADPURITY;
    public static ResourceLocation DUNANSTRANSPORT_ARMOR;
    public static ResourceLocation DURITOS;
    public static ResourceLocation DURITOS_RANCH_ARMOR;
    public static ResourceLocation ECOLOGICAL;
    public static ResourceLocation ECOLOGICAL_ARMOR;
    public static ResourceLocation ELEMENTAL;
    public static ResourceLocation EMERALD;
    public static ResourceLocation EMERALD_ARMOR;
    public static ResourceLocation ENDERFERENCE;
    public static ResourceLocation ENDERPICKUP;
    public static ResourceLocation ENDERPORT;
    public static ResourceLocation ENDERPORT_ARMOR;
    public static ResourceLocation ENDSPEED;
    public static ResourceLocation ENDURANCETRAIT;
    public static ResourceLocation ENDURANCE_ARMOR;
    public static ResourceLocation ENLIGHTENED;
    public static ResourceLocation ESTABLISHED;
    public static ResourceLocation EXECUTIONERTRAIT;
    public static ResourceLocation EXPLOSIVE;
    public static ResourceLocation EXTRATRAIT;
    public static ResourceLocation EXTRATRAIT_ARMOR;
    public static ResourceLocation FEATHERWEIGHT_ARMOR;
    public static ResourceLocation FESTIVE;
    public static ResourceLocation FIERY;
    public static ResourceLocation FINS;
    public static ResourceLocation FIRE_RESISTANT_ARMOR;
    public static ResourceLocation FIRSTGUARDTRAIT;
    public static ResourceLocation FIRSTGUARDTRAIT_ARMOR;
    public static ResourceLocation FLAMMABLE;
    public static ResourceLocation FORTIFIEDTRAIT;
    public static ResourceLocation FORTIFIEDTRAIT_ARMOR;
    public static ResourceLocation FORTIFY;
    public static ResourceLocation FRACTURE;
    public static ResourceLocation FRACTURED;
    public static ResourceLocation FRAGILE;
    public static ResourceLocation FREEZING;
    public static ResourceLocation FROSTWALKER_ARMOR;
    public static ResourceLocation FRUITSALAD;
    public static ResourceLocation GAMBLE;
    public static ResourceLocation GAMBLETRAIT;
    public static ResourceLocation GAMBLE_ARMOR;
    public static ResourceLocation GARISHLY;
    public static ResourceLocation GETLUCKY;
    public static ResourceLocation GHASTLY;
    public static ResourceLocation GIANTSLAYER;
    public static ResourceLocation GIANTSLAYERTRAIT;
    public static ResourceLocation GILDED;
    public static ResourceLocation GLIMMER;
    public static ResourceLocation GLOBAL;
    public static ResourceLocation GLOWING;
    public static ResourceLocation GLOWING_ARMOR;
    public static ResourceLocation GOODFRIDAYAGREEMENT_ARMOR;
    public static ResourceLocation HAILHYDRA;
    public static ResourceLocation HALLOWEENTRAIT;
    public static ResourceLocation HAORANS_CULT_ARMOR;
    public static ResourceLocation HARVESTHEIGHT;
    public static ResourceLocation HARVESTWIDTH;
    public static ResourceLocation HASTE;
    public static ResourceLocation HEARTS;
    public static ResourceLocation HEAVY;
    public static ResourceLocation HEAVY_ARMOR;
    public static ResourceLocation HEAVY_METAL;
    public static ResourceLocation HELLISH;
    public static ResourceLocation HEROIC;
    public static ResourceLocation HIGH_STRIDE_ARMOR;
    public static ResourceLocation HITECH;
    public static ResourceLocation HOLDGROUNDTRAIT;
    public static ResourceLocation HOLD_GROUND;
    public static ResourceLocation HOLD_GROUND_ARMOR;
    public static ResourceLocation HOLLOW;
    public static ResourceLocation HOLY;
    public static ResourceLocation HOVERING;
    public static ResourceLocation IGNOBLE;
    public static ResourceLocation ILLUMINATI;
    public static ResourceLocation IM_A_SUPERSTAR;
    public static ResourceLocation INDOMITABLE_ARMOR;
    public static ResourceLocation INFERNAL_ARMOR;
    public static ResourceLocation INFERNOTRAIT;
    public static ResourceLocation INFERNOTRAIT_ARMOR;
    public static ResourceLocation INSATIABLE;
    public static ResourceLocation INVARIANT_ARMOR;
    public static ResourceLocation INVIGORATING_ARMOR;
    public static ResourceLocation JADED;
    public static ResourceLocation JAGGED;
    public static ResourceLocation JETPACKPANCAKEHIPPOS;
    public static ResourceLocation KNOCKBACK;
    public static ResourceLocation KNOWLEDGEFUL;
    public static ResourceLocation KNOWLEDGEFULTRAIT;
    public static ResourceLocation KNOWLEDGEFUL_ARMOR;
    public static ResourceLocation KUNGFUTRAIT;
    public static ResourceLocation KUNGFU_ARMOR;
    public static ResourceLocation LAUNCHING;
    public static ResourceLocation LEVELINGDAMAGETRAIT;
    public static ResourceLocation LEVELINGDEFENSETRAIT;
    public static ResourceLocation LIGHT;
    public static ResourceLocation LIGHT_PIERCE;
    public static ResourceLocation LIGHTNING;
    public static ResourceLocation LIGHTWEIGHT;
    public static ResourceLocation LIGHTWEIGHT_ARMOR;
    public static ResourceLocation LIVING;
    public static ResourceLocation LIVING2;
    public static ResourceLocation LUCK;
    public static ResourceLocation LUCKYTRAIT;
    public static ResourceLocation LUCKYTRAIT_ARMOR;
    public static ResourceLocation MADNESSTRAIT;
    public static ResourceLocation MAGICAL_MODIFIER;
    public static ResourceLocation MAGICMUSHROOM;
    public static ResourceLocation MAGNETIC;
    public static ResourceLocation MAGNETIC_ARMOR;
    public static ResourceLocation MAGNETIC_ARMOR1;
    public static ResourceLocation MAGNETIC_ARMOR2;
    public static ResourceLocation MANA;
    public static ResourceLocation MATTERTRAIT1;
    public static ResourceLocation MATTERTRAIT11;
    public static ResourceLocation MATTERTRAIT12;
    public static ResourceLocation MATTERTRAIT2;
    public static ResourceLocation MELTING;
    public static ResourceLocation MUTATE;
    public static ResourceLocation SOUL_STAINED;
    // GreedyCraft Creative Modifier: +1 free modifier slot per application, stacking
    // (Tinkers ModCreative set FreeModifiers = level). Slot count comes from the
    // bonus_slots_per_level param x level, read by SmitheryToolItem.bonusModifierSlots.
    public static ResourceLocation CREATIVE;
    public static ResourceLocation WELL_USED;
    public static ResourceLocation MENDING_ARMOR;
    public static ResourceLocation MENDING_MOSS;
    public static ResourceLocation MILKYTRAIT;
    public static ResourceLocation MILKYTRAIT_ARMOR;
    public static ResourceLocation MIND;
    public static ResourceLocation MIRABILE;
    public static ResourceLocation MOMENTUM;
    public static ResourceLocation MORGANLEFAY;
    public static ResourceLocation MOTIONTRAIT;
    public static ResourceLocation MOTIONTRAIT_ARMOR;
    public static ResourceLocation MUNDANE_ARMOR;
    public static ResourceLocation MUNDANE_ARMOR1;
    public static ResourceLocation MUSICOFTHESPHERES;
    public static ResourceLocation MYSTICAL_FIRE;
    public static ResourceLocation NAPHTHA;
    public static ResourceLocation NATUREBOUND;
    public static ResourceLocation NATURESBLESSING;
    public static ResourceLocation NATURESPOWER;
    public static ResourceLocation NATURESWRATH;
    public static ResourceLocation NICKOFTIME;
    public static ResourceLocation NIGHTBANETRAIT;
    public static ResourceLocation NIGHT_VISION_ARMOR;
    public static ResourceLocation OREEXCAVATE;
    public static ResourceLocation PARASITIC_ARMOR;
    public static ResourceLocation PENETRATIONTRAIT;
    public static ResourceLocation PERFECTIONIST;
    public static ResourceLocation PERFECTIONISTTRAIT;
    public static ResourceLocation PERFECTIONISTTRAIT_ARMOR;
    public static ResourceLocation PETRAMOR;
    public static ResourceLocation PETRAVIDITY_ARMOR;
    public static ResourceLocation PINKYTRAIT;
    public static ResourceLocation POISONOUS;
    public static ResourceLocation POLISHED_ARMOR;
    public static ResourceLocation POOPTRAIT;
    public static ResourceLocation POOPY;
    public static ResourceLocation POOPY_ARMOR;
    public static ResourceLocation PORTED;
    public static ResourceLocation PORTLY;
    public static ResourceLocation POTION_BELT_ARMOR;
    public static ResourceLocation POWERFUL_ARMOR;
    public static ResourceLocation PRECIPITATE;
    public static ResourceLocation PRICKLY;
    public static ResourceLocation PRIDEFUL_ARMOR;
    public static ResourceLocation PROJECTILE_RESISTANT_ARMOR;
    public static ResourceLocation PROSPEROUS;
    public static ResourceLocation PSICOLOGICAL;
    public static ResourceLocation PULVERIZING;
    public static ResourceLocation PURIFYINGTRAIT;
    public static ResourceLocation PURIFYINGTRAIT_ARMOR;
    public static ResourceLocation RAGING;
    public static ResourceLocation RAGINGTRAIT;
    public static ResourceLocation REACH;
    public static ResourceLocation REFRIGERATION;
    public static ResourceLocation REINFORCED;
    public static ResourceLocation REINFORCED_ARMOR;
    public static ResourceLocation RELIABLETRAIT;
    public static ResourceLocation RESISTANT_ARMOR;
    public static ResourceLocation RESONANCE;
    public static ResourceLocation REVIVING;
    public static ResourceLocation RUDEAWAKENING;
    public static ResourceLocation SACRIFICIALTRAIT;
    public static ResourceLocation SECONDLIFETRAIT;
    public static ResourceLocation SECONDLIFETRAIT_ARMOR;
    public static ResourceLocation SENTIENCE;
    public static ResourceLocation SERRATED;
    public static ResourceLocation SHADOW;
    public static ResourceLocation SHARP;
    public static ResourceLocation SHARPNESS;
    public static ResourceLocation SHIELDING_ARMOR;
    public static ResourceLocation SHOCKING;
    public static ResourceLocation SHULKERWEIGHT_ARMOR;
    public static ResourceLocation SHULKING;
    public static ResourceLocation SILKTOUCH;
    public static ResourceLocation SKELETAL_ARMOR;
    public static ResourceLocation SKYROOTED;
    public static ResourceLocation SLASHING;
    public static ResourceLocation SLAUGHTERING;
    public static ResourceLocation SLIMEY;
    public static ResourceLocation SLIMEY_ARMOR;
    public static ResourceLocation SMITE;
    public static ResourceLocation SOFTY;
    public static ResourceLocation SOUL;
    public static ResourceLocation SOUL_SIGHT_ARMOR;
    public static ResourceLocation SOULBOUND;
    public static ResourceLocation SOULBOUND_ARMOR;
    public static ResourceLocation SOULCHARGE;
    public static ResourceLocation SOULEATER;
    public static ResourceLocation SOULPOWER;
    public static ResourceLocation SPADES;
    public static ResourceLocation SPAGHETTI_MEAT;
    public static ResourceLocation SPAGHETTI_SAUCE;
    public static ResourceLocation SPARTAN;
    public static ResourceLocation SPARTANTRAIT;
    public static ResourceLocation SPARTAN_ARMOR;
    public static ResourceLocation SPEEDY_ARMOR;
    public static ResourceLocation SPIKY;
    public static ResourceLocation SPINY_ARMOR;
    public static ResourceLocation SPLINTERING;
    public static ResourceLocation SPLINTERS;
    public static ResourceLocation SPLITTING;
    public static ResourceLocation SQUEAKY;
    public static ResourceLocation STALWART;
    public static ResourceLocation STARFISHY;
    public static ResourceLocation STEADY_ARMOR;
    public static ResourceLocation STICKY_ARMOR;
    public static ResourceLocation STIFF;
    public static ResourceLocation STONEBOUND;
    public static ResourceLocation STOPBEINGSELFISH;
    public static ResourceLocation STRONGVACCINETRAIT;
    public static ResourceLocation STRONGVACCINETRAIT_ARMOR;
    public static ResourceLocation SUBTERRANEAN_ARMOR;
    public static ResourceLocation SUPERHEAT;
    public static ResourceLocation SUPERHEAVY;
    public static ResourceLocation SUPERHOT_ARMOR;
    public static ResourceLocation SUPERKNOCKBACK;
    public static ResourceLocation SUPERKNOCKPACKTRAIT;
    public static ResourceLocation SWETTY;
    public static ResourceLocation SYNERGY;
    public static ResourceLocation TANTRUM;
    public static ResourceLocation TASTY;
    public static ResourceLocation TASTY_ARMOR;
    public static ResourceLocation TCONEVO_ABSORPTION_ARMOR;
    public static ResourceLocation TCONEVO_AFTERSHOCK;
    public static ResourceLocation TCONEVO_AFTERSHOCK3;
    public static ResourceLocation TCONEVO_APIARY_AFFINITY_ARMOR;
    public static ResourceLocation TCONEVO_ARTIFACT;
    public static ResourceLocation TCONEVO_ASTRAL;
    public static ResourceLocation TCONEVO_ASTRAL_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_AEVITAS;
    public static ResourceLocation TCONEVO_ATTUNED_AEVITAS_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_ARMARA;
    public static ResourceLocation TCONEVO_ATTUNED_ARMARA_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_BOOTES;
    public static ResourceLocation TCONEVO_ATTUNED_BOOTES_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_DISCIDIA;
    public static ResourceLocation TCONEVO_ATTUNED_DISCIDIA_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_EVORSIO;
    public static ResourceLocation TCONEVO_ATTUNED_EVORSIO_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_FORNAX;
    public static ResourceLocation TCONEVO_ATTUNED_FORNAX_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_HOROLOGIUM;
    public static ResourceLocation TCONEVO_ATTUNED_HOROLOGIUM_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_LUCERNA;
    public static ResourceLocation TCONEVO_ATTUNED_LUCERNA_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_MINERALIS;
    public static ResourceLocation TCONEVO_ATTUNED_MINERALIS_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_OCTANS;
    public static ResourceLocation TCONEVO_ATTUNED_OCTANS_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_PELOTRIO;
    public static ResourceLocation TCONEVO_ATTUNED_PELOTRIO_ARMOR;
    public static ResourceLocation TCONEVO_ATTUNED_VICIO;
    public static ResourceLocation TCONEVO_ATTUNED_VICIO_ARMOR;
    public static ResourceLocation TCONEVO_AURA_INFUSED_ARMOR;
    public static ResourceLocation TCONEVO_AURA_SIPHON;
    public static ResourceLocation TCONEVO_BATTLE_FUROR;
    public static ResourceLocation TCONEVO_BLASTING;
    public static ResourceLocation TCONEVO_BLOODBOUND;
    public static ResourceLocation TCONEVO_BLOODBOUND_ARMOR;
    public static ResourceLocation TCONEVO_BULWARK_ARMOR;
    public static ResourceLocation TCONEVO_CASCADING;
    public static ResourceLocation TCONEVO_CELESTIAL_ARMOR;
    public static ResourceLocation TCONEVO_CHAIN_LIGHTNING;
    public static ResourceLocation TCONEVO_CHAOS_RESISTANCE_ARMOR;
    public static ResourceLocation TCONEVO_CHILLING_TOUCH_ARMOR;
    public static ResourceLocation TCONEVO_CONDENSING;
    public static ResourceLocation TCONEVO_CORRUPTING;
    public static ResourceLocation TCONEVO_CRYSTALYS;
    public static ResourceLocation TCONEVO_CULLING;
    public static ResourceLocation TCONEVO_DEADLY_PRECISION;
    public static ResourceLocation TCONEVO_DIVINE_GRACE_ARMOR;
    public static ResourceLocation TCONEVO_DRACONIC_ARROW_DAMAGE;
    public static ResourceLocation TCONEVO_DRACONIC_ARROW_SPEED;
    public static ResourceLocation TCONEVO_DRACONIC_ATTACK_AOE;
    public static ResourceLocation TCONEVO_DRACONIC_ATTACK_DAMAGE;
    public static ResourceLocation TCONEVO_DRACONIC_DIG_AOE;
    public static ResourceLocation TCONEVO_DRACONIC_DIG_SPEED;
    public static ResourceLocation TCONEVO_DRACONIC_DRAW_SPEED;
    public static ResourceLocation TCONEVO_DRACONIC_ENERGY;
    public static ResourceLocation TCONEVO_DRACONIC_ENERGY_ARMOR;
    public static ResourceLocation TCONEVO_DRACONIC_JUMP_BOOST_ARMOR;
    public static ResourceLocation TCONEVO_DRACONIC_MOVE_SPEED_ARMOR;
    public static ResourceLocation TCONEVO_DRACONIC_SHIELD_CAPACITY_ARMOR;
    public static ResourceLocation TCONEVO_DRACONIC_SHIELD_RECOVERY_ARMOR;
    public static ResourceLocation TCONEVO_ELECTRIC;
    public static ResourceLocation TCONEVO_ELECTRIC_ARMOR;
    public static ResourceLocation TCONEVO_ENERGIZED;
    public static ResourceLocation TCONEVO_ENERGIZED_ARMOR;
    public static ResourceLocation TCONEVO_ENERGIZED_ARMOR2;
    public static ResourceLocation TCONEVO_ENTROPIC;
    public static ResourceLocation TCONEVO_ETERNAL_DENSITY;
    public static ResourceLocation TCONEVO_ETERNITY_ARMOR;
    public static ResourceLocation TCONEVO_EVOLVED;
    public static ResourceLocation TCONEVO_EVOLVED_ARMOR;
    public static ResourceLocation TCONEVO_EXECUTOR;
    public static ResourceLocation TCONEVO_FAE_VOICE;
    public static ResourceLocation TCONEVO_FAE_VOICE_ARMOR;
    public static ResourceLocation TCONEVO_FERTILIZING;
    public static ResourceLocation TCONEVO_FINAL_GUARD_ARMOR;
    public static ResourceLocation TCONEVO_FLUX_BURN;
    public static ResourceLocation TCONEVO_FLUXED;
    public static ResourceLocation TCONEVO_FLUXED_ARMOR;
    public static ResourceLocation TCONEVO_FOOT_FLEET;
    public static ResourceLocation TCONEVO_GAIA_WILL_AHRIM_ARMOR;
    public static ResourceLocation TCONEVO_GAIA_WILL_DHAROK_ARMOR;
    public static ResourceLocation TCONEVO_GAIA_WILL_GUTHAN_ARMOR;
    public static ResourceLocation TCONEVO_GAIA_WILL_KARIL_ARMOR;
    public static ResourceLocation TCONEVO_GAIA_WILL_TORAG_ARMOR;
    public static ResourceLocation TCONEVO_GAIA_WILL_VERAC_ARMOR;
    public static ResourceLocation TCONEVO_GAIA_WRATH;
    public static ResourceLocation TCONEVO_GALE_FORCE_ARMOR;
    public static ResourceLocation TCONEVO_GALE_FORCE_ARMOR1;
    public static ResourceLocation TCONEVO_HEARTH_EMBRACE_ARMOR;
    public static ResourceLocation TCONEVO_IMPACT_FORCE;
    public static ResourceLocation TCONEVO_INFINITUM;
    public static ResourceLocation TCONEVO_JUGGERNAUT;
    public static ResourceLocation TCONEVO_LUMINIFEROUS;
    public static ResourceLocation TCONEVO_MANA_AFFINITY_ARMOR;
    public static ResourceLocation TCONEVO_MANA_INFUSED;
    public static ResourceLocation TCONEVO_MANA_INFUSED_ARMOR;
    public static ResourceLocation TCONEVO_MODIFIABLE;
    public static ResourceLocation TCONEVO_MODIFIABLE1;
    public static ResourceLocation TCONEVO_MODIFIABLE2;
    public static ResourceLocation TCONEVO_MORTAL_WOUNDS;
    public static ResourceLocation TCONEVO_NULL_ALMIGHTY_ARMOR;
    public static ResourceLocation TCONEVO_CRYSTALLINE;
    public static ResourceLocation TCONEVO_RUINATION;
    public static ResourceLocation TCONEVO_PHOENIX_ASPECT_ARMOR;
    public static ResourceLocation TCONEVO_WILL_STRENGTH_ARMOR;
    public static ResourceLocation TCONEVO_OMNIPOTENCE;
    public static ResourceLocation TCONEVO_OPPORTUNIST;
    public static ResourceLocation TCONEVO_OVERWHELM;
    public static ResourceLocation TCONEVO_PHOTOSYNTHETIC;
    public static ResourceLocation TCONEVO_PHOTOSYNTHETIC_ARMOR;
    public static ResourceLocation TCONEVO_PHOTOVOLTAIC;
    public static ResourceLocation TCONEVO_PHOTOVOLTAIC_ARMOR;
    public static ResourceLocation TCONEVO_PIEZOELECTRIC;
    public static ResourceLocation TCONEVO_PRIMORDIAL;
    public static ResourceLocation TCONEVO_RADIANT_ARMOR;
    public static ResourceLocation TCONEVO_REACTIVE_ARMOR;
    public static ResourceLocation TCONEVO_REAPING;
    public static ResourceLocation TCONEVO_REJUVENATING;
    public static ResourceLocation TCONEVO_RELENTLESS;
    public static ResourceLocation TCONEVO_SECOND_WIND_ARMOR;
    public static ResourceLocation TCONEVO_SENTIENT;
    public static ResourceLocation TCONEVO_SENTIENT_ARMOR;
    public static ResourceLocation TCONEVO_SHADOWSTEP_ARMOR;
    public static ResourceLocation TCONEVO_SOUL_GUARD_ARMOR;
    public static ResourceLocation TCONEVO_SOUL_REND;
    public static ResourceLocation TCONEVO_SOUL_REND1;
    public static ResourceLocation TCONEVO_SOUL_REND3;
    public static ResourceLocation TCONEVO_SPECTRAL_ARMOR;
    public static ResourceLocation TCONEVO_STAGGERING;
    public static ResourceLocation TCONEVO_STIFLING_ARMOR;
    public static ResourceLocation TCONEVO_STONEBOUND_ARMOR;
    public static ResourceLocation TCONEVO_SUNDERING;
    public static ResourceLocation TCONEVO_SUPERDENSE_ARMOR;
    public static ResourceLocation TCONEVO_THUNDERGOD_FAVOUR_ARMOR;
    public static ResourceLocation TCONEVO_THUNDERGOD_WRATH;
    public static ResourceLocation TCONEVO_ULTRADENSE_ARMOR;
    public static ResourceLocation TCONEVO_VAMPIRIC;
    public static ResourceLocation TCONEVO_WARPING;
    public static ResourceLocation TCONEVO_WARPING_ARMOR;
    public static ResourceLocation TCONEVO_WILLFUL;
    public static ResourceLocation TCONEVO_WILLFUL_ARMOR;
    public static ResourceLocation TELEKINETIC_ARMOR;
    public static ResourceLocation TERRAFIRMA;
    public static ResourceLocation TERRAFIRMA1;
    public static ResourceLocation TERRAFIRMA2;
    public static ResourceLocation THAUMIC;
    public static ResourceLocation THRONY;
    public static ResourceLocation THRONYTRAIT;
    public static ResourceLocation THRONY_ARMOR;
    public static ResourceLocation THUNDERING;
    public static ResourceLocation THUNDERINGTRAIT;
    public static ResourceLocation TIDALFORCETRAIT;
    public static ResourceLocation TIDALFORCETRAIT_ARMOR;
    public static ResourceLocation TIDAL_FORCE;
    public static ResourceLocation TOM_AND_JERRY_ARMOR;
    public static ResourceLocation TRASH;
    public static ResourceLocation TRAVEL_BELT_ARMOR;
    public static ResourceLocation TRAVEL_GOGGLES_ARMOR;
    public static ResourceLocation TRAVEL_SACK_ARMOR;
    public static ResourceLocation TRAVEL_SLOWFALL_ARMOR;
    public static ResourceLocation TRAVEL_SNEAK_ARMOR;
    public static ResourceLocation TRUEDEFENSETRAIT;
    public static ResourceLocation TRUEDEFENSETRAIT_ARMOR;
    public static ResourceLocation TWILIT;
    public static ResourceLocation UNCERTAIN;
    public static ResourceLocation UNNAMED;
    public static ResourceLocation UNNATURAL;
    public static ResourceLocation UNSTABLE;
    public static ResourceLocation VACCINETRAIT;
    public static ResourceLocation VACCINETRAIT_ARMOR;
    public static ResourceLocation VEILED;
    public static ResourceLocation VENGEFUL_ARMOR;
    public static ResourceLocation VINDICTIVE;
    public static ResourceLocation VISIONTRAIT;
    public static ResourceLocation VISIONTRAIT_ARMOR;
    public static ResourceLocation VOLTAIC_ARMOR;
    public static ResourceLocation WARMTRAIT;
    public static ResourceLocation WARMTRAIT_ARMOR;
    public static ResourceLocation WARPDRAINTRAIT;
    public static ResourceLocation WARPDRAINTRAIT_ARMOR;
    public static ResourceLocation WEBBED;
    public static ResourceLocation WHIRL;
    public static ResourceLocation WRITABLE;
    public static ResourceLocation WRITABLE1;
    public static ResourceLocation WRITABLE2;
    public static ResourceLocation XU_WHISPERING;
    public static ResourceLocation XU_WITHERING;
    public static ResourceLocation XU_XP_BOOST;
    public static ResourceLocation ZANY;

    public static void register() {
        ABSORBENT_ARMOR = id("absorbent_armor");
        SmitheryAPI.registerModifier(Modifier.builder(ABSORBENT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (!ctx.wearer().isUnderWater()) return;
                    if (ctx.wearer().tickCount % 3 == 0) {
                        ctx.wearer().setAirSupply(Math.min(ctx.wearer().getMaxAirSupply(),
                                ctx.wearer().getAirSupply() + 1));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ALIEN_ARMOR = id("alien_armor");
        SmitheryAPI.registerModifier(Modifier.builder(ALIEN_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 600 == 0) {
                        var stack = ctx.armor();
                        if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        AMBITIOUS_ARMOR = id("ambitious_armor");
        SmitheryAPI.registerModifier(Modifier.builder(AMBITIOUS_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onKill((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.5f)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        AMPHIBIOUS_ARMOR = id("amphibious_armor");
        SmitheryAPI.registerModifier(Modifier.builder(AMPHIBIOUS_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (!ctx.wearer().isUnderWater()) return;
                    if (ctx.wearer().tickCount % 3 == 0) {
                        ctx.wearer().setAirSupply(Math.min(ctx.wearer().getMaxAirSupply(),
                                ctx.wearer().getAirSupply() + 1));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ANALYSING = id("analysing");
        SmitheryAPI.registerModifier(Modifier.builder(ANALYSING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() < 0.1F) ctx.drops().clear();
                    // XP modification on block break
                    if (ctx.xp() > 0) {
                        float exp = rng.nextFloat() * rng.nextFloat() * rng.nextFloat()
                            * (ctx.xp() + rng.nextInt(ctx.xp()) * (1.0F + rng.nextFloat()));
                        ctx.setXp(Math.round(exp));
                    }
                })
                .onMobDrops((effect, ctx) -> {
                    var rng = ctx.victim().level().getRandom();
                    if (rng.nextFloat() < 0.1F) ctx.drops().clear();
                })
                .onKill((effect, ctx) -> {
                    // XP modification on mob kill
                    var rng = ctx.victim().level().getRandom();
                    if (ctx.xp() > 0) {
                        int xp = ctx.xp();
                        float exp = rng.nextFloat() * rng.nextFloat() * rng.nextFloat()
                            * (xp + rng.nextInt(xp) * (1.0F + rng.nextFloat()));
                        ctx.setXp(Math.round(exp));
                    }
                })
                .build());

        ANTICORROSION = id("anticorrosion");
        SmitheryAPI.registerModifier(Modifier.builder(ANTICORROSION)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.POISON,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .build());

        ANTIGRAV = id("antigrav");
        SmitheryAPI.registerModifier(Modifier.builder(ANTIGRAV)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0));
                })
                .build());

        AOA_THRESHOLD = id("aoa_threshold");
        SmitheryAPI.registerModifier(Modifier.builder(AOA_THRESHOLD)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (!(attacker instanceof Player player)) return;
                    float bonus = Math.min(0.25f, player.experienceLevel * 0.005f);
                    ctx.amount().set(ctx.amount().get() * (1.0f + bonus));
                })
                .build());

        APOCALYPSE = id("apocalypse");
        SmitheryAPI.registerModifier(Modifier.builder(APOCALYPSE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .build());

        AQUADYNAMIC = id("aquadynamic");
        SmitheryAPI.registerModifier(Modifier.builder(AQUADYNAMIC)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 4.0f))
                .build());

        AQUASPEED_ARMOR = id("aquaspeed_armor");
        SmitheryAPI.registerModifier(Modifier.builder(AQUASPEED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (!ctx.wearer().isUnderWater()) return;
                    if (ctx.wearer().tickCount % 3 == 0) {
                        ctx.wearer().setAirSupply(Math.min(ctx.wearer().getMaxAirSupply(),
                                ctx.wearer().getAirSupply() + 1));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ARCANE = id("arcane");
        SmitheryAPI.registerModifier(Modifier.builder(ARCANE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player() == null) return;
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() <= 0.05F && ctx.level().isNight()) {
                        // Repair tool 1-8 durability at night
                        var tool = ctx.player().getMainHandItem();
                        if (!tool.isEmpty()) tool.setDamageValue(Math.max(0, tool.getDamageValue() - (rng.nextInt(8) + 1)));
                    }
                })
                .onAttackEntity((effect, ctx) -> {
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() <= 0.05F && attacker.level().isNight()) {
                        var tool = ctx.tool();
                        if (tool != null && !tool.isEmpty()) tool.setDamageValue(Math.max(0, tool.getDamageValue() - (rng.nextInt(8) + 1)));
                    }
                })
                .build());

        ARIDICULOUS = id("aridiculous");
        SmitheryAPI.registerModifier(Modifier.builder(ARIDICULOUS)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f);
                    stats.bonusAttackDamage += effect.paramFloat("damage", 1.0f);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        ARIDICULOUS_ARMOR = id("aridiculous_armor");
        SmitheryAPI.registerModifier(Modifier.builder(ARIDICULOUS_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ASSASSINTRAIT = id("assassin_trait");
        SmitheryAPI.registerModifier(Modifier.builder(ASSASSINTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Assassin: 1.4x damage on backstab (attacker and target facing same direction)
                    // NOTE: onDealDamage ctx lacks attacker(); uses target.getLastAttacker() as proxy
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    net.minecraft.world.phys.Vec3 a = attacker.getLookAngle();
                    net.minecraft.world.phys.Vec3 t = target.getLookAngle();
                    if (a.dot(t) > 0.0) {
                        ctx.amount().set(dmg * 1.4F);
                    }
                })
                .build());

        AUTOFORGE_ARMOR = id("autoforge_armor");
        SmitheryAPI.registerModifier(Modifier.builder(AUTOFORGE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        AUTOREPAIR = id("autorepair");
        SmitheryAPI.registerModifier(Modifier.builder(AUTOREPAIR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        AUTOSMELT = id("autosmelt");
        SmitheryAPI.registerModifier(Modifier.builder(AUTOSMELT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    var level = ctx.level();
                    for (var drop : ctx.drops()) {
                        var container = new net.minecraft.world.SimpleContainer(drop.getItem());
                        level.getRecipeManager().getRecipeFor(
                                net.minecraft.world.item.crafting.RecipeType.SMELTING, container, level)
                                .ifPresent(recipe -> {
                                    var smelted = recipe.assemble(container, level.registryAccess()).copy();
                                    if (!smelted.isEmpty()) {
                                        smelted.setCount(smelted.getCount() * drop.getItem().getCount());
                                        drop.setItem(smelted);
                                    }
                                });
                    }
                })
                .build());

        BACONLICIOUS = id("baconlicious");
        SmitheryAPI.registerModifier(Modifier.builder(BACONLICIOUS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.target().level().getRandom().nextFloat() < 0.15f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.target().level(), ctx.target().getX(), ctx.target().getY(),
                                ctx.target().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_PORKCHOP));
                        ctx.target().level().addFreshEntity(drop);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        BACONLICIOUS_ARMOR = id("baconlicious_armor");
        SmitheryAPI.registerModifier(Modifier.builder(BACONLICIOUS_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.target().level().getRandom().nextFloat() < 0.15f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.target().level(), ctx.target().getX(), ctx.target().getY(),
                                ctx.target().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_PORKCHOP));
                        ctx.target().level().addFreshEntity(drop);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        BANE_OF_ARTHOPODS = id("bane_of_arthopods");
        SmitheryAPI.registerModifier(Modifier.builder(BANE_OF_ARTHOPODS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (ctx.target().getMobType() == MobType.ARTHROPOD) {
                        ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 5.0f));
                    }
                })
                .build());

        BANE_OF_NIGHT = id("bane_of_night");
        SmitheryAPI.registerModifier(Modifier.builder(BANE_OF_NIGHT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Bane of Night: +2.5% damage per light level below 7
                    // NOTE: uses target position for light since onDealDamage ctx lacks attacker
                    var attacker = target.getLastAttacker();
                    var entity = attacker != null ? attacker : target;
                    net.minecraft.core.BlockPos pos = entity.blockPosition();
                    int light = entity.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
                    int dark = Math.max(0, 7 - light);
                    ctx.amount().set(dmg * (1.0F + 0.025F * dark));
                })
                .build());

        BARRETT = id("barrett");
        SmitheryAPI.registerModifier(Modifier.builder(BARRETT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    var attacker = ctx.attacker();
                    float missing = 1.0f - attacker.getHealth() / attacker.getMaxHealth();
                    if (attacker.getRandom().nextFloat() < missing * 0.5f) {
                        ctx.amount().set(ctx.amount().get() * 1.5f);
                    }
                })
                .build());

        BEHEADING = id("beheading");
        SmitheryAPI.registerModifier(Modifier.builder(BEHEADING)
                .category(Modifier.ModifierCategory.ACTIVE)
                // 1.12: LevelAspect(10) and +10% head chance per level.
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .maxLevel(10)
                .onKill((effect, ctx) -> {
                    if (ctx.victim() == null) return;
                    float chance = effect.paramFloat("chance", 0.1f);
                    if (ctx.victim().getRandom().nextFloat() >= chance) return;
                    net.minecraft.world.item.ItemStack head = net.minecraft.world.item.ItemStack.EMPTY;
                    if (ctx.victim() instanceof net.minecraft.world.entity.monster.Skeleton)
                        head = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SKELETON_SKULL);
                    else if (ctx.victim() instanceof net.minecraft.world.entity.monster.WitherSkeleton)
                        head = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL);
                    else if (ctx.victim() instanceof net.minecraft.world.entity.monster.Zombie)
                        head = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ZOMBIE_HEAD);
                    else if (ctx.victim() instanceof net.minecraft.world.entity.monster.Creeper)
                        head = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CREEPER_HEAD);
                    if (!head.isEmpty()) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.victim().level(), ctx.victim().getX(), ctx.victim().getY(),
                                ctx.victim().getZ(), head);
                        ctx.victim().level().addFreshEntity(drop);
                    }
                })
                .build());

        BERSERK = id("berserk");
        SmitheryAPI.registerModifier(Modifier.builder(BERSERK)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    // Constant 2x boost to both damage and mining speed
                    stats.bonusAttackDamage *= 2.0F;
                    stats.bonusMiningSpeed *= 2.0F;
                })
                .build());

        BLAST_RESISTANT_ARMOR = id("blast_resistant_armor");
        SmitheryAPI.registerModifier(Modifier.builder(BLAST_RESISTANT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            if (!ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return;
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        BLASTING = id("blasting");
        SmitheryAPI.registerModifier(Modifier.builder(BLASTING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    net.minecraft.world.level.Level level = target.level();
                    if (level.isClientSide) return;
                    int modLevel = effect.paramInt("level", 1);
                    float chance = effect.paramFloat("chance_per_level", 0.08f) * modLevel;
                    if (level.getRandom().nextFloat() >= chance) return;
                    float radius = effect.paramFloat("radius", 2.0f);
                    level.explode(ctx.attacker(), target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                            radius, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                })
                .build());

        BLASTING_MINING = id("blasting_mining");
        // Behavior lives in SmitheryTraitEvents: mining any block at full speed needs Forge's
        // BreakSpeed event (no Modifier hook sees an ineffective block), and suppressing the drop
        // needs the break itself cancelled. Distinct id from BLASTING above, which is TConEvo's
        // explode-on-hit trait and unrelated despite the shared 1.12 word.
        SmitheryAPI.registerModifier(Modifier.builder(BLASTING_MINING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .maxLevel(3)
                .levelCost(3)
                .build());

        BLESSED_ARMOR = id("blessed_armor");
        SmitheryAPI.registerModifier(Modifier.builder(BLESSED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        BLIND = id("blind");
        SmitheryAPI.registerModifier(Modifier.builder(BLIND)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null || !(target instanceof LivingEntity living)) return;
                    var rng = ctx.target().level().getRandom();
                    boolean night = living.level().isNight();
                    boolean trigger = rng.nextFloat() <= 0.01F || (night && rng.nextFloat() <= 0.03F);
                    if (!trigger) return;
                    if (rng.nextBoolean()) {
                        living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, rng.nextInt(400) + 200));
                    } else {
                        living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, rng.nextInt(400) + 200));
                    }
                })
                .onBlockBreak((effect, ctx) -> {
                    var player = ctx.player();
                    if (player == null) return;
                    var rng = ctx.level().getRandom();
                    boolean night = ctx.level().isNight();
                    boolean trigger = rng.nextFloat() <= 0.01F || (night && rng.nextFloat() <= 0.03F);
                    if (!trigger) return;
                    if (rng.nextBoolean()) {
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, rng.nextInt(200) + 100));
                    } else {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, rng.nextInt(200) + 100));
                    }
                })
                .build());

        BLINDBANDIT = id("blindbandit");
        SmitheryAPI.registerModifier(Modifier.builder(BLINDBANDIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (ctx.attacker().level().getMaxLocalRawBrightness(ctx.attacker().blockPosition()) < 5) {
                        ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 3.0f));
                    }
                })
                .build());

        BLOODLUSTTRAIT = id("bloodlust_trait");
        SmitheryAPI.registerModifier(Modifier.builder(BLOODLUSTTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Bloodlust: bonus damage scales with target's missing HP fraction (up to +50% at 0 HP)
                    float ratio = target.getHealth() / target.getMaxHealth();
                    ctx.amount().set(dmg * (1.0F + (1.0F - ratio) * 0.5F));
                })
                .build());

        BODY = id("body");
        SmitheryAPI.registerModifier(Modifier.builder(BODY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.15f))
                .build());

        BOTANICAL = id("botanical");
        SmitheryAPI.registerModifier(Modifier.builder(BOTANICAL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var pos = ctx.pos();
                    var level = ctx.level();
                    for (var dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                        var adj = pos.relative(dir);
                        var state = level.getBlockState(adj);
                        if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bm
                                && bm.isValidBonemealTarget(level, adj, state, false)) {
                            bm.performBonemeal(level instanceof net.minecraft.server.level.ServerLevel sl ? sl : null,
                                    level.getRandom(), adj, state);
                        }
                    }
                })
                .build());

        BOUNCY_ARMOR = id("bouncy_armor");
        SmitheryAPI.registerModifier(Modifier.builder(BOUNCY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onFall((effect, ctx) -> {
                    float pct = effect.paramFloat("pct", 0.5f);
                    ctx.distance().set(ctx.distance().get() * (1.0 - Math.min(1.0f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        BREAKABLE = id("breakable");
        SmitheryAPI.registerModifier(Modifier.builder(BREAKABLE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f - effect.paramFloat("penalty", 0.2f))
                .build());

        BRIGHT = id("bright");
        SmitheryAPI.registerModifier(Modifier.builder(BRIGHT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var rng = ctx.target().level().getRandom();
                    boolean night = target.level().isNight();
                    float amount = ctx.amount().get();
                    if (night) {
                        ctx.amount().set(amount / (1.0F + rng.nextFloat() / 3.0F));
                    } else {
                        ctx.amount().set(amount * (1.0F + rng.nextFloat() / 2.0F));
                    }
                })
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player() == null) return;
                    if (ctx.level().getRandom().nextFloat() >= 0.9F) {
                        ctx.player().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200));
                    }
                })
                .build());

        BRITTLE = id("brittle");
        SmitheryAPI.registerModifier(Modifier.builder(BRITTLE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f - effect.paramFloat("penalty", 0.15f))
                .build());

        BROWNMAGIC = id("brownmagic");
        SmitheryAPI.registerModifier(Modifier.builder(BROWNMAGIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() >= 0.3f) return;
                    double x = target.getX() + (target.getRandom().nextDouble() - 0.5) * 16;
                    double y = target.getY() + (target.getRandom().nextInt(8) - 4);
                    double z = target.getZ() + (target.getRandom().nextDouble() - 0.5) * 16;
                    target.randomTeleport(x, y, z, true);
                })
                .build());

        CALCIC_ARMOR = id("calcic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CALCIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 100 == 0 && ctx.wearer().getHealth() < ctx.wearer().getMaxHealth()) {
                        ctx.wearer().heal(0.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CAMDAIBAY_ARMOR = id("camdaibay_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CAMDAIBAY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CASCADE = id("cascade");
        SmitheryAPI.registerModifier(Modifier.builder(CASCADE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() > 0.1F) return;
                    var player = ctx.player();
                    if (player == null) return;
                    var level = ctx.level();
                    var origin = ctx.pos();
                    var tool = player.getMainHandItem();
                    int max = tool.getMaxDamage();
                    int cur = max - tool.getDamageValue();
                    int cap = Math.min((int)(300.0F * (float)cur / Math.max(max, 1)), 50);
                    if (cap <= 0) return;
                    int count = rng.nextInt(cap);
                    net.minecraft.world.level.block.state.BlockState matchState = level.getBlockState(origin);
                    double[] cursor = {origin.getX(), origin.getY(), origin.getZ()};
                    double[] saved = cursor.clone();
                    for (int i = 0; i < count; i++) {
                        int axis = rng.nextInt(3);
                        int delta = rng.nextBoolean() ? 1 : -1;
                        double[] next = cursor.clone();
                        next[axis] += delta;
                        net.minecraft.core.BlockPos nPos = new net.minecraft.core.BlockPos((int)next[0], (int)next[1], (int)next[2]);
                        if (level.getBlockState(nPos) == matchState) {
                            level.destroyBlock(nPos, true, player);
                            cursor = next;
                            saved = cursor.clone();
                        } else {
                            cursor = saved.clone();
                        }
                    }
                })
                .build());

        CATCHER = id("catcher");
        SmitheryAPI.registerModifier(Modifier.builder(CATCHER)
                .category(Modifier.ModifierCategory.ACTIVE)
                /* STUB: Capture-and-release mechanic from 1.12 is not portable.
                 * The original serialized entity class via reflection into tool NBT,
                 * which is unsafe with 1.20 data-driven entities.
                 * Trait registered for material JSON compatibility only. */
                .build());

        CHADTHUNDER = id("chadthunder");
        SmitheryAPI.registerModifier(Modifier.builder(CHADTHUNDER)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.target().level().getRandom().nextFloat() < 0.1f) {
                        var lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(target.level());
                        if (lightning != null) {
                            lightning.moveTo(target.getX(), target.getY(), target.getZ());
                            target.level().addFreshEntity(lightning);
                        }
                    }
                })
                .build());

        CHEAP = id("cheap");
        SmitheryAPI.registerModifier(Modifier.builder(CHEAP)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        CHEAP_ARMOR = id("cheap_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CHEAP_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CHEAPSKATE = id("cheapskate");
        SmitheryAPI.registerModifier(Modifier.builder(CHEAPSKATE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f - effect.paramFloat("penalty", 0.1f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        CHEAPSKATE_ARMOR = id("cheapskate_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CHEAPSKATE_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f - effect.paramFloat("penalty", 0.1f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CHUNKY = id("chunky");
        SmitheryAPI.registerModifier(Modifier.builder(CHUNKY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.25f))
                .build());

        COLDBLOODED = id("coldblooded");
        SmitheryAPI.registerModifier(Modifier.builder(COLDBLOODED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                })
                .build());

        COMBUSTIBLE_ARMOR = id("combustible_armor");
        SmitheryAPI.registerModifier(Modifier.builder(COMBUSTIBLE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof LivingEntity attacker && attacker != ctx.wearer()) {
                        attacker.setSecondsOnFire(3);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CONCEALED_ARMOR = id("concealed_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CONCEALED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0 && ctx.wearer().isCrouching()) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CONGENIAL = id("congenial");
        SmitheryAPI.registerModifier(Modifier.builder(CONGENIAL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    var target = ctx.target();
                    LivingEntity living = target;
                    var tool = ctx.tool();
                    if (tool == null || tool.isEmpty()) return;
                    net.minecraft.nbt.CompoundTag tag = tool.getOrCreateTag();
                    String bound = tag.getString("smithery.congenial.name");
                    if (bound.isEmpty()) return;
                    String current = living.getName().getString();
                    float amount = ctx.amount().get();
                    var rng = ctx.target().level().getRandom();
                    if (bound.equals(current)) {
                        ctx.amount().set(amount * (1.0F + rng.nextFloat() * 9.0F));
                    } else {
                        ctx.amount().set(amount / (rng.nextInt(5) + 5));
                    }
                })
                .onKill((effect, ctx) -> {
                    // Bind tool to first killed mob's display name
                    var killed = ctx.victim();
                    if (killed == null || !(killed instanceof net.minecraft.world.entity.PathfinderMob)) return;
                    // Note: requires tool access from kill context to store NBT.
                    // if tag "smithery.congenial.name" is empty:
                    //   tag.putString("smithery.congenial.name", killed.getName().getString());
                })
                .build());

        CORALIUMPLAGUE = id("coraliumplague");
        SmitheryAPI.registerModifier(Modifier.builder(CORALIUMPLAGUE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                })
                .build());

        COTLIFESTEAL = id("cotlifesteal");
        SmitheryAPI.registerModifier(Modifier.builder(COTLIFESTEAL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    var target = ctx.target();
                    if (target == null) return;
                    // Cotlifesteal: 33% chance to heal damage*5% (cap 5) on non-undead mob hit
                    if (((target instanceof LivingEntity _le) ? _le.getMobType() : MobType.UNDEFINED) == net.minecraft.world.entity.MobType.UNDEAD) return;
                    if (ctx.attacker().getRandom().nextFloat() >= 0.33F) return;
                    float heal = Math.min(ctx.damageDealt() * 0.05F, 5.0F);
                    ctx.attacker().heal(heal);
                })
                .build());

        COTLIFESTEALTRAIT = id("cotlifesteal_trait");
        SmitheryAPI.registerModifier(Modifier.builder(COTLIFESTEALTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    var target = ctx.target();
                    if (target == null) return;
                    // Cotlifesteal: 33% chance to heal damage*5% (cap 5) on non-undead mob hit
                    if (((target instanceof LivingEntity _le) ? _le.getMobType() : MobType.UNDEFINED) == net.minecraft.world.entity.MobType.UNDEAD) return;
                    if (ctx.attacker().getRandom().nextFloat() >= 0.33F) return;
                    float heal = Math.min(ctx.damageDealt() * 0.05F, 5.0F);
                    ctx.attacker().heal(heal);
                })
                .build());

        CRUDE = id("crude");
        SmitheryAPI.registerModifier(Modifier.builder(CRUDE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.5f))
                .build());

        CRUMBLING = id("crumbling");
        SmitheryAPI.registerModifier(Modifier.builder(CRUMBLING)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f))
                .build());

        CRUSHING = id("crushing");
        SmitheryAPI.registerModifier(Modifier.builder(CRUSHING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    boolean mineStone = ctx.drops().stream()
                        .anyMatch(s -> s.getItem().is(net.minecraft.world.item.Items.COBBLESTONE) || s.getItem().is(net.minecraft.world.item.Items.STONE));
                    if (!mineStone) return;
                    ctx.drops().clear();
                    var rng = ctx.level().getRandom();
                    float f = rng.nextFloat();
                    if (f < 0.3F) ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.level(), ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.SAND)));
                    else if (f < 0.6F) ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.level(), ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.GRAVEL)));
                    else if (f <= 0.9F) ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.level(), ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.COBBLESTONE)));
                    else ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.level(), ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.level.block.Blocks.STONE)));
                })
                .build());

        CRYONICTRAIT = id("cryonic_trait");
        SmitheryAPI.registerModifier(Modifier.builder(CRYONICTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    // Cryonic: 20% chance to slow the attacker (Slowness II, 10s) on incoming hit
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
                    if (ctx.wearer().getRandom().nextFloat() >= 0.2F) return;
                    attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, false));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        CRYONICTRAIT_ARMOR = id("cryonic_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CRYONICTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    // Cryonic: 20% chance to slow the attacker (Slowness II, 10s) on incoming hit
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
                    if (ctx.wearer().getRandom().nextFloat() >= 0.2F) return;
                    attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, false));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CRYSTALTRAIT = id("crystal_trait");
        SmitheryAPI.registerModifier(Modifier.builder(CRYSTALTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Crystal Force (tool): damage = base * (0.8 + durabilityFrac * 0.5)
                    // High durability = more damage
                    // NOTE: smithery onDealDamage ctx has tool(); need to read durability from it
                    var tool = ctx.tool();
                    int max = tool.getMaxDamage();
                    if (max <= 0) return;
                    float frac = 1.0F - (float) tool.getDamageValue() / (float) max;
                    ctx.amount().set(dmg * (0.8F + frac * 0.5F));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Crystal Force (armor): incoming = base * (1.05 - durabilityFrac * 0.12)
                    // High durability = less incoming damage
                    var armor = ctx.armor();
                    if (armor == null) return;
                    int max = armor.getMaxDamage();
                    if (max <= 0) return;
                    float frac = 1.0F - (float) armor.getDamageValue() / (float) max;
                    ctx.amount().set(dmg * (1.05F - frac * 0.12F));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        CRYSTALTRAIT_ARMOR = id("crystal_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(CRYSTALTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Crystal Force (tool): damage = base * (0.8 + durabilityFrac * 0.5)
                    // High durability = more damage
                    // NOTE: smithery onDealDamage ctx has tool(); need to read durability from it
                    var tool = ctx.tool();
                    int max = tool.getMaxDamage();
                    if (max <= 0) return;
                    float frac = 1.0F - (float) tool.getDamageValue() / (float) max;
                    ctx.amount().set(dmg * (0.8F + frac * 0.5F));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Crystal Force (armor): incoming = base * (1.05 - durabilityFrac * 0.12)
                    // High durability = less incoming damage
                    var armor = ctx.armor();
                    if (armor == null) return;
                    int max = armor.getMaxDamage();
                    if (max <= 0) return;
                    float frac = 1.0F - (float) armor.getDamageValue() / (float) max;
                    ctx.amount().set(dmg * (1.05F - frac * 0.12F));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        CURSED = id("cursed");
        SmitheryAPI.registerModifier(Modifier.builder(CURSED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    var wearer = ctx.wearer();
                    if (wearer == null || wearer.level().isClientSide()) return;
                    var rng = ctx.wearer().getRandom();
                    // Accumulating curse counter stored in tool NBT
                    var armor = ctx.armor();
                    net.minecraft.nbt.CompoundTag tag = armor.getOrCreateTag();
                    int curse = tag.getInt("smithery.cursed.curse");
                    if (rng.nextInt((60000 + curse) / (curse + 1)) == 1) {
                        curse += 10; // +10 when selected/worn
                        float dmg = rng.nextFloat() * wearer.getMaxHealth() / 2.0F;
                        wearer.hurt(wearer.damageSources().generic(), dmg);
                        tag.putInt("smithery.cursed.curse", curse);
                    }
                })
                .build());

        CURVATURE = id("curvature");
        SmitheryAPI.registerModifier(Modifier.builder(CURVATURE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() > 0.15F) return;
                    var target = ctx.target();
                    var attacker = ctx.attacker();
                    if (target == null || attacker == null) return;
                    if (!(target instanceof LivingEntity livingTarget)) return;
                    LivingEntity livingAttacker = attacker;
                    net.minecraft.core.BlockPos ap = livingAttacker.blockPosition();
                    net.minecraft.core.BlockPos tp = livingTarget.blockPosition();
                    livingAttacker.teleportTo(tp.getX() + 0.5, tp.getY(), tp.getZ() + 0.5);
                    livingTarget.teleportTo(ap.getX() + 0.5, ap.getY(), ap.getZ() + 0.5);
                })
                .onMobDrops((effect, ctx) -> {
                    // Bonus ender pearl drop from monster kills
                    var rng = ctx.victim().level().getRandom();
                    ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.victim().level(), ctx.victim().getX(), ctx.victim().getY(), ctx.victim().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL, Math.max(1, rng.nextInt(2)))));
                })
                .build());

        CUSHY = id("cushy");
        SmitheryAPI.registerModifier(Modifier.builder(CUSHY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.source().is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
                        ctx.amount().set(0.0F);
                    }
                })
                .build());

        DARK = id("dark");
        SmitheryAPI.registerModifier(Modifier.builder(DARK)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var rng = ctx.target().level().getRandom();
                    boolean night = target.level().isNight();
                    float amount = ctx.amount().get();
                    if (night) {
                        ctx.amount().set(amount * (1.0F + rng.nextFloat() / 2.0F));
                    } else {
                        ctx.amount().set(amount / (1.0F + rng.nextFloat() / 3.0F));
                    }
                })
                .build());

        DARKTRAVELER = id("darktraveler");
        SmitheryAPI.registerModifier(Modifier.builder(DARKTRAVELER)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 == 0 && !ctx.wearer().level().isDay()) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 260, 0, true, false));
                    }
                })
                .build());

        DECAY = id("decay");
        SmitheryAPI.registerModifier(Modifier.builder(DECAY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    var wearer = ctx.wearer();
                    if (wearer == null || wearer.level().isClientSide()) return;
                    // Drain 1 durability every 24 ticks (passive degradation)
                    if (wearer.tickCount % 24 != 0) return;
                    var armor = ctx.armor();
                    if (armor.isEmpty() || armor.getDamageValue() >= armor.getMaxDamage()) return;
                    armor.setDamageValue(armor.getDamageValue() + 1);
                })
                .build());

        DEFILED = id("defiled");
        SmitheryAPI.registerModifier(Modifier.builder(DEFILED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                })
                .build());

        DENSE = id("dense");
        SmitheryAPI.registerModifier(Modifier.builder(DENSE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        DENSE_ARMOR = id("dense_armor");
        SmitheryAPI.registerModifier(Modifier.builder(DENSE_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        DEPTHDIGGER = id("depthdigger");
        SmitheryAPI.registerModifier(Modifier.builder(DEPTHDIGGER)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f))
                .build());

        DEVILSSTRENGTH = id("devilsstrength");
        SmitheryAPI.registerModifier(Modifier.builder(DEVILSSTRENGTH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 3.0f));
                })
                .build());

        DEXTEROUS_ARMOR = id("dexterous_armor");
        SmitheryAPI.registerModifier(Modifier.builder(DEXTEROUS_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        DIAMOND = id("diamond");
        SmitheryAPI.registerModifier(Modifier.builder(DIAMOND)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.5f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        DIAMOND_ARMOR = id("diamond_armor");
        SmitheryAPI.registerModifier(Modifier.builder(DIAMOND_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.5f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        DIFFUSE = id("diffuse");
        SmitheryAPI.registerModifier(Modifier.builder(DIFFUSE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.level().getRandom().nextFloat() < 0.35F) ctx.drops().clear();
                })
                .build());

        DISSOLVING = id("dissolving");
        SmitheryAPI.registerModifier(Modifier.builder(DISSOLVING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onKill((effect, ctx) -> {
                    var rng = ctx.victim().level().getRandom();
                    if (rng.nextFloat() <= 0.8F) {
                        ctx.setXp(0);
                    } else {
                        ctx.setXp(ctx.xp() * (rng.nextInt(3) + 2));
                    }
                })
                .build());

        DIVINESHIELD = id("divineshield");
        SmitheryAPI.registerModifier(Modifier.builder(DIVINESHIELD)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            if (!ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return;
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .build());

        DPRK = id("dprk");
        SmitheryAPI.registerModifier(Modifier.builder(DPRK)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() < 0.08f) {
                        target.level().explode(null, target.getX(), target.getY(), target.getZ(),
                                1.5f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                    }
                })
                .build());

        DRAMATIC_ARMOR = id("dramatic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(DRAMATIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.wearer().getHealth() / ctx.wearer().getMaxHealth() < 0.3f) {
                        ctx.amount().set(ctx.amount().get() * 0.7f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        DREADPLAGUE = id("dreadplague");
        SmitheryAPI.registerModifier(Modifier.builder(DREADPLAGUE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                })
                .build());

        DREADPURITY = id("dreadpurity");
        SmitheryAPI.registerModifier(Modifier.builder(DREADPURITY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (ctx.target().isInvertedHealAndHarm()) {
                        ctx.amount().set(ctx.amount().get() + 4.0f);
                    }
                })
                .build());

        DUNANSTRANSPORT_ARMOR = id("dunanstransport_armor");
        SmitheryAPI.registerModifier(Modifier.builder(DUNANSTRANSPORT_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("dunanstransport_armor", () -> Attributes.ARMOR_TOUGHNESS, "amount", 2.0f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        // Tool-side only: GreedyCraft pairs this with duritos_ranch_armor on core/plates/trim,
        // so leaving it unscoped would double it up on armour.
        DURITOS = id("duritos");
        SmitheryAPI.registerModifier(Modifier.builder(DURITOS)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        DURITOS_RANCH_ARMOR = id("duritos_ranch_armor");
        SmitheryAPI.registerModifier(Modifier.builder(DURITOS_RANCH_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ECOLOGICAL = id("ecological");
        SmitheryAPI.registerModifier(Modifier.builder(ECOLOGICAL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        ECOLOGICAL_ARMOR = id("ecological_armor");
        SmitheryAPI.registerModifier(Modifier.builder(ECOLOGICAL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ELEMENTAL = id("elemental");
        SmitheryAPI.registerModifier(Modifier.builder(ELEMENTAL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    int roll = target.level().getRandom().nextInt(3);
                    if (roll == 0) target.setSecondsOnFire(3);
                    else if (roll == 1) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                    else target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                })
                .build());

        EMERALD = id("emerald");
        SmitheryAPI.registerModifier(Modifier.builder(EMERALD)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.5f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        EMERALD_ARMOR = id("emerald_armor");
        SmitheryAPI.registerModifier(Modifier.builder(EMERALD_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.5f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ENDERFERENCE = id("enderference");
        SmitheryAPI.registerModifier(Modifier.builder(ENDERFERENCE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() >= 0.3f) return;
                    double x = target.getX() + (target.getRandom().nextDouble() - 0.5) * 16;
                    double y = target.getY() + (target.getRandom().nextInt(8) - 4);
                    double z = target.getZ() + (target.getRandom().nextDouble() - 0.5) * 16;
                    target.randomTeleport(x, y, z, true);
                })
                .build());

        ENDERPICKUP = id("enderpickup");
        SmitheryAPI.registerModifier(Modifier.builder(ENDERPICKUP)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() >= 0.3f) return;
                    double x = target.getX() + (target.getRandom().nextDouble() - 0.5) * 16;
                    double y = target.getY() + (target.getRandom().nextInt(8) - 4);
                    double z = target.getZ() + (target.getRandom().nextDouble() - 0.5) * 16;
                    target.randomTeleport(x, y, z, true);
                })
                .build());

        ENDERPORT = id("enderport");
        SmitheryAPI.registerModifier(Modifier.builder(ENDERPORT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() >= 0.3f) return;
                    double x = target.getX() + (target.getRandom().nextDouble() - 0.5) * 16;
                    double y = target.getY() + (target.getRandom().nextInt(8) - 4);
                    double z = target.getZ() + (target.getRandom().nextDouble() - 0.5) * 16;
                    target.randomTeleport(x, y, z, true);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        ENDERPORT_ARMOR = id("enderport_armor");
        SmitheryAPI.registerModifier(Modifier.builder(ENDERPORT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() >= 0.3f) return;
                    double x = target.getX() + (target.getRandom().nextDouble() - 0.5) * 16;
                    double y = target.getY() + (target.getRandom().nextInt(8) - 4);
                    double z = target.getZ() + (target.getRandom().nextDouble() - 0.5) * 16;
                    target.randomTeleport(x, y, z, true);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ENDSPEED = id("endspeed");
        SmitheryAPI.registerModifier(Modifier.builder(ENDSPEED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 3.0f))
                .build());

        ENDURANCETRAIT = id("endurance_trait");
        SmitheryAPI.registerModifier(Modifier.builder(ENDURANCETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    float dmg = ctx.amount().get();
                    // Endurance: 20% reduction when incoming damage is below 5% of max HP
                    if (dmg < entity.getMaxHealth() * 0.05F) {
                        ctx.amount().set(dmg * 0.8F);
                    }
                })
                .build());

        ENDURANCE_ARMOR = id("endurance_armor");
        SmitheryAPI.registerModifier(Modifier.builder(ENDURANCE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    float dmg = ctx.amount().get();
                    // Endurance: 20% reduction when incoming damage is below 5% of max HP
                    if (dmg < entity.getMaxHealth() * 0.05F) {
                        ctx.amount().set(dmg * 0.8F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        ENLIGHTENED = id("enlightened");
        SmitheryAPI.registerModifier(Modifier.builder(ENLIGHTENED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.level().getRandom().nextFloat() >= 0.05f) return;
                    var ambrosium = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("aether", "ambrosium_shard"));
                    if (ambrosium == null || ambrosium == net.minecraft.world.item.Items.AIR) return;
                    var stack = new net.minecraft.world.item.ItemStack(ambrosium);
                    if (!stack.isEmpty() && !ctx.drops().isEmpty()) {
                        var ref = ctx.drops().get(0);
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.level(), ref.getX(), ref.getY(), ref.getZ(), stack);
                        ctx.drops().add(drop);
                    }
                })
                .build());

        ESTABLISHED = id("established");
        SmitheryAPI.registerModifier(Modifier.builder(ESTABLISHED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    stats.bonusAttackDamage += effect.paramFloat("damage", 1.0f);
                    stats.durabilityMultiplier *= 1.0f + effect.paramFloat("durability", 0.1f);
                })
                .build());

        EXECUTIONERTRAIT = id("executioner_trait");
        SmitheryAPI.registerModifier(Modifier.builder(EXECUTIONERTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null) return;
                    var target = ctx.target();
                    if (target == null) return;
                    if (ctx.damageDealt() < 0.1F) return;
                    // Executioner: instakill via OUT_OF_WORLD on targets below 20% HP (10% if boss-scale)
                    // "boss" = anything with maxHealth >= 100 (rough proxy)
                    float threshold = ((LivingEntity) target).getMaxHealth() >= 100.0F ? 0.1F : 0.2F;
                    if ((((LivingEntity) target).getHealth() / ((LivingEntity) target).getMaxHealth()) >= threshold) return;
                    net.minecraft.world.damagesource.DamageSource src = ((LivingEntity) target).damageSources().fellOutOfWorld();
                    target.hurt(src, Float.MAX_VALUE);
                })
                .build());

        EXPLOSIVE = id("explosive");
        SmitheryAPI.registerModifier(Modifier.builder(EXPLOSIVE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.target().level().getRandom().nextFloat() < 0.15f) {
                        ctx.target().level().explode(null, ctx.target().getX(), ctx.target().getY(),
                                ctx.target().getZ(), 1.5f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                    }
                })
                .build());

        EXTRATRAIT = id("extratrait");
        SmitheryAPI.registerModifier(Modifier.builder(EXTRATRAIT)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        EXTRATRAIT_ARMOR = id("extratrait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(EXTRATRAIT_ARMOR)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        FEATHERWEIGHT_ARMOR = id("featherweight_armor");
        SmitheryAPI.registerModifier(Modifier.builder(FEATHERWEIGHT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onFall((effect, ctx) -> {
                    float pct = effect.paramFloat("pct", 0.5f);
                    ctx.distance().set(ctx.distance().get() * (1.0 - Math.min(1.0f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        FESTIVE = id("festive");
        SmitheryAPI.registerModifier(Modifier.builder(FESTIVE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (ctx.target() instanceof net.minecraft.world.entity.monster.Monster) {
                        ctx.amount().set(ctx.amount().get() * 1.5F);
                    }
                })
                .build());

        FIERY = id("fiery");
        SmitheryAPI.registerModifier(Modifier.builder(FIERY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .build());

        FINS = id("fins");
        // Tinkers 1.12 Fins: "attaching fins to the projectiles makes them travel like normal
        // underwater" - it cancels water drag on what the weapon fires. It was stubbed here as a
        // mining-speed passive, which is a different modifier entirely. Behavior now lives in
        // FinsEvents; this registration is the marker the launcher carries.
        SmitheryAPI.registerModifier(Modifier.builder(FINS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .maxLevel(1)
                .levelCost(2)
                .build());

        FIRE_RESISTANT_ARMOR = id("fire_resistant_armor");
        SmitheryAPI.registerModifier(Modifier.builder(FIRE_RESISTANT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            if (!ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return;
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        FIRSTGUARDTRAIT = id("first_guard_trait");
        SmitheryAPI.registerModifier(Modifier.builder(FIRSTGUARDTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        FIRSTGUARDTRAIT_ARMOR = id("first_guard_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(FIRSTGUARDTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        FLAMMABLE = id("flammable");
        SmitheryAPI.registerModifier(Modifier.builder(FLAMMABLE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .build());

        FORTIFIEDTRAIT = id("fortified_trait");
        SmitheryAPI.registerModifier(Modifier.builder(FORTIFIEDTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    // Fortified (tool): Resistance II for 5s after every hit
                    ctx.attacker().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 99, 1, false, false));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Fortified (armor): -15% projectile damage
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
                        ctx.amount().set(dmg * 0.85F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        FORTIFIEDTRAIT_ARMOR = id("fortified_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(FORTIFIEDTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    // Fortified (tool): Resistance II for 5s after every hit
                    ctx.attacker().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 99, 1, false, false));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Fortified (armor): -15% projectile damage
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
                        ctx.amount().set(dmg * 0.85F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        // FORTIFY is defined by data/soa_additions/smithery/modifier/fortify.json
        // (GC's harvest-level lift, applied with flint). Registering it here too made
        // SimpleRegistry throw "Modifier already contains soa_additions:fortify" during
        // the datapack reload, which aborted every world load.
        FORTIFY = id("fortify");

        FRACTURE = id("fracture");
        SmitheryAPI.registerModifier(Modifier.builder(FRACTURE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var player = ctx.player();
                    if (player == null) return;
                    var rng = ctx.level().getRandom();
                    var tool = player.getMainHandItem();
                    int max = tool.getMaxDamage();
                    int cur = max - tool.getDamageValue();
                    if (max <= 50) return;
                    float bonus = 0.99F * (0.4F / (max - 50) * cur + 0.55F);
                    if (rng.nextFloat() > bonus) return;
                    var hit = player.pick(5.0D, 0.0F, false);
                    if (!(hit instanceof net.minecraft.world.phys.BlockHitResult bhit)) return;
                    net.minecraft.core.Direction dir = bhit.getDirection();
                    var level = ctx.level();
                    var origin = ctx.pos();
                    int length = rng.nextInt(9) + 1;
                    for (int i = 1; i <= length; i++) {
                        net.minecraft.core.BlockPos next = origin.relative(dir.getOpposite(), i);
                        net.minecraft.world.level.block.state.BlockState ns = level.getBlockState(next);
                        if (ns.is(net.minecraft.world.level.block.Blocks.BEDROCK) || ns.isAir()) continue;
                        if (!tool.isCorrectToolForDrops(ns)) continue;
                        level.destroyBlock(next, true, player);
                    }
                })
                .build());

        FRACTURED = id("fractured");
        SmitheryAPI.registerModifier(Modifier.builder(FRACTURED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f - effect.paramFloat("penalty", 0.1f))
                .build());

        FRAGILE = id("fragile");
        SmitheryAPI.registerModifier(Modifier.builder(FRAGILE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var player = ctx.player();
                    if (player == null) return;
                    var rng = ctx.level().getRandom();
                    var tool = player.getMainHandItem();
                    int max = tool.getMaxDamage();
                    int cur = max - tool.getDamageValue();
                    if (max <= 50) return;
                    float bonus = 0.99F * (0.4F / (max - 50) * cur + 0.55F);
                    if (rng.nextFloat() > bonus) return;
                    if (rng.nextBoolean()) {
                        // Random extra damage to tool
                        tool.setDamageValue(tool.getDamageValue() + rng.nextInt(3));
                    } else {
                        // Random repair
                        tool.setDamageValue(Math.max(0, tool.getDamageValue() - rng.nextInt(3)));
                    }
                })
                .build());

        FREEZING = id("freezing");
        SmitheryAPI.registerModifier(Modifier.builder(FREEZING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .build());

        FROSTWALKER_ARMOR = id("frostwalker_armor");
        SmitheryAPI.registerModifier(Modifier.builder(FROSTWALKER_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var level = ctx.level();
                    for (var dir : net.minecraft.core.Direction.values()) {
                        var adj = ctx.pos().relative(dir);
                        if (level.getBlockState(adj).is(net.minecraft.world.level.block.Blocks.WATER)) {
                            level.setBlockAndUpdate(adj, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        FRUITSALAD = id("fruitsalad");
        SmitheryAPI.registerModifier(Modifier.builder(FRUITSALAD)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.level().getRandom().nextFloat() < 0.05f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.level(), ctx.drops().isEmpty() ? 0 : ctx.drops().get(0).getX(),
                                ctx.drops().isEmpty() ? 0 : ctx.drops().get(0).getY(),
                                ctx.drops().isEmpty() ? 0 : ctx.drops().get(0).getZ(),
                                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));
                        ctx.level().addFreshEntity(drop);
                    }
                })
                .build());

        GAMBLE = id("gamble");
        SmitheryAPI.registerModifier(Modifier.builder(GAMBLE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Gamble (tool): 45% chance for 2x outgoing damage
                    if (attacker != null && attacker.getRandom().nextFloat() < 0.45F) {
                        ctx.amount().set(dmg * 2.0F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var rng = ctx.wearer().getRandom();
                    // Gamble (armor): 5% chance for 2x incoming damage; else 25% chance for 0.5x incoming
                    if (rng.nextFloat() < 0.05F) {
                        ctx.amount().set(dmg * 2.0F);
                    } else if (rng.nextFloat() < 0.25F) {
                        ctx.amount().set(dmg * 0.5F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        GAMBLETRAIT = id("gamble_trait");
        SmitheryAPI.registerModifier(Modifier.builder(GAMBLETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Gamble (tool): 45% chance for 2x outgoing damage
                    if (attacker != null && attacker.getRandom().nextFloat() < 0.45F) {
                        ctx.amount().set(dmg * 2.0F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var rng = ctx.wearer().getRandom();
                    // Gamble (armor): 5% chance for 2x incoming damage; else 25% chance for 0.5x incoming
                    if (rng.nextFloat() < 0.05F) {
                        ctx.amount().set(dmg * 2.0F);
                    } else if (rng.nextFloat() < 0.25F) {
                        ctx.amount().set(dmg * 0.5F);
                    }
                })
                .build());

        GAMBLE_ARMOR = id("gamble_armor");
        SmitheryAPI.registerModifier(Modifier.builder(GAMBLE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Gamble (tool): 45% chance for 2x outgoing damage
                    if (attacker != null && attacker.getRandom().nextFloat() < 0.45F) {
                        ctx.amount().set(dmg * 2.0F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var rng = ctx.wearer().getRandom();
                    // Gamble (armor): 5% chance for 2x incoming damage; else 25% chance for 0.5x incoming
                    if (rng.nextFloat() < 0.05F) {
                        ctx.amount().set(dmg * 2.0F);
                    } else if (rng.nextFloat() < 0.25F) {
                        ctx.amount().set(dmg * 0.5F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        GARISHLY = id("garishly");
        SmitheryAPI.registerModifier(Modifier.builder(GARISHLY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.level().getRandom().nextBoolean()) ctx.drops().clear();
                })
                .onMobDrops((effect, ctx) -> {
                    var rng = ctx.victim().level().getRandom();
                    int r = rng.nextInt(3);
                    net.minecraft.world.item.ItemStack bonus = switch (r) {
                        case 0 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BLAZE_POWDER, Math.max(1, rng.nextInt(3)));
                        case 1 -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BLAZE_ROD, Math.max(1, rng.nextInt(3)));
                        default -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, Math.max(1, rng.nextInt(3)));
                    };
                    ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.victim().level(), ctx.victim().getX(), ctx.victim().getY(), ctx.victim().getZ(), bonus));
                })
                .build());

        GETLUCKY = id("getlucky");
        SmitheryAPI.registerModifier(Modifier.builder(GETLUCKY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() >= 0.3f) return;
                    double x = target.getX() + (target.getRandom().nextDouble() - 0.5) * 16;
                    double y = target.getY() + (target.getRandom().nextInt(8) - 4);
                    double z = target.getZ() + (target.getRandom().nextDouble() - 0.5) * 16;
                    target.randomTeleport(x, y, z, true);
                })
                .build());

        GHASTLY = id("ghastly");
        SmitheryAPI.registerModifier(Modifier.builder(GHASTLY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .build());

        GIANTSLAYER = id("giantslayer");
        SmitheryAPI.registerModifier(Modifier.builder(GIANTSLAYER)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Giantslayer: bonus damage when target HP > attacker maxHP. Multiplier in [1.0, 2.5].
                    // NOTE: onDealDamage ctx lacks attacker(); uses target.getLastAttacker() as proxy
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    float mult = 0.05F * (target.getHealth() / attacker.getMaxHealth());
                    if (mult < 1.0F) mult = 1.0F;
                    if (mult > 2.5F) mult = 2.5F;
                    ctx.amount().set(dmg * mult);
                })
                .build());

        GIANTSLAYERTRAIT = id("giantslayer_trait");
        SmitheryAPI.registerModifier(Modifier.builder(GIANTSLAYERTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Giantslayer: bonus damage when target HP > attacker maxHP. Multiplier in [1.0, 2.5].
                    // NOTE: onDealDamage ctx lacks attacker(); uses target.getLastAttacker() as proxy
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    float mult = 0.05F * (target.getHealth() / attacker.getMaxHealth());
                    if (mult < 1.0F) mult = 1.0F;
                    if (mult > 2.5F) mult = 2.5F;
                    ctx.amount().set(dmg * mult);
                })
                .build());

        GILDED = id("gilded");
        SmitheryAPI.registerModifier(Modifier.builder(GILDED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.25f)));
                })
                .build());

        GLIMMER = id("glimmer");
        SmitheryAPI.registerModifier(Modifier.builder(GLIMMER)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    var wearer = ctx.wearer();
                    if (wearer == null || wearer.level().isClientSide()) return;
                    if (!(wearer instanceof Player)) return;
                    // Refresh night vision every second (100t duration > 20t refresh)
                    if (wearer.tickCount % 20 != 0) return;
                    wearer.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, true, false));
                })
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player() == null) return;
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() <= 0.05F) {
                        ctx.player().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, rng.nextInt(600) + 300));
                    }
                })
                .onAttackEntity((effect, ctx) -> {
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() <= 0.05F) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, rng.nextInt(600) + 300));
                    }
                })
                .build());

        GLOBAL = id("global");
        SmitheryAPI.registerModifier(Modifier.builder(GLOBAL)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 1.5f))
                .build());

        GLOWING = id("glowing");
        SmitheryAPI.registerModifier(Modifier.builder(GLOWING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        GLOWING_ARMOR = id("glowing_armor");
        SmitheryAPI.registerModifier(Modifier.builder(GLOWING_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof LivingEntity attacker && attacker != ctx.wearer()) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        GOODFRIDAYAGREEMENT_ARMOR = id("goodfridayagreement_armor");
        SmitheryAPI.registerModifier(Modifier.builder(GOODFRIDAYAGREEMENT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
                        ctx.amount().set(ctx.amount().get() * 0.7f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        HAILHYDRA = id("hailhydra");
        SmitheryAPI.registerModifier(Modifier.builder(HAILHYDRA)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.5f)));
                })
                .build());

        HALLOWEENTRAIT = id("halloween_trait");
        SmitheryAPI.registerModifier(Modifier.builder(HALLOWEENTRAIT)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (!(ctx.attacker() instanceof Player player)) return;
                    if (player.getRandom().nextFloat() >= 0.01f) return;
                    var candyBag = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("extrabotany:candybag"));
                    if (candyBag != null && candyBag != Items.AIR) {
                        player.addItem(new net.minecraft.world.item.ItemStack(candyBag));
                    }
                })
                .build());

        HAORANS_CULT_ARMOR = id("haorans_cult_armor");
        SmitheryAPI.registerModifier(Modifier.builder(HAORANS_CULT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        HARVESTHEIGHT = id("harvestheight");
        SmitheryAPI.registerModifier(Modifier.builder(HARVESTHEIGHT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.5f)));
                })
                .build());

        HARVESTWIDTH = id("harvestwidth");
        SmitheryAPI.registerModifier(Modifier.builder(HARVESTWIDTH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.5f)));
                })
                .build());

        HASTE = id("haste");
        SmitheryAPI.registerModifier(Modifier.builder(HASTE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f))
                .build());

        HEARTS = id("hearts");
        SmitheryAPI.registerModifier(Modifier.builder(HEARTS)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        HEAVY = id("heavy");
        SmitheryAPI.registerModifier(Modifier.builder(HEAVY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.0f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        HEAVY_ARMOR = id("heavy_armor");
        SmitheryAPI.registerModifier(Modifier.builder(HEAVY_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("heavy_armor", () -> Attributes.KNOCKBACK_RESISTANCE, "amount", 0.1f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        HEAVY_METAL = id("heavy_metal");
        SmitheryAPI.registerModifier(Modifier.builder(HEAVY_METAL)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        HELLISH = id("hellish");
        SmitheryAPI.registerModifier(Modifier.builder(HELLISH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 4));
                })
                .build());

        HEROIC = id("heroic");
        SmitheryAPI.registerModifier(Modifier.builder(HEROIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (tool == null) return;
                    int dur = tool.getDamageValue();
                    int max = tool.getMaxDamage();
                    if (max <= 0) return;
                    int remaining = max - dur;
                    boolean lowDur = remaining < (int)(0.1F * max);
                    var attacker = ctx.target(); // need attacker context
                    // Note: smithery onDealDamage lacks attacker(); low-health check requires onAttackEntity
                    float amount = ctx.amount().get();
                    if (lowDur) {
                        ctx.amount().set(amount * 1.5F);
                    } else {
                        ctx.amount().set(amount * 0.9F);
                    }
                })
                .build());

        HIGH_STRIDE_ARMOR = id("high_stride_armor");
        SmitheryAPI.registerModifier(Modifier.builder(HIGH_STRIDE_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("high_stride_armor", () -> ForgeMod.STEP_HEIGHT_ADDITION.get(), "amount", 0.5f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        HITECH = id("hitech");
        SmitheryAPI.registerModifier(Modifier.builder(HITECH)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (attacker != null && attacker.fallDistance > 0.0f) {
                        ctx.amount().set(ctx.amount().get() * 1.25f);
                    }
                })
                .build());

        HOLDGROUNDTRAIT = id("hold_ground_trait");
        SmitheryAPI.registerModifier(Modifier.builder(HOLDGROUNDTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Hold Ground (tool): 1.25x damage while sneaking
                    if (attacker != null && attacker.isCrouching()) {
                        ctx.amount().set(dmg * 1.25F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Hold Ground (armor): -8% damage while sneaking
                    if (ctx.wearer().isCrouching()) {
                        ctx.amount().set(dmg * 0.92F);
                    }
                })
                .build());

        HOLD_GROUND = id("hold_ground");
        SmitheryAPI.registerModifier(Modifier.builder(HOLD_GROUND)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Hold Ground (tool): 1.25x damage while sneaking
                    if (attacker != null && attacker.isCrouching()) {
                        ctx.amount().set(dmg * 1.25F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Hold Ground (armor): -8% damage while sneaking
                    if (ctx.wearer().isCrouching()) {
                        ctx.amount().set(dmg * 0.92F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        HOLD_GROUND_ARMOR = id("hold_ground_armor");
        SmitheryAPI.registerModifier(Modifier.builder(HOLD_GROUND_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Hold Ground (tool): 1.25x damage while sneaking
                    if (attacker != null && attacker.isCrouching()) {
                        ctx.amount().set(dmg * 1.25F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Hold Ground (armor): -8% damage while sneaking
                    if (ctx.wearer().isCrouching()) {
                        ctx.amount().set(dmg * 0.92F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        HOLLOW = id("hollow");
        SmitheryAPI.registerModifier(Modifier.builder(HOLLOW)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null || !(target instanceof LivingEntity living)) return;
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() > 0.01F) return;
                    living.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200));
                    if (living.getMaxHealth() < 250.0F) {
                        living.setHealth(living.getMaxHealth() * (1.8F - rng.nextFloat() * 0.4F));
                    }
                })
                .onMobDrops((effect, ctx) -> {
                    // 90% chance to clear all monster drops
                    if (ctx.victim().level().getRandom().nextFloat() <= 0.9F) ctx.drops().clear();
                })
                .build());

        HOLY = id("holy");
        SmitheryAPI.registerModifier(Modifier.builder(HOLY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (ctx.target().isInvertedHealAndHarm()) {
                        ctx.amount().set(ctx.amount().get() + 4.0f);
                    }
                })
                .build());

        HOVERING = id("hovering");
        SmitheryAPI.registerModifier(Modifier.builder(HOVERING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onFall((effect, ctx) -> {
                    ctx.distance().set(ctx.distance().get() * 0.5);
                })
                .build());

        IGNOBLE = id("ignoble");
        SmitheryAPI.registerModifier(Modifier.builder(IGNOBLE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed -= effect.paramFloat("penalty", 1.0f))
                .build());

        ILLUMINATI = id("illuminati");
        SmitheryAPI.registerModifier(Modifier.builder(ILLUMINATI)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                })
                .build());

        IM_A_SUPERSTAR = id("im_a_superstar");
        SmitheryAPI.registerModifier(Modifier.builder(IM_A_SUPERSTAR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.tickCount % 40 != 0) return;
                    if (!player.level().canSeeSky(player.blockPosition())) return;
                    long dayTime = player.level().getDayTime() % 24000L;
                    if (dayTime >= 13000 && dayTime < 23000) {
                        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, true, false));
                    } else {
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
                    }
                })
                .build());

        INDOMITABLE_ARMOR = id("indomitable_armor");
        SmitheryAPI.registerModifier(Modifier.builder(INDOMITABLE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.92f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        INFERNAL_ARMOR = id("infernal_armor");
        SmitheryAPI.registerModifier(Modifier.builder(INFERNAL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        INFERNOTRAIT = id("inferno_trait");
        SmitheryAPI.registerModifier(Modifier.builder(INFERNOTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    // Inferno: 20% chance to set the attacker on fire for 8s on incoming hit
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity)) return;
                    if (ctx.wearer().getRandom().nextFloat() >= 0.2F) return;
                    src.getEntity().setSecondsOnFire(8);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        INFERNOTRAIT_ARMOR = id("inferno_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(INFERNOTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    // Inferno: 20% chance to set the attacker on fire for 8s on incoming hit
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity)) return;
                    if (ctx.wearer().getRandom().nextFloat() >= 0.2F) return;
                    src.getEntity().setSecondsOnFire(8);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        INSATIABLE = id("insatiable");
        SmitheryAPI.registerModifier(Modifier.builder(INSATIABLE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    { var a = ctx.attacker();
                        float bonus = (1.0f - a.getHealth() / a.getMaxHealth()) * effect.paramFloat("damage", 4.0f);
                        ctx.amount().set(ctx.amount().get() + bonus);
                    }
                })
                .build());

        INVARIANT_ARMOR = id("invariant_armor");
        SmitheryAPI.registerModifier(Modifier.builder(INVARIANT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onKill((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.5f)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        INVIGORATING_ARMOR = id("invigorating_armor");
        SmitheryAPI.registerModifier(Modifier.builder(INVIGORATING_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("invigorating_armor", () -> Attributes.MAX_HEALTH, "amount", 4.0f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        // PlusTiC's Jaded (landmaster.plustic.traits.Jaded): every hit stacks a wound level to a
        // cap of 3 and refreshes an 80-tick window during which the victim's healing is throttled
        // to (3 - level)/3 — fully negated at level 3. The throttle itself lives in
        // SmitheryTraitEvents.onHeal, since Smithery has no heal hook.
        JADED = id("jaded");
        SmitheryAPI.registerModifier(Modifier.builder(JADED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> SmitheryTraitEvents.applyJaded(
                        ctx.target(),
                        effect.paramInt("max_level", 3),
                        effect.paramInt("duration", 80)))
                .build());

        JAGGED = id("jagged");
        SmitheryAPI.registerModifier(Modifier.builder(JAGGED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .durabilityScaled()
                                .passive((effect, stats) -> {
                    float pct = effect.paramFloat("pct", 0.5f);
                    stats.bonusAttackDamage += pct * stats.missingDurability;
                })
                .build());

        JETPACKPANCAKEHIPPOS = id("jetpackpancakehippos");
        SmitheryAPI.registerModifier(Modifier.builder(JETPACKPANCAKEHIPPOS)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.isCreative() || player.isSpectator()) return;
                    if (!player.getAbilities().mayfly) {
                        player.getAbilities().mayfly = true;
                        player.onUpdateAbilities();
                    }
                    if (player.getAbilities().flying && player.tickCount % 60 == 0) {
                        if (player.totalExperience > 0) {
                            player.giveExperiencePoints(-3);
                        } else {
                            player.getAbilities().flying = false;
                            player.getAbilities().mayfly = false;
                            player.onUpdateAbilities();
                        }
                    }
                })
                .onEquipChange((effect, ctx) -> {
                    if (ctx.equipped()) return;
                    if (!(ctx.wearer() instanceof Player player)) return;
                    if (player.isCreative() || player.isSpectator()) return;
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                })
                .build());

        KNOCKBACK = id("knockback");
        SmitheryAPI.registerModifier(Modifier.builder(KNOCKBACK)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    var dir = ctx.attacker().getLookAngle().normalize();
                    target.push(dir.x * 0.8, 0.2, dir.z * 0.8);
                    target.hurtMarked = true;
                })
                .build());

        KNOWLEDGEFUL = id("knowledgeful");
        SmitheryAPI.registerModifier(Modifier.builder(KNOWLEDGEFUL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Knowledgeful (tool): +0.2% damage per XP level (cap 300 levels, +60% max)
                    if (!(attacker instanceof net.minecraft.world.entity.player.Player p)) return;
                    int xp = Math.min(p.experienceLevel, 300);
                    ctx.amount().set(dmg * (1.0F + xp * 0.002F));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Knowledgeful (armor): scale incoming reduction with xp/300 * 36% (per-piece via perPiece)
                    if (!(ctx.wearer() instanceof net.minecraft.world.entity.player.Player p)) return;
                    float xp = Math.min(p.experienceLevel, 300);
                    float reduction = (xp / 300.0F) * 0.36F;
                    // GC's calcSingleArmor: full-set reduction -> per-piece factor
                    float perPiece = reduction <= 0F ? 1F : reduction >= 1F ? 0F
                        : (float) Math.pow(1.0F - reduction, 0.25);
                    ctx.amount().set(dmg * perPiece);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        KNOWLEDGEFULTRAIT = id("knowledgeful_trait");
        SmitheryAPI.registerModifier(Modifier.builder(KNOWLEDGEFULTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Knowledgeful (tool): +0.2% damage per XP level (cap 300 levels, +60% max)
                    if (!(attacker instanceof net.minecraft.world.entity.player.Player p)) return;
                    int xp = Math.min(p.experienceLevel, 300);
                    ctx.amount().set(dmg * (1.0F + xp * 0.002F));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Knowledgeful (armor): scale incoming reduction with xp/300 * 36% (per-piece via perPiece)
                    if (!(ctx.wearer() instanceof net.minecraft.world.entity.player.Player p)) return;
                    float xp = Math.min(p.experienceLevel, 300);
                    float reduction = (xp / 300.0F) * 0.36F;
                    // GC's calcSingleArmor: full-set reduction -> per-piece factor
                    float perPiece = reduction <= 0F ? 1F : reduction >= 1F ? 0F
                        : (float) Math.pow(1.0F - reduction, 0.25);
                    ctx.amount().set(dmg * perPiece);
                })
                .build());

        KNOWLEDGEFUL_ARMOR = id("knowledgeful_armor");
        SmitheryAPI.registerModifier(Modifier.builder(KNOWLEDGEFUL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Knowledgeful (tool): +0.2% damage per XP level (cap 300 levels, +60% max)
                    if (!(attacker instanceof net.minecraft.world.entity.player.Player p)) return;
                    int xp = Math.min(p.experienceLevel, 300);
                    ctx.amount().set(dmg * (1.0F + xp * 0.002F));
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Knowledgeful (armor): scale incoming reduction with xp/300 * 36% (per-piece via perPiece)
                    if (!(ctx.wearer() instanceof net.minecraft.world.entity.player.Player p)) return;
                    float xp = Math.min(p.experienceLevel, 300);
                    float reduction = (xp / 300.0F) * 0.36F;
                    // GC's calcSingleArmor: full-set reduction -> per-piece factor
                    float perPiece = reduction <= 0F ? 1F : reduction >= 1F ? 0F
                        : (float) Math.pow(1.0F - reduction, 0.25);
                    ctx.amount().set(dmg * perPiece);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        KUNGFUTRAIT = id("kungfu_trait");
        SmitheryAPI.registerModifier(Modifier.builder(KUNGFUTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    if (entity.level().isClientSide()) return;
                    float dmg = ctx.amount().get();
                    // Kung Fu: 4% chance to dodge a non-absolute hit, gaining Speed IV (5s)
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) return;
                    if (entity.getRandom().nextFloat() < 0.04F) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 100, 3, false, false));
                        ctx.amount().set(0.0F);
                    }
                })
                .build());

        KUNGFU_ARMOR = id("kungfu_armor");
        SmitheryAPI.registerModifier(Modifier.builder(KUNGFU_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    if (entity.level().isClientSide()) return;
                    float dmg = ctx.amount().get();
                    // Kung Fu: 4% chance to dodge a non-absolute hit, gaining Speed IV (5s)
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) return;
                    if (entity.getRandom().nextFloat() < 0.04F) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 100, 3, false, false));
                        ctx.amount().set(0.0F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        LAUNCHING = id("launching");
        SmitheryAPI.registerModifier(Modifier.builder(LAUNCHING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.target() instanceof net.minecraft.world.entity.LivingEntity living) {
                        var m = living.getDeltaMovement();
                        living.setDeltaMovement(m.x, Math.max(m.y, 0) + 1.0, m.z);
                        living.hurtMarked = true;
                    }
                })
                .build());

        LEVELINGDAMAGETRAIT = id("levelingdamage_trait");
        SmitheryAPI.registerModifier(Modifier.builder(LEVELINGDAMAGETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Leveling Damage: +5% per modifier level, cap +50%
                    int level = effect.paramInt("level", 1);
                    float mult = Math.min(1.5F, 1.0F + 0.05F * level);
                    ctx.amount().set(dmg * mult);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        LEVELINGDEFENSETRAIT = id("levelingdefense_trait");
        SmitheryAPI.registerModifier(Modifier.builder(LEVELINGDEFENSETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Leveling Defense: ~3% reduction per modifier level, cap 50%
                    int level = effect.paramInt("level", 1);
                    float reduction = Math.min(0.5F, 0.03F * level);
                    ctx.amount().set(dmg * (1.0F - reduction));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        LIGHT = id("light");
        SmitheryAPI.registerModifier(Modifier.builder(LIGHT)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 1.0f))
                .build());

        LIGHT_PIERCE = id("light_pierce");
        SmitheryAPI.registerModifier(Modifier.builder(LIGHT_PIERCE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        LIGHTNING = id("lightning");
        SmitheryAPI.registerModifier(Modifier.builder(LIGHTNING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null) return;
                    if (!(ctx.attacker().level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
                    var target = ctx.target();
                    if (target == null) return;
                    // Lightning: 4% chance to spawn a visual-only lightning bolt at target
                    if (sl.getRandom().nextFloat() >= 0.04F) return;
                    net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt == null) return;
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    bolt.setVisualOnly(true);
                    sl.addFreshEntity(bolt);
                })
                .build());

        LIGHTWEIGHT = id("lightweight");
        SmitheryAPI.registerModifier(Modifier.builder(LIGHTWEIGHT)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 1.5f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        LIGHTWEIGHT_ARMOR = id("lightweight_armor");
        SmitheryAPI.registerModifier(Modifier.builder(LIGHTWEIGHT_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("lightweight_armor", () -> Attributes.MOVEMENT_SPEED, "pct", 0.02f, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        LIVING = id("living");
        SmitheryAPI.registerModifier(Modifier.builder(LIVING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        LIVING2 = id("living2");
        SmitheryAPI.registerModifier(Modifier.builder(LIVING2)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 400 != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .build());

        LUCK = id("luck");
        SmitheryAPI.registerModifier(Modifier.builder(LUCK)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.LUCK, 40, 0, true, false));
                    }
                })
                .build());

        LUCKYTRAIT = id("lucky_trait");
        SmitheryAPI.registerModifier(Modifier.builder(LUCKYTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Lucky: Luck I while equipped (refresh every 20 ticks, 25t duration outlasts refresh)
                    if (ctx.wearer().tickCount % 20 != 0) return;
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.LUCK, 25, 0, false, false));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        LUCKYTRAIT_ARMOR = id("lucky_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(LUCKYTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Lucky: Luck I while equipped (refresh every 20 ticks, 25t duration outlasts refresh)
                    if (ctx.wearer().tickCount % 20 != 0) return;
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.LUCK, 25, 0, false, false));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        MADNESSTRAIT = id("madness_trait");
        SmitheryAPI.registerModifier(Modifier.builder(MADNESSTRAIT)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        MAGICAL_MODIFIER = id("magical_modifier");
        SmitheryAPI.registerModifier(Modifier.builder(MAGICAL_MODIFIER)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        MAGICMUSHROOM = id("magicmushroom");
        SmitheryAPI.registerModifier(Modifier.builder(MAGICMUSHROOM)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                })
                .build());

        MAGNETIC = id("magnetic");
        SmitheryAPI.registerModifier(Modifier.builder(MAGNETIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.player() == null) return;
                    var playerPos = ctx.player().position();
                    for (var drop : ctx.drops()) {
                        var toward = playerPos.subtract(drop.position()).normalize().scale(0.4);
                        drop.setDeltaMovement(toward);
                        drop.setPickUpDelay(0);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        MAGNETIC_ARMOR = id("magnetic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(MAGNETIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 5 != 0) return;
                    float radius = effect.paramFloat("radius", 5.0f);
                    var box = ctx.wearer().getBoundingBox().inflate(radius);
                    var playerPos = ctx.wearer().position();
                    for (var drop : ctx.wearer().level().getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class, box)) {
                        if (drop.hasPickUpDelay()) continue;
                        var toward = playerPos.subtract(drop.position());
                        if (toward.length() < 0.5 || toward.length() > radius) continue;
                        drop.setDeltaMovement(toward.normalize().scale(0.35));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        MAGNETIC_ARMOR1 = id("magnetic_armor1");
        SmitheryAPI.registerModifier(Modifier.builder(MAGNETIC_ARMOR1)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 5 != 0) return;
                    float radius = 8.0f;
                    var box = ctx.wearer().getBoundingBox().inflate(radius);
                    var pos = ctx.wearer().position();
                    for (var drop : ctx.wearer().level().getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class, box)) {
                        if (drop.hasPickUpDelay()) continue;
                        var t = pos.subtract(drop.position());
                        if (t.length() > 0.5 && t.length() <= radius) drop.setDeltaMovement(t.normalize().scale(0.35));
                    }
                })
                .build());

        MAGNETIC_ARMOR2 = id("magnetic_armor2");
        SmitheryAPI.registerModifier(Modifier.builder(MAGNETIC_ARMOR2)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 5 != 0) return;
                    float radius = 12.0f;
                    var box = ctx.wearer().getBoundingBox().inflate(radius);
                    var pos = ctx.wearer().position();
                    for (var drop : ctx.wearer().level().getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class, box)) {
                        if (drop.hasPickUpDelay()) continue;
                        var t = pos.subtract(drop.position());
                        if (t.length() > 0.5 && t.length() <= radius) drop.setDeltaMovement(t.normalize().scale(0.35));
                    }
                })
                .build());

        MANA = id("mana");
        SmitheryAPI.registerModifier(Modifier.builder(MANA)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (tool.isDamageableItem() && ctx.attacker().getRandom().nextFloat() < 0.15f) {
                        tool.setDamageValue(Math.max(0, tool.getDamageValue() - 1));
                    }
                })
                .onBlockBreak((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (tool.isDamageableItem() && ctx.player().getRandom().nextFloat() < 0.15f) {
                        tool.setDamageValue(Math.max(0, tool.getDamageValue() - 1));
                    }
                })
                .build());

        MATTERTRAIT1 = id("matter_trait1");
        SmitheryAPI.registerModifier(Modifier.builder(MATTERTRAIT1)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    // Matter Condensing I: adds floor(damage * 0.1) personal EMC on hostile-mob hit
                    // STUB: requires ProjectE API (PECapabilities.KNOWLEDGE_CAPABILITY)
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    long emc = (long) Math.floor(ctx.damageDealt() * 0.1);
                    if (emc <= 0) return;
                    // TODO: ProjectE EMC integration
                    // ctx.attacker().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(k -> {
                    //     k.setEmc(k.getEmc().add(java.math.BigInteger.valueOf(emc)));
                    //     if (ctx.attacker() instanceof ServerPlayer sp) k.syncEmc(sp);
                    // });
                })
                .build());

        MATTERTRAIT11 = id("matter_trait11");
        SmitheryAPI.registerModifier(Modifier.builder(MATTERTRAIT11)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    // Matter Condensing I: adds floor(damage * 0.1) personal EMC on hostile-mob hit
                    // STUB: requires ProjectE API (PECapabilities.KNOWLEDGE_CAPABILITY)
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    long emc = (long) Math.floor(ctx.damageDealt() * 0.1);
                    if (emc <= 0) return;
                    // TODO: ProjectE EMC integration
                    // ctx.attacker().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(k -> {
                    //     k.setEmc(k.getEmc().add(java.math.BigInteger.valueOf(emc)));
                    //     if (ctx.attacker() instanceof ServerPlayer sp) k.syncEmc(sp);
                    // });
                })
                .build());

        MATTERTRAIT12 = id("matter_trait12");
        SmitheryAPI.registerModifier(Modifier.builder(MATTERTRAIT12)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    // Matter Condensing II: adds floor(damage * 0.25) personal EMC on hostile-mob hit
                    // STUB: requires ProjectE API (PECapabilities.KNOWLEDGE_CAPABILITY)
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    long emc = (long) Math.floor(ctx.damageDealt() * 0.25);
                    if (emc <= 0) return;
                    // TODO: ProjectE EMC integration
                    // ctx.attacker().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(k -> {
                    //     k.setEmc(k.getEmc().add(java.math.BigInteger.valueOf(emc)));
                    //     if (ctx.attacker() instanceof ServerPlayer sp) k.syncEmc(sp);
                    // });
                })
                .build());

        MATTERTRAIT2 = id("matter_trait2");
        SmitheryAPI.registerModifier(Modifier.builder(MATTERTRAIT2)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    // Matter Condensing II: adds floor(damage * 0.25) personal EMC on hostile-mob hit
                    // STUB: requires ProjectE API (PECapabilities.KNOWLEDGE_CAPABILITY)
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    long emc = (long) Math.floor(ctx.damageDealt() * 0.25);
                    if (emc <= 0) return;
                    // TODO: ProjectE EMC integration
                    // ctx.attacker().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(k -> {
                    //     k.setEmc(k.getEmc().add(java.math.BigInteger.valueOf(emc)));
                    //     if (ctx.attacker() instanceof ServerPlayer sp) k.syncEmc(sp);
                    // });
                })
                .build());

        MELTING = id("melting");
        SmitheryAPI.registerModifier(Modifier.builder(MELTING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() > 0.025F) return;
                    net.minecraft.world.level.block.Block b = ctx.level().getBlockState(ctx.pos()).getBlock();
                    if (b == net.minecraft.world.level.block.Blocks.STONE || b == net.minecraft.world.level.block.Blocks.COBBLESTONE || b == net.minecraft.world.level.block.Blocks.NETHERRACK || b == net.minecraft.world.level.block.Blocks.OBSIDIAN) {
                        ctx.level().setBlockAndUpdate(ctx.pos(), net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
                    }
                })
                .build());

        // Malum scythe mechanic as a material trait: kills shatter the victim's soul
        // into arcane spirits, exactly like killing with a Malum scythe. Attached to
        // soul_stained_steel — the metal Malum's scythes are forged from.
        SOUL_STAINED = id("soul_stained");
        SmitheryAPI.registerModifier(Modifier.builder(SOUL_STAINED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onKill((effect, ctx) -> {
                    if (!net.minecraftforge.fml.ModList.get().isLoaded("malum")) return;
                    if (ctx.victim() == null || ctx.attacker() == null) return;
                    MalumCompat.spawnSpirits(ctx.victim(), ctx.attacker(), ctx.tool());
                })
                .build());

        // Tool leveling (TinkerToolLeveling port — GC balance assumed it): tools gain
        // XP from use; each level grants a bonus modifier slot. The effect is applied
        // by ToolLevelingEvents with params {level, bonus_slots}; bonus_slots is read
        // natively by SmitheryToolItem.bonusModifierSlots.
        WELL_USED = id("well_used");
        SmitheryAPI.registerModifier(Modifier.builder(WELL_USED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        // TAIGA nucleum trait: 5% chance a broken natural block mutates into another
        // instead of dropping. Behavior lives in SmitheryTraitEvents (needs BreakEvent
        // cancellation, which the onBlockBreak hook can't do).
        MUTATE = id("mutate");
        SmitheryAPI.registerModifier(Modifier.builder(MUTATE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .build());

        MENDING_ARMOR = id("mending_armor");
        SmitheryAPI.registerModifier(Modifier.builder(MENDING_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        MENDING_MOSS = id("mending_moss");
        SmitheryAPI.registerModifier(Modifier.builder(MENDING_MOSS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        MILKYTRAIT = id("milky_trait");
        SmitheryAPI.registerModifier(Modifier.builder(MILKYTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Milky: periodically clear all status effects
                    // Armor slot: every 18000 ticks (matches GC armor cadence)
                    // Tool slot (mainhand selected): every 12000 ticks (one MC day)
                    // NOTE: smithery onArmorTick fires for armor only; tool-held cadence
                    // would need a separate .onCompose or inventory-tick hook.
                    long t = ctx.wearer().level().getGameTime();
                    if (t % 18000L == 0L) {
                        ctx.wearer().removeAllEffects();
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        MILKYTRAIT_ARMOR = id("milky_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(MILKYTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Milky: periodically clear all status effects
                    // Armor slot: every 18000 ticks (matches GC armor cadence)
                    // Tool slot (mainhand selected): every 12000 ticks (one MC day)
                    // NOTE: smithery onArmorTick fires for armor only; tool-held cadence
                    // would need a separate .onCompose or inventory-tick hook.
                    long t = ctx.wearer().level().getGameTime();
                    if (t % 18000L == 0L) {
                        ctx.wearer().removeAllEffects();
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        MIND = id("mind");
        SmitheryAPI.registerModifier(Modifier.builder(MIND)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 1.0f))
                .build());

        MIRABILE = id("mirabile");
        SmitheryAPI.registerModifier(Modifier.builder(MIRABILE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.15f))
                .build());

        MOMENTUM = id("momentum");
        SmitheryAPI.registerModifier(Modifier.builder(MOMENTUM)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    int cap = effect.paramInt("max_amplifier", 2);
                    var current = ctx.player().getEffect(MobEffects.DIG_SPEED);
                    int nextAmp = current == null ? 0 : Math.min(cap, current.getAmplifier() + 1);
                    ctx.player().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, nextAmp, true, false));
                })
                .build());

        MORGANLEFAY = id("morganlefay");
        SmitheryAPI.registerModifier(Modifier.builder(MORGANLEFAY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float bonus = 0.5f + ctx.target().level().getRandom().nextFloat() * 3.5f;
                    ctx.amount().set(ctx.amount().get() + bonus);
                })
                .build());

        MOTIONTRAIT = id("motion_trait");
        SmitheryAPI.registerModifier(Modifier.builder(MOTIONTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Motion (tool): 1.33x damage while sprinting
                    if (attacker != null && attacker.isSprinting()) {
                        ctx.amount().set(dmg * 1.33F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Motion (armor): -7% damage while sprinting
                    if (ctx.wearer().isSprinting()) {
                        ctx.amount().set(dmg * 0.93F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        MOTIONTRAIT_ARMOR = id("motion_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(MOTIONTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Motion (tool): 1.33x damage while sprinting
                    if (attacker != null && attacker.isSprinting()) {
                        ctx.amount().set(dmg * 1.33F);
                    }
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Motion (armor): -7% damage while sprinting
                    if (ctx.wearer().isSprinting()) {
                        ctx.amount().set(dmg * 0.93F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        MUNDANE_ARMOR = id("mundane_armor");
        SmitheryAPI.registerModifier(Modifier.builder(MUNDANE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        MUNDANE_ARMOR1 = id("mundane_armor1");
        SmitheryAPI.registerModifier(Modifier.builder(MUNDANE_ARMOR1)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof LivingEntity attacker) {
                        if (attacker.getMainHandItem().isEmpty()) {
                            ctx.amount().set(ctx.amount().get() * 0.85f);
                        }
                    }
                })
                .build());

        MUSICOFTHESPHERES = id("musicofthespheres");
        SmitheryAPI.registerModifier(Modifier.builder(MUSICOFTHESPHERES)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    var sounds = new net.minecraft.sounds.SoundEvent[]{
                            SoundEvents.NOTE_BLOCK_HARP.value(), SoundEvents.NOTE_BLOCK_BELL.value(),
                            SoundEvents.NOTE_BLOCK_CHIME.value(), SoundEvents.NOTE_BLOCK_FLUTE.value(),
                            SoundEvents.NOTE_BLOCK_GUITAR.value(), SoundEvents.NOTE_BLOCK_XYLOPHONE.value()};
                    var sound = sounds[target.level().getRandom().nextInt(sounds.length)];
                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            sound, SoundSource.RECORDS, 1.0f,
                            0.5f + target.level().getRandom().nextFloat());
                })
                .build());

        MYSTICAL_FIRE = id("mystical_fire");
        SmitheryAPI.registerModifier(Modifier.builder(MYSTICAL_FIRE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .build());

        NAPHTHA = id("naphtha");
        SmitheryAPI.registerModifier(Modifier.builder(NAPHTHA)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(5);
                })
                .build());

        NATUREBOUND = id("naturebound");
        SmitheryAPI.registerModifier(Modifier.builder(NATUREBOUND)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    var wearer = ctx.wearer();
                    if (wearer == null || wearer.level().isClientSide()) return;
                    var rng = ctx.wearer().getRandom();
                    if (rng.nextInt(400) != 0) return;
                    net.minecraft.core.BlockPos below = wearer.blockPosition().below();
                    net.minecraft.world.level.block.state.BlockState state = wearer.level().getBlockState(below);
                    String descId = state.getBlock().getDescriptionId();
                    boolean isNature = descId.contains("grass") || descId.contains("leaves") || descId.contains("moss");
                    var armor = ctx.armor();
                    if (isNature) {
                        // Heal tool 1-2 durability
                        armor.setDamageValue(Math.max(0, armor.getDamageValue() - (rng.nextInt(2) + 1)));
                    } else {
                        // Damage tool 1 durability
                        armor.setDamageValue(armor.getDamageValue() + 1);
                    }
                })
                .build());

        NATURESBLESSING = id("naturesblessing");
        SmitheryAPI.registerModifier(Modifier.builder(NATURESBLESSING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var attacker = ctx.attacker();
                    float heal = ctx.damageDealt() * effect.paramFloat("pct", 0.1f);
                    if (heal > 0) attacker.heal(heal);
                })
                .build());

        NATURESPOWER = id("naturespower");
        SmitheryAPI.registerModifier(Modifier.builder(NATURESPOWER)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .build());

        NATURESWRATH = id("natureswrath");
        SmitheryAPI.registerModifier(Modifier.builder(NATURESWRATH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .build());

        NICKOFTIME = id("nickoftime");
        SmitheryAPI.registerModifier(Modifier.builder(NICKOFTIME)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (!(ctx.wearer() instanceof Player player)) return;
                    if (player.getHealth() - ctx.amount().get() > 0) return;
                    int pearlSlot = -1;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        if (player.getInventory().getItem(i).is(Items.ENDER_PEARL)) {
                            pearlSlot = i;
                            break;
                        }
                    }
                    if (pearlSlot < 0) return;
                    player.getInventory().getItem(pearlSlot).shrink(1);
                    var random = player.getRandom();
                    for (int attempt = 0; attempt < 16; attempt++) {
                        double dx = (random.nextDouble() - 0.5) * 32.0;
                        double dz = (random.nextDouble() - 0.5) * 32.0;
                        int tx = (int) (player.getX() + dx);
                        int tz = (int) (player.getZ() + dz);
                        for (int dy = 0; dy < 8; dy++) {
                            BlockPos check = new BlockPos(tx, (int) player.getY() + dy, tz);
                            if (player.level().getBlockState(check).isAir()
                                    && player.level().getBlockState(check.above()).isAir()
                                    && !player.level().getBlockState(check.below()).isAir()) {
                                player.teleportTo(check.getX() + 0.5, check.getY(), check.getZ() + 0.5);
                                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                                ctx.amount().set(0f);
                                player.setHealth(Math.max(1.0f, player.getHealth()));
                                return;
                            }
                        }
                    }
                    ctx.amount().set(0f);
                    player.setHealth(1.0f);
                })
                .build());

        NIGHTBANETRAIT = id("night_bane_trait");
        SmitheryAPI.registerModifier(Modifier.builder(NIGHTBANETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity a = ctx.attacker();
                    if (a != null && a.level().getMaxLocalRawBrightness(a.blockPosition()) > 11) {
                        ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 2.0f));
                    }
                })
                .build());

        NIGHT_VISION_ARMOR = id("night_vision_armor");
        SmitheryAPI.registerModifier(Modifier.builder(NIGHT_VISION_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        OREEXCAVATE = id("oreexcavate");
        SmitheryAPI.registerModifier(Modifier.builder(OREEXCAVATE)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var state = ctx.state();
                    if (state.getDestroySpeed(ctx.level(), ctx.pos()) < 0) return;
                    int radius = effect.paramInt("radius", 1);
                    int maxBlocks = effect.paramInt("max_blocks", 32);
                    var block = state.getBlock();
                    var level = ctx.level();
                    var center = ctx.pos();
                    int broken = 0;
                    for (int dx = -radius; dx <= radius && broken < maxBlocks; dx++) {
                        for (int dy = -radius; dy <= radius && broken < maxBlocks; dy++) {
                            for (int dz = -radius; dz <= radius && broken < maxBlocks; dz++) {
                                if (dx == 0 && dy == 0 && dz == 0) continue;
                                var pos = center.offset(dx, dy, dz);
                                if (level.getBlockState(pos).getBlock() == block) {
                                    level.destroyBlock(pos, true, ctx.player());
                                    broken++;
                                }
                            }
                        }
                    }
                })
                .build());

        PARASITIC_ARMOR = id("parasitic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(PARASITIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PENETRATIONTRAIT = id("penetration_trait");
        SmitheryAPI.registerModifier(Modifier.builder(PENETRATIONTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Penetration: bonus damage scaled by target armor (cap +100% at 50 armor)
                    int armor = target.getArmorValue();
                    if (armor <= 0) return;
                    float mult = Math.min(1.0F, armor * 0.02F);
                    ctx.amount().set(dmg * (1.0F + mult));
                })
                .build());

        PERFECTIONIST = id("perfectionist");
        SmitheryAPI.registerModifier(Modifier.builder(PERFECTIONIST)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Perfectionist (tool): round outgoing damage to nearest 10
                    ctx.amount().set(Math.round(dmg / 10.0F) * 10.0F);
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Perfectionist (armor): round incoming damage > 5 to nearest 5
                    if (dmg > 5.0F) {
                        ctx.amount().set(Math.round(dmg / 5.0F) * 5.0F);
                    }
                })
                .build());

        PERFECTIONISTTRAIT = id("perfectionist_trait");
        SmitheryAPI.registerModifier(Modifier.builder(PERFECTIONISTTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Perfectionist (tool): round outgoing damage to nearest 10
                    ctx.amount().set(Math.round(dmg / 10.0F) * 10.0F);
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Perfectionist (armor): round incoming damage > 5 to nearest 5
                    if (dmg > 5.0F) {
                        ctx.amount().set(Math.round(dmg / 5.0F) * 5.0F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        PERFECTIONISTTRAIT_ARMOR = id("perfectionist_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(PERFECTIONISTTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Perfectionist (tool): round outgoing damage to nearest 10
                    ctx.amount().set(Math.round(dmg / 10.0F) * 10.0F);
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Perfectionist (armor): round incoming damage > 5 to nearest 5
                    if (dmg > 5.0F) {
                        ctx.amount().set(Math.round(dmg / 5.0F) * 5.0F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PETRAMOR = id("petramor");
        SmitheryAPI.registerModifier(Modifier.builder(PETRAMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (tool.getDamageValue() > 0 && ctx.level().getRandom().nextFloat() < 0.1f) {
                        tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        PETRAVIDITY_ARMOR = id("petravidity_armor");
        SmitheryAPI.registerModifier(Modifier.builder(PETRAVIDITY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 400 != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PINKYTRAIT = id("pinky_trait");
        SmitheryAPI.registerModifier(Modifier.builder(PINKYTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.level().isClientSide()) return;
                    // Pinky: 20% chance to drop a sakura_diamond when mining diamond_ore
                    net.minecraft.world.level.block.state.BlockState state = ctx.level().getBlockState(ctx.pos());
                    if (!state.is(net.minecraft.world.level.block.Blocks.DIAMOND_ORE)
                            && !state.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE)) return;
                    if (ctx.level().getRandom().nextFloat() >= 0.2F) return;
                    var sd = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("soa_additions", "sakura_diamond"));
                    if (sd == null) return;
                    net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                        ctx.level(), ctx.pos().getX() + 0.5, ctx.pos().getY() + 0.5, ctx.pos().getZ() + 0.5,
                        new net.minecraft.world.item.ItemStack(sd));
                    ctx.level().addFreshEntity(drop);
                })
                .build());

        POISONOUS = id("poisonous");
        SmitheryAPI.registerModifier(Modifier.builder(POISONOUS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.POISON,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .build());

        POLISHED_ARMOR = id("polished_armor");
        SmitheryAPI.registerModifier(Modifier.builder(POLISHED_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("polished_armor", () -> Attributes.ARMOR_TOUGHNESS, "amount", 2.0f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        POOPTRAIT = id("poop_trait");
        SmitheryAPI.registerModifier(Modifier.builder(POOPTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    // Poopy (tool): 0.5% chance to drop a poop item on hostile-mob hit
                    if (ctx.attacker().getRandom().nextFloat() >= 0.005F) return;
                    var poop = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("soa_additions", "poop"));
                    if (poop != null) ((net.minecraft.world.entity.player.Player) ctx.attacker()).addItem(new net.minecraft.world.item.ItemStack(poop));
                })
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Poopy (armor): applies Nausea while equipped
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.CONFUSION, 25, 0, false, false));
                })
                .build());

        POOPY = id("poopy");
        SmitheryAPI.registerModifier(Modifier.builder(POOPY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    // Poopy (tool): 0.5% chance to drop a poop item on hostile-mob hit
                    if (ctx.attacker().getRandom().nextFloat() >= 0.005F) return;
                    var poop = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("soa_additions", "poop"));
                    if (poop != null) ((net.minecraft.world.entity.player.Player) ctx.attacker()).addItem(new net.minecraft.world.item.ItemStack(poop));
                })
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Poopy (armor): applies Nausea while equipped
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.CONFUSION, 25, 0, false, false));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        POOPY_ARMOR = id("poopy_armor");
        SmitheryAPI.registerModifier(Modifier.builder(POOPY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    // Poopy (tool): 0.5% chance to drop a poop item on hostile-mob hit
                    if (ctx.attacker().getRandom().nextFloat() >= 0.005F) return;
                    var poop = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("soa_additions", "poop"));
                    if (poop != null) ((net.minecraft.world.entity.player.Player) ctx.attacker()).addItem(new net.minecraft.world.item.ItemStack(poop));
                })
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Poopy (armor): applies Nausea while equipped
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.CONFUSION, 25, 0, false, false));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PORTED = id("ported");
        SmitheryAPI.registerModifier(Modifier.builder(PORTED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var player = ctx.player();
                    if (player == null) return;
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() > 0.005F) return;
                    var level = ctx.level();
                    net.minecraft.core.BlockPos target = player.blockPosition().above(10);
                    if (target.getY() >= level.getMaxBuildHeight()) return;
                    while (!level.getBlockState(target).isAir() && target.getY() <= level.getMaxBuildHeight()) {
                        target = target.above();
                    }
                    if (level.getBlockState(target).isAir()) {
                        player.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                    }
                })
                .onAttackEntity((effect, ctx) -> {
                    LivingEntity living = ctx.attacker();
                    if (living == null) return;
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() > 0.005F) return;
                    var level = living.level();
                    net.minecraft.core.BlockPos target = living.blockPosition().above(10);
                    if (target.getY() >= level.getMaxBuildHeight()) return;
                    while (!level.getBlockState(target).isAir() && target.getY() <= level.getMaxBuildHeight()) {
                        target = target.above();
                    }
                    if (level.getBlockState(target).isAir()) {
                        living.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                    }
                })
                .build());

        PORTLY = id("portly");
        SmitheryAPI.registerModifier(Modifier.builder(PORTLY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.1f))
                .build());

        POTION_BELT_ARMOR = id("potion_belt_armor");
        SmitheryAPI.registerModifier(Modifier.builder(POTION_BELT_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player wearer = ctx.wearer();
                    if (wearer.tickCount % 20 != 0) return;
                    if (wearer.getHealth() > wearer.getMaxHealth() * 0.4f) return;
                    for (int i = 0; i < wearer.getInventory().getContainerSize(); i++) {
                        ItemStack slot = wearer.getInventory().getItem(i);
                        if (slot.getItem() instanceof net.minecraft.world.item.PotionItem) {
                            var potionEffects = net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(slot);
                            boolean beneficial = false;
                            for (var pe : potionEffects) {
                                if (pe.getEffect().isBeneficial()) { beneficial = true; break; }
                            }
                            if (beneficial) {
                                for (var pe : potionEffects) {
                                    wearer.addEffect(new MobEffectInstance(pe));
                                }
                                slot.shrink(1);
                                if (slot.isEmpty()) {
                                    wearer.getInventory().setItem(i, new ItemStack(Items.GLASS_BOTTLE));
                                } else {
                                    wearer.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                                }
                                break;
                            }
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        POWERFUL_ARMOR = id("powerful_armor");
        SmitheryAPI.registerModifier(Modifier.builder(POWERFUL_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PRECIPITATE = id("precipitate");
        SmitheryAPI.registerModifier(Modifier.builder(PRECIPITATE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    stats.bonusMiningSpeed += effect.paramFloat("speed", 3.0f);
                })
                .build());

        PRICKLY = id("prickly");
        SmitheryAPI.registerModifier(Modifier.builder(PRICKLY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.hurt(ctx.wearer().damageSources().thorns(ctx.wearer()),
                            effect.paramFloat("damage", 2.0f));
                })
                .build());

        PRIDEFUL_ARMOR = id("prideful_armor");
        SmitheryAPI.registerModifier(Modifier.builder(PRIDEFUL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.wearer().getHealth() >= ctx.wearer().getMaxHealth()) {
                        ctx.amount().set(ctx.amount().get() * 0.85f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PROJECTILE_RESISTANT_ARMOR = id("projectile_resistant_armor");
        SmitheryAPI.registerModifier(Modifier.builder(PROJECTILE_RESISTANT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            if (!ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) return;
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        PROSPEROUS = id("prosperous");
        SmitheryAPI.registerModifier(Modifier.builder(PROSPEROUS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * 2.0f));
                })
                .build());

        PSICOLOGICAL = id("psicological");
        SmitheryAPI.registerModifier(Modifier.builder(PSICOLOGICAL)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (!tool.isDamageableItem()) return;
                    float durPct = (float) tool.getDamageValue() / tool.getMaxDamage();
                    if (durPct < 0.80f) return;
                    Player player = ctx.player();
                    if (player.experienceLevel < 1) return;
                    player.giveExperienceLevels(-1);
                    int restored = Math.max(1, tool.getMaxDamage() / 10);
                    tool.setDamageValue(Math.max(0, tool.getDamageValue() - restored));
                })
                .build());

        PULVERIZING = id("pulverizing");
        SmitheryAPI.registerModifier(Modifier.builder(PULVERIZING)
                .category(Modifier.ModifierCategory.BOTH)
                .durabilityScaled()
                .passive((effect, stats) -> {
                    // Mining speed ramps up as durability drops: 1.0x at full -> 1.9x at empty
                    stats.bonusMiningSpeed += stats.bonusMiningSpeed * 0.9F * stats.missingDurability;
                })
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.level().getRandom().nextFloat() < 0.6F) ctx.drops().clear();
                })
                .build());

        PURIFYINGTRAIT = id("purifying_trait");
        SmitheryAPI.registerModifier(Modifier.builder(PURIFYINGTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Purifying: strips Wither effect each tick
                    if (ctx.wearer().hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
                        ctx.wearer().removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        PURIFYINGTRAIT_ARMOR = id("purifying_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(PURIFYINGTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Purifying: strips Wither effect each tick
                    if (ctx.wearer().hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
                        ctx.wearer().removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        RAGING = id("raging");
        SmitheryAPI.registerModifier(Modifier.builder(RAGING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Raging: non-crit 1.25x, crit 0.75x
                    // NOTE: smithery onDealDamage ctx has no isCritical(); approximate via
                    // player fall-distance heuristic (vanilla crit = falling + not on ground)
                    var target = ctx.target();
                    var attacker = target.getLastAttacker();
                    boolean crit = attacker instanceof net.minecraft.world.entity.player.Player p
                        && p.fallDistance > 0.0F && !p.onGround() && !p.isInWater();
                    ctx.amount().set(crit ? dmg * 0.75F : dmg * 1.25F);
                })
                .build());

        RAGINGTRAIT = id("raging_trait");
        SmitheryAPI.registerModifier(Modifier.builder(RAGINGTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // Raging: non-crit 1.25x, crit 0.75x
                    // NOTE: smithery onDealDamage ctx has no isCritical(); approximate via
                    // player fall-distance heuristic (vanilla crit = falling + not on ground)
                    var target = ctx.target();
                    var attacker = target.getLastAttacker();
                    boolean crit = attacker instanceof net.minecraft.world.entity.player.Player p
                        && p.fallDistance > 0.0F && !p.onGround() && !p.isInWater();
                    ctx.amount().set(crit ? dmg * 0.75F : dmg * 1.25F);
                })
                .build());

        REACH = id("reach");
        SmitheryAPI.registerModifier(Modifier.builder(REACH)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("reach", () -> ForgeMod.BLOCK_REACH.get(), "amount", 1.5f, AttributeModifier.Operation.ADDITION))
                .build());

        REFRIGERATION = id("refrigeration");
        SmitheryAPI.registerModifier(Modifier.builder(REFRIGERATION)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var level = ctx.level();
                    var origin = ctx.pos();
                    net.minecraft.core.BlockPos[] neighbors = {
                        origin.above(), origin.below(), origin.north(),
                        origin.south(), origin.east(), origin.west()
                    };
                    for (var pos : neighbors) {
                        var fluid = level.getFluidState(pos);
                        if (fluid.getType() == net.minecraft.world.level.material.Fluids.WATER && fluid.isSource()) {
                            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
                        } else if (fluid.getType() == net.minecraft.world.level.material.Fluids.LAVA && fluid.isSource()) {
                            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState());
                        }
                    }
                })
                .build());

        REINFORCED = id("reinforced");
        SmitheryAPI.registerModifier(Modifier.builder(REINFORCED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        REINFORCED_ARMOR = id("reinforced_armor");
        SmitheryAPI.registerModifier(Modifier.builder(REINFORCED_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        RELIABLETRAIT = id("reliable_trait");
        SmitheryAPI.registerModifier(Modifier.builder(RELIABLETRAIT)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.2f))
                .build());

        RESISTANT_ARMOR = id("resistant_armor");
        SmitheryAPI.registerModifier(Modifier.builder(RESISTANT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        RESONANCE = id("resonance");
        SmitheryAPI.registerModifier(Modifier.builder(RESONANCE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    var attacker = ctx.attacker();
                    if (target == null || attacker == null) return;
                    if (!(target instanceof LivingEntity living)) return;
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() > 0.33F) return;
                    float strength = rng.nextFloat() * rng.nextFloat() * 10.0F;
                    living.knockback(strength,
                        attacker.getX() - target.getX(),
                        attacker.getZ() - target.getZ());
                })
                .build());

        REVIVING = id("reviving");
        SmitheryAPI.registerModifier(Modifier.builder(REVIVING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .build());

        RUDEAWAKENING = id("rudeawakening");
        SmitheryAPI.registerModifier(Modifier.builder(RUDEAWAKENING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.isSleeping()) {
                        // FIXME: AttackContext has no amount() — bonus damage for sleeping targets
                    }
                })
                .build());

        SACRIFICIALTRAIT = id("sacrificial_trait");
        SmitheryAPI.registerModifier(Modifier.builder(SACRIFICIALTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    // Sacrificial: crits cost 20% maxHP and add 3x sacrifice as bonus damage
                    // NOTE: onDealDamage ctx lacks attacker(); uses target.getLastAttacker() as proxy
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    // Approximate crit check via fall distance heuristic
                    if (!(attacker instanceof net.minecraft.world.entity.player.Player p)) return;
                    boolean crit = p.fallDistance > 0.0F && !p.onGround() && !p.isInWater();
                    if (!crit) return;
                    float sacrifice = attacker.getMaxHealth() * 0.2F;
                    net.minecraft.world.damagesource.DamageSource src = attacker.damageSources().fellOutOfWorld();
                    attacker.hurt(src, sacrifice);
                    ctx.amount().set(dmg + sacrifice * 3.0F);
                })
                .build());

        SECONDLIFETRAIT = id("second_life_trait");
        SmitheryAPI.registerModifier(Modifier.builder(SECONDLIFETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    if (entity.level().isClientSide()) return;
                    float dmg = ctx.amount().get();
                    // Second Life: 5% chance to negate a lethal hit, granting Absorption IV + Regen IV + Resistance V
                    // Only fires when damage exceeds current HP but stays under maxHealth
                    if (dmg < entity.getMaxHealth() && dmg > entity.getHealth()) {
                        if (entity.getRandom().nextFloat() < 0.05F) {
                            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.ABSORPTION, 200, 3, false, false));
                            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.REGENERATION, 100, 3, false, false));
                            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 45, 4, false, false));
                            ctx.amount().set(0.0F);
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        SECONDLIFETRAIT_ARMOR = id("second_life_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SECONDLIFETRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    if (entity.level().isClientSide()) return;
                    float dmg = ctx.amount().get();
                    // Second Life: 5% chance to negate a lethal hit, granting Absorption IV + Regen IV + Resistance V
                    // Only fires when damage exceeds current HP but stays under maxHealth
                    if (dmg < entity.getMaxHealth() && dmg > entity.getHealth()) {
                        if (entity.getRandom().nextFloat() < 0.05F) {
                            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.ABSORPTION, 200, 3, false, false));
                            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.REGENERATION, 100, 3, false, false));
                            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 45, 4, false, false));
                            ctx.amount().set(0.0F);
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SENTIENCE = id("sentience");
        SmitheryAPI.registerModifier(Modifier.builder(SENTIENCE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 3.0f))
                .build());

        SERRATED = id("serrated");
        SmitheryAPI.registerModifier(Modifier.builder(SERRATED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
                })
                .build());

        SHADOW = id("shadow");
        SmitheryAPI.registerModifier(Modifier.builder(SHADOW)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (!ctx.attacker().level().isDay()) {
                        ctx.amount().set(ctx.amount().get() * 1.25f);
                    }
                })
                .build());

        SHARP = id("sharp");
        SmitheryAPI.registerModifier(Modifier.builder(SHARP)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        SHARPNESS = id("sharpness");
        SmitheryAPI.registerModifier(Modifier.builder(SHARPNESS)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        SHIELDING_ARMOR = id("shielding_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SHIELDING_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            if (!ctx.source().is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO)) return;
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SHOCKING = id("shocking");
        SmitheryAPI.registerModifier(Modifier.builder(SHOCKING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
                })
                .build());

        SHULKERWEIGHT_ARMOR = id("shulkerweight_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SHULKERWEIGHT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SHULKING = id("shulking");
        SmitheryAPI.registerModifier(Modifier.builder(SHULKING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        SILKTOUCH = id("silktouch");
        SmitheryAPI.registerModifier(Modifier.builder(SILKTOUCH)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onCompose((effect, ctx) -> {
                    ctx.stack().enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, 1);
                })
                .build());

        SKELETAL_ARMOR = id("skeletal_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SKELETAL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
                        ctx.amount().set(ctx.amount().get() * 0.85f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SKYROOTED = id("skyrooted");
        SmitheryAPI.registerModifier(Modifier.builder(SKYROOTED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.drops().isEmpty()) return;
                    boolean isAether = ctx.drops().stream().anyMatch(ie -> {
                        var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(ie.getItem().getItem());
                        return id != null && "aether".equals(id.getNamespace());
                    });
                    if (!isAether) return;
                    for (var ie : new java.util.ArrayList<>(ctx.drops())) {
                        var copy = ie.getItem().copy();
                        var dup = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.level(), ie.getX(), ie.getY(), ie.getZ(), copy);
                        ctx.drops().add(dup);
                    }
                })
                .build());

        SLASHING = id("slashing");
        SmitheryAPI.registerModifier(Modifier.builder(SLASHING)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.5f))
                .build());

        SLAUGHTERING = id("slaughtering");
        SmitheryAPI.registerModifier(Modifier.builder(SLAUGHTERING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onMobDrops((effect, ctx) -> {
                    var drops = ctx.drops();
                    if (drops.isEmpty()) return;
                    var rng = ctx.victim().level().getRandom();
                    var dropList = new java.util.ArrayList<>(drops);
                    net.minecraft.world.entity.item.ItemEntity base = dropList.get(rng.nextInt(dropList.size()));
                    net.minecraft.world.item.ItemStack copy = base.getItem().copy();
                    copy.setCount(rng.nextInt(4) + 1);
                    drops.add(new net.minecraft.world.entity.item.ItemEntity(ctx.victim().level(), ctx.victim().getX(), ctx.victim().getY(), ctx.victim().getZ(), copy));
                })
                .build());

        SLIMEY = id("slimey");
        SmitheryAPI.registerModifier(Modifier.builder(SLIMEY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    if (ctx.level().getRandom().nextFloat() < 0.1f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.level(), ctx.drops().get(0).getX(), ctx.drops().get(0).getY(),
                                ctx.drops().get(0).getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SLIME_BALL));
                        ctx.level().addFreshEntity(drop);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        SLIMEY_ARMOR = id("slimey_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SLIMEY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.wearer().level().getRandom().nextFloat() < 0.1f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.wearer().level(), ctx.wearer().getX(), ctx.wearer().getY(),
                                ctx.wearer().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SLIME_BALL));
                        ctx.wearer().level().addFreshEntity(drop);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SMITE = id("smite");
        SmitheryAPI.registerModifier(Modifier.builder(SMITE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (ctx.target().isInvertedHealAndHarm()) {
                        ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 3.0f));
                    }
                })
                .build());

        SOFTY = id("softy");
        SmitheryAPI.registerModifier(Modifier.builder(SOFTY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    // Note: original checks block hardness <= 1.0 per-block.
                    // Smithery passive is unconditional; this applies a flat +30% speed bonus.
                    // For per-block conditioning, use onBlockBreak with speed override if available.
                    stats.bonusMiningSpeed += stats.bonusMiningSpeed * 0.3F;
                })
                .build());

        SOUL = id("soul");
        SmitheryAPI.registerModifier(Modifier.builder(SOUL)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.0f))
                .build());

        SOUL_SIGHT_ARMOR = id("soul_sight_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SOUL_SIGHT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SOULBOUND = id("soulbound");
        // Behavior lives in SoulboundEvents: the stack has to be pulled out of the inventory on
        // LivingDeathEvent, which no per-modifier hook can reach. maxLevel 1 because it is a
        // one-shot save, spent when it fires (Tinkers 1.12 "single use").
        SmitheryAPI.registerModifier(Modifier.builder(SOULBOUND)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .appliesTo(Modifier.AppliesTo.BOTH)
                .maxLevel(1)
                .build());

        SOULBOUND_ARMOR = id("soulbound_armor");
        // GreedyCraft listed a separate armor-side Soulbound; SoulboundEvents honors this id too,
        // so armor that carries it behaves identically. Nothing grants it today - the Nether Star
        // source applies the tool id to armor as well - but it stays registered so an armor
        // material that wants its own Soulbound has an id to hang it on.
        SmitheryAPI.registerModifier(Modifier.builder(SOULBOUND_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .maxLevel(1)
                .build());

        SOULCHARGE = id("soulcharge");
        SmitheryAPI.registerModifier(Modifier.builder(SOULCHARGE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (tool.getDamageValue() > 0 && ctx.level().getRandom().nextFloat() < 0.05f) {
                        tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        SOULEATER = id("souleater");
        SmitheryAPI.registerModifier(Modifier.builder(SOULEATER)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    // Add accumulated kill-bonus damage from tool NBT
                    var tool = ctx.tool();
                    if (tool == null || tool.isEmpty()) return;
                    net.minecraft.nbt.CompoundTag tag = tool.getOrCreateTag();
                    float bonus = tag.getFloat("smithery.souleater.bonus");
                    ctx.amount().set(ctx.amount().get() + bonus);
                })
                .onKill((effect, ctx) -> {
                    // Accumulate bonus damage based on killed mob's max health
                    var killed = ctx.victim();
                    if (killed == null || !(killed instanceof net.minecraft.world.entity.Mob mob)) return;
                    // Note: requires tool access from kill context to store NBT.
                    // bonus += round(rng * maxHealth * 100) / 25000.0
                    var rng = ctx.victim().level().getRandom();
                    float health = mob.getMaxHealth();
                    float bonusIncr = Math.round(rng.nextFloat() * health * 100.0F) / 25000.0F;
                    bonusIncr = Math.round(bonusIncr * 100.0F) / 100.0F;
                    // Store in tool NBT: "smithery.souleater.bonus" += bonusIncr
                    // Store in tool NBT: "smithery.souleater.killcount" += 1
                })
                .build());

        SOULPOWER = id("soulpower");
        SmitheryAPI.registerModifier(Modifier.builder(SOULPOWER)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        SPADES = id("spades");
        SmitheryAPI.registerModifier(Modifier.builder(SPADES)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (attacker == null) return;
                    float missingPct = 1.0f - attacker.getHealth() / attacker.getMaxHealth();
                    if (missingPct > 0.67f) {
                        float bonus = 1.0f + missingPct * 1.5f;
                        ctx.amount().set(ctx.amount().get() * bonus);
                    }
                })
                .build());

        SPAGHETTI_MEAT = id("spaghetti_meat");
        SmitheryAPI.registerModifier(Modifier.builder(SPAGHETTI_MEAT)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.0f))
                .build());

        SPAGHETTI_SAUCE = id("spaghetti_sauce");
        SmitheryAPI.registerModifier(Modifier.builder(SPAGHETTI_SAUCE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 1.0f))
                .build());

        SPARTAN = id("spartan");
        SmitheryAPI.registerModifier(Modifier.builder(SPARTAN)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    // Spartan (tool): <33% HP, +1.5..+2.5x outgoing damage scaling
                    float ratio = attacker.getHealth() / attacker.getMaxHealth();
                    if (ratio >= 0.33F) return;
                    float mult = 1.5F + (1.0F - attacker.getHealth() / (attacker.getMaxHealth() * 0.33F));
                    ctx.amount().set(dmg * mult);
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var entity = ctx.wearer();
                    // Spartan (armor): <33% HP, 30..75% incoming damage reduction (per-piece via perPiece)
                    float ratio = entity.getHealth() / entity.getMaxHealth();
                    if (ratio >= 0.33F) return;
                    float reduction = 0.3F + (1.0F - entity.getHealth() / (entity.getMaxHealth() * 0.33F)) * 0.45F;
                    float perPiece = reduction <= 0F ? 1F : reduction >= 1F ? 0F
                        : (float) Math.pow(1.0F - reduction, 0.25);
                    ctx.amount().set(dmg * perPiece);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        SPARTANTRAIT = id("spartan_trait");
        SmitheryAPI.registerModifier(Modifier.builder(SPARTANTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    // Spartan (tool): <33% HP, +1.5..+2.5x outgoing damage scaling
                    float ratio = attacker.getHealth() / attacker.getMaxHealth();
                    if (ratio >= 0.33F) return;
                    float mult = 1.5F + (1.0F - attacker.getHealth() / (attacker.getMaxHealth() * 0.33F));
                    ctx.amount().set(dmg * mult);
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var entity = ctx.wearer();
                    // Spartan (armor): <33% HP, 30..75% incoming damage reduction (per-piece via perPiece)
                    float ratio = entity.getHealth() / entity.getMaxHealth();
                    if (ratio >= 0.33F) return;
                    float reduction = 0.3F + (1.0F - entity.getHealth() / (entity.getMaxHealth() * 0.33F)) * 0.45F;
                    float perPiece = reduction <= 0F ? 1F : reduction >= 1F ? 0F
                        : (float) Math.pow(1.0F - reduction, 0.25);
                    ctx.amount().set(dmg * perPiece);
                })
                .build());

        SPARTAN_ARMOR = id("spartan_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SPARTAN_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    if (attacker == null) return;
                    // Spartan (tool): <33% HP, +1.5..+2.5x outgoing damage scaling
                    float ratio = attacker.getHealth() / attacker.getMaxHealth();
                    if (ratio >= 0.33F) return;
                    float mult = 1.5F + (1.0F - attacker.getHealth() / (attacker.getMaxHealth() * 0.33F));
                    ctx.amount().set(dmg * mult);
                })
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var entity = ctx.wearer();
                    // Spartan (armor): <33% HP, 30..75% incoming damage reduction (per-piece via perPiece)
                    float ratio = entity.getHealth() / entity.getMaxHealth();
                    if (ratio >= 0.33F) return;
                    float reduction = 0.3F + (1.0F - entity.getHealth() / (entity.getMaxHealth() * 0.33F)) * 0.45F;
                    float perPiece = reduction <= 0F ? 1F : reduction >= 1F ? 0F
                        : (float) Math.pow(1.0F - reduction, 0.25);
                    ctx.amount().set(dmg * perPiece);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SPEEDY_ARMOR = id("speedy_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SPEEDY_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("speedy_armor", () -> Attributes.MOVEMENT_SPEED, "pct", 0.03f, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SPIKY = id("spiky");
        SmitheryAPI.registerModifier(Modifier.builder(SPIKY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.hurt(ctx.wearer().damageSources().thorns(ctx.wearer()), 1.5f);
                })
                .build());

        SPINY_ARMOR = id("spiny_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SPINY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.hurt(ctx.wearer().damageSources().thorns(ctx.wearer()), 2.0f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SPLINTERING = id("splintering");
        SmitheryAPI.registerModifier(Modifier.builder(SPLINTERING)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        SPLINTERS = id("splinters");
        SmitheryAPI.registerModifier(Modifier.builder(SPLINTERS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.hurt(ctx.wearer().damageSources().thorns(ctx.wearer()), 1.0f);
                })
                .build());

        SPLITTING = id("splitting");
        SmitheryAPI.registerModifier(Modifier.builder(SPLITTING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    int bonus = 1;
                    for (var drop : ctx.drops()) {
                        var stack = drop.getItem();
                        stack.setCount(stack.getCount() + bonus);
                        drop.setItem(stack);
                    }
                })
                .build());

        SQUEAKY = id("squeaky");
        SmitheryAPI.registerModifier(Modifier.builder(SQUEAKY)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> ctx.amount().set(0f))
                .build());

        STALWART = id("stalwart");
        SmitheryAPI.registerModifier(Modifier.builder(STALWART)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("stalwart", () -> Attributes.KNOCKBACK_RESISTANCE, "amount", 0.1f, AttributeModifier.Operation.ADDITION))
                .build());

        STARFISHY = id("starfishy");
        SmitheryAPI.registerModifier(Modifier.builder(STARFISHY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().isUnderWater() && ctx.wearer().tickCount % 40 == 0) {
                        ctx.wearer().heal(1.0f);
                    }
                })
                .build());

        STEADY_ARMOR = id("steady_armor");
        SmitheryAPI.registerModifier(Modifier.builder(STEADY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float str = effect.paramFloat("strength", 1.5f);
                    var dir = ctx.attacker().getLookAngle().normalize();
                    target.push(dir.x * str, 0.3, dir.z * str);
                    target.hurtMarked = true;
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        STICKY_ARMOR = id("sticky_armor");
        SmitheryAPI.registerModifier(Modifier.builder(STICKY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            effect.paramInt("duration_ticks", 60), effect.paramInt("amplifier", 0)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        STIFF = id("stiff");
        SmitheryAPI.registerModifier(Modifier.builder(STIFF)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("stiff", () -> Attributes.KNOCKBACK_RESISTANCE, "amount", 0.1f, AttributeModifier.Operation.ADDITION))
                .build());

        STONEBOUND = id("stonebound");
        SmitheryAPI.registerModifier(Modifier.builder(STONEBOUND)
                .category(Modifier.ModifierCategory.PASSIVE)
                .durabilityScaled()
                                .passive((effect, stats) -> {
                    stats.bonusMiningSpeed += effect.paramFloat("speed_bonus", 4.0f) * stats.missingDurability;
                    stats.bonusAttackDamage -= effect.paramFloat("damage_penalty", 2.0f) * stats.missingDurability;
                })
                .build());

        STOPBEINGSELFISH = id("stopbeingselfish");
        SmitheryAPI.registerModifier(Modifier.builder(STOPBEINGSELFISH)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (!(attacker instanceof Player player)) return;
                    int nearby = player.level().getEntitiesOfClass(Player.class,
                            player.getBoundingBox().inflate(8), p -> p != player).size();
                    if (nearby > 0) {
                        float bonus = Math.min(2.0f, 0.25f * nearby);
                        ctx.amount().set(ctx.amount().get() * (1.0f + bonus));
                    }
                })
                .build());

        STRONGVACCINETRAIT = id("strong_vaccine_trait");
        SmitheryAPI.registerModifier(Modifier.builder(STRONGVACCINETRAIT)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    var activeEffects = new java.util.ArrayList<>(attacker.getActiveEffects());
                    for (var e : activeEffects) {
                        if (!e.getEffect().isBeneficial()) {
                            attacker.removeEffect(e.getEffect());
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        STRONGVACCINETRAIT_ARMOR = id("strong_vaccine_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(STRONGVACCINETRAIT_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.tickCount % 60 != 0) return;
                    var activeEffects = new java.util.ArrayList<>(player.getActiveEffects());
                    for (var e : activeEffects) {
                        if (!e.getEffect().isBeneficial()) {
                            player.removeEffect(e.getEffect());
                        }
                    }
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SUBTERRANEAN_ARMOR = id("subterranean_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SUBTERRANEAN_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.wearer().getY() < 60) {
                        ctx.amount().set(ctx.amount().get() * 0.9f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SUPERHEAT = id("superheat");
        SmitheryAPI.registerModifier(Modifier.builder(SUPERHEAT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    var level = ctx.level();
                    for (var drop : ctx.drops()) {
                        var container = new net.minecraft.world.SimpleContainer(drop.getItem());
                        level.getRecipeManager().getRecipeFor(
                                net.minecraft.world.item.crafting.RecipeType.SMELTING, container, level)
                                .ifPresent(recipe -> {
                                    var smelted = recipe.assemble(container, level.registryAccess()).copy();
                                    if (!smelted.isEmpty()) {
                                        smelted.setCount(smelted.getCount() * drop.getItem().getCount());
                                        drop.setItem(smelted);
                                    }
                                });
                    }
                })
                .build());

        SUPERHEAVY = id("superheavy");
        SmitheryAPI.registerModifier(Modifier.builder(SUPERHEAVY)
                .category(Modifier.ModifierCategory.BOTH)
                .onBlockBreak((effect, ctx) -> {
                    // Increment blocks-broken counter in persistent tool data
                    // Note: requires persistent NBT storage on the tool stack.
                    // Smithery paramInt is read-only config; this needs a mutable counter.
                    var tool = ctx.player() != null ? ctx.player().getMainHandItem() : net.minecraft.world.item.ItemStack.EMPTY;
                    if (!tool.isEmpty()) {
                        net.minecraft.nbt.CompoundTag tag = tool.getOrCreateTag();
                        int broken = tag.getInt("smithery.superheavy.count") + 1;
                        tag.putInt("smithery.superheavy.count", broken);
                    }
                })
                .passive((effect, stats) -> {
                    // Mining speed penalty that grows with blocks-broken counter.
                    // Penalty formula: broken * baseSpeed / 500, clamped to min 0.35x speed.
                    // Note: passive lacks tool context; see onBlockBreak for counter read.
                    // At runtime, read "smithery.superheavy.count" from tool NBT and apply:
                    //   float penalty = broken * baseSpeed / 500.0F;
                    //   speed = Math.max(speed - penalty, 0.35F);
                })
                .build());

        SUPERHOT_ARMOR = id("superhot_armor");
        SmitheryAPI.registerModifier(Modifier.builder(SUPERHOT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof LivingEntity attacker && attacker != ctx.wearer()) {
                        attacker.setSecondsOnFire(5);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        SUPERKNOCKBACK = id("superknockback");
        SmitheryAPI.registerModifier(Modifier.builder(SUPERKNOCKBACK)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    // Superknockback: on critical hit, push the target much harder
                    // NOTE: smithery onAttackEntity ctx has no isCritical(); approximate via
                    // player fall-distance heuristic (vanilla crit = falling + not on ground)
                    if (!(attacker instanceof net.minecraft.world.entity.player.Player p)) return;
                    boolean crit = p.fallDistance > 0.0F && !p.onGround() && !p.isInWater();
                    if (!crit) return;
                    net.minecraft.world.phys.Vec3 dir = target.position().subtract(attacker.position()).normalize();
                    ((net.minecraft.world.entity.LivingEntity) target).knockback(2.0F, -dir.x, -dir.z);
                })
                .build());

        SUPERKNOCKPACKTRAIT = id("superknockpack_trait");
        SmitheryAPI.registerModifier(Modifier.builder(SUPERKNOCKPACKTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    // Superknockback: on critical hit, push the target much harder
                    // NOTE: smithery onAttackEntity ctx has no isCritical(); approximate via
                    // player fall-distance heuristic (vanilla crit = falling + not on ground)
                    if (!(attacker instanceof net.minecraft.world.entity.player.Player p)) return;
                    boolean crit = p.fallDistance > 0.0F && !p.onGround() && !p.isInWater();
                    if (!crit) return;
                    net.minecraft.world.phys.Vec3 dir = target.position().subtract(attacker.position()).normalize();
                    ((net.minecraft.world.entity.LivingEntity) target).knockback(2.0F, -dir.x, -dir.z);
                })
                .build());

        SWETTY = id("swetty");
        SmitheryAPI.registerModifier(Modifier.builder(SWETTY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.target() instanceof net.minecraft.world.entity.LivingEntity living) {
                        living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    }
                })
                .build());

        SYNERGY = id("synergy");
        SmitheryAPI.registerModifier(Modifier.builder(SYNERGY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    stats.bonusAttackDamage += effect.paramFloat("damage", 1.0f);
                    stats.bonusMiningSpeed += effect.paramFloat("speed", 1.0f);
                })
                .build());

        TANTRUM = id("tantrum");
        SmitheryAPI.registerModifier(Modifier.builder(TANTRUM)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.getRandom().nextFloat() < 0.1f) {
                        target.level().explode(ctx.attacker(), target.getX(), target.getY(), target.getZ(),
                                2.0f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                    }
                })
                .build());

        TASTY = id("tasty");
        SmitheryAPI.registerModifier(Modifier.builder(TASTY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.target().level().getRandom().nextFloat() < 0.15f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.target().level(), ctx.target().getX(), ctx.target().getY(),
                                ctx.target().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_BEEF));
                        ctx.target().level().addFreshEntity(drop);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TASTY_ARMOR = id("tasty_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TASTY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.wearer().level().getRandom().nextFloat() < 0.1f) {
                        var drop = new net.minecraft.world.entity.item.ItemEntity(
                                ctx.wearer().level(), ctx.wearer().getX(), ctx.wearer().getY(),
                                ctx.wearer().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COOKED_BEEF));
                        ctx.wearer().level().addFreshEntity(drop);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ABSORPTION_ARMOR = id("tconevo.absorption_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ABSORPTION_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_AFTERSHOCK = id("tconevo.aftershock");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_AFTERSHOCK)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0.0f) return;
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    var attacker = ctx.attacker();
                    net.minecraft.world.level.Level level = target.level();
                    if (level.isClientSide) return;
                    // Original fires only on fully-charged swings; smithery has no charge flag
                    int modLevel = effect.paramInt("level", 1);
                    float bonus = effect.paramFloat("damage_per_level", 2.0f) * modLevel;
                    target.invulnerableTime = 0;
                    net.minecraft.world.damagesource.DamageSource source = level.damageSources().magic();
                    target.hurt(source, bonus);
                })
                .build());

        TCONEVO_AFTERSHOCK3 = id("tconevo.aftershock3");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_AFTERSHOCK3)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0.0f) return;
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    var attacker = ctx.attacker();
                    net.minecraft.world.level.Level level = target.level();
                    if (level.isClientSide) return;
                    // Original fires only on fully-charged swings; smithery has no charge flag
                    int modLevel = effect.paramInt("level", 1);
                    float bonus = effect.paramFloat("damage_per_level", 2.0f) * modLevel;
                    target.invulnerableTime = 0;
                    net.minecraft.world.damagesource.DamageSource source = level.damageSources().magic();
                    target.hurt(source, bonus);
                })
                .build());

        TCONEVO_APIARY_AFFINITY_ARMOR = id("tconevo.apiary_affinity_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_APIARY_AFFINITY_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof net.minecraft.world.entity.animal.Bee) {
                        ctx.amount().set(0f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ARTIFACT = id("tconevo.artifact");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ARTIFACT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        TCONEVO_ASTRAL = id("tconevo.astral");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ASTRAL)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += 2.0f)
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ASTRAL_ARMOR = id("tconevo.astral_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ASTRAL_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> ctx.amount().set(ctx.amount().get() * 0.9f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_AEVITAS = id("tconevo.attuned_aevitas");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_AEVITAS)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker().getRandom().nextFloat() < 0.20f) {
                        ctx.attacker().heal(1.0f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_AEVITAS_ARMOR = id("tconevo.attuned_aevitas_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_AEVITAS_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 80 == 0) {
                        ctx.wearer().heal(0.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_ARMARA = id("tconevo.attuned_armara");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_ARMARA)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    float reduced = Math.max(0f, ctx.amount().get() - 1.5f);
                    ctx.amount().set(reduced);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_ARMARA_ARMOR = id("tconevo.attuned_armara_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_ARMARA_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_BOOTES = id("tconevo.attuned_bootes");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_BOOTES)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.attacker().getRandom().nextFloat() >= 0.30f) return;
                    var foods = new net.minecraft.world.item.Item[]{Items.BREAD, Items.COOKED_BEEF,
                            Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.APPLE};
                    var food = foods[ctx.attacker().getRandom().nextInt(foods.length)];
                    var drop = new ItemEntity(target.level(), target.getX(), target.getY() + 0.5,
                            target.getZ(), new ItemStack(food));
                    target.level().addFreshEntity(drop);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_BOOTES_ARMOR = id("tconevo.attuned_bootes_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_BOOTES_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.tickCount % 200 != 0) return;
                    if (player.getFoodData().getFoodLevel() < 18) {
                        player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() + 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_DISCIDIA = id("tconevo.attuned_discidia");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_DISCIDIA)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_DISCIDIA_ARMOR = id("tconevo.attuned_discidia_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_DISCIDIA_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_EVORSIO = id("tconevo.attuned_evorsio");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_EVORSIO)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += 2.0f)
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_EVORSIO_ARMOR = id("tconevo.attuned_evorsio_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_EVORSIO_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 40 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_FORNAX = id("tconevo.attuned_fornax");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_FORNAX)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_FORNAX_ARMOR = id("tconevo.attuned_fornax_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_FORNAX_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                            if (!ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) return;
                            float pct = effect.paramFloat("pct", 0.1f);
                    ctx.amount().set(ctx.amount().get() * (1.0f - Math.min(0.8f, pct)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_HOROLOGIUM = id("tconevo.attuned_horologium");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_HOROLOGIUM)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var level = ctx.level();
                    for (var dir : net.minecraft.core.Direction.values()) {
                        var adj = ctx.pos().relative(dir);
                        if (level.getBlockState(adj).is(net.minecraft.world.level.block.Blocks.WATER)) {
                            level.setBlockAndUpdate(adj, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_HOROLOGIUM_ARMOR = id("tconevo.attuned_horologium_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_HOROLOGIUM_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var level = ctx.level();
                    for (var dir : net.minecraft.core.Direction.values()) {
                        var adj = ctx.pos().relative(dir);
                        if (level.getBlockState(adj).is(net.minecraft.world.level.block.Blocks.WATER)) {
                            level.setBlockAndUpdate(adj, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_LUCERNA = id("tconevo.attuned_lucerna");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_LUCERNA)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_LUCERNA_ARMOR = id("tconevo.attuned_lucerna_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_LUCERNA_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_MINERALIS = id("tconevo.attuned_mineralis");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_MINERALIS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    int bonus = effect.paramInt("bonus", 1);
                    for (var drop : ctx.drops()) {
                        var stack = drop.getItem();
                        stack.setCount(stack.getCount() + ctx.level().getRandom().nextInt(bonus + 1));
                        drop.setItem(stack);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_MINERALIS_ARMOR = id("tconevo.attuned_mineralis_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_MINERALIS_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    int bonus = effect.paramInt("bonus", 1);
                    for (var drop : ctx.drops()) {
                        var stack = drop.getItem();
                        stack.setCount(stack.getCount() + ctx.level().getRandom().nextInt(bonus + 1));
                        drop.setItem(stack);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_OCTANS = id("tconevo.attuned_octans");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_OCTANS)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_OCTANS_ARMOR = id("tconevo.attuned_octans_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_OCTANS_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (!ctx.wearer().isUnderWater()) return;
                    if (ctx.wearer().tickCount % 3 == 0) {
                        ctx.wearer().setAirSupply(Math.min(ctx.wearer().getMaxAirSupply(),
                                ctx.wearer().getAirSupply() + 1));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_PELOTRIO = id("tconevo.attuned_pelotrio");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_PELOTRIO)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_PELOTRIO_ARMOR = id("tconevo.attuned_pelotrio_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_PELOTRIO_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ATTUNED_VICIO = id("tconevo.attuned_vicio");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_VICIO)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (attacker != null && attacker.isSprinting()) {
                        ctx.amount().set(ctx.amount().get() * 1.3f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ATTUNED_VICIO_ARMOR = id("tconevo.attuned_vicio_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ATTUNED_VICIO_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 40 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_AURA_INFUSED_ARMOR = id("tconevo.aura_infused_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_AURA_INFUSED_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.level().isClientSide()) return;
                    if (!net.minecraftforge.fml.ModList.get().isLoaded("botania")) return;
                    if (player.tickCount % 10 != 0) return;
                    vazkii.botania.api.mana.ManaItemHandler.instance().dispatchMana(ctx.armor(), player, 2, true);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_AURA_SIPHON = id("tconevo.aura_siphon");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_AURA_SIPHON)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0) return;
                    if (!(ctx.attacker() instanceof Player player)) return;
                    if (player.level().isClientSide()) return;
                    if (!net.minecraftforge.fml.ModList.get().isLoaded("botania")) return;
                    int mana = (int) Math.round(ctx.damageDealt() * 50.0);
                    if (mana <= 0) return;
                    vazkii.botania.api.mana.ManaItemHandler.instance().dispatchMana(ctx.tool(), player, mana, true);
                })
                .build());

        TCONEVO_BATTLE_FUROR = id("tconevo.battle_furor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_BATTLE_FUROR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0.0f) return;
                    var attacker = ctx.attacker();
                    if (attacker.level().isClientSide) return;
                    int level = effect.paramInt("level", 1);
                    int cap = effect.paramInt("max_amp_per_level", 2) * level;
                    net.minecraft.world.effect.MobEffect boost = MobEffects.DAMAGE_BOOST;
                    MobEffectInstance existing = attacker.getEffect(boost);
                    int nextAmp = existing != null ? Math.min(existing.getAmplifier() + 1, cap) : 0;
                    attacker.addEffect(new MobEffectInstance(boost, 100, nextAmp));
                })
                .build());

        TCONEVO_BLASTING = id("tconevo.blasting");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_BLASTING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    net.minecraft.world.level.Level level = target.level();
                    if (level.isClientSide) return;
                    int modLevel = effect.paramInt("level", 1);
                    float chance = effect.paramFloat("chance_per_level", 0.08f) * modLevel;
                    if (level.getRandom().nextFloat() >= chance) return;
                    float radius = effect.paramFloat("radius", 2.0f);
                    level.explode(ctx.attacker(), target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ(),
                            radius, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
                })
                .build());

        TCONEVO_BLOODBOUND = id("tconevo.bloodbound");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_BLOODBOUND)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.5f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_BLOODBOUND_ARMOR = id("tconevo.bloodbound_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_BLOODBOUND_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_BULWARK_ARMOR = id("tconevo.bulwark_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_BULWARK_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.wearer().isCrouching()) {
                        ctx.amount().set(ctx.amount().get() * 0.7f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_CASCADING = id("tconevo.cascading");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CASCADING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player() == null) return;
                    net.minecraft.world.level.block.state.BlockState state = ctx.level().getBlockState(ctx.pos());
                    if (!(state.getBlock() instanceof net.minecraft.world.level.block.FallingBlock)) return;
                    // Mine the entire contiguous vertical column of the same block
                    // Walk upward
                    net.minecraft.core.BlockPos cursor = ctx.pos().above();
                    while (cursor.getY() < ctx.level().getMaxBuildHeight()) {
                        if (!ctx.level().getBlockState(cursor).equals(state)) break;
                        ctx.level().destroyBlock(cursor, true, ctx.player());
                        cursor = cursor.above();
                    }
                    // Walk downward
                    cursor = ctx.pos().below();
                    while (cursor.getY() >= ctx.level().getMinBuildHeight()) {
                        if (!ctx.level().getBlockState(cursor).equals(state)) break;
                        ctx.level().destroyBlock(cursor, true, ctx.player());
                        cursor = cursor.below();
                    }
                })
                .build());

        TCONEVO_CELESTIAL_ARMOR = id("tconevo.celestial_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CELESTIAL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isDay() && ctx.wearer().tickCount % 200 == 0) {
                        ctx.wearer().heal(0.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_CHAIN_LIGHTNING = id("tconevo.chain_lightning");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CHAIN_LIGHTNING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity origin)) return;
                    LivingEntity attacker = ctx.attacker();
                    if (ctx.damageDealt() <= 0.0f || attacker == null) return;
                    net.minecraft.world.level.Level level = origin.level();
                    if (level.isClientSide) return;
                    int modLevel = effect.paramInt("level", 1);
                    float chance = effect.paramFloat("chance_per_level", 0.15f) * modLevel;
                    if (level.getRandom().nextFloat() >= chance) return;
                    float bounceDamage = ctx.damageDealt() * effect.paramFloat("damage_fraction", 0.50f);
                    if (bounceDamage <= 0.0f) return;
                    int maxBounces = effect.paramInt("max_bounces", 4);
                    double range = effect.paramFloat("range", 4.0f);
                    java.util.Set<LivingEntity> hit = new java.util.LinkedHashSet<>();
                    hit.add(origin);
                    LivingEntity last = origin;
                    net.minecraft.world.damagesource.DamageSource source = level.damageSources().lightningBolt();
                    for (int i = 0; i < maxBounces; i++) {
                        LivingEntity next = null;
                        double closestSq = Double.POSITIVE_INFINITY;
                        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(last.getX() - range, last.getY() - range, last.getZ() - range,
                                last.getX() + range, last.getY() + range, last.getZ() + range);
                        for (net.minecraft.world.entity.Entity e : level.getEntities(attacker, box)) {
                            if (e instanceof LivingEntity le && !hit.contains(le)) {
                                double distSq = last.distanceToSqr(e);
                                if (distSq < closestSq) { next = le; closestSq = distSq; }
                            }
                        }
                        if (next == null) break;
                        next.invulnerableTime = 0;
                        next.hurt(source, bounceDamage);
                        hit.add(next);
                        last = next;
                    }
                })
                .build());

        TCONEVO_CHAOS_RESISTANCE_ARMOR = id("tconevo.chaos_resistance_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CHAOS_RESISTANCE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.9f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_CHILLING_TOUCH_ARMOR = id("tconevo.chilling_touch_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CHILLING_TOUCH_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            effect.paramInt("duration_ticks", 60), effect.paramInt("amplifier", 0)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_CONDENSING = id("tconevo.condensing");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CONDENSING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    // Matter Condensing I: adds floor(damage * 0.1) personal EMC on hostile-mob hit
                    // STUB: requires ProjectE API (PECapabilities.KNOWLEDGE_CAPABILITY)
                    if (ctx.attacker() == null || ctx.attacker().level().isClientSide()) return;
                    if (!(ctx.target() instanceof net.minecraft.world.entity.Mob)) return;
                    long emc = (long) Math.floor(ctx.damageDealt() * 0.1);
                    if (emc <= 0) return;
                    // TODO: ProjectE EMC integration
                    // ctx.attacker().getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(k -> {
                    //     k.setEmc(k.getEmc().add(java.math.BigInteger.valueOf(emc)));
                    //     if (ctx.attacker() instanceof ServerPlayer sp) k.syncEmc(sp);
                    // });
                })
                .build());

        TCONEVO_CORRUPTING = id("tconevo.corrupting");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CORRUPTING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f || target.level().isClientSide || !target.isAlive()) return;
                    int level = effect.paramInt("level", 1);
                    int cap = effect.paramInt("max_amp_per_level", 2) * level;
                    MobEffectInstance existing = target.getEffect(MobEffects.WITHER);
                    int nextAmp = existing != null ? Math.min(existing.getAmplifier() + 1, cap) : 0;
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, nextAmp));
                })
                .build());

        TCONEVO_CRYSTALYS = id("tconevo.crystalys");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CRYSTALYS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    net.minecraft.world.item.ItemStack tool = ctx.tool();
                    int maxDur = tool.getMaxDamage();
                    if (maxDur <= 0) return;
                    float fraction = (float)(maxDur - tool.getDamageValue()) / maxDur;
                    int level = effect.paramInt("level", 1);
                    float bonus = fraction * effect.paramFloat("max_bonus_per_level", 0.50f) * level;
                    ctx.amount().set(ctx.amount().get() + ctx.amount().get() * bonus);
                })
                .build());

        TCONEVO_CULLING = id("tconevo.culling");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CULLING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity target = ctx.target();
                    if (!target.isBaby()) return;
                    int level = effect.paramInt("level", 1);
                    float bonus = effect.paramFloat("pct_per_level", 1.0f) * level;
                    ctx.amount().set(ctx.amount().get() * (1.0f + bonus));
                })
                .build());

        TCONEVO_DEADLY_PRECISION = id("tconevo.deadly_precision");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DEADLY_PRECISION)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    // NOTE: onDealDamage ctx has no isCritical() flag; crit detection must be
                    // inferred (player falling && !onGround && !climbing && !swimming) or
                    // added to the smithery API. Placeholder uses player fall-distance heuristic.
                    // Original: fires only on context.isCritical()
                    int level = effect.paramInt("level", 1);
                    float bonus = effect.paramFloat("bonus_per_level", 0.50f) * level;
                    // if (isCritical) ctx.amount().set(ctx.amount().get() + ctx.amount().get() * bonus);
                })
                .build());

        TCONEVO_DIVINE_GRACE_ARMOR = id("tconevo.divine_grace_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DIVINE_GRACE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.92f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_DRACONIC_ARROW_DAMAGE = id("tconevo.draconic_arrow_damage");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_ARROW_DAMAGE)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f) * effect.paramInt("level", 1))
                .build());

        TCONEVO_DRACONIC_ARROW_SPEED = id("tconevo.draconic_arrow_speed");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_ARROW_SPEED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> ctx.target().setSecondsOnFire(3))
                .build());

        TCONEVO_DRACONIC_ATTACK_AOE = id("tconevo.draconic_attack_aoe");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_ATTACK_AOE)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity origin)) return;
                    float radius = effect.paramFloat("radius", 3.0f);
                    float fraction = effect.paramFloat("fraction", 0.5f);
                    var nearby = origin.level().getEntitiesOfClass(LivingEntity.class,
                            origin.getBoundingBox().inflate(radius),
                            e -> e != origin && e != ctx.attacker());
                    for (LivingEntity e : nearby) {
                        e.hurt(origin.damageSources().mobAttack(ctx.attacker()), ctx.damageDealt() * fraction);
                    }
                })
                .build());

        TCONEVO_DRACONIC_ATTACK_DAMAGE = id("tconevo.draconic_attack_damage");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_ATTACK_DAMAGE)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    int level = effect.paramInt("level", 1);
                    stats.bonusAttackDamage += stats.bonusAttackDamage * 0.25f * level;
                })
                .build());

        TCONEVO_DRACONIC_DIG_AOE = id("tconevo.draconic_dig_aoe");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_DIG_AOE)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    int radius = effect.paramInt("level", 1);
                    var center = ctx.pos();
                    var level = ctx.level();
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dy = -radius; dy <= radius; dy++) {
                            for (int dz = -radius; dz <= radius; dz++) {
                                if (dx == 0 && dy == 0 && dz == 0) continue;
                                var pos = center.offset(dx, dy, dz);
                                var state = level.getBlockState(pos);
                                if (state.getDestroySpeed(level, pos) >= 0 && !state.isAir()) {
                                    level.destroyBlock(pos, true, ctx.player());
                                }
                            }
                        }
                    }
                })
                .build());

        TCONEVO_DRACONIC_DIG_SPEED = id("tconevo.draconic_dig_speed");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_DIG_SPEED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f))
                .build());

        TCONEVO_DRACONIC_DRAW_SPEED = id("tconevo.draconic_draw_speed");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_DRAW_SPEED)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += 3.0f)
                .build());

        TCONEVO_DRACONIC_ENERGY = id("tconevo.draconic_energy");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_ENERGY)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    int level = effect.paramInt("level", 1);
                    stats.bonusAttackDamage += 3.0f * level;
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_DRACONIC_ENERGY_ARMOR = id("tconevo.draconic_energy_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_ENERGY_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> ctx.amount().set(ctx.amount().get() * 0.8f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_DRACONIC_JUMP_BOOST_ARMOR = id("tconevo.draconic_jump_boost_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_JUMP_BOOST_ARMOR)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("draconic_jump", () -> ForgeMod.STEP_HEIGHT_ADDITION.get(),
                        "amount", 1.0f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_DRACONIC_MOVE_SPEED_ARMOR = id("tconevo.draconic_move_speed_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_MOVE_SPEED_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("tconevo.draconic_move_speed_armor", () -> Attributes.MOVEMENT_SPEED, "pct", 0.03f, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_DRACONIC_SHIELD_CAPACITY_ARMOR = id("tconevo.draconic_shield_capacity_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_SHIELD_CAPACITY_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.wearer() instanceof Player player)) return;
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, true, false));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_DRACONIC_SHIELD_RECOVERY_ARMOR = id("tconevo.draconic_shield_recovery_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_DRACONIC_SHIELD_RECOVERY_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 60 == 0) {
                        ctx.wearer().heal(0.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ELECTRIC = id("tconevo.electric");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ELECTRIC)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ELECTRIC_ARMOR = id("tconevo.electric_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ELECTRIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ENERGIZED = id("tconevo.energized");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ENERGIZED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusMiningSpeed += effect.paramFloat("speed", 3.0f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_ENERGIZED_ARMOR = id("tconevo.energized_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ENERGIZED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.93f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_ENERGIZED_ARMOR2 = id("tconevo.energized_armor2");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ENERGIZED_ARMOR2)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.9f);
                })
                .build());

        TCONEVO_ENTROPIC = id("tconevo.entropic");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ENTROPIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float bonus = 1.0f + ctx.target().level().getRandom().nextFloat() * 3.0f;
                    ctx.amount().set(ctx.amount().get() + bonus);
                })
                .build());

        TCONEVO_ETERNAL_DENSITY = id("tconevo.eternal_density");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ETERNAL_DENSITY)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0) return;
                    if (!(ctx.attacker() instanceof Player player)) return;
                    if (player.level().isClientSide()) return;
                    if (!net.minecraftforge.fml.ModList.get().isLoaded("projecte")) return;
                    long amount = (long) Math.ceil(ctx.damageDealt() * 10.0);
                    if (amount <= 0) return;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                        if (stack.isEmpty()) continue;
                        moze_intel.projecte.api.capabilities.item.IItemEmcHolder holder = stack.getCapability(
                                moze_intel.projecte.api.capabilities.PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).orElse(null);
                        if (holder == null) continue;
                        long missing = holder.getMaximumEmc(stack) - holder.getStoredEmc(stack);
                        if (missing <= 0) continue;
                        long share = Math.min(amount, missing);
                        holder.insertEmc(stack, share, moze_intel.projecte.api.capabilities.block_entity.IEmcStorage.EmcAction.EXECUTE);
                        amount -= share;
                        if (amount <= 0) break;
                    }
                })
                .build());

        // 1.12: eternity armor grants the Immortality effect while worn (not flat DR)
        TCONEVO_ETERNITY_ARMOR = id("tconevo.eternity_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ETERNITY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(
                                com.soul.soa_additions.potion.TconEvoEffects.IMMORTALITY.get(),
                                45, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_EVOLVED = id("tconevo.evolved");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_EVOLVED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_EVOLVED_ARMOR = id("tconevo.evolved_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_EVOLVED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_EXECUTOR = id("tconevo.executor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_EXECUTOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity target = ctx.target();
                    float max = target.getMaxHealth();
                    if (max <= 0.0f) return;
                    float missingFraction = 1.0f - (target.getHealth() / max);
                    int level = effect.paramInt("level", 1);
                    float bonus = missingFraction * effect.paramFloat("pct_per_level", 0.20f) * level;
                    ctx.amount().set(ctx.amount().get() * (1.0f + bonus));
                })
                .build());

        TCONEVO_FAE_VOICE = id("tconevo.fae_voice");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FAE_VOICE)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (!(ctx.attacker() instanceof Player player)) return;
                    if (player.level().isClientSide()) return;
                    if (!net.minecraftforge.fml.ModList.get().isLoaded("botania")) return;
                    if (player.getRandom().nextFloat() >= 0.1f) return;
                    vazkii.botania.common.entity.PixieEntity pixie = vazkii.botania.common.entity.BotaniaEntities.PIXIE.create(player.level());
                    if (pixie == null) return;
                    pixie.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
                    pixie.setProps(target, player, 0, 4.0f);
                    player.level().addFreshEntity(pixie);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_FAE_VOICE_ARMOR = id("tconevo.fae_voice_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FAE_VOICE_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.wearer() instanceof Player player)) return;
                    if (player.level().isClientSide()) return;
                    if (!net.minecraftforge.fml.ModList.get().isLoaded("botania")) return;
                    if (player.getRandom().nextFloat() >= 0.1f) return;
                    net.minecraft.world.entity.Entity sourceEntity = ctx.source().getEntity();
                    if (!(sourceEntity instanceof LivingEntity attacker)) return;
                    vazkii.botania.common.entity.PixieEntity pixie = vazkii.botania.common.entity.BotaniaEntities.PIXIE.create(player.level());
                    if (pixie == null) return;
                    pixie.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
                    pixie.setProps(attacker, player, 0, 4.0f);
                    player.level().addFreshEntity(pixie);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_FERTILIZING = id("tconevo.fertilizing");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FERTILIZING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var level = ctx.level();
                    var pos = ctx.pos();
                    for (var dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                        var adj = pos.relative(dir);
                        var state = level.getBlockState(adj);
                        if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bm
                                && bm.isValidBonemealTarget(level, adj, state, false)
                                && level instanceof net.minecraft.server.level.ServerLevel sl) {
                            bm.performBonemeal(sl, level.getRandom(), adj, state);
                        }
                    }
                })
                .build());

        TCONEVO_FINAL_GUARD_ARMOR = id("tconevo.final_guard_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FINAL_GUARD_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.wearer().getHealth() - ctx.amount().get() <= 0 && ctx.wearer().getRandom().nextFloat() < 0.15f) {
                        ctx.amount().set(0.0f);
                        ctx.wearer().heal(4.0f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_FLUX_BURN = id("tconevo.flux_burn");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FLUX_BURN)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    ctx.target().setSecondsOnFire(effect.paramInt("seconds", 3));
                })
                .build());

        TCONEVO_FLUXED = id("tconevo.fluxed");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FLUXED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.5f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_FLUXED_ARMOR = id("tconevo.fluxed_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FLUXED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.95f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_FOOT_FLEET = id("tconevo.foot_fleet");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_FOOT_FLEET)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0.0f) return;
                    var attacker = ctx.attacker();
                    if (attacker.level().isClientSide) return;
                    int level = effect.paramInt("level", 1);
                    int amplifier = Math.max(0, level - 1) + 1;
                    attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, amplifier));
                })
                .build());

        TCONEVO_GAIA_WILL_AHRIM_ARMOR = id("tconevo.gaia_will_ahrim_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WILL_AHRIM_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GAIA_WILL_DHAROK_ARMOR = id("tconevo.gaia_will_dharok_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WILL_DHAROK_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (attacker == null) return;
                    float missingPct = 1.0f - attacker.getHealth() / attacker.getMaxHealth();
                    ctx.amount().set(ctx.amount().get() * (1.0f + missingPct));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GAIA_WILL_GUTHAN_ARMOR = id("tconevo.gaia_will_guthan_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WILL_GUTHAN_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    LivingEntity attacker = ctx.attacker();
                    if (attacker == null) return;
                    if (attacker.getRandom().nextFloat() < 0.25f) {
                        attacker.heal(ctx.damageDealt() * 0.25f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GAIA_WILL_KARIL_ARMOR = id("tconevo.gaia_will_karil_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WILL_KARIL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GAIA_WILL_TORAG_ARMOR = id("tconevo.gaia_will_torag_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WILL_TORAG_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            effect.paramInt("duration_ticks", 60), effect.paramInt("amplifier", 0)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GAIA_WILL_VERAC_ARMOR = id("tconevo.gaia_will_verac_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WILL_VERAC_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (attacker == null) return;
                    if (attacker.getRandom().nextFloat() < 0.25f) {
                        ctx.amount().set(ctx.amount().get() * 1.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GAIA_WRATH = id("tconevo.gaia_wrath");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GAIA_WRATH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> ctx.target().setSecondsOnFire(3))
                .build());

        TCONEVO_GALE_FORCE_ARMOR = id("tconevo.gale_force_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GALE_FORCE_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("tconevo_gale_force", () -> Attributes.MOVEMENT_SPEED, "pct", 0.04f, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_GALE_FORCE_ARMOR1 = id("tconevo.gale_force_armor1");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_GALE_FORCE_ARMOR1)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("tconevo_gale_force1", () -> Attributes.MOVEMENT_SPEED, "pct", 0.06f, AttributeModifier.Operation.MULTIPLY_TOTAL))
                .build());

        TCONEVO_HEARTH_EMBRACE_ARMOR = id("tconevo.hearth_embrace_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_HEARTH_EMBRACE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().isOnFire() && ctx.wearer().tickCount % 20 == 0) {
                        ctx.wearer().heal(1.0f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_IMPACT_FORCE = id("tconevo.impact_force");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_IMPACT_FORCE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    // NOTE: onDealDamage ctx lacks attacker(); velocity check needs entity ref
                    // Original: speed = attacker.getDeltaMovement().length()
                    // bonus = speed * 1.0 * level; damage += baseDamage * bonus
                    // Placeholder: assumes ctx exposes attacker() in a future revision
                    int level = effect.paramInt("level", 1);
                    float bonusPerSpeedPerLevel = effect.paramFloat("bonus_per_speed_per_level", 1.0f);
                    // Vec3 move = attacker.getDeltaMovement();
                    // float speed = (float) move.length();
                    // if (speed > 0.0f) ctx.amount().set(ctx.amount().get() + ctx.amount().get() * speed * bonusPerSpeedPerLevel * level);
                })
                .build());

        TCONEVO_INFINITUM = id("tconevo.infinitum");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_INFINITUM)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 10.0f))
                .build());

        TCONEVO_JUGGERNAUT = id("tconevo.juggernaut");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_JUGGERNAUT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    // NOTE: onDealDamage ctx lacks attacker(); uses tool holder via player-context workaround
                    if (!(ctx.target() instanceof LivingEntity)) return;
                    // Attacker missing-HP fraction drives the bonus; retrieve via net.minecraft.world.item.ItemStack owner if API supports it
                    // Placeholder: assume ctx exposes player() or attacker() in a future revision
                    // Original logic: missingFraction = 1 - (attacker.health / attacker.maxHealth)
                    // bonus = missingFraction * 0.50 * level; damage *= (1 + bonus)
                    int level = effect.paramInt("level", 1);
                    float pctPerLevel = effect.paramFloat("pct_per_level", 0.50f);
                    // ctx.amount().set(ctx.amount().get() * (1.0f + missingFraction * pctPerLevel * level));
                })
                .build());

        TCONEVO_LUMINIFEROUS = id("tconevo.luminiferous");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_LUMINIFEROUS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f || target.level().isClientSide || !target.isAlive()) return;
                    int level = effect.paramInt("level", 1);
                    int duration = effect.paramInt("duration_per_level_ticks", 100) * level;
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0));
                })
                .build());

        TCONEVO_MANA_AFFINITY_ARMOR = id("tconevo.mana_affinity_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MANA_AFFINITY_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 != 0) return;
                    var armor = ctx.armor();
                    if (armor.getDamageValue() > 0) {
                        armor.setDamageValue(armor.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_MANA_INFUSED = id("tconevo.mana_infused");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MANA_INFUSED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_MANA_INFUSED_ARMOR = id("tconevo.mana_infused_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MANA_INFUSED_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_MODIFIABLE = id("tconevo.modifiable");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MODIFIABLE)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        TCONEVO_MODIFIABLE1 = id("tconevo.modifiable1");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MODIFIABLE1)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        TCONEVO_MODIFIABLE2 = id("tconevo.modifiable2");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MODIFIABLE2)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        TCONEVO_MORTAL_WOUNDS = id("tconevo.mortal_wounds");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_MORTAL_WOUNDS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f || target.level().isClientSide || !target.isAlive()) return;
                    int level = effect.paramInt("level", 1);
                    // 1.12: 100-tick Mortal Wounds (75% heal suppression), not Wither
                    int duration = effect.paramInt("duration_per_level_ticks", 100) * level;
                    target.addEffect(new MobEffectInstance(
                            com.soul.soa_additions.potion.TconEvoEffects.MORTAL_WOUNDS.get(), duration, 0));
                })
                .build());

        TCONEVO_NULL_ALMIGHTY_ARMOR = id("tconevo.null_almighty_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_NULL_ALMIGHTY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    // 1.12: 25% reduction
                    ctx.amount().set(ctx.amount().get() * 0.75f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        // Crystalline — bonus damage scaling with remaining durability (GC: +20% max)
        TCONEVO_CRYSTALLINE = id("tconevo.crystalline");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_CRYSTALLINE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target) || ctx.damageDealt() <= 0.0f) return;
                    if (target.level().isClientSide || !target.isAlive()) return;
                    var tool = ctx.tool();
                    float intact = tool.getMaxDamage() > 0
                            ? 1.0f - (float) tool.getDamageValue() / tool.getMaxDamage() : 1.0f;
                    float bonus = ctx.damageDealt() * 0.2f * intact;
                    if (bonus > 0.05f) {
                        target.hurt(ctx.attacker().damageSources().mobAttack(ctx.attacker()), bonus);
                    }
                })
                .build());

        // Ruination — fully-charged hits deal bonus damage from the target's current health (GC: 4%)
        TCONEVO_RUINATION = id("tconevo.ruination");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_RUINATION)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target) || ctx.damageDealt() <= 0.0f) return;
                    if (target.level().isClientSide || !target.isAlive()) return;
                    if (ctx.attacker() instanceof net.minecraft.world.entity.player.Player p
                            && p.getAttackStrengthScale(0.5f) < 0.9f) {
                        return;
                    }
                    float bonus = target.getHealth() * 0.04f;
                    if (bonus > 0.0f) {
                        target.hurt(ctx.attacker().damageSources().mobAttack(ctx.attacker()), bonus);
                    }
                })
                .build());

        // Phoenix Aspect — death-save that sacrifices the armor piece (1.12 parity)
        TCONEVO_PHOENIX_ASPECT_ARMOR = id("tconevo.phoenix_aspect_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PHOENIX_ASPECT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.amount().get() < ctx.wearer().getHealth()) return;
                    ctx.amount().set(Math.max(0.0f, ctx.wearer().getHealth() - 1.0f));
                    ctx.armor().hurtAndBreak(ctx.armor().getMaxDamage(), ctx.wearer(),
                            e -> e.broadcastBreakEvent(ctx.slot()));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        // Strength of Will — taking a hit at full health grants Immortality (GC: 200 ticks)
        TCONEVO_WILL_STRENGTH_ARMOR = id("tconevo.will_strength_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_WILL_STRENGTH_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    if (ctx.wearer().getHealth() >= ctx.wearer().getMaxHealth()) {
                        ctx.wearer().addEffect(new MobEffectInstance(
                                com.soul.soa_additions.potion.TconEvoEffects.IMMORTALITY.get(),
                                200, 0, true, true));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_OMNIPOTENCE = id("tconevo.omnipotence");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_OMNIPOTENCE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() + 999.0f);
                })
                .build());

        TCONEVO_OPPORTUNIST = id("tconevo.opportunist");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_OPPORTUNIST)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity target = ctx.target();
                    boolean hasDebuff = false;
                    for (MobEffectInstance inst : target.getActiveEffects()) {
                        if (!inst.getEffect().isBeneficial()) { hasDebuff = true; break; }
                    }
                    if (!hasDebuff) return;
                    int level = effect.paramInt("level", 1);
                    float bonus = effect.paramFloat("bonus_per_level", 0.30f) * level;
                    ctx.amount().set(ctx.amount().get() + ctx.amount().get() * bonus);
                })
                .build());

        TCONEVO_OVERWHELM = id("tconevo.overwhelm");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_OVERWHELM)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    LivingEntity target = ctx.target();
                    float armour = (float) target.getAttributeValue(Attributes.ARMOR);
                    if (armour <= 0.0f) return;
                    int level = effect.paramInt("level", 1);
                    float bonus = armour * effect.paramFloat("bonus_per_armour_per_level", 0.04f) * level;
                    ctx.amount().set(ctx.amount().get() + ctx.amount().get() * bonus);
                })
                .build());

        TCONEVO_PHOTOSYNTHETIC = id("tconevo.photosynthetic");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PHOTOSYNTHETIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    if (ctx.player().tickCount % 200 == 0) {
                        var tool = ctx.tool();
                        if (tool.getDamageValue() > 0) tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_PHOTOSYNTHETIC_ARMOR = id("tconevo.photosynthetic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PHOTOSYNTHETIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    int interval = Math.max(20, effect.paramInt("interval_ticks", 600));
                    if (ctx.wearer().tickCount % interval != 0) return;
                    var stack = ctx.armor();
                    if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_PHOTOVOLTAIC = id("tconevo.photovoltaic");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PHOTOVOLTAIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isDay() && ctx.wearer().level().canSeeSky(ctx.wearer().blockPosition())
                            && ctx.wearer().tickCount % 100 == 0) {
                        var stack = ctx.armor();
                        if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_PHOTOVOLTAIC_ARMOR = id("tconevo.photovoltaic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PHOTOVOLTAIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isDay() && ctx.wearer().level().canSeeSky(ctx.wearer().blockPosition())
                            && ctx.wearer().tickCount % 100 == 0) {
                        var stack = ctx.armor();
                        if (stack.getDamageValue() > 0) stack.setDamageValue(stack.getDamageValue() - 1);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_PIEZOELECTRIC = id("tconevo.piezoelectric");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PIEZOELECTRIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var tool = ctx.tool();
                    if (tool.getDamageValue() > 0 && ctx.level().getRandom().nextFloat() < 0.05f) {
                        tool.setDamageValue(tool.getDamageValue() - 1);
                    }
                })
                .build());

        TCONEVO_PRIMORDIAL = id("tconevo.primordial");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_PRIMORDIAL)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    stats.bonusAttackDamage += effect.paramFloat("damage", 3.0f);
                    stats.bonusMiningSpeed += effect.paramFloat("speed", 3.0f);
                })
                .build());

        TCONEVO_RADIANT_ARMOR = id("tconevo.radiant_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_RADIANT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.GLOWING, 260, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_REACTIVE_ARMOR = id("tconevo.reactive_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_REACTIVE_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof LivingEntity attacker && attacker != ctx.wearer()) {
                        attacker.push(
                                (attacker.getX() - ctx.wearer().getX()) * 0.5,
                                0.3,
                                (attacker.getZ() - ctx.wearer().getZ()) * 0.5);
                        attacker.hurtMarked = true;
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_REAPING = id("tconevo.reaping");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_REAPING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    float perLevel = effect.paramFloat("lifesteal_pct", 0.1f);
                    int level = effect.paramInt("level", 1);
                    float heal = ctx.damageDealt() * perLevel * level;
                    if (heal > 0.0f) {
                        LivingEntity healer = ctx.attacker();
                        if (healer != null) healer.heal(heal);
                    }
                })
                .build());

        TCONEVO_REJUVENATING = id("tconevo.rejuvenating");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_REJUVENATING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.damageDealt() <= 0.0f) return;
                    var attacker = ctx.attacker();
                    if (attacker.level().isClientSide) return;
                    int level = effect.paramInt("level", 1);
                    int seconds = effect.paramInt("repair_seconds", 2);
                    int duration = seconds * 20 * level;
                    attacker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 2));
                })
                .build());

        TCONEVO_RELENTLESS = id("tconevo.relentless");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_RELENTLESS)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    int level = effect.paramInt("level", 1);
                    int reduction = effect.paramInt("ticks_per_level", 4) * level;
                    target.invulnerableTime = Math.max(0, target.invulnerableTime - reduction);
                })
                .build());

        TCONEVO_SECOND_WIND_ARMOR = id("tconevo.second_wind_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SECOND_WIND_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().getHealth() < ctx.wearer().getMaxHealth() * 0.3f
                            && ctx.wearer().tickCount % 20 == 0) {
                        ctx.wearer().heal(0.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_SENTIENT = id("tconevo.sentient");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SENTIENT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 2.0f));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_SENTIENT_ARMOR = id("tconevo.sentient_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SENTIENT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.93f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_SHADOWSTEP_ARMOR = id("tconevo.shadowstep_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SHADOWSTEP_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0 && ctx.wearer().isCrouching()) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_SOUL_GUARD_ARMOR = id("tconevo.soul_guard_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SOUL_GUARD_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    ctx.amount().set(ctx.amount().get() * 0.93f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_SOUL_REND = id("tconevo.soul_rend");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SOUL_REND)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    float perLevel = effect.paramFloat("lifesteal_pct", 0.1f);
                    int level = effect.paramInt("level", 1);
                    float heal = ctx.damageDealt() * perLevel * level;
                    if (heal > 0.0f) {
                        LivingEntity healer = ctx.attacker();
                        if (healer != null) healer.heal(heal);
                    }
                })
                .build());

        TCONEVO_SOUL_REND1 = id("tconevo.soul_rend1");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SOUL_REND1)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    float perLevel = effect.paramFloat("lifesteal_pct", 0.1f);
                    int level = effect.paramInt("level", 1);
                    float heal = ctx.damageDealt() * perLevel * level;
                    if (heal > 0.0f) {
                        LivingEntity healer = ctx.attacker();
                        if (healer != null) healer.heal(heal);
                    }
                })
                .build());

        TCONEVO_SOUL_REND3 = id("tconevo.soul_rend3");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SOUL_REND3)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    float perLevel = effect.paramFloat("lifesteal_pct", 0.1f);
                    int level = effect.paramInt("level", 1);
                    float heal = ctx.damageDealt() * perLevel * level;
                    if (heal > 0.0f) {
                        LivingEntity healer = ctx.attacker();
                        if (healer != null) healer.heal(heal);
                    }
                })
                .build());

        TCONEVO_SPECTRAL_ARMOR = id("tconevo.spectral_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SPECTRAL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0 && ctx.wearer().getHealth() < ctx.wearer().getMaxHealth() * 0.5f) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_STAGGERING = id("tconevo.staggering");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_STAGGERING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f || target.level().isClientSide || !target.isAlive()) return;
                    // Original fires only on fully-charged swings; smithery has no charge flag,
                    // so this fires unconditionally (or gate on cooldown externally)
                    int level = effect.paramInt("level", 1);
                    int duration = 20 + 10 * level;
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 4));
                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            net.minecraft.sounds.SoundEvents.ANVIL_LAND, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                })
                .build());

        TCONEVO_STIFLING_ARMOR = id("tconevo.stifling_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_STIFLING_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.source().getEntity() instanceof LivingEntity attacker && attacker != ctx.wearer()) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_STONEBOUND_ARMOR = id("tconevo.stonebound_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_STONEBOUND_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var stack = ctx.armor();
                    if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
                        float wornPct = (float) stack.getDamageValue() / stack.getMaxDamage();
                        ctx.amount().set(ctx.amount().get() * (1.0f - 0.15f * wornPct));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_SUNDERING = id("tconevo.sundering");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SUNDERING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f || target.level().isClientSide || !target.isAlive()) return;
                    int level = effect.paramInt("level", 1);
                    int duration = effect.paramInt("duration_per_level_ticks", 60) * level;
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
                })
                .build());

        TCONEVO_SUPERDENSE_ARMOR = id("tconevo.superdense_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_SUPERDENSE_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.5f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_THUNDERGOD_FAVOUR_ARMOR = id("tconevo.thundergod_favour_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_THUNDERGOD_FAVOUR_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.IS_LIGHTNING)) {
                        ctx.wearer().heal(ctx.damage() * 0.5f);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_THUNDERGOD_WRATH = id("tconevo.thundergod_wrath");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_THUNDERGOD_WRATH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    net.minecraft.world.level.Level level = target.level();
                    if (level.isClientSide || !(level instanceof net.minecraft.server.level.ServerLevel server)) return;
                    // Original fires when pre-hit HP was at/near max (first-strike lightning)
                    // or when damage >= maxHealth (one-shot). Faithful translation:
                    if (target.getHealth() + ctx.damageDealt() + 1.0E-4f < target.getMaxHealth()) return;
                    net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(server);
                    if (bolt != null) {
                        bolt.moveTo(target.getX(), target.getY(), target.getZ());
                        bolt.setVisualOnly(false);
                        server.addFreshEntity(bolt);
                    }
                })
                .build());

        TCONEVO_ULTRADENSE_ARMOR = id("tconevo.ultradense_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_ULTRADENSE_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 1.0f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_VAMPIRIC = id("tconevo.vampiric");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_VAMPIRIC)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.damageDealt() <= 0.0f) return;
                    float perLevel = effect.paramFloat("lifesteal_pct", 0.1f);
                    int level = effect.paramInt("level", 1);
                    float heal = ctx.damageDealt() * perLevel * level;
                    if (heal > 0.0f) {
                        LivingEntity healer = ctx.attacker();
                        if (healer != null) healer.heal(heal);
                    }
                })
                .build());

        TCONEVO_WARPING = id("tconevo.warping");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_WARPING)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    var random = target.level().getRandom();
                    if (random.nextFloat() < 0.10f) {
                        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    }
                    if (random.nextFloat() < 0.05f) {
                        ctx.attacker().addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0));
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_WARPING_ARMOR = id("tconevo.warping_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_WARPING_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    var random = ctx.wearer().getRandom();
                    if (random.nextFloat() < 0.15f) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                    }
                    if (random.nextFloat() < 0.05f) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TCONEVO_WILLFUL = id("tconevo.willful");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_WILLFUL)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.15f))
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TCONEVO_WILLFUL_ARMOR = id("tconevo.willful_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TCONEVO_WILLFUL_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.durabilityMultiplier *= 1.0f + effect.paramFloat("bonus", 0.15f))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TELEKINETIC_ARMOR = id("telekinetic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TELEKINETIC_ARMOR)
                .category(Modifier.ModifierCategory.PASSIVE)
                .onCompose(composeArmorAttribute("telekinetic_armor", () -> ForgeMod.ENTITY_REACH.get(), "amount", 1.0f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TERRAFIRMA = id("terrafirma");
        SmitheryAPI.registerModifier(Modifier.builder(TERRAFIRMA)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.5f))
                .build());

        TERRAFIRMA1 = id("terrafirma1");
        SmitheryAPI.registerModifier(Modifier.builder(TERRAFIRMA1)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.5f))
                .build());

        TERRAFIRMA2 = id("terrafirma2");
        SmitheryAPI.registerModifier(Modifier.builder(TERRAFIRMA2)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 3.5f))
                .build());

        THAUMIC = id("thaumic");
        SmitheryAPI.registerModifier(Modifier.builder(THAUMIC)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += 1.0f)
                .build());

        THRONY = id("throny");
        SmitheryAPI.registerModifier(Modifier.builder(THRONY)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    // Throny (tool): reflect 10% of attacker's maxHP as thorns damage per hit
                    target.hurt(attacker.damageSources().thorns(attacker), attacker.getMaxHealth() * 0.10F);
                })
                .onDamaged((effect, ctx) -> {
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
                    var entity = ctx.wearer();
                    // Throny (armor): 2.5% of incoming damage as thorns (cap 10) per hit
                    // NOTE: onDamaged ctx has no damage amount; using source entity maxHP * 0.025 as proxy
                    // Original: Math.min(10.0F, dmg * 0.025F)
                    float refl = Math.min(10.0F, attacker.getMaxHealth() * 0.025F);
                    attacker.hurt(entity.damageSources().thorns(entity), refl);
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        THRONYTRAIT = id("throny_trait");
        SmitheryAPI.registerModifier(Modifier.builder(THRONYTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    // Throny (tool): reflect 10% of attacker's maxHP as thorns damage per hit
                    target.hurt(attacker.damageSources().thorns(attacker), attacker.getMaxHealth() * 0.10F);
                })
                .onDamaged((effect, ctx) -> {
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
                    var entity = ctx.wearer();
                    // Throny (armor): 2.5% of incoming damage as thorns (cap 10) per hit
                    // NOTE: onDamaged ctx has no damage amount; using source entity maxHP * 0.025 as proxy
                    // Original: Math.min(10.0F, dmg * 0.025F)
                    float refl = Math.min(10.0F, attacker.getMaxHealth() * 0.025F);
                    attacker.hurt(entity.damageSources().thorns(entity), refl);
                })
                .build());

        THRONY_ARMOR = id("throny_armor");
        SmitheryAPI.registerModifier(Modifier.builder(THRONY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = ctx.attacker();
                    if (attacker == null) return;
                    // Throny (tool): reflect 10% of attacker's maxHP as thorns damage per hit
                    target.hurt(attacker.damageSources().thorns(attacker), attacker.getMaxHealth() * 0.10F);
                })
                .onDamaged((effect, ctx) -> {
                    var src = ctx.source();
                    if (!(src.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
                    var entity = ctx.wearer();
                    // Throny (armor): 2.5% of incoming damage as thorns (cap 10) per hit
                    // NOTE: onDamaged ctx has no damage amount; using source entity maxHP * 0.025 as proxy
                    // Original: Math.min(10.0F, dmg * 0.025F)
                    float refl = Math.min(10.0F, attacker.getMaxHealth() * 0.025F);
                    attacker.hurt(entity.damageSources().thorns(entity), refl);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        THUNDERING = id("thundering");
        SmitheryAPI.registerModifier(Modifier.builder(THUNDERING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (ctx.target().level().getRandom().nextFloat() < 0.08f) {
                        var lightning = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(target.level());
                        if (lightning != null) {
                            lightning.moveTo(target.getX(), target.getY(), target.getZ());
                            target.level().addFreshEntity(lightning);
                        }
                    }
                })
                .build());

        THUNDERINGTRAIT = id("thundering_trait");
        SmitheryAPI.registerModifier(Modifier.builder(THUNDERINGTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (ctx.attacker() == null) return;
                    if (!(ctx.attacker().level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
                    var target = ctx.target();
                    if (target == null) return;
                    // Lightning: 4% chance to spawn a visual-only lightning bolt at target
                    if (sl.getRandom().nextFloat() >= 0.04F) return;
                    net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt == null) return;
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    bolt.setVisualOnly(true);
                    sl.addFreshEntity(bolt);
                })
                .build());

        TIDALFORCETRAIT = id("tidal_force_trait");
        SmitheryAPI.registerModifier(Modifier.builder(TIDALFORCETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Tidal Force (armor): Water Breathing III (25t duration, refreshed every 20 ticks)
                    if (ctx.wearer().tickCount % 20 != 0) return;
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WATER_BREATHING, 25, 2, false, false));
                })
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Tidal Force (tool): 1.33x damage when attacker is wet (in water or rain)
                    if (attacker != null && (attacker.isInWater() || attacker.isInWaterRainOrBubble())) {
                        ctx.amount().set(dmg * 1.33F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TIDALFORCETRAIT_ARMOR = id("tidal_force_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TIDALFORCETRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Tidal Force (armor): Water Breathing III (25t duration, refreshed every 20 ticks)
                    if (ctx.wearer().tickCount % 20 != 0) return;
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WATER_BREATHING, 25, 2, false, false));
                })
                .onDealDamage((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    var target = ctx.target();
                    if (target == null) return;
                    var attacker = target.getLastAttacker();
                    // Tidal Force (tool): 1.33x damage when attacker is wet (in water or rain)
                    if (attacker != null && (attacker.isInWater() || attacker.isInWaterRainOrBubble())) {
                        ctx.amount().set(dmg * 1.33F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TIDAL_FORCE = id("tidal_force");
        SmitheryAPI.registerModifier(Modifier.builder(TIDAL_FORCE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        TOM_AND_JERRY_ARMOR = id("tom_and_jerry_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TOM_AND_JERRY_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                            effect.paramInt("duration_ticks", 60), effect.paramInt("amplifier", 0)));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TRASH = id("trash");
        SmitheryAPI.registerModifier(Modifier.builder(TRASH)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    float chance = effect.paramFloat("chance", 0.25f);
                    if (target.level().getRandom().nextFloat() < chance) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                                effect.paramInt("duration_ticks", 100), effect.paramInt("amplifier", 0)));
                    }
                })
                .build());

        TRAVEL_BELT_ARMOR = id("travel_belt_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TRAVEL_BELT_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player wearer = ctx.wearer();
                    if (wearer.tickCount % 40 == 0) {
                        wearer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
                    }
                })
                .onCompose(composeArmorAttribute("travel_belt", () -> ForgeMod.STEP_HEIGHT_ADDITION.get(),
                        "step_height", 0.5f, AttributeModifier.Operation.ADDITION))
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TRAVEL_GOGGLES_ARMOR = id("travel_goggles_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TRAVEL_GOGGLES_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 200 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TRAVEL_SACK_ARMOR = id("travel_sack_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TRAVEL_SACK_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player wearer = ctx.wearer();
                    if (wearer.tickCount % 5 != 0) return;
                    float radius = effect.paramFloat("radius", 6.0f);
                    var box = wearer.getBoundingBox().inflate(radius);
                    var playerPos = wearer.position();
                    for (ItemEntity drop : wearer.level().getEntitiesOfClass(ItemEntity.class, box)) {
                        if (drop.hasPickUpDelay()) continue;
                        var toward = playerPos.subtract(drop.position());
                        double dist = toward.length();
                        if (dist < 0.5 || dist > radius) continue;
                        drop.setDeltaMovement(toward.normalize().scale(0.4));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TRAVEL_SLOWFALL_ARMOR = id("travel_slowfall_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TRAVEL_SLOWFALL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onFall((effect, ctx) -> {
                    ctx.damageMultiplier().set(0.0f);
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TRAVEL_SNEAK_ARMOR = id("travel_sneak_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TRAVEL_SNEAK_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TRUEDEFENSETRAIT = id("true_defense_trait");
        SmitheryAPI.registerModifier(Modifier.builder(TRUEDEFENSETRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // True Defense: 10% reduction on absolute (armor-bypassing) damage
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
                        ctx.amount().set(dmg * 0.9F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        TRUEDEFENSETRAIT_ARMOR = id("true_defense_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(TRUEDEFENSETRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    float dmg = ctx.amount().get();
                    // True Defense: 10% reduction on absolute (armor-bypassing) damage
                    if (ctx.source().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
                        ctx.amount().set(dmg * 0.9F);
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        TWILIT = id("twilit");
        SmitheryAPI.registerModifier(Modifier.builder(TWILIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    if (!ctx.attacker().level().isDay()) {
                        ctx.amount().set(ctx.amount().get() + effect.paramFloat("damage", 2.5f));
                    }
                })
                .build());

        UNCERTAIN = id("uncertain");
        SmitheryAPI.registerModifier(Modifier.builder(UNCERTAIN)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDealDamage((effect, ctx) -> {
                    float mult = 0.5f + ctx.target().level().getRandom().nextFloat() * 1.5f;
                    ctx.amount().set(ctx.amount().get() * mult);
                })
                .build());

        UNNAMED = id("unnamed");
        SmitheryAPI.registerModifier(Modifier.builder(UNNAMED)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 1.5f))
                .build());

        UNNATURAL = id("unnatural");
        SmitheryAPI.registerModifier(Modifier.builder(UNNATURAL)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> {
                    stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f);
                })
                .build());

        UNSTABLE = id("unstable");
        SmitheryAPI.registerModifier(Modifier.builder(UNSTABLE)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    var rng = ctx.level().getRandom();
                    if (rng.nextFloat() > 0.03F) return;
                    var level = ctx.level();
                    var pos = ctx.pos();
                    if (!level.isClientSide()) {
                        level.explode(rng.nextBoolean() ? ctx.player() : null,
                            pos.getX(), pos.getY(), pos.getZ(),
                            1.2F + rng.nextFloat() * 5.0F,
                            net.minecraft.world.level.Level.ExplosionInteraction.TNT);
                    }
                })
                .onAttackEntity((effect, ctx) -> {
                    var rng = ctx.target().level().getRandom();
                    if (rng.nextFloat() > 0.04F) return;
                    var target = ctx.target();
                    var attacker = ctx.attacker();
                    if (target == null || attacker == null) return;
                    if (attacker.level().isClientSide()) return;
                    attacker.level().explode(
                        rng.nextBoolean() ? attacker : target,
                        target.getX(), target.getY(), target.getZ(),
                        1.2F + rng.nextFloat() * 5.0F,
                        net.minecraft.world.level.Level.ExplosionInteraction.TNT);
                })
                .onMobDrops((effect, ctx) -> {
                    var rng = ctx.victim().level().getRandom();
                    if (rng.nextFloat() < 0.05F) {
                        ctx.drops().add(new net.minecraft.world.entity.item.ItemEntity(ctx.victim().level(), ctx.victim().getX(), ctx.victim().getY(), ctx.victim().getZ(), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GUNPOWDER, Math.max(1, rng.nextInt(2)))));
                    }
                })
                .build());

        VACCINETRAIT = id("vaccine_trait");
        SmitheryAPI.registerModifier(Modifier.builder(VACCINETRAIT)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    LivingEntity attacker = ctx.attacker();
                    if (attacker.getRandom().nextFloat() >= 0.25f) return;
                    var activeEffects = new java.util.ArrayList<>(attacker.getActiveEffects());
                    for (var e : activeEffects) {
                        if (!e.getEffect().isBeneficial()) {
                            attacker.removeEffect(e.getEffect());
                            break;
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        VACCINETRAIT_ARMOR = id("vaccine_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(VACCINETRAIT_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.tickCount % 100 != 0) return;
                    var activeEffects = new java.util.ArrayList<>(player.getActiveEffects());
                    for (var e : activeEffects) {
                        if (!e.getEffect().isBeneficial()) {
                            player.removeEffect(e.getEffect());
                            break;
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        VEILED = id("veiled");
        SmitheryAPI.registerModifier(Modifier.builder(VEILED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().tickCount % 20 == 0) {
                        ctx.wearer().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false));
                    }
                })
                .build());

        VENGEFUL_ARMOR = id("vengeful_armor");
        SmitheryAPI.registerModifier(Modifier.builder(VENGEFUL_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    if (ctx.wearer().getRandom().nextFloat() < 0.3f) {
                        attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        VINDICTIVE = id("vindictive");
        SmitheryAPI.registerModifier(Modifier.builder(VINDICTIVE)
                .category(Modifier.ModifierCategory.PASSIVE)
                .passive((effect, stats) -> stats.bonusAttackDamage += effect.paramFloat("damage", 2.0f))
                .build());

        VISIONTRAIT = id("vision_trait");
        SmitheryAPI.registerModifier(Modifier.builder(VISIONTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Vision: Night Vision while armor is worn (level 2 / amplified, 330 ticks)
                    // NOTE: original also applied level 0 when tool was held in mainhand;
                    // smithery onArmorTick fires for armor only.
                    if (ctx.wearer().tickCount % 20 != 0) return;
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NIGHT_VISION, 330, 2, false, false));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        VISIONTRAIT_ARMOR = id("vision_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(VISIONTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    if (ctx.wearer().level().isClientSide()) return;
                    // Vision: Night Vision while armor is worn (level 2 / amplified, 330 ticks)
                    // NOTE: original also applied level 0 when tool was held in mainhand;
                    // smithery onArmorTick fires for armor only.
                    if (ctx.wearer().tickCount % 20 != 0) return;
                    ctx.wearer().addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NIGHT_VISION, 330, 2, false, false));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        VOLTAIC_ARMOR = id("voltaic_armor");
        SmitheryAPI.registerModifier(Modifier.builder(VOLTAIC_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onDamaged((effect, ctx) -> {
                    if (!(ctx.source().getEntity() instanceof LivingEntity attacker)) return;
                    if (attacker == ctx.wearer()) return;
                    if (ctx.wearer().getRandom().nextFloat() < 0.2f) {
                        var aabb = ctx.wearer().getBoundingBox().inflate(4.0);
                        for (var entity : ctx.wearer().level().getEntitiesOfClass(LivingEntity.class, aabb,
                                e -> e != ctx.wearer())) {
                            entity.hurt(ctx.wearer().damageSources().thorns(ctx.wearer()), 3.0f);
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        WARMTRAIT = id("warm_trait");
        SmitheryAPI.registerModifier(Modifier.builder(WARMTRAIT)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    float dmg = ctx.amount().get();
                    // Warm: -5% damage in snowy biomes (-7.5% if also raining)
                    var biome = entity.level().getBiome(entity.blockPosition());
                    boolean isSnowy = biome.is(net.minecraft.tags.BiomeTags.IS_FOREST)
                        || biome.containsTag(net.minecraft.tags.TagKey.create(
                            net.minecraft.core.registries.Registries.BIOME,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("forge", "is_snowy")));
                    if (!isSnowy) return;
                    float reduction = 0.05F;
                    if (entity.level().isRaining()) reduction += 0.025F;
                    ctx.amount().set(dmg * (1.0F - reduction));
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        WARMTRAIT_ARMOR = id("warm_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(WARMTRAIT_ARMOR)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onHurt((effect, ctx) -> {
                    var entity = ctx.wearer();
                    float dmg = ctx.amount().get();
                    // Warm: -5% damage in snowy biomes (-7.5% if also raining)
                    var biome = entity.level().getBiome(entity.blockPosition());
                    boolean isSnowy = biome.is(net.minecraft.tags.BiomeTags.IS_FOREST)
                        || biome.containsTag(net.minecraft.tags.TagKey.create(
                            net.minecraft.core.registries.Registries.BIOME,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("forge", "is_snowy")));
                    if (!isSnowy) return;
                    float reduction = 0.05F;
                    if (entity.level().isRaining()) reduction += 0.025F;
                    ctx.amount().set(dmg * (1.0F - reduction));
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        WARPDRAINTRAIT = id("warp_drain_trait");
        SmitheryAPI.registerModifier(Modifier.builder(WARPDRAINTRAIT)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onKill((effect, ctx) -> {
                    Player player = ctx.attacker();
                    var activeEffects = new java.util.ArrayList<>(player.getActiveEffects());
                    for (var e : activeEffects) {
                        if (!e.getEffect().isBeneficial()) {
                            player.removeEffect(e.getEffect());
                            break;
                        }
                    }
                    switch (player.getRandom().nextInt(3)) {
                        case 0 -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
                        case 1 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
                        default -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                    }
                })
                .appliesTo(Modifier.AppliesTo.TOOLS)
                .build());

        WARPDRAINTRAIT_ARMOR = id("warp_drain_trait_armor");
        SmitheryAPI.registerModifier(Modifier.builder(WARPDRAINTRAIT_ARMOR)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onArmorTick((effect, ctx) -> {
                    Player player = ctx.wearer();
                    if (player.tickCount % 100 != 0) return;
                    var activeEffects = new java.util.ArrayList<>(player.getActiveEffects());
                    for (var e : activeEffects) {
                        if (!e.getEffect().isBeneficial()) {
                            player.removeEffect(e.getEffect());
                            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, false));
                            break;
                        }
                    }
                })
                .appliesTo(Modifier.AppliesTo.ARMOR)
                .build());

        WEBBED = id("webbed");
        SmitheryAPI.registerModifier(Modifier.builder(WEBBED)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                })
                .build());

        WHIRL = id("whirl");
        SmitheryAPI.registerModifier(Modifier.builder(WHIRL)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockBreak((effect, ctx) -> {
                    int radius = 3;
                    var center = ctx.pos();
                    var level = ctx.level();
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dy = -radius; dy <= radius; dy++) {
                            for (int dz = -radius; dz <= radius; dz++) {
                                var pos = center.offset(dx, dy, dz);
                                if (level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)) {
                                    level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                    }
                })
                .build());

        // GreedyCraft Creative Modifier — high maxLevel so each applied
        // soa_additions:creative_modifier stacks another free slot (see
        // data/soa_additions/smithery/modifier_source/creative_modifier.json).
        CREATIVE = id("creative");
        SmitheryAPI.registerModifier(Modifier.builder(CREATIVE)
                                .category(Modifier.ModifierCategory.PASSIVE)
                                .maxLevel(999)
                .build());

        WRITABLE = id("writable");
        SmitheryAPI.registerModifier(Modifier.builder(WRITABLE)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        WRITABLE1 = id("writable1");
        SmitheryAPI.registerModifier(Modifier.builder(WRITABLE1)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        WRITABLE2 = id("writable2");
        SmitheryAPI.registerModifier(Modifier.builder(WRITABLE2)
                                .category(Modifier.ModifierCategory.PASSIVE)
                .build());

        XU_WHISPERING = id("xu_whispering");
        SmitheryAPI.registerModifier(Modifier.builder(XU_WHISPERING)
                                .category(Modifier.ModifierCategory.ACTIVE)
                .onAttackEntity((effect, ctx) -> {
                    if (!(ctx.target() instanceof LivingEntity target)) return;
                    if (target.level().getRandom().nextFloat() < 0.15f) {
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                    }
                })
                .build());

        XU_WITHERING = id("xu_withering");
        SmitheryAPI.registerModifier(Modifier.builder(XU_WITHERING)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    int bonus = effect.paramInt("bonus", 1);
                    for (var drop : ctx.drops()) {
                        var stack = drop.getItem();
                        stack.setCount(stack.getCount() + ctx.level().getRandom().nextInt(bonus + 1));
                        drop.setItem(stack);
                    }
                })
                .build());

        XU_XP_BOOST = id("xu_xp_boost");
        SmitheryAPI.registerModifier(Modifier.builder(XU_XP_BOOST)
                .category(Modifier.ModifierCategory.ACTIVE)
                .onBlockDrops((effect, ctx) -> {
                    ctx.setXp(Math.round(ctx.xp() * effect.paramFloat("xp_multiplier", 1.5f)));
                })
                .build());

        ZANY = id("zany");
        SmitheryAPI.registerModifier(Modifier.builder(ZANY)
                .category(Modifier.ModifierCategory.PASSIVE)
                .durabilityScaled()
                                .passive((effect, stats) -> {
                    stats.bonusMiningSpeed += effect.paramFloat("speed", 2.0f) * stats.missingDurability;
                })
                .build());

    }

    private static Modifier.OnCompose composeArmorAttribute(
            String name, Supplier<Attribute> attribute,
            String amountParam, float defaultAmount, AttributeModifier.Operation operation) {
        return (effect, ctx) -> {
            var stack = ctx.stack();
            if (!(stack.getItem() instanceof SmitheryArmorItem armorItem)) return;
            ResourceLocation attributeId = ForgeRegistries.ATTRIBUTES.getKey(attribute.get());
            if (attributeId == null) return;
            int level = Math.max(1, effect.paramInt("level", 1));
            double amount = effect.paramFloat(amountParam, defaultAmount) * (double) level;
            EquipmentSlot slot = SmitheryArmorItem.slotForToolTypeId(armorItem.toolTypeId());
            SmitheryToolData.putExtraAttribute(stack, new SmitheryToolData.ExtraAttribute(
                    name + "." + slot.getName(), attributeId, amount, operation, slot));
        };
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("soa_additions", path);
    }

    private SoaSmitheryModifiers() {}
}