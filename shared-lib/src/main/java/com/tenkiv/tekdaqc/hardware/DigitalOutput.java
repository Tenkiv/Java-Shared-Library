package com.tenkiv.tekdaqc.hardware;


import com.tenkiv.tekdaqc.utility.ChannelType;
import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities;
import tec.uom.se.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import java.security.InvalidParameterException;

/**
 * Container class for all data/settings of an digital output on the Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com) * @since v1.0.0.0
 */
public class DigitalOutput extends IInputOutputHardware {

    /**
     * The name of the input channel. Must be 24 characters or less. Must be 24
     * characters or less
     */
    private volatile String mName = null;

    /**
     * The current output state.
     */
    private volatile boolean mIsOn;

    /**
     * Uptime for this output's PWM.
     */
    private volatile int mPulseWidthModulationDutyCycle = -1;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.DIGITAL_OUTPUT;
    }

    /**
     * Constructor
     *
     * @param output {@code int} The output channel number.
     */
    DigitalOutput(final ATekdaqc tekdaqc, final int output) {
        super(tekdaqc, output);
    }


    /**
     * Retrieves the currently set output name value.
     *
     * @return {@link String} The current output name value.
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the current output name for this output.
     *
     * @param name {@link String} The output name to set.
     * @throws IllegalArgumentException Must not exceed maximum name length.
     */
    public void setName(final String name) {
        if (name.length() >= ATekdaqc.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("The maximum length of a name is " + ATekdaqc.MAX_NAME_LENGTH + " characters.");
        } else {
            mName = name;
        }
    }

    /**
     * Retrieve the current {@link boolean} of this output.
     *
     * @return {@link boolean} The current state of this output.
     */
    public boolean getIsActivated() {
        return mIsOn;
    }

    public int getPulseWidthModulationDutyCycle() {
        return mPulseWidthModulationDutyCycle;
    }

    /**
     * Updates the state of the output's activity boolean.
     *
     * @param isOn The revised state.
     */
    protected void setIsActive(final boolean isOn) {
        mIsOn = isOn;
    }

    @Override
    public void activate() {
        if (getTekdaqc().isConnected()) {
            isActivated = true;
            mPulseWidthModulationDutyCycle = -1;
            mIsOn = true;
            getTekdaqc().queueCommand(CommandBuilderKt.setDigitalOutputByBinaryString(getTekdaqc().generateBinaryStringFromOutput()));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    @Override
    public void deactivate() {
        if (getTekdaqc().isConnected()) {
            isActivated = false;
            mPulseWidthModulationDutyCycle = -1;
            mIsOn = false;
            getTekdaqc().queueCommand(CommandBuilderKt.setDigitalOutputByBinaryString(getTekdaqc().generateBinaryStringFromOutput()));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    /**
     * Activates pulse width modulation on a digital output; allowing the user to set the percentage of the time
     * the digital output will be active.
     *
     * @param dutyCycle A int value between 0 and 100 to set as the uptime percentage.
     */
    public void setPulseWidthModulation(final int dutyCycle){
        if(dutyCycle < 0 || dutyCycle > 100){
            throw new InvalidParameterException("Uptime must be a value between 0 and 100");
        }
        isActivated = true;
        getTekdaqc().queueCommand(CommandBuilderKt.setDigitalOutputPulseWidthModulation
                        (DigitalOutputUtilities.intToHex(getChannelNumber()),dutyCycle));

    }

    /**
     * Activates pulse width modulation on a digital output; allowing the user to set the percentage of the time
     * the digital output will be active.
     *
     * @param dutyCycle A {@link Quantity} that should contain a value in {@link Units#PERCENT}.
     */
    public void setPulseWidthModulation(final Quantity<Dimensionless> dutyCycle){
        int iUptime = dutyCycle.to(Units.PERCENT).getValue().intValue();

        setPulseWidthModulation(iUptime);
    }

    @Override
    protected void queueStatusChange() {

    }
}
