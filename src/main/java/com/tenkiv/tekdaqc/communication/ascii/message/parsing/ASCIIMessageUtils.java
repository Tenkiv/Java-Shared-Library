package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import java.util.regex.Pattern;

/**
 * Utility class for ASCII Messages.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 2.0.0.0
 */
public class ASCIIMessageUtils {

    protected static final String V2_ANALOG_INPUT_HEADER = "?A";

    protected static final String V2_DIGITAL_INPUT_HEADER = "?D";

    protected static final String DIGITAL_PWM_INPUT_HEADER = "?P";

    protected static final String V1_ANALOG_INPUT_HEADER = "Analog Input";

    protected static final String V1_DIGITAL_INPUT_HEADER = "Digital Input";

    protected static final String DIGITAL_OUTPUT_HEADER = "Digital Output";

    protected static final String DEBUG_MESSAGE_HEADER = "Debug Message";

    protected static final String STATUS_MESSAGE_HEADER = "Status Message";

    protected static final String COMMAND_MESSAGE_HEADER = "Command Data Message";

    protected static final String ERROR_MESSAGE_HEADER = "Error Message";

    protected static final String NETWORK_ERROR_FLAG = "[NETWORK]";

    protected static final String MESSAGE_TAG = "Message: ";

    protected static final String PWM_OUTPUT_TAG = "Pwm Output: ";

    protected static final String DUTY_CYCLE = "DutyCycle: ";

    protected static final String NAME_TAG = "Name: ";

    protected static final String PHYSICAL_INPUT_TAG = "Physical Input: ";

    protected static final String PGA_TAG = "PGA: ";

    protected static final String RATE_TAG = "Rate: ";

    protected static final String BUFFER_STATUS_TAG = "Buffer Status: ";

    protected static final String TIMESTAMP_TAG = "Timestamp: ";

    protected static final String VALUE_TAG = "Value: ";

    protected static final String LEVEL_TAG = "Level: ";

    protected static final String LINE_MARKER = "--------------------";

    protected static final String HIGH_MARKER = "H";

    protected static final String LOW_MARKER = "L";

    protected static final int UNIT_SEPARATOR_CHAR = 0x1F;

    protected static final int NEW_LINE_CHAR = 0x0A;

    protected static final int CARRIAGE_RETURN_CHAR = 0x0D;

    public static final Pattern RECORD_SEPARATOR_PATTERN = Pattern
            .compile("\\x1E");

    /**
     * Factory method to produce the appropriate messages from the provided raw
     * message data.
     *
     * @param messageData {@link String} The raw message data.
     * @return {@link AASCIIMessage} The constructed message.
     */
    public static AASCIIMessage parseMessage(final String messageData) {
        final AASCIIMessage message;
        try {
            if (messageData == null)
                return null;
            /*
			 * The order here is important because debug/status/error messages
			 * may contain tags which could register as other message types.
			 */
            if (messageData.contains(DEBUG_MESSAGE_HEADER)) {
                // This is an ASCII Debug message
                message = new ASCIIDebugMessage(messageData);
            } else if (messageData.contains(STATUS_MESSAGE_HEADER)) {
                // This is an ASCII Status message
                message = new ASCIIStatusMessage(messageData);
                message.setData(messageData);
            } else if (messageData.contains(ERROR_MESSAGE_HEADER)) {
                // This is an ASCII Error message
                message = new ASCIIErrorMessage(messageData);
            } else if (messageData.contains(COMMAND_MESSAGE_HEADER)) {
                message = new ASCIICommandMessage(messageData);
            } else if (messageData.contains(V1_ANALOG_INPUT_HEADER)
                    || messageData.contains(V2_ANALOG_INPUT_HEADER)) {
                // This is an ASCII Analog Input Data message
                message = new ASCIIAnalogInputDataMessage(messageData);
            } else if (messageData.contains(V1_DIGITAL_INPUT_HEADER)
                    || messageData.contains(V2_DIGITAL_INPUT_HEADER)) {
                // This is an ASCII Digital Input Data message
                message = new ASCIIDigitalInputDataMessage(messageData);
            } else if (messageData.contains(DIGITAL_OUTPUT_HEADER)) {
                // This is an ASCII Digital Output Data message
                message = new ASCIIDigitalOutputDataMessage(messageData);
            }else if (messageData.contains(DIGITAL_PWM_INPUT_HEADER)) {
                // This is an ASCII Digital PWM Input Data message
                message = new ASCIIPWMInputDataMessage(messageData);
            } else {
                // This is an unrecognized message format
                message = null;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }

    /**
     * Message type enumeration.
     */
    public static enum MESSAGE_TYPE {
        /**
         * Debug
         */
        DEBUG,
        /**
         * Status
         */
        STATUS,
        /**
         * Error
         */
        ERROR,
        /**
         * AI Data
         */
        ANALOG_INPUT_DATA,
        /**
         * DI Data
         */
        DIGITAL_INPUT_DATA,
        /**
         * DO Data
         */
        DIGITAL_OUTPUT_DATA,
        /**
         * Command Data
         */
        COMMAND_DATA,
        /**
         * PWM Data
         */
        PWM_INPUT_DATA
    }
}
