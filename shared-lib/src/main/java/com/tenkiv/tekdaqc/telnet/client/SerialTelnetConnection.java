package com.tenkiv.tekdaqc.telnet.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serial port implementation of the Tekdaqc Telnet connection.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public class SerialTelnetConnection implements ITekdaqcTelnetConnection {

    @Override
    public void disconnect() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

}
