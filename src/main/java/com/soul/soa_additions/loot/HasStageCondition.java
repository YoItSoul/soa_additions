package com.soul.soa_additions.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Loot condition that passes when the opening/triggering player has the named
 * GameStages stage. Mirrors the reflection pattern used in
 * {@link com.soul.soa_additions.item.StageItem} — GameStages is a soft
 * dependency, so we dodge classloading it unless it's present. When the mod
 * is absent the condition always fails (fail-closed), which is the desired
 * behavior for hardmode-gated chest injections.
 */
public class HasStageCondition implements LootItemCondition {

    private final String stage;

    public HasStageCondition(String stage) {
        this.stage = stage;
    }

    @Override
    public LootItemConditionType getType() {
        return LootConditions.HAS_STAGE.get();
    }

    @Override
    public boolean test(LootContext ctx) {
        Player player = resolvePlayer(ctx);
        if (player == null) return false;
        return hasStage(player, stage);
    }

    private static Player resolvePlayer(LootContext ctx) {
        // Chest loot: BlockEntity.unpackLootTable passes the opening player as
        // THIS_ENTITY. Fish/treasure tables also set THIS_ENTITY to the angler.
        Entity thisEntity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (thisEntity instanceof Player p) return p;
        // Fallback: mob drops set KILLER_ENTITY to the attacker.
        Entity killer = ctx.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (killer instanceof Player p) return p;
        Entity lastPlayer = ctx.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        if (lastPlayer instanceof Player p) return p;
        return null;
    }

    private static boolean hasStage(Player player, String stage) {
        try {
            Class<?> helperCls = Class.forName("net.darkhax.gamestages.GameStageHelper");
            return (boolean) helperCls.getMethod("hasStage", Player.class, String.class)
                    .invoke(null, player, stage);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<HasStageCondition> {
        @Override
        public void serialize(JsonObject json, HasStageCondition value, JsonSerializationContext ctx) {
            json.addProperty("stage", value.stage);
        }

        @Override
        public HasStageCondition deserialize(JsonObject json, JsonDeserializationContext ctx) {
            return new HasStageCondition(GsonHelper.getAsString(json, "stage"));
        }
    }
}
