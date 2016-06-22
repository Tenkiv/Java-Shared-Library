package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.utility.DigitalState;

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
    private volatile DigitalState mCurrentState;

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
     * Retrieve the current {@link DigitalState} of this output.
     *
     * @return {@link DigitalState} The current state of this output.
     */
    public DigitalState getCurrentState() {
        return mCurrentState;
    }

    protected void setCurrentState(final DigitalState currentState) {
        mCurrentState = currentState;
    }

    @Override
    public void activate() {
        if (getTekdaqc().isConnected()) {
            isActivated = true;
            mCurrentState = DigitalState.LOGIC_HIGH;
            getTekdaqc().queueCommand(CommandBuilder.setDigitalOutputByBinaryString(getTekdaqc().generateBinaryStringFromOutput()));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    @Override
    public void deactivate() {
        if (getTekdaqc().isConnected()) {
            isActivated = false;
            mCurrentState = DigitalState.LOGIC_LOW;
            getTekdaqc().queueCommand(CommandBuilder.setDigitalOutputByBinaryString(getTekdaqc().generateBinaryStringFromOutput()));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    @Override
    protected void queueStatusChange() {

    }
}
