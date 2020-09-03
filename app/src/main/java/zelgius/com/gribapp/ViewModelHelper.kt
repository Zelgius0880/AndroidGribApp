package zelgius.com.gribapp

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

object ViewModelHelper {

    inline fun <reified T : AndroidViewModel> create(activity: FragmentActivity) =
        ViewModelProvider(
            activity,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
        ).get(T::class.java)


    inline fun <reified T : AndroidViewModel> create(owner: ViewModelStoreOwner, app: Application) =
        ViewModelProvider(
            owner,
            ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        ).get(T::class.java)
}