package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.message.IDigitalChannelListener;
import com.tenkiv.tekdaqc.utility.ChannelType;

/**
 * Container class for all data/settings of an digital input on the Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class DigitalInput extends IInputOutputHardware {

    /**
     * The name of the input channel. Must be 24 characters or less. Must be 24
     * characters or less
     */
    private volatile String mName = null;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.DIGITAL_INPUT;
    }

    /**
     * Constructor
     *
     * @param input {@code int} The input channel number.
     */
    DigitalInput(final ATekdaqc tekdaqc, final int input) {
        super(tekdaqc, input);

    }

    @Override
    protected void queueStatusChange() {
        if (getTekdaqc().isConnected() && isActivated) {
            getTekdaqc().queueCommand(CommandBuilder.removeDigitalInputByNumber(mChannelNumber));
            getTekdaqc().queueCommand(CommandBuilder.addDigitalInput(this));
        }
    }


    /**
     * Retrieves the currently set input name value.
     *
     * @return {@link String} The current input name value.
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the current input name for this input.
     *
     * @param name {@link String} The input name to set.
     * @return {@link DigitalInput} This input to facilitate chaining.
     * @throws IllegalArgumentException Must not exceed maximum name length.
     */
    public DigitalInput setName(final String name) throws IllegalArgumentException {
        if (name.length() >= ATekdaqc.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("The maximum length of a name is " + ATekdaqc.MAX_NAME_LENGTH + " characters.");
        } else {
            mName = name;
        }
        return this;
    }

    @Override
    public void activate() {
        if (getTekdaqc().isConnected()) {
            getTekdaqc().queueCommand(CommandBuilder.addDigitalInput(this));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    @Override
    public void deactivate() {
        if (getTekdaqc().isConnected()) {
            getTekdaqc().queueCommand(CommandBuilder.removeDigitalInput(this));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    /**
     * Method to add a {@link IDigitalChannelListener} to listen for data on only this channel.
     *
     * @param listener The {@link IDigitalChannelListener} to add for callbacks.
     */
    public void addDigitalListener(IDigitalChannelListener listener) {
        getTekdaqc().addDigitalChannelListener(listener, this);
    }
}
