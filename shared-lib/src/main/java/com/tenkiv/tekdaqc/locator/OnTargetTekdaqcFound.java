package com.tenkiv.tekdaqc.locator;

import com.tenkiv.tekdaqc.hardware.ATekdaqc;

import java.util.Set;

/**
 * Callback for sets of specific boards which have been discovered by the {@link Locator}.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.1.3.0
 */
public interface OnTargetTekdaqcFound {

    /**
     * Enum stating the failure point of the {@link Locator} in managing discovery of the {@link ATekdaqc}s.
     */
    public enum FailureFlag{

        TEKDAQC_NOT_LOCATED,

        AUTO_CONNECT_FAILURE

    }

    /**
     * Callback stating that target {@link ATekdaqc} has been successfully located.
     *
     * @param tekdaqc The located {@link ATekdaqc}.
     */
    void onTargetFound(ATekdaqc tekdaqc);

    /**
     * Callback stating that the {@link Locator} encountered an error.
     *
     * @param serial The {@link String} of the serial number of the the {@link ATekdaqc} which had the error.
     * @param flag The {@link FailureFlag} stating the reason of the failure.
     */
    void onTargetFailure(String serial, FailureFlag flag);

    /**
     * Callback stating that all {@link ATekdaqc}s have been found with the designated serial numbers.
     *
     * @param tekdaqcs A {@link Set} of all the {@link ATekdaqc}s found with designated serial numbers.
     */
    void onAllTargetsFound(Set<ATekdaqc> tekdaqcs);

}
