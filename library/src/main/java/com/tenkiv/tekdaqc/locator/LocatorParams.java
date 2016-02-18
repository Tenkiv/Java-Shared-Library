package com.tenkiv.tekdaqc.locator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Configuration parameters for beginning location search of TekDAQC devices. This is an immutable class that may only
 * be constructed through {@link LocatorParams.Builder}.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public final class LocatorParams implements Externalizable {

    /**
     * Filter for any type of board.
     */
    public static final char TYPE_ANY = (char) -1;

    /**
     * Is Debug?
     */
    public static final boolean DEBUG = false;

    /**
     * Default broadcast IP address.
     */
    public static final String DEFAULT_IP_ADDRESS = "255.255.255.255";

    /**
     * Default port number to search on.
     */
    public static final int DEFAULT_PORT = 9800;

    /**
     * Default timeout in milliseconds.
     */
    public static final int DEFAULT_TIMEOUT = 500;

    /**
     * Default activation message.
     */
    public static final String DEFAULT_MESSAGE = "TEKDAQC CONNECT";

    /**
     * Invalid firmware version returned by the UDP broadcast.
     */
    private static final String INVALID_FIRMWARE_VERSION = "0.0.0.0";

    /**
     * The firmware version string to search for.
     */
    private String firmware;

    /**
     * The board IP Address to search for. Use of a broadcast/multicast IP will allow unknown board discovery.
     */
    private String ipAddress;

    /**
     * The discovery message to send in the locator packet.
     */
    private String message;

    /**
     * The application title to search for.
     */
    private String title;

    /**
     * The board serial number to search for.
     */
    private String serial;

    /**
     * The UDP port to search on.
     */
    private int port;

    /**
     * The timeout period in milliseconds for the search.
     */
    private int timeout;

    /**
     * The board version type to search for.
     */
    private char type;

    /**
     * Constructor.
     */
    public LocatorParams() {
    }

    /**
     * Retrieve a default instance of the locator parameters.
     *
     * @return {@link LocatorParams} A locator parameter packet with default configuration.
     */
    public static LocatorParams getDefaultInstance() {
        return new Builder().build();
    }

    /**
     * Determine if a {@link LocatorResponse} meets the minimum criteria of a match to {@link LocatorParams}. A match is
     * found if the Title, Serial, Type, and Firmware are equal.
     *
     * @param params   {@link LocatorParams} The parameter set that was used in the search.
     * @param response {@link LocatorResponse} The response which was generated from the search.
     * @return boolean True if the locator response and parameters match.
     */
    static boolean isValidResponse(LocatorParams params, LocatorResponse response) {
        if (params.title != null) {
            if (!params.title.equals(response.getTitle())) {
                if (DEBUG) System.out.println("Invalid Title in response: " + response.getTitle());
                return false;
            }
        }

        if (params.serial != null) {
            if (!params.serial.equals(response.getSerial())) {
                if (DEBUG) System.out.println("Invalid Serial in response: " + response.getSerial());
                return false;
            }
        }

        if (params.type != TYPE_ANY) {
            if (params.type != response.getType()) {
                if (DEBUG) System.out.println("Invalid Type in response: " + response.getType());
                return false;
            }
        }

        if (params.firmware != null) {
            if (!params.firmware.equals(response.getFirwareVersion())) {
                if (DEBUG)
                    System.out.println("Invalid Firmware Version in response: " + response.getFirwareVersion());
                return false;
            }

        }

        if (response.getFirwareVersion().equals(INVALID_FIRMWARE_VERSION)) {
            System.out.println("Firmware Response is Null! Cannot Process Response with no Firmware Version.");
            return false;
        }
        return true;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        firmware = (String) in.readObject();
        ipAddress = (String) in.readObject();
        message = (String) in.readObject();
        title = (String) in.readObject();
        serial = (String) in.readObject();
        port = in.readInt();
        timeout = in.readInt();
        type = in.readChar();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(firmware);
        out.writeObject(ipAddress);
        out.writeObject(message);
        out.writeObject(title);
        out.writeObject(serial);
        out.writeInt(port);
        out.writeInt(timeout);
        out.writeChar(type);
    }

    /**
     * Retrieve the firmware version string for these parameters.
     *
     * @return {@link String} The firmware version.
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Retrieve the IP Address string for these parameters.
     *
     * @return {@link String} The IP Address.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Retrieve the locator message string for these parameters.
     *
     * @return {@link String} The locator message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieve the serial number string for these parameters.
     *
     * @return {@link String} The serial number string.
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Retrieve the application title string for these parameters.
     *
     * @return {@link String} The application title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieve the UDP port number for these parameters.
     *
     * @return int The UDP port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Retrieve the search timeout for these parameters.
     *
     * @return int The search timeout in milliseconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Retrieve the board revision for these parameters.
     *
     * @return char The board revision character.
     */
    public char getType() {
        return type;
    }

    /**
     * @author Ian Thomas (toxicbakery@gmail.com)
     * @since v1.0.0.0
     */
    public static final class Builder {

        private String mFirmware;
        private String mIpAddress;
        private String mMessage;
        private String mSerial;
        private String mTitle;
        private int mPort;
        private int mTimeout;
        private char mType;

        /**
         * Constructs a builder initialized for all default parameters.
         */
        public Builder() {
            mIpAddress = DEFAULT_IP_ADDRESS;
            mMessage = DEFAULT_MESSAGE;
            mPort = DEFAULT_PORT;
            mTimeout = DEFAULT_TIMEOUT;
            mType = TYPE_ANY;
        }

        /**
         * Returns a new instance of {@link LocatorParams}.
         *
         * @return {@link LocatorParams} The built instance.
         */
        public LocatorParams build() {
            final LocatorParams params = new LocatorParams();
            params.firmware = mFirmware == null ? null : mFirmware;
            params.ipAddress = mIpAddress;
            params.message = mMessage;
            params.serial = mSerial == null ? null : mSerial;
            params.title = mTitle == null ? null : mTitle;
            params.port = mPort;
            params.timeout = mTimeout;
            params.type = mType;

            return params;
        }

        /**
         * Firmware version string. Can be null. If provided, the locator will only provide boards which have the
         * specified firmware version.
         *
         * @param firmware {@link String} The firmware version string to filter for.
         */
        public void setFirmware(String firmware) {
            if (firmware == null)
                throw new IllegalArgumentException();

            mFirmware = firmware;
        }

        /**
         * String IP address to broadcast to. Can be null, defaults to 255.255.255.255 (broadcast).
         *
         * @param address {@link String} The IP Address string to send to.
         */
        public void setIpAddress(String address) {
            if (address == null)
                throw new IllegalArgumentException();

            mIpAddress = address;
        }

        /**
         * The message to send in the broadcast. This message must match what the board is expecting for it to send a
         * response.
         *
         * @param message {@link String} The activation message to send.
         */
        public void setMessage(String message) {
            if (message == null)
                throw new IllegalArgumentException();

            mMessage = message;
        }

        /**
         * The board serial number to search for. Can be null. If provided, the locator will only provide a board with a
         * matching serial number.
         *
         * @param serial {@link String} The serial number to filter for.
         */
        public void setSerial(String serial) {
            if (serial == null)
                throw new IllegalArgumentException();

            mSerial = serial;
        }

        /**
         * The application title to search for. Can be null. If provided, the locator will only provide boards which
         * have a matching application.
         *
         * @param title {@link String} The application title to filter for.
         */
        public void setTitle(String title) {
            if (title == null)
                throw new IllegalArgumentException();

            mTitle = title;
        }

        /**
         * The port number to send the broadcast on.
         *
         * @param port int The UDP port to broadcast on.
         */
        public void setPort(int port) {
            mPort = port;
        }

        /**
         * How long to wait for a response before giving up.
         *
         * @param timeout int Timeout period in milliseconds.
         */
        public void setTimeout(int timeout) {
            mTimeout = timeout;
        }

        /**
         * The board type to search for. If positive, the locator will only provide boards which have the specified
         * type.
         *
         * @param type char The board revision to filter for.
         */
        public void setType(char type) {
            mType = type;
        }

    }
}
