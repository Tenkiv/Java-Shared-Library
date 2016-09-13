package com.tenkiv.tekdaqc.locator;



import com.tenkiv.tekdaqc.hardware.ATekdaqc;
import com.tenkiv.tekdaqc.hardware.Tekdaqc_RevD;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Singleton class which is responsible for searching for any Tekdaqcs on the network
 * which match the parameters provided at construction.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public final class Locator {

    /**
     * Lock ensuring thread safety of {@param activeTekdaqcMap}
     */
    private static final ReadWriteLock tekdaqcMapLock = new ReentrantReadWriteLock();

    /**
     * List of all active tekdaqcs
     */
    private static final Map<String, ATekdaqc> activeTekdaqcMap = new HashMap<String, ATekdaqc>();

    /**
     * Flag to enable/disable debug logging
     */
    private static boolean DEBUG = true;

    /**
     * The parameter set to use for the locator request
     */
    private LocatorParams mParams;

    /**
     * Lock ensuring thread safety of {@param mTempMapLock}
     */
    private final ReadWriteLock mTempMapLock = new ReentrantReadWriteLock();

    /**
     * The list of tekdaqcs currently discovered.
     */
    private final Map<String, ATekdaqc> mTempTekdaqcMap = new HashMap<String, ATekdaqc>();

    /**
     * The listeners associated with the locator.
     */
    private final ArrayList<OnTekdaqcDiscovered> mListeners =
            (ArrayList<OnTekdaqcDiscovered>) Collections.synchronizedList(new ArrayList<OnTekdaqcDiscovered>());

    /**
     * Instance of the Singleton.
     */
    private static Locator mInstance;

    /**
     * The timer run periodically to search for tekdaqcs on the local area network.
     */
    private final Timer mUpdateTimer = new Timer();

    /**
     * The timer task run at interval, which updates the {@link List} of known {@link ATekdaqc}
     */
    private final TimerTask mUpdateTask = new TimerTask() {
        @Override
        public void run() {

            updateKnownTekdaqcs();

            try {
                locate();

            } catch (SocketException e) {
                e.printStackTrace();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Create a Tekdaqc locator which will search for Tekdaqcs matching
     * {@link LocatorParams}.
     *
     * @param params Locator parameters to configure the UDP broadcasts.
     */
    protected Locator(final LocatorParams params){
        if (params == null) {
            mParams = new LocatorParams.Builder().build();
        } else {
            mParams = params;
        }
    }

    /**
     * Gets the instance of the singleton class {@link Locator}. To ensure safe execution of the {@link Locator}, there
     * needs to be only a single instance of the class as it occupies a single designated port for a long duration.
     * To get an unsafe instance use {@param createUnsafeInstance}
     * @return
     */
    public static Locator getInstance(){
        if(mInstance == null){
            mInstance = new Locator(null);
        }
        return mInstance;
    }

    /**
     * Sets new params for the {@link Locator}.
     * Setting new params cancels execution of the current Locator if it is running, requiring it to be restarted.
     *
     *
     * @param params The {@link LocatorParams} to be set for the {@link Locator}.
     */
    public void setLocatorParams(final LocatorParams params){
        mUpdateTimer.cancel();

        mParams = params;

    }

    /**
     * Creates an unsafe instance of the {@link Locator} class. If not properly configured, this locator will not
     * function while another is active. Use {@link LocatorParams} to set a different port to search on, however this
     * will only work if the Tekdaqc is programmed to respond to the new port.
     *
     * @param params The {@link LocatorParams} to be set for the {@link Locator}.
     * @return An unsafe {@link Locator} instance.
     */
    public static Locator createUnsafeLocator(final LocatorParams params){
        return new Locator(params);
    }

    /**
     * Method to add a {@link OnTekdaqcDiscovered} listener to be notified about {@link Locator} discoveries.
     *
     * @param listener The {@link OnTekdaqcDiscovered} locator to be added.
     */
    public void addLocatorListener(final OnTekdaqcDiscovered listener){
        mListeners.add(listener);
    }

    /**
     * Get the status of debug logging.
     *
     * @return boolean True if debug logging is enabled.
     */
    public static final boolean getDebug() {
        return DEBUG;
    }

    /**
     * Set status of debug logging.
     *
     * @param debug boolean The state to set debugging to.
     */
    public static final void setDebug(boolean debug) {
        DEBUG = debug;
    }

    /**
     * Factory method for producing a {@link ATekdaqc} of the correct subclass
     * based on the response from the locator service.
     *
     * @param response {@link LocatorResponse} The response sent by the Tekdaqc.
     * @return {@link ATekdaqc} The constructed Tekdaqc.
     */
    protected static ATekdaqc createTekdaqc(final LocatorResponse response) {
        // This is here to allow for future backwards compatibility with
        // different board versions
        final ATekdaqc tekdaqc;
        switch (response.getType()) {
            case 'D':
            case 'E':
                tekdaqc = new Tekdaqc_RevD(response);
                addTekdaqctoMap(tekdaqc);
                break;
            default:
                throw new IllegalArgumentException("Unknown Tekdaqc Revision: " + ((char) response.getType()));
        }
        return tekdaqc;
    }

    /**
     * Retrieves a {@link ATekdaqc} for the specified serial {@link String}.
     *
     * @param serial {@link String} The serial number to search for.
     * @return {@link ATekdaqc} The known {@link ATekdaqc} for
     * <code>serial</code> or <code>null</code> if no match was found.
     */
    public static ATekdaqc getTekdaqcForSerial(final String serial) {
        tekdaqcMapLock.readLock().lock();
        final ATekdaqc tekdaqc = activeTekdaqcMap.get(serial);
        tekdaqcMapLock.readLock().unlock();
        return tekdaqc;
    }

    /**
     * Removes a {@link ATekdaqc} for the specified serial {@link String}.
     *
     * @param serial {@link String} The serial number to of the {@link ATekdaqc} to
     *               remove.
     */
    protected static void removeTekdaqcForSerial(final String serial) {
        tekdaqcMapLock.writeLock().lock();
        activeTekdaqcMap.remove(serial);
        tekdaqcMapLock.writeLock().unlock();
    }

    protected static void addTekdaqctoMap(final ATekdaqc tekdaqc){
        tekdaqcMapLock.writeLock().lock();
        if (!activeTekdaqcMap.containsKey(tekdaqc.getSerialNumber())) {
            activeTekdaqcMap.put(tekdaqc.getSerialNumber(), tekdaqc);
        }
        tekdaqcMapLock.writeLock().unlock();
    }

    /**
     * Gets a copy of a {@link Map} representing all currently located {@link ATekdaqc}.
     *
     * @return A new {@link HashMap} which contains all currently located {@link ATekdaqc}.
     */
    public static Map<String, ATekdaqc> getActiveTekdaqcMap() {
        tekdaqcMapLock.readLock().lock();
        final Map<String, ATekdaqc> copy = new HashMap<String, ATekdaqc>(activeTekdaqcMap);
        tekdaqcMapLock.readLock().unlock();
        return copy;
    }

    /**
     * Activates the search for Tekdaqcs.
     *
     * @return True if discovery is started successfully.
     * @throws SocketException      Thrown if there is a problem with the underlying socket.
     * @throws UnknownHostException Thrown if the IP address this {@link Locator} is pinging is
     *                              invalid.
     */
    private boolean locate() throws SocketException, UnknownHostException {

        final InetAddress mAddress = InetAddress.getByName(mParams.getIpAddress());
        final DatagramSocket mSocket = new DatagramSocket(mParams.getPort());
        mSocket.setBroadcast(true);
        mSocket.setSoTimeout(mParams.getTimeout());

        try {
            sendDiscoveryRequest(mSocket, mAddress);
            return true;
        } catch (IOException e) {
            if (DEBUG)
                System.out.println("Unable to discover targets due to IO Exception.");

            return false;
        } finally {
            if (DEBUG)
                System.out.println("Closing socket...");
            mSocket.close();
        }
    }

    /**
     * Sends out the discovery packet on the network.
     *
     * @param socket {@link DatagramSocket} to send on.
     * @param addr   {@link InetAddress} to send the discovery packet to.
     * @throws {@link IOException} Thrown if there is a problem with the
     *                underlying interface.
     */
    private void sendDiscoveryRequest(final DatagramSocket socket, final InetAddress addr)
            throws IOException {
        final String message = mParams.getMessage();
        final byte[] data = message.getBytes();
        byte[] buf = new byte[1024];

        if (DEBUG) {
            System.out.println("Data bytes length: " + data.length + " String Length: " + message.length());
            System.out.println("Sending discovery packet...");
        }

        socket.send(new DatagramPacket(data, data.length, addr, mParams.getPort()));

        while (true) {
            try {
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                if (DEBUG)
                    System.out.println("Waiting for return packet...");
                socket.receive(packet);
                final LocatorResponse response = new LocatorResponse(packet.getAddress().getHostAddress(), packet.getData());
                if (response.isValid(mParams)) {

                    mTempMapLock.writeLock().lock();
                    if (isKnownTekdaqc(response.getSerial())) {
                        mTempTekdaqcMap.put(response.getSerial(), getTekdaqcForSerial(response.getSerial()));
                    } else {
                        final ATekdaqc tekdaqc = createTekdaqc(response);
                        mTempTekdaqcMap.put(tekdaqc.getSerialNumber(), tekdaqc);

                        for(OnTekdaqcDiscovered listener: mListeners) {
                            listener.onTekdaqcFirstLocated(tekdaqc);
                        }
                    }
                    for(OnTekdaqcDiscovered listener: mListeners) {
                        listener.onTekdaqcResponse(getTekdaqcForSerial(response.getSerial()));
                    }
                    mTempMapLock.writeLock().unlock();
                } else {
                    if (DEBUG) System.out.println("Invalid response received: ");
                }
            } catch (SocketTimeoutException e) {
                if (DEBUG) System.out.println("Discovery timed out.");
                return;
            }
            buf = new byte[1024];
        }
    }

    /**
     * Method to update the current list of discovered {@link ATekdaqc}.
     */
    private void updateKnownTekdaqcs() {
        tekdaqcMapLock.readLock().lock();
        for (final Map.Entry<String, ATekdaqc> questionableBoard : getActiveTekdaqcMap().entrySet()) {

            mTempMapLock.readLock().lock();

            System.out.println("Recent has "+questionableBoard.getKey()+"?: "+mTempTekdaqcMap.containsKey(questionableBoard.getKey())+" Is Connected?: "+questionableBoard.getValue().isConnected());

            if (!mTempTekdaqcMap.containsKey(questionableBoard.getKey()) && !questionableBoard.getValue().isConnected()) {
                System.out.println("Is WriteLocked on current thread? "+((ReentrantReadWriteLock) tekdaqcMapLock).isWriteLockedByCurrentThread()+'\n'+
                "Is WriteLocked total? "+((ReentrantReadWriteLock) tekdaqcMapLock).isWriteLocked());
                tekdaqcMapLock.readLock().unlock();
                tekdaqcMapLock.writeLock().lock();
                removeTekdaqcForSerial(questionableBoard.getKey());
                tekdaqcMapLock.readLock().lock();
                tekdaqcMapLock.writeLock().unlock();
                for(OnTekdaqcDiscovered listener: mListeners) {
                    listener.onTekdaqcNoLongerLocated(questionableBoard.getValue());
                }
            }
            mTempMapLock.readLock().unlock();
        }
        tekdaqcMapLock.readLock().unlock();
        mTempTekdaqcMap.clear();
    }

    /**
     * Internal method to determine if a {@link ATekdaqc} has been located.
     *
     * @param serialNumber The {@link String} of the {@link ATekdaqc} serial number.
     * @return {@link Boolean} of if the {@link ATekdaqc} has been located.
     */
    private boolean isKnownTekdaqc(final String serialNumber) {
        tekdaqcMapLock.readLock().lock();
        try{
            return getActiveTekdaqcMap().containsKey(serialNumber);
        }finally {
            tekdaqcMapLock.readLock().unlock();
        }
    }

    /**
     * Method which halts the locator.
     */
    public void cancelLocator() {

        mUpdateTimer.cancel();
    }

    /**
     * Method which starts the locator at a given time and rate.
     *
     * @param delay  The delay at which to start the locator as a {@link Long}.
     * @param period The period at which to run the locator as a {@link Long}.
     */
    public void searchForTekDAQCS(final long delay, final long period) {

        mUpdateTimer.scheduleAtFixedRate(mUpdateTask, delay, period);
    }
}
