package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.message.IDigitalChannelListener;
import com.tenkiv.tekdaqc.communication.message.IPWMChannelListener;
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

    private volatile Boolean isPWM = null;

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
            getTekdaqc().queueCommand(CommandBuilderKt.removeDigitalInputByNumber(mChannelNumber));
            getTekdaqc().queueCommand(CommandBuilderKt.addDigitalInput(this));
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
        if (getTekdaqc().isConnected() && isPWM == null) {
            isPWM = false;
            getTekdaqc().queueCommand(CommandBuilderKt.addDigitalInput(this));
        } else if(!getTekdaqc().isConnected()){
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        } else{
            throw new IllegalStateException("Input already added as a PWM input.");
        }
    }

    @Override
    public void deactivate() {
        if (getTekdaqc().isConnected()) {
            isPWM = null;
            getTekdaqc().queueCommand(CommandBuilderKt.removeDigitalInput(this));
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

    /**
     * Method to remove a {@link IDigitalChannelListener} this channel.
     *
     * @param listener The {@link IDigitalChannelListener} to remove from callbacks.
     */
    public void removeDigitalListener(IDigitalChannelListener listener) {
        getTekdaqc().removeDigitalChannelListener(this,listener);
    }

    /**
     * Method to add a {@link IPWMChannelListener} to listen for data on only this channel.
     *
     * @param listener The {@link IPWMChannelListener} to add for callbacks.
     */
    public void addPWMListener(IPWMChannelListener listener){
        getTekdaqc().addPWMChannelListener(listener, this);
    }

    /**
     * Method to remove a {@link IPWMChannelListener} this channel.
     *
     * @param listener The {@link IPWMChannelListener} to remove from callbacks.
     */
    public void removePWMListener(IPWMChannelListener listener){
        getTekdaqc().removePWMChannelListener(listener, this);
    }

    /**
     * Method to activate Pulse Width Modulation on this input. Input cannot be added already.
     */
    public void activatePWM(){
        if (getTekdaqc().isConnected() && isPWM == null) {
            getTekdaqc().queueCommand(CommandBuilderKt.addPWMInput(this));
        } else if(!getTekdaqc().isConnected()){
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        } else{
            throw new IllegalStateException("Input already added as a non-PWM input.");
        }
    }

    /**
     * Method to deactivate Pulse Width Modulation on this input.
     */
    public void deactivatePWM(){
        if (getTekdaqc().isConnected()) {
            isPWM = null;
            getTekdaqc().queueCommand(CommandBuilderKt.removePWMInput(this.getChannelNumber()));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    /**
     * Returns true if the {@link DigitalInput} is set to Pulse Width Modulate. Null if it is not added.
     *
     * @return Boolean if the added input is activated. Null if inactive.
     */
    public boolean isPWM() {
        return isPWM;
    }
}
