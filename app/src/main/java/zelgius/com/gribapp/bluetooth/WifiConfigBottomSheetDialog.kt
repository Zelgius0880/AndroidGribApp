package zelgius.com.gribapp.bluetooth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import zelgius.com.gribapp.R
import zelgius.com.gribapp.databinding.DialogWifiConfigBinding

private const val TAG = "DeviceListBottomSheetDialog"

interface ConfirmListener {
    fun onConfirm(ssid: String, psk: String)
}

class WifiConfigBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogWifiConfigBinding? = null
    val binding get() = _binding!!


    var listener: (ssid: String, psk: String) -> Unit = {_, _ ->}
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DialogWifiConfigBinding.inflate(inflater, container, false).let {
            _binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.configure.setOnClickListener {
            if(binding.ssid.editText?.text.isNullOrEmpty()) {
                binding.ssid.error = getString(R.string.field_required)
            } else {
                listener(binding.ssid.editText?.text?.toString()?: "",
                    binding.psk.editText?.text?.toString()?: "")
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}