package com.tenkiv.tekdaqc.hardware;

/**
 * Abstract class detailing the basic behavior for inputs and output classesin  the tekdaqc java library.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 2.1.0.0
 */
public abstract class IInputOutputHardware {

    public static final String TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT
            = "Unable to preform operation. Tekdaqc is not connected.";
    /**
     * The physical channel. This is unique to a type of channel, there will be repeats for different channel type,
     * i.e. analog inputs and digital inputs.
     */
    protected final int mChannelNumber;
    /**
     * The {@link ATekdaqc} the input is associated with.
     */
    private transient final ATekdaqc mTekdaqc;
    /**
     * Boolean flag for determining if the input is activated or not.
     * Internally, this is represented by the input being added or the output being powered.
     */
    protected boolean isActivated = false;

    public IInputOutputHardware(final ATekdaqc mTekdaqc, final int channelNumber) {
        this.mTekdaqc = mTekdaqc;
        mChannelNumber = channelNumber;
    }

    /**
     * Method to activate an input or output. Internally this represents the
     * "ADD_ANALOG_INPUT/ADD_DIGITAL_INPUT" command or the corresponding output being powered.
     */
    public abstract void activate();

    /**
     * Method to deactivate an input or output. Internally this represents the
     * "REMOVE_ANALOG_INPUT/REMOVE_DIGITAL_INPUT" command or the corresponding output being unpowered.
     */
    public abstract void deactivate();

    /**
     * Internal method called to queue the proper add and remove commands so that the input is correctly updated to what the user has designated.
     */
    protected abstract void queueStatusChange();

    /**
     * Method returning the current activation status of the object.
     *
     * @return A {@link Boolean} representing this objects activation status.
     */
    public boolean isActivated() {
        return isActivated;
    }

    public ATekdaqc getTekdaqc() {
        return mTekdaqc;
    }

    /**
     * Retrieves the currently set channel number value.
     *
     * @return {@code int} The current input number value.
     */
    public int getChannelNumber() {
        return mChannelNumber;
    }
}
