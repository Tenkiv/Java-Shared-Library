package com.tenkiv.tekdaqc.locator;

import com.tenkiv.tekdaqc.hardware.ATekdaqc;

/**
 * Callback for discovered boards. This handler will be called for each
 * discovered board in the order of discovery.
 *
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @since v1.0.0.0
 */
public interface OnTekdaqcDiscovered {
    /**
     * Called when a Tekdaqc has been discovered.
     *
     * @param board {@link ATekdaqc} The constructed Tekdaqc.
     */
    void onTekdaqcResponse(final ATekdaqc board);

    void onTekdaqcFirstLocated(final ATekdaqc board);

    void onTekdaqcNoLongerLocated(final ATekdaqc board);
}
