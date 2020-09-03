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

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent.setEventListener
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import zelgius.com.gribapp.R
import zelgius.com.gribapp.ViewModelHelper
import zelgius.com.gribapp.databinding.AdapterCommandRequestBinding
import zelgius.com.gribapp.databinding.AdapterCommandResponseBinding
import zelgius.com.gribapp.databinding.FragmentBluetoothCliBinding
import zelgius.com.gribapp.observe


/**
 * Created by yubo on 7/11/17.
 */
/**
 * This fragment controls Bluetooth to communicate with other devices.
 */

private const val TAG = "BluetoothCLIFragment"

class BluetoothCLIFragment : Fragment() {
    /**
     * Name of the connected device
     */
    private val viewModel by lazy { ViewModelHelper.create<BluetoothViewModel>(requireActivity()) }

    /**
     * Array adapter for the conversation thread
     */
    private val adapter by lazy { CommandAdapter() }

    private var _binding: FragmentBluetoothCliBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var state = ServiceState.STATE_NONE

    val parent by lazy { requireActivity() as AppCompatActivity }

    var deviceName: String? = null

    private val navController by lazy { findNavController() }

    /**
     * Member object for the chat services
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Get local Bluetooth adapter

        setEventListener(
            activity!!,
            object : KeyboardVisibilityEventListener {
                override fun onVisibilityChanged(isOpen: Boolean) {
                    adapter.notifyDataSetChanged()
                }
            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothCliBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If the adapter is null, then Bluetooth is not supported
        with(BluetoothAdapter.getDefaultAdapter()) {

            if (this == null) {
                Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show()
                activity!!.finish()
            }

            if (!this.isEnabled) {
                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
                // Otherwise, setup the chat session
            } else {
                setupChat()
            }
        }

        viewModel.state.observe(this) {
            state = it

            when (it) {
                ServiceState.STATE_CONNECTED -> {
                    adapter.clear()
                    binding.command.isEnabled = true
                    binding.send.isEnabled = true
                }
                ServiceState.STATE_CONNECTING -> setStatus(R.string.title_connecting)
                ServiceState.STATE_LISTEN, ServiceState.STATE_NONE -> {
                    setStatus(R.string.title_not_connected)
                    binding.command.isEnabled = false
                    binding.send.isEnabled = false
                }
            }
        }

        viewModel.displayMessage.observe(this) {
            if (it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.send)
                    .show()

                viewModel.displayMessage.value = null
            }
        }

        viewModel.deviceName.observe(this) {
            if (deviceName != it && state == ServiceState.STATE_CONNECTED) {
                setStatus(getString(R.string.title_connected_to, it))
                Snackbar.make(
                        binding.root,
                        parent.getString(R.string.title_connected_to, it), Snackbar.LENGTH_SHORT
                    )
                    .setAnchorView(R.id.send)
                    .show()
            }
            deviceName = it
        }

        viewModel.newMessage.observe(this) {
            if (it) {
                synchronized(viewModel.messages) {
                    viewModel.messages.forEach { m ->
                        adapter.add(m.first, m.second)
                    }

                    viewModel.messages.clear()
                    viewModel.newMessage.value = false
                    adapter.notifyDataSetChanged()
                }
            }
        }

        adapter.listener = {
            binding.command.editText?.setText(it)
        }

        viewModel.progress.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
            /*binding.command.isEnabled = !it
            binding.send.isEnabled = !it*/
        }


        binding.recyclerView.adapter = adapter
        (binding.recyclerView.layoutManager as? LinearLayoutManager)?.reverseLayout = true

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private fun setupChat() {
        Log.d(TAG, "setupChat()")

        binding.recyclerView.adapter = adapter

        binding.command.editText?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.send.performClick()
            }
            true
        }

        binding.send.setOnClickListener {
            if (binding.command.editText?.text.isNullOrEmpty())
                binding.command.error = getString(R.string.field_required)

            viewModel.sendMessage(binding.command.editText?.text.toString())
            binding.command.editText?.setText("")
        }

        //get spinner

    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private fun setStatus(resId: Int) {
        Log.d(TAG, "actionBar.setSubtitle(resId) = $resId")
        currentStatus = getString(resId)
        parent.supportActionBar?.setSubtitle(resId)
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private fun setStatus(subTitle: CharSequence) {
        Log.d(TAG, "actionBar.setSubtitle(subTitle) = $subTitle")
        currentStatus = subTitle.toString()
        parent.supportActionBar?.subtitle = subTitle


    }

    private fun showConnectDeviceDialog() {
        DeviceListBottomSheetDialog().apply {
            listener = {
                viewModel.connectDevice(it.device.address, false)
            }
        }.show(parentFragmentManager, "dialog_devices")
    }

    private fun showWifiConfigDialog() {
        WifiConfigBottomSheetDialog().apply {
            listener = { ssid, psk ->
                viewModel.sendMessage(ssid, psk)
            }
        }.show(parentFragmentManager, "dialog_wifi")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return
        when (requestCode) {

            REQUEST_ENABLE_BT -> {
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat()
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled")
                    Toast.makeText(
                        requireActivity(),
                        R.string.bt_not_enabled_leaving,
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().finish()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bluetooth_cli_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.wifi_configuration -> {
                showWifiConfigDialog()
                return true
            }
            R.id.insecure_connect_scan -> {
                showConnectDeviceDialog()
            }
            /*R.id.discoverable -> {

                // Ensure this device is discoverable by others
                ensureDiscoverable()
                return true
            }*/
        }
        return false
    }

    companion object {
        //current connection status
        var currentStatus = "not connected"

        // Intent request codes
        private const val REQUEST_ENABLE_BT = 3
    }
}

abstract class CommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract val textView: TextView
}

class RequestViewHolder(private val binding: AdapterCommandRequestBinding) :
    CommandViewHolder(binding.root) {
    override val textView: TextView
        get() = binding.command
}

class ResponseViewHolder(private val binding: AdapterCommandResponseBinding) :
    CommandViewHolder(binding.root) {
    override val textView: TextView
        get() = binding.command
}

class CommandAdapter(var listener: (cmd: String) -> Unit = {}) :
    RecyclerView.Adapter<CommandViewHolder>() {
    private val list = mutableListOf<Pair<Message, String>>()
    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun getItemViewType(position: Int): Int =
        when (list[position].first) {
            Message.MESSAGE_WRITE -> R.layout.adapter_command_request
            Message.MESSAGE_READ -> R.layout.adapter_command_response
            else -> error("Could not create a ViewHolder with this message: ${list[position].first}")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder =
        LayoutInflater.from(parent.context).let {
            when (viewType) {
                R.layout.adapter_command_request -> RequestViewHolder(
                    AdapterCommandRequestBinding.inflate(
                        it,
                        parent,
                        false
                    )
                )
                R.layout.adapter_command_response -> ResponseViewHolder(
                    AdapterCommandResponseBinding.inflate(it, parent, false)
                )
                else -> error("Could not create a ViewHolder with this viewType: $viewType")
            }
        }


    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        val item = list[position]
        holder.textView.text = item.second
        if (item.first == Message.MESSAGE_WRITE)
            holder.itemView.setOnLongClickListener {
                listener(item.second)
                true
            }
        else
            holder.itemView.setOnLongClickListener(null)
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun add(message: Message, command: String) {
        list.add(0, message to command)
    }
}