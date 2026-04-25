package com.soul.soa_additions.tr.client.tooltip;

import com.soul.soa_additions.tr.core.AspectStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

/**
 * Common-side carrier for the aspect tooltip line. Holds the filtered list
 * of (aspect, amount) entries the player should see — the filter (apply
 * "ARE KNOWN" check) happens before construction in
 * {@link AspectTooltipHandler}, so this component just renders what it's given.
 *
 * <p>Mapped to {@link ClientAspectTooltipComponent} via the registration in
 * {@link com.soul.soa_additions.tr.client.TrClientSetup}.
 */
public record AspectTooltipComponent(List<AspectStack> aspects) implements TooltipComponent {

    public AspectTooltipComponent {
        aspects = List.copyOf(aspects);
    }
}
