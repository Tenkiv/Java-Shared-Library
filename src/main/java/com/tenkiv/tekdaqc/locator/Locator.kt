//Need to suppress these warnings because Kotlin hasn't implemented Map.computeIfAbsent() or Map.putIfAbsent().
package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.Tekdaqc_RevD
import com.tenkiv.tekdaqc.utility.reprepare
import java.io.IOException
import java.net.*
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.LinkedHashSet
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

/**
 * Created by tenkiv on 2/7/17.
 */
class Locator private constructor(params: LocatorParams) {

    private object SINGLETON_INSTANCE {
        val INSTANCE = Locator(LocatorParams())
    }

    companion object {
        val instance: Locator by lazy { SINGLETON_INSTANCE.INSTANCE }
    }

    /**
     * Lock ensuring thread safety of [Locator.activeTekdaqcMap]
     */
    private val tekdaqcMapLock = ReentrantReadWriteLock()

    /**
     * Lock ensuring thread safety of binding to the locator's socket
     */
    private val socketLock = ReentrantLock()

    /**
     * List of all active tekdaqcs
     */
    private val activeTekdaqcMap = HashMap<String, ATekdaqc>()

    /**
     * Flag to enable/disable debug logging
     */
    private var DEBUG = false

    /**
     * The parameter set to use for the locator request
     */
    private var params: LocatorParams = LocatorParams.Builder().build()

    /**
     * Lock ensuring thread safety of [Locator.mTempMapLock]
     */
    private val tempMapLock = ReentrantReadWriteLock()

    /**
     * The list of tekdaqcs currently discovered.
     */
    private val tempTekdaqcMap = HashMap<String, ATekdaqc>()

    /**
     * The listeners associated with the locator.
     */
    private val listeners = Collections.synchronizedList(ArrayList<OnTekdaqcDiscovered>())

    /**
     * The default delay on the [Locator] running.
     */
    private val DEFAULT_LOCATOR_DELAY: Long = 0

    /**
     * The default period of running the [Locator].
     */
    private val DEFAULT_LOCATOR_PERIOD: Long = 500

    /**
     * Boolean determining if the [Locator] is running.
     */
    private var isActive = false

    /**
     * Time remaining on the locator timer, if it is running.
     */
    private var timeRemaining: Long = -1

    /**
     * Boolean for if the locator is on a timer.
     */
    private var isTimed = false

    /**
     * The timer run periodically to search for tekdaqcs on the local area network.
     */
    private var updateTimer = Timer("Update Timer", false)

    /**
     * The timer task run at interval, which updates the [List] of known [ATekdaqc]
     */
    private val updateTask: TimerTask
        get() = object : TimerTask() {

            override fun run() {
                updateKnownTekdaqcs()
                socketLock.withLock {
                    val interfaces = NetworkInterface.getNetworkInterfaces()
                    while (interfaces.hasMoreElements()) {
                        val iAddrs = interfaces.nextElement().interfaceAddresses
                        iAddrs.forEach { addr ->
                            if (addr.broadcast != null) {
                                locate(addr.broadcast)
                            }
                        }
                    }
                }

                if (isTimed) {
                    timeRemaining -= DEFAULT_LOCATOR_PERIOD

                    if (timeRemaining < 1) {
                        isTimed = false
                        cancelLocator()
                    }
                }
            }
        }

    /**
     * Sets new params for the [Locator].
     * Setting new params cancels execution of the current Locator if it is running, requiring it to be restarted.


     * @param params The [LocatorParams] to be set for the [Locator].
     */
    fun setLocatorParams(params: LocatorParams) {
        updateTimer = updateTimer.reprepare()

        this.params = params

    }

    /**
     * Creates an unsafe instance of the [Locator] class. If not properly configured, this locator will not
     * function while another is active. Use [LocatorParams] to set a different port to search on, however this
     * will only work if the Tekdaqc is programmed to respond to the new port.

     * @param params The [LocatorParams] to be set for the [Locator].
     * *
     * @return An unsafe [Locator] instance.
     */
    fun createUnsafeLocator(params: LocatorParams): Locator {
        return Locator(params)
    }

    /**
     * Method to add a [OnTekdaqcDiscovered] listener to be notified about [Locator] discoveries.

     * @param listener The [OnTekdaqcDiscovered] locator to be added.
     */
    fun addLocatorListener(listener: OnTekdaqcDiscovered) {
        listeners.add(listener)
    }

    /**
     * Method to remove a [OnTekdaqcDiscovered] listener from the [Locator] callbacks.

     * @param listener The [OnTekdaqcDiscovered] listener to be removed.
     */
    fun removeLocatorListener(listener: OnTekdaqcDiscovered) {
        listeners.remove(listener)
    }

    /**
     * Get the status of debug logging.

     * @return boolean True if debug logging is enabled.
     */
    fun getDebug(): Boolean {
        return DEBUG
    }

    /**
     * Set status of debug logging.

     * @param debug boolean The state to set debugging to.
     */
    fun setDebug(debug: Boolean) {
        DEBUG = debug
    }

    /**
     * Factory method for producing a [ATekdaqc] of the correct subclass
     * based on the response from the locator service.

     * @param response [LocatorResponse] The response sent by the Tekdaqc.
     * *
     * @param isSafeCreation If the [ATekdaqc] was found through reliable location instead of an automatically
     * *                       assumed preexisting IP address and MAC values.
     * *
     * *
     * @return [ATekdaqc] The constructed Tekdaqc.
     */
    private fun createTekdaqc(response: LocatorResponse, isSafeCreation: Boolean): ATekdaqc {
        // This is here to allow for future backwards compatibility with
        // different board versions
        val tekdaqc: ATekdaqc
        when (response.type) {
            'D', 'E' -> {
                tekdaqc = Tekdaqc_RevD(response)

                if (isSafeCreation) {
                    addTekdaqcToMap(tekdaqc)
                }
            }
            else -> throw IllegalArgumentException("Unknown Tekdaqc Revision: " + response.type.toChar())
        }
        return tekdaqc
    }

    /**
     * Method to connect to a discovered Tekdaqc of target serial number. Returns a CONNECTED [ATekdaqc].

     * @param serialNumber A [String] of the target [ATekdaqc]'s serial number.
     * *
     * @param defaultScale The current [ATekdaqc.AnalogScale] the board is set to.
     * *                     This should match the physical jumpers on the board.
     * *
     * @throws IOException
     * *
     * @return A [ATekdaqc] with an open connection.
     */
    fun connectToTargetTekdaqc(serialNumber: String, defaultScale: ATekdaqc.AnalogScale): ATekdaqc? {

        val map = getActiveTekdaqcMap()

        if (map.containsKey(serialNumber)) {
            val tekdaqc = map[serialNumber]
            tekdaqc?.connect(defaultScale, ATekdaqc.CONNECTION_METHOD.ETHERNET)

            return tekdaqc

        } else {
            throw IOException("No Tekdaqc Found with serial number " + serialNumber)
        }
    }

    /**
     * Method to create a [ATekdaqc] from an assumed pre-known serial number, IP address, and board revision.
     * Because this does not guarantee that the [ATekdaqc] actually exists, the [ATekdaqc] created in this
     * manner will not be added to the global [Locator.getActiveTekdaqcMap].

     * @param serialNumber The assumed serial number of the hypothetical [ATekdaqc].
     * *
     * @param hostIPAdress The assumed IP address of the hypothetical [ATekdaqc].
     * *
     * @param tekdaqcRevision The assumed revision of the hypothetical [ATekdaqc].
     * *
     * @param defaultScale The current [ATekdaqc.AnalogScale] the board is set to.
     * *                     This should match the physical jumpers on the board.
     * *
     * *
     * @throws IOException
     * @return A [ATekdaqc] object that represents an un-located, hypothetical Tekdaqc on the network.
     */
    fun connectToUnsafeTarget(serialNumber: String, hostIPAdress: String, tekdaqcRevision: Char, defaultScale: ATekdaqc.AnalogScale): ATekdaqc {

        val pseudoResponse = LocatorResponse()

        pseudoResponse.mHostIPAddress = hostIPAdress

        pseudoResponse.mType = tekdaqcRevision

        pseudoResponse.mSerial = serialNumber

        val tekdaqc = createTekdaqc(pseudoResponse, false)

        try {
            tekdaqc.connect(defaultScale, ATekdaqc.CONNECTION_METHOD.ETHERNET)
        } catch (e: IOException) {
            throw e
        }

        return tekdaqc

    }

    /**
     * Retrieves a [ATekdaqc] for the specified serial [String].

     * @param serial [String] The serial number to search for.
     * *
     * @return [ATekdaqc] The known [ATekdaqc] for
     * * `serial` or `null` if no match was found.
     */
    fun getTekdaqcForSerial(serial: String): ATekdaqc? {
        tekdaqcMapLock.read {
            return activeTekdaqcMap[serial]
        }
    }

    /**
     * Removes a [ATekdaqc] for the specified serial [String].

     * @param serial [String] The serial number to of the [ATekdaqc] to
     * *               remove.
     */
    protected fun removeTekdaqcForSerial(serial: String) {
        tekdaqcMapLock.write { activeTekdaqcMap.remove(serial) }
    }

    /**
     * Adds a [ATekdaqc] to the global map of [Locator.getActiveTekdaqcMap], This should only be done if
     * you are certain the [ATekdaqc] exists. Adding unsafe [ATekdaqc]s may cause crashes or spooky behavior.

     * @param tekdaqc The [ATekdaqc] to be added.
     */
    protected fun addTekdaqcToMap(tekdaqc: ATekdaqc) {
        tekdaqcMapLock.write {
            (activeTekdaqcMap as java.util.Map<String, ATekdaqc>).putIfAbsent(tekdaqc.serialNumber, tekdaqc)
        }
    }

    /**
     * Gets a copy of a [Map] representing all currently located [ATekdaqc].

     * @return A new [HashMap] which contains all currently located [ATekdaqc].
     */
    fun getActiveTekdaqcMap(): Map<String, ATekdaqc> {
        tekdaqcMapLock.read {
            return HashMap(activeTekdaqcMap)
        }
    }

    /**
     * Activates the search for Tekdaqcs.

     * @return True if discovery is started successfully.
     * *
     * @throws SocketException      Thrown if there is a problem with the underlying socket.
     * *
     * @throws UnknownHostException Thrown if the IP address this [Locator] is pinging is
     * *                              invalid.
     */
    @Throws(SocketException::class, UnknownHostException::class)
    private fun locate(address: InetAddress): Boolean {

        val mSocket = DatagramSocket(params.getPort())
        mSocket.broadcast = true
        mSocket.soTimeout = params.getTimeout()

        try {
            sendDiscoveryRequest(mSocket, address)
            return true
        } catch (e: IOException) {
            if (DEBUG)
                println("Unable to discover targets due to IO Exception.")

            return false
        } finally {
            if (DEBUG)
                println("Closing socket...")
            mSocket.close()
        }
    }

    /**
     * Sends out the discovery packet on the network.

     * @param socket [DatagramSocket] to send on.
     * *
     * @param addr   [InetAddress] to send the discovery packet to.
     * *
     * @throws [IOException] Thrown if there is a problem with the
     * *                underlying interface.
     */
    @Throws(IOException::class)
    private fun sendDiscoveryRequest(socket: DatagramSocket, addr: InetAddress) {
        val message = params.getMessage()
        val data = message.toByteArray()
        var buf = ByteArray(1024)

        if (DEBUG) {
            println("Data bytes length: " + data.size + " String Length: " + message.length)
            println("Sending discovery packet...")
        }

        socket.send(DatagramPacket(data, data.size, addr, params.getPort()))

        while (true) {
            try {
                val packet = DatagramPacket(buf, buf.size)
                if (DEBUG)
                    println("Waiting for return packet...")
                socket.receive(packet)

                val response = LocatorResponse(packet.address.hostAddress, packet.data)

                if (response.isValid(params)) {

                    tempMapLock.write {
                        if (isKnownTekdaqc(response.serial)) {
                            tempTekdaqcMap.put(response.serial, getTekdaqcForSerial(response.serial)!!)
                        } else {
                            val tekdaqc = createTekdaqc(response, true)
                            tempTekdaqcMap.put(tekdaqc.serialNumber, tekdaqc)

                            for (listener in listeners) {
                                listener.onTekdaqcFirstLocated(tekdaqc)
                            }
                        }
                        for (listener in listeners) {
                            listener.onTekdaqcResponse(getTekdaqcForSerial(response.serial))
                        }
                    }

                } else {
                    if (DEBUG) println("Invalid response received: ")
                }
            } catch (e: SocketTimeoutException) {
                if (DEBUG) println("Discovery timed out.")
                return
            }

            buf = ByteArray(1024)
        }
    }

    /**
     * Method to update the current list of discovered [ATekdaqc].
     */
    private fun updateKnownTekdaqcs() {
        tekdaqcMapLock.read {
            getActiveTekdaqcMap().entries.forEach { questionableBoard ->

                tempMapLock.readLock().lock()

                if (!tempTekdaqcMap.containsKey(questionableBoard.key) && !questionableBoard.value.isConnected) {
                    removeTekdaqcForSerial(questionableBoard.key)
                    listeners.forEach { listener -> listener.onTekdaqcNoLongerLocated(questionableBoard.value) }
                }
                tempMapLock.readLock().unlock()
            }
        }
        tempTekdaqcMap.clear()
    }

    /**
     * Internal method to determine if a [ATekdaqc] has been located.

     * @param serialNumber The [String] of the [ATekdaqc] serial number.
     * *
     * @return [Boolean] of if the [ATekdaqc] has been located.
     */
    private fun isKnownTekdaqc(serialNumber: String): Boolean {
        tekdaqcMapLock.read {
            return getActiveTekdaqcMap().containsKey(serialNumber)
        }
    }

    /**
     * Method which halts the locator.
     */
    fun cancelLocator() {
        isActive = false

        isTimed = false

        timeRemaining = -1

        updateTimer = updateTimer.reprepare()
    }

    /**
     * Method that returns if the [Locator] is active.

     * @return Boolean if the [Locator] is active.
     */
    fun isActive(): Boolean {
        return isActive
    }

    /**
     * Method which starts the locator at a given delay and period.

     * @param delay  The delay at which to start the locator in milliseconds as a [Long].
     * *
     * @param period The period at which to run the locator in milliseconds as a [Long].
     */
    fun searchForTekdaqcs(delay: Long, period: Long) {

        isActive = true

        updateTimer = updateTimer.reprepare()
        updateTimer.scheduleAtFixedRate(updateTask, delay, period)
    }

    /**
     * Method that activates the locator for a given duration

     * @param totalTimeMillis Total time in milliseconds.
     */
    fun searchForTekdaqcsForDuration(totalTimeMillis: Long) {
        timeRemaining = totalTimeMillis

        if (isActive()) {
            cancelLocator()
            isActive = true
        }

        searchForTekdaqcs()
    }

    /**
     * Method which starts the locator at the default delay and period.
     */
    fun searchForTekdaqcs() {
        isActive = true

        updateTimer.scheduleAtFixedRate(updateTask, DEFAULT_LOCATOR_DELAY, DEFAULT_LOCATOR_PERIOD)
    }

    /**
     * Convenience method to search for specific [ATekdaqc]s on the network. Note: this method will start the [Locator]'s
     * default method ([Locator.searchForTekdaqcs]), so other classes may also be notified of discovered [ATekdaqc]s.
     * Contains the option to automatically connect to the [ATekdaqc] so that the boards returned will not be taken by other
     * listeners and will not need the [ATekdaqc.connect] method called on them.

     * @param listener The [OnTargetTekdaqcFound] listener to be notified.
     * *
     * @param timeoutMillis The maximum time to run before returning [OnTargetTekdaqcFound.onTargetFailure]
     * *
     * @param autoConnect If the [Locator] should automatically connect to the [ATekdaqc] for you.
     * *
     * @param autoConnectDefaultScale The current [ATekdaqc.AnalogScale] the board is set to.
     * *                     This should match the physical jumpers on the board.
     * *
     * @param serials Variable arguments of the serial numbers of the [ATekdaqc] to find.
     */
    fun searchForSpecificTekdaqcs(listener: OnTargetTekdaqcFound, timeoutMillis: Long,
                                  autoConnect: Boolean = false,
                                  autoConnectDefaultScale: ATekdaqc.AnalogScale = ATekdaqc.AnalogScale.ANALOG_SCALE_5V,
                                  vararg serials: String) {

        val previouslyLocated = getActiveTekdaqcMap()

        val serialList = ArrayList(Arrays.asList(*serials))

        previouslyLocated.forEach { k, v ->
            if (serialList.contains(k)) {
                listener.onTargetFound(v)
                serialList.remove(k)
            }
        }

        val searchTimer = Timer("Specific Tekdaqc Search Timer", false)

        searchTimer.schedule(AwaitSpecificTekdaqcTask(serialList, listener, autoConnect, autoConnectDefaultScale), timeoutMillis)

    }

    /**
     * Method which runs the locator while blocking the current thread to look for specific Tekdaqcs.

     * @param timeoutMillis The maximum time to search for [ATekdaqc]s.
     * *
     * @param lock Optional lock to be used. This should be implemented where custom threading libraries or concurrency is being used.
     * *
     * @param autoConnect If [ATekdaqc]s should be automatically connected to.
     * *
     * @param autoConnectDefaultScale The optional scale of the [ATekdaqc]s that are auto-connected to.
     * *                                THIS MUST BE NON-NULL IN ORDER TO AUTOMATICALLY CONNECT.
     * *
     * @param serials The serial numbers of [ATekdaqc]s to search for.
     *
     * @throws IOException
     * *
     * @return A list of [ATekdaqc]s found during the timeout with the listed serial numbers.
     */
    fun blockingSearchForSpecificTekdaqcs(timeoutMillis: Long,
                                          lock: Lock = ReentrantLock(),
                                          autoConnect: Boolean = false,
                                          autoConnectDefaultScale: ATekdaqc.AnalogScale? = null,
                                          vararg serials: String): List<ATekdaqc> {
        val discoveredTekdaqcs = ArrayList<ATekdaqc>()
        val condition = lock.newCondition()

        val timer = Timer("Blocking Search for Specific Tekdaqcs", false)

        timer.schedule(BlockingWakeTask(lock, condition), timeoutMillis)

        addLocatorListener(object : OnTekdaqcDiscovered {
            override fun onTekdaqcResponse(board: ATekdaqc) {
                for (serial in serials) {
                    if (serial == board.serialNumber && !discoveredTekdaqcs.contains(board)) {

                        if (autoConnect && autoConnectDefaultScale != null) {

                            board.connect(autoConnectDefaultScale, ATekdaqc.CONNECTION_METHOD.ETHERNET)
                        }

                        discoveredTekdaqcs.add(board)

                        if (discoveredTekdaqcs.size == serials.size) {

                            timer.purge()
                            timer.cancel()

                            lock.withLock { condition.signalAll() }
                        }
                    }
                }
            }

            override fun onTekdaqcFirstLocated(board: ATekdaqc) {

            }

            override fun onTekdaqcNoLongerLocated(board: ATekdaqc) {

            }
        })
        searchForTekdaqcs()

        lock.withLock {
            condition.await()
        }

        timer.purge()
        timer.cancel()

        cancelLocator()

        return discoveredTekdaqcs

    }

    /**
     * Internal class used for [Locator.blockingSearchForSpecificTekdaqcs]
     * and similar methods.
     */
    private inner class BlockingWakeTask internal constructor(private val mLock: Lock, private val mCondition: Condition) : TimerTask() {

        override fun run() {
            mLock.withLock { mCondition.signalAll() }
        }
    }


    /**
     * Internal class to handle waiting for the location of specific [ATekdaqc]s
     */
    private inner class AwaitSpecificTekdaqcTask
    /**
     * Constructor for the [AwaitSpecificTekdaqcTask].

     * @param serialList The [List] of serial numbers.
     * *
     * @param listener The [OnTargetTekdaqcFound] listener to be notified.
     * *
     * @param autoConnect If the program should automatically connect.
     */
    internal constructor(
            /**
             * The [List] of the serial numbers to find.
             */
            private val mSerialList: MutableList<String>,
            /**
             * The [OnTargetTekdaqcFound] listener to be notified.
             */
            private val mListener: OnTargetTekdaqcFound,
            /**
             * If [ATekdaqc] should be automatically connected to.
             */
            private val mAutoConnect: Boolean,
            /**
             * The default [ATekdaqc.AnalogScale] for autoconnect.
             */
            private val mDefaultScale: ATekdaqc.AnalogScale) : TimerTask(), OnTekdaqcDiscovered {

        /**
         * The list of [ATekdaqc]s which have been found.
         */
        private val mTekdaqcList = ArrayList<ATekdaqc>()

        init {

            instance.addLocatorListener(this)

            if (!instance.isActive) {
                instance.searchForTekdaqcs()
            }
        }

        override fun onTekdaqcResponse(board: ATekdaqc) {

        }

        override fun onTekdaqcFirstLocated(board: ATekdaqc) {

            if (mSerialList.contains(board.serialNumber)) {
                if (mAutoConnect) {
                    board.connect(mDefaultScale, ATekdaqc.CONNECTION_METHOD.ETHERNET)
                }

                mListener.onTargetFound(board)

                mSerialList.remove(board.serialNumber)

                mTekdaqcList.add(board)

                if (mSerialList.size == 0) {
                    mListener.onAllTargetsFound(LinkedHashSet(mTekdaqcList))
                }
            }
        }

        override fun onTekdaqcNoLongerLocated(board: ATekdaqc) {

        }

        override fun run() {

            instance.removeLocatorListener(this)

            mSerialList.forEach { serial -> mListener.onTargetFailure(serial, OnTargetTekdaqcFound.FailureFlag.TEKDAQC_NOT_LOCATED) }

        }
    }


}