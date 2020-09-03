package zelgius.com.gribapp.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import zelgius.com.gribapp.R
import zelgius.com.gribapp.databinding.AdapterDeviceBinding
import zelgius.com.gribapp.databinding.DialogDeviceListBinding

private const val TAG = "DeviceListBottomSheetDialog"

class DeviceListBottomSheetDialog : BottomSheetDialogFragment() {

    private val btAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var _binding: DialogDeviceListBinding? = null
    val binding get() = _binding!!
    val adapter by lazy { Adapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DialogDeviceListBinding.inflate(inflater, container, false).let {
            _binding = it
            it.root
        }
    }

    var listener: (Device) -> Unit = {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.recyclerView.adapter = adapter
        // Get a set of currently paired devices
        val pairedDevices: Set<BluetoothDevice> = btAdapter.bondedDevices

        // If there are paired devices, add each one to the ArrayAdapter

        // If there are paired devices, add each one to the ArrayAdapter
        pairedDevices.forEach {
            adapter.add(Device(it, true))
        }

        adapter.listener = {
            listener(it)
            dismiss()
        }

        binding.scan.setOnClickListener {
            Dexter.withActivity(requireActivity())
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if(report?.areAllPermissionsGranted() != true) {
                            showSnacknar()
                        } else {
                            val dIntent =
                                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                            //dIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                            doDiscovery()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showSnacknar()
                    }

                    fun showSnacknar() {
                        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                            .with(binding.root, R.string.permission_denied)
                            .withOpenSettingsButton(R.string.settings)
                            .build()
                    }

                })
                .check();
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered

        // Register for broadcasts when a device is discovered
        requireActivity().registerReceiver(mReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        })


    }

    override fun onDestroy() {
        super.onDestroy()

        requireActivity().unregisterReceiver(mReceiver)
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private fun doDiscovery() {
        Log.d(TAG, "doDiscovery()")
        binding.scan.isEnabled = false
        // Indicate scanning in the title
        binding.progressBar.visibility = View.VISIBLE

        // If we're already discovering, stop it
        if (btAdapter.isDiscovering) {
            btAdapter.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        btAdapter.startDiscovery()
    }
    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device !=  null && device.bondState != BluetoothDevice.BOND_BONDED) {
                    adapter.add(Device(device, false))
                }
                binding.noDevice.visibility = View.GONE
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                binding.progressBar.visibility = View.GONE
                binding.scan.isEnabled = true
                //setTitle(R.string.select_device)
                if (adapter.isEmpty) {
                    binding.noDevice.visibility = View.GONE
                }
            }
        }
    }

    class Adapter(var listener: (Device) -> Unit = {}) : RecyclerView.Adapter<ViewHolder>() {
        private val list = mutableListOf<Device>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                AdapterDeviceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            with(holder.binding){
                if(!item.paired)
                    paired.visibility = View.GONE
                else
                    paired.visibility = View.VISIBLE

                name.text = item.device.name
                address.text = item.device.address

                root.setOnClickListener {
                    listener(item)
                }
            }

        }

        fun add(device: Device) {
            list.add(device)
            notifyItemInserted(list.size - 1)
        }

        val isEmpty get() = list.isEmpty()
    }

    class ViewHolder(val binding: AdapterDeviceBinding) : RecyclerView.ViewHolder(binding.root)
    data class Device(val device: BluetoothDevice, val paired: Boolean = false)
}