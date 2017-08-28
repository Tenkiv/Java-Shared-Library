package com.tenkiv.tekdaqc.communication.ascii.message.parsing

/**
 * Created by tenkiv on 1/25/17.
 */
enum class TekdaqcStatuses {
    COMMAND_SUCCESS,
    SLOW_ANALOG_SAMPLING,
    ANALOG_MAY_BE_SET_TO_ORIGINAL_RATE,
    INPUTS_SET_TO
}