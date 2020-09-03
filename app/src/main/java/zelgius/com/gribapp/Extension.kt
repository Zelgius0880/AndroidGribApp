package zelgius.com.gribapp

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, Observer {
        work(it)
    })
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, work: (T) -> Unit) {
    observe(lifecycleOwner, object  : Observer<T> {
        override fun onChanged(t: T) {
            work(t)
            removeObserver(this)
        }
    })
}
