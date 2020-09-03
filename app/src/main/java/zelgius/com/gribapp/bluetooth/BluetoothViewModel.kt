package zelgius.com.gribapp.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONException
import org.json.JSONObject
import zelgius.com.gribapp.R

private const val TAG = "BlutoothViewModel"

class BluetoothViewModel(private val app: Application) : AndroidViewModel(app) {


    /**
     * Local Bluetooth adapter
     */
    private var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val adapter: BluetoothAdapter get() = mBluetoothAdapter

    /**
     * String buffer for outgoing messages
     */

    private val _state = MutableLiveData(ServiceState.STATE_NONE)
    val state: LiveData<ServiceState> get() = _state

    private val _deviceName = MutableLiveData<String>()
    val deviceName: LiveData<String> get() = _deviceName


    val displayMessage = MutableLiveData<String?>()

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> get() = _progress

    val newMessage= MutableLiveData<Boolean>()

    private val handler = Handler()
    private var isCountdown = false
    val messages = mutableListOf<Pair<Message, String>>()

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    fun sendMessage(message: String) {
        // Check that we're actually connected before trying anything
        if (mChatService.state != ServiceState.STATE_CONNECTED) {
            displayMessage.value = app.getString(R.string.not_connected)

            return
        }

        // Check that there's actually something to send
        if (message.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray()
            mChatService.write(send)
            _progress.postValue(true)
        }
    }

    fun sendMessage(SSID: String, PWD: String) {
        //TODO change to view
        // Check that we're actually connected before trying anything
        if (mChatService.state !== ServiceState.STATE_CONNECTED) {
            displayMessage.value = app.getString(R.string.not_connected)
            return
        }

        // Check that there's actually something to send
        if (SSID.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val mJson = JSONObject()
            try {
                mJson.put("SSID", SSID)
                mJson.put("PWD", PWD)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val send = mJson.toString().toByteArray()
            mChatService.write(send)
            _progress.postValue(true)
            isCountdown = true
            handler.postDelayed(watchDogTimeOut, 5 * 60 * 1000 )
        }
    }

    private fun isJson(str: String?): Boolean {
        try {
            JSONObject(str?:"")
        } catch (ex: JSONException) {
            return false
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()

        mChatService.stopService()

        try {
            handler.removeCallbacks(watchDogTimeOut)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * Establish connection with other device
     *
     * @param macAddress   the mac address of the device.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    fun connectDevice(macAddress: String, secure: Boolean) {
        // Get the BluetoothDevice object

        val device = mBluetoothAdapter!!.getRemoteDevice(macAddress)
        _progress.value = true
        // Attempt to connect to the device
        mChatService.connect(device, secure)
    }

    // Initialize the BluetoothChatService to perform bluetooth connections
    private val mChatService = BluetoothCLIService { message, payload ->
        when (message) {
            Message.MESSAGE_STATE_CHANGE -> {
                if(payload as ServiceState == ServiceState.STATE_CONNECTED) {
                    _progress.postValue(false)
                    displayMessage.postValue(app.getString(R.string.connected))
                } else if (payload == ServiceState.STATE_NONE)
                    _progress.postValue(false)
                _state.postValue(payload)
            }

            Message.MESSAGE_WRITE -> {
                synchronized(message) {
                    messages.add(message to payload as String)
                    newMessage.postValue(true)
                }
            }
            Message.MESSAGE_READ -> {
                if (payload is String) {
                    val m = payload.substring(0, payload.length - 1)
                    Log.d(TAG, "readMessage = $m")
                    //TODO: if message is json -> callback from RPi
                    if (isJson(m)) {
                        handleCallback(m)
                    } else {
                        if (isCountdown) {
                            handler.removeCallbacks(watchDogTimeOut)
                            isCountdown = false

                            if (_progress.value == true) {
                                displayMessage.postValue(app.getString(R.string.wifi_already_configured))
                            }
                        }

                        //remove the space at the very end of the readMessage -> eliminate space between items
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                        Log.e(TAG, m)
                        synchronized(messages) {
                            messages.add(message to m)
                            newMessage.postValue(true)
                        }

                    }
                    _progress.postValue(false)
                }


            }
            Message.MESSAGE_DEVICE_NAME -> {
                // save the connected device's name
                _deviceName.postValue(payload as String)
            }
            Message.MESSAGE_TOAST -> displayMessage.postValue(payload as String)
        }
    }


    private val watchDogTimeOut = Runnable {
        isCountdown = false
        //time out

        _progress.postValue(false)
        displayMessage.postValue(app.getString(R.string.no_response_from_rpi))
    }

    private fun handleCallback(str: String) {
        val result: String
        if (isCountdown) {
            handler.removeCallbacks(watchDogTimeOut)
            isCountdown = false
        }

        //enable user interaction
        _progress.postValue(false)
        try {
            val mJSON = JSONObject(str)
            result = mJSON.getString("result")
            //Toast.makeText(getActivity(), "result: "+result+", IP: "+ip, Toast.LENGTH_LONG).show();
            if (result != "SUCCESS") {
                displayMessage.postValue(app.getString(R.string.config_fail))
            } else {
//                Toast.makeText(getActivity(), R.string.config_success,
//                            Toast.LENGTH_SHORT).show();
                displayMessage.postValue(app.getString(R.string.config_success))
            }
        } catch (e: JSONException) {
            // error handling
            displayMessage.postValue(app.getString(R.string.something_when_wrong))
        }
    }

}
