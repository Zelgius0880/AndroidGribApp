/*
* Copyright 2017 The Android Open Source Project, Inc.
*
* Licensed to the Apache Software Foundation (ASF) under one or more contributor
* license agreements. See the NOTICE file distributed with this work for additional
* information regarding copyright ownership. The ASF licenses this file to you under
* the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at

* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software distributed under
* the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
* ANY KIND, either express or implied. See the License for the specific language
* governing permissions and limitations under the License.

*/
package zelgius.com.gribapp.bluetooth

/**
 * Created by yubo on 7/11/17.
 */
/**
 * Defines several constants used between [BluetoothCLIService] and the UI.
 */
interface Constants {
    companion object {
        // Message types sent from the BluetoothChatService Handler

        // Key names received from the BluetoothChatService Handler
        const val DEVICE_NAME = "device_name"
        const val TOAST = "toast"


    }
}


enum class Message(val value: Int) {
    MESSAGE_STATE_CHANGE(1),
    MESSAGE_READ(2),
    MESSAGE_WRITE(3),
    MESSAGE_DEVICE_NAME(4),
    MESSAGE_TOAST(5)
}

enum class ServiceState(val value: Int) {
    STATE_NONE(0), // we're doing nothing
    STATE_LISTEN(1), // now listening for incoming connections
    STATE_CONNECTING(2), // now initiating an outgoing connection
    STATE_CONNECTED(3) // now connected to a remote device
}