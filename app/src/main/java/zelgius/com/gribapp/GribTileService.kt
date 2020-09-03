package zelgius.com.gribapp

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.lifecycle.*

class GribTileService : TileService() {


    override fun onTileAdded() {
        super.onTileAdded()

        // Update state
        qsTile.state = Tile.STATE_INACTIVE

        // Update looks
        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        // Update state
        qsTile.state = Tile.STATE_INACTIVE

        // Update looks
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        val viewModel = FCMViewModel(application)
        // Update looks
/*        viewModel.sendNotification().observeOnce {
            Toast.makeText(
                this,
                if (it) R.string.message_sent else R.string.message_not_send,
                Toast.LENGTH_SHORT
            ).show()
        }*/
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
    }


    class OneTimeObserver<T>(private val handler: (T) -> Unit) : Observer<T>, LifecycleOwner {
        private val lifecycle = LifecycleRegistry(this)

        init {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        override fun getLifecycle(): Lifecycle = lifecycle

        override fun onChanged(t: T) {
            handler(t)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
        val observer = OneTimeObserver(handler = onChangeHandler)
        observe(observer, observer)
    }
}