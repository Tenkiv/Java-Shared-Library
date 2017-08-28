package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.hardware.ATekdaqc

/**
 * Callback for sets of specific boards which have been discovered by the [Locator].

 * @author Tenkiv (software@tenkiv.com)
 * *
 * @since v2.1.3.0
 */
interface OnTargetTekdaqcFound {

    /**
     * Enum stating the failure point of the [Locator] in managing discovery of the [ATekdaqc]s.
     */
    enum class FailureFlag {

        TEKDAQC_NOT_LOCATED,

        AUTO_CONNECT_FAILURE

    }

    /**
     * Callback stating that target [ATekdaqc] has been successfully located.

     * @param tekdaqc The located [ATekdaqc].
     */
    fun onTargetFound(tekdaqc: ATekdaqc)

    /**
     * Callback stating that the [Locator] encountered an error.

     * @param serial The [String] of the serial number of the the [ATekdaqc] which had the error.
     * *
     * @param flag The [FailureFlag] stating the reason of the failure.
     */
    fun onTargetFailure(serial: String, flag: FailureFlag)

    /**
     * Callback stating that all [ATekdaqc]s have been found with the designated serial numbers.

     * @param tekdaqcs A [Set] of all the [ATekdaqc]s found with designated serial numbers.
     */
    fun onAllTargetsFound(tekdaqcs: Set<ATekdaqc>)

}
