package com.tenkiv.tekdaqc.telnet.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface defining methods for interfacing to the Tekdaqc's Telnet server.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public interface ITekdaqcTelnetConnection {

    /**
     * Indicates the status of the Telnet connection.
     *
     * @return True if the connection is open.
     */
    boolean isConnected();

    /**
     * Closes the Telnet connection.
     *
     * @throws IOException Thrown if the closing the Telnet socket fails.
     */
    void disconnect() throws IOException;

    /**
     * Gets the {@link InputStream} to read data from the Telnet connection.
     *
     * @return {@link InputStream} The input stream.
     */
    InputStream getInputStream();

    /**
     * Gets the {@link OutputStream} to write data to the Telnet connection.
     *
     * @return {@link OutputStream} The output stream.
     */
    OutputStream getOutputStream();
}
