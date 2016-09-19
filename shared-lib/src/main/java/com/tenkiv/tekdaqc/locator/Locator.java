package com.tenkiv.tekdaqc.locator;



import com.sun.javafx.collections.UnmodifiableListSet;
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
     * Lock ensuring thread safety of {@link Locator#activeTekdaqcMap}
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
     * Lock ensuring thread safety of {@link Locator#mTempMapLock}
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
     * The default delay on the {@link Locator} running.
     */
    private static final long DEFAULT_LOCATOR_DELAY = 0;

    /**
     * The default period of running the {@link Locator}.
     */
    private static final long DEFAULT_LOCATOR_PERIOD = 5000;

    /**
     * Boolean determining if the {@link Locator} is running.
     */
    private boolean mIsActive = false;

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
     * To get an unsafe instance use {@link Locator#createUnsafeLocator(LocatorParams)}
     *
     * @return The instance of the {@link Locator}.
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
     * Method to remove a {@link OnTekdaqcDiscovered} listener from the {@link Locator} callbacks.
     *
     * @param listener The {@link OnTekdaqcDiscovered} listener to be removed.
     */
    public void removeLocatorListener(final OnTekdaqcDiscovered listener){
        mListeners.remove(listener);
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
     * @param isSafeCreation If the {@link ATekdaqc} was found through reliable location instead of an automatically
     *                       assumed preexisting IP address and MAC values.
     *
     * @return {@link ATekdaqc} The constructed Tekdaqc.
     */
    protected static ATekdaqc createTekdaqc(final LocatorResponse response, boolean isSafeCreation) {
        // This is here to allow for future backwards compatibility with
        // different board versions
        final ATekdaqc tekdaqc;
        switch (response.getType()) {
            case 'D':
            case 'E':
                tekdaqc = new Tekdaqc_RevD(response);

                if(isSafeCreation) {
                    addTekdaqctoMap(tekdaqc);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown Tekdaqc Revision: " + ((char) response.getType()));
        }
        return tekdaqc;
    }

    /**
     * Method to connect to a discovered Tekdaqc of target serial number. Returns a CONNECTED {@link ATekdaqc}.
     *
     * @param serialNumber A {@link String} of the target {@link ATekdaqc}'s serial number.
     * @return A {@link ATekdaqc} with an open connection.
     */
    public static ATekdaqc connectToTargetTekdaqc(String serialNumber){

        Map<String,ATekdaqc> map = Locator.getActiveTekdaqcMap();

        if(map.containsKey(serialNumber)){
            ATekdaqc tekdaqc = map.get(serialNumber);
            try {
                tekdaqc.connect(ATekdaqc.CONNECTION_METHOD.ETHERNET);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return tekdaqc;

        }else{
            try {
                throw new Exception("No Tekdaqc Found with serial number "+serialNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Method to create a {@link ATekdaqc} from an assumed pre-known serial number, IP address, and board revision.
     * Because this does not guarantee that the {@link ATekdaqc} actually exists, the {@link ATekdaqc} created in this
     * manner will not be added to the global {@link Locator#getActiveTekdaqcMap()}.
     *
     * @param serialNumber The assumed serial number of the hypothetical {@link ATekdaqc}.
     * @param hostIPAdress The assumed IP address of the hypothetical {@link ATekdaqc}.
     * @param tekdaqcRevision The assumed revision of the hypothetical {@link ATekdaqc}.
     *
     * @return A {@link ATekdaqc} object that represents an un-located, hypothetical Tekdaqc on the network.
     */
    public static ATekdaqc connectToUnsafeTarget(String serialNumber, String hostIPAdress, char tekdaqcRevision){

        LocatorResponse pseudoResponse = new LocatorResponse();

        pseudoResponse.mHostIPAddress = hostIPAdress;

        pseudoResponse.mType = tekdaqcRevision;

        pseudoResponse.mSerial = serialNumber;

        ATekdaqc tekdaqc = Locator.createTekdaqc(pseudoResponse, false);

        try {
            tekdaqc.connect(ATekdaqc.CONNECTION_METHOD.ETHERNET);
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * Adds a {@link ATekdaqc} to the global map of {@link Locator#getActiveTekdaqcMap()}, This should only be done if
     * you are certain the {@link ATekdaqc} exists. Adding unsafe {@link ATekdaqc}s may cause crashes or spooky behavior.
     *
     * @param tekdaqc The {@link ATekdaqc} to be added.
     */
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
                        final ATekdaqc tekdaqc = createTekdaqc(response, true);
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

            if (!mTempTekdaqcMap.containsKey(questionableBoard.getKey()) && !questionableBoard.getValue().isConnected()) {

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

        mIsActive = false;

        mUpdateTimer.cancel();
    }

    public boolean isActive(){
        return mIsActive;
    }

    /**
     * Method which starts the locator at a given delay and period.
     *
     * @param delay  The delay at which to start the locator as a {@link Long}.
     * @param period The period at which to run the locator as a {@link Long}.
     */
    public void searchForTekdaqcs(final long delay, final long period) {

        mIsActive = true;

        mUpdateTimer.scheduleAtFixedRate(mUpdateTask, delay, period);
    }

    /**
     * Method which starts the locator at the default delay and period.
     */
    public void searchForTekdaqcs(){

        mIsActive = true;

        mUpdateTimer.scheduleAtFixedRate(mUpdateTask, DEFAULT_LOCATOR_DELAY, DEFAULT_LOCATOR_PERIOD);
    }

    /**
     * Convenience method to search for specific {@link ATekdaqc}s on the network. Note: this method will start the {@link Locator}'s
     * default method ({@link Locator#searchForTekdaqcs()}), so other classes may also be notified of discovered {@link ATekdaqc}s.
     *
     * @param listener The {@link OnTargetTekdaqcFound} listener to be notified.
     * @param timeoutDuration The maximum time to run before returning {@link OnTargetTekdaqcFound#onFailure(String, OnTargetTekdaqcFound.FailureFlag)}
     * @param serials Variable arguments of the serial numbers of the {@link ATekdaqc} to find.
     */
    public void searchForSpecificTekdaqcs(final OnTargetTekdaqcFound listener, final long timeoutDuration, final String... serials){
        searchForSpecificTekdaqcs(listener, timeoutDuration, false, serials);
    }

    /**
     * Convenience method to search for specific {@link ATekdaqc}s on the network. Note: this method will start the {@link Locator}'s
     * default method ({@link Locator#searchForTekdaqcs()}), so other classes may also be notified of discovered {@link ATekdaqc}s.
     * Contains the option to automatically connect to the {@link ATekdaqc} so that the boards returned will not be taken by other
     * listeners and will not need the {@link ATekdaqc#connect(ATekdaqc.CONNECTION_METHOD)} method called on them.
     *
     * @param listener The {@link OnTargetTekdaqcFound} listener to be notified.
     * @param timeoutDuration The maximum time to run before returning {@link OnTargetTekdaqcFound#onFailure(String, OnTargetTekdaqcFound.FailureFlag)}
     * @param autoConnect If the {@link Locator} should automatically connect to the {@link ATekdaqc} for you.
     * @param serials Variable arguments of the serial numbers of the {@link ATekdaqc} to find.
     */
    public void searchForSpecificTekdaqcs(final OnTargetTekdaqcFound listener, final long timeoutDuration, final boolean autoConnect, final String... serials){

        Map<String,ATekdaqc> previouslyLocated = getActiveTekdaqcMap();

        ArrayList<String> serialList = new ArrayList<String>(Arrays.asList(serials));

        for(String serial: serialList){
            if(previouslyLocated.containsKey(serial)){
                listener.onSuccess(previouslyLocated.get(serial));
                serialList.remove(serial);
            }
        }

        Timer searchTimer = new Timer();

        searchTimer.schedule(new AwaitSpecifcTekdaqcTask(serialList,listener,autoConnect),timeoutDuration);

    }

    /**
     * Internal class to handle waiting for the location of specific {@link ATekdaqc}s by the method
     * {@link Locator#searchForSpecificTekdaqcs(OnTargetTekdaqcFound, long, boolean, String...)}
     */
    private class AwaitSpecifcTekdaqcTask extends TimerTask implements OnTekdaqcDiscovered{

        /**
         * The {@link List} of the serial numbers to find.
         */
        private List<String> mSerialList;

        /**
         * The list of {@link ATekdaqc}s which have been found.
         */
        private List<ATekdaqc> mTekdaqcList = new ArrayList<ATekdaqc>();

        /**
         * The {@link OnTargetTekdaqcFound} listener to be notified.
         */
        private OnTargetTekdaqcFound mListener;

        /**
         * If {@link ATekdaqc} should be automatically connected to.
         */
        private boolean mAutoConnect;

        /**
         * Constructor for the {@link AwaitSpecifcTekdaqcTask}.
         *
         * @param serialList The {@link List} of serial numbers.
         * @param listener The {@link OnTargetTekdaqcFound} listener to be notified.
         * @param autoConnect If the program should automatically connect.
         */
        private AwaitSpecifcTekdaqcTask(final List<String> serialList, final OnTargetTekdaqcFound listener, boolean autoConnect){

            mSerialList = serialList;

            mListener = listener;

            mAutoConnect = autoConnect;

            Locator.getInstance().addLocatorListener(this);

            if(!Locator.getInstance().isActive()){
                Locator.getInstance().searchForTekdaqcs();
            }
        }

        @Override
        public void onTekdaqcResponse(ATekdaqc board) {

        }

        @Override
        public void onTekdaqcFirstLocated(ATekdaqc board) {

            for(String serial: mSerialList){
                if(board.getSerialNumber() == serial){

                    if(mAutoConnect){
                        try {
                            board.connect(ATekdaqc.CONNECTION_METHOD.ETHERNET);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    mListener.onSuccess(board);

                    mSerialList.remove(serial);

                    mTekdaqcList.add(board);

                    if(mSerialList.size() == 0){
                        mListener.onAllTargetaFound(new UnmodifiableListSet<>(mTekdaqcList));
                    }

                }
            }

        }

        @Override
        public void onTekdaqcNoLongerLocated(ATekdaqc board) {

        }

        @Override
        public void run() {

            Locator.getInstance().removeLocatorListener(this);

            for(String serial: mSerialList){
                mListener.onFailure(serial, OnTargetTekdaqcFound.FailureFlag.TEKDAQC_NOT_LOCATED);
            }

        }
    }
}
