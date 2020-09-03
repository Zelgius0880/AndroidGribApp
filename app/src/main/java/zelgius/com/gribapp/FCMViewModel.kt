package zelgius.com.gribapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

private const val TAG = "FCMViewModel"

class FCMViewModel(private val app: Application) : AndroidViewModel(app) {
    private val functions: FirebaseFunctions = Firebase.functions

    fun getStatus(): LiveData<Boolean> {
        val res = MutableLiveData<Boolean> ()
        val data = hashMapOf(
            "key" to BuildConfig.KEY
        )
        functions
            .getHttpsCallable("ack")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data?.let {
                    it as? Map<*, *>
                }

                res.postValue(result?.get("ack")?.let {
                    if(it is Boolean) it
                    else false
                }?:false)
                result
            }

        return res
    }

    fun sendNotification(): LiveData<Boolean> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "key" to BuildConfig.KEY
        )

        val res = MutableLiveData<Boolean> ()

        functions
            .getHttpsCallable("notify")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data?.let {
                    (it as? Map<*, *>)
                }

                res.postValue(result?.get("error")?.let {
                    if(it is Boolean) !it
                    else false
                }?:false)
                result
            }

        return res

    }

}