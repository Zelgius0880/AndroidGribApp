package zelgius.com.gribapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import zelgius.com.gribapp.databinding.FragmentMainBinding

class MainFragment : Fragment(){


    private var _binding: FragmentMainBinding? = null
    private lateinit var fcmViewModel: FCMViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val context by lazy { requireActivity() }
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fcmViewModel = ViewModelHelper.create(context)
        binding.button.setOnClickListener {
            fcmViewModel.sendNotification().observeOnce(this) {
                Snackbar.make(binding.root, if(it) R.string.message_sent else R.string.message_not_send, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        binding.serverStatusText.setText(R.string.checking_state)
        fcmViewModel.getStatus().observeOnce(this){
            binding.serverState.setCardBackgroundColor(context.getColor(if(it) R.color.md_green_100 else R.color.md_red_100))
            binding.serverStatusText.setText(if(it) R.string.online else R.string.unreachable)
        }

        binding.serverState.setOnClickListener {
            binding.serverStatusText.setText(R.string.checking_state)
            fcmViewModel.getStatus().observeOnce(this){
                binding.serverState.setCardBackgroundColor(context.getColor(if(it) R.color.md_green_100 else R.color.md_red_100))
                binding.serverStatusText.setText(if(it) R.string.online else R.string.unreachable)
            }
        }


        binding.serverStatusText.setText(R.string.connection)

        binding.setupWifi.setOnClickListener {
            navController.navigate(R.id.action_mainFragment_to_bluetoothCLIFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}