package com.tenkiv.tekdaqc.utility;

import com.tenkiv.tekdaqc.hardware.IInputOutputHardware;

/**
 * Enum determining channel type for {@link IInputOutputHardware}
 */
public enum ChannelType {
    ANALOG_INPUT,
    DIGITAL_INPUT,
    DIGITAL_OUTPUT
}
