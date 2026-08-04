package com.soul.soa_additions.cyclicaqua;

/**
 * Marker mixed into Cyclic's {@code TileFisher} by
 * {@link com.soul.soa_additions.mixin.cyclic.TileFisherMixin}.
 *
 * <p>Its only job is to make mixin application observable at runtime: if
 * {@code TileFisher} implements this, the injection landed. Startup logging uses
 * that to report a verified state instead of an assumed one.</p>
 */
public interface SoaFisherPatched {
}
