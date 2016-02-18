package com.tenkiv.tekdaqc.telnet.client;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Ethernet implementation of the Tekdaqc Telnet connection.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public class EthernetTelnetConnection implements ITekdaqcTelnetConnection {
    /**
     * Default connection parameters
     */
    public static final int TEKDAQC_TELNET_PORT = 9801;
    public static final int TEKDAQC_TELNET_CONNECTION_TIMEOUT = 5000;
    public static final int TEKDAQC_KEEP_ALIVE_TIMEOUT = 5 * 60 * 1000;

    /**
     * The telnet client
     */
    private final TelnetClient mTelnet;

    /**
     * The output stream
     */
    private final OutputStream mOut;

    /**
     * The input stream
     */
    private final InputStream mIn;

    /**
     * Creates and connects a Telnet client.
     *
     * @param host {@link String} Target host name
     * @param port {@code int} Port number
     * @throws IOException Thrown if the Telnet connection fails.
     */
    public EthernetTelnetConnection(final String host, final int port) throws IOException {
        mTelnet = new TelnetClient();
        mTelnet.setDefaultTimeout(TEKDAQC_KEEP_ALIVE_TIMEOUT);
        mTelnet.setConnectTimeout(TEKDAQC_TELNET_CONNECTION_TIMEOUT);
        mTelnet.connect(host, port);

        mOut = mTelnet.getOutputStream();
        mIn = mTelnet.getInputStream();
    }

    @Override
    public boolean isConnected() {
        return mTelnet.isConnected();
    }

    @Override
    public void disconnect() throws IOException {
        mTelnet.disconnect();
    }

    @Override
    public InputStream getInputStream() {
        return mIn;
    }

    @Override
    public OutputStream getOutputStream() {
        return mOut;
    }
}
