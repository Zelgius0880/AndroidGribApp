package zelgius.com.gribapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("zelgius.com.gribapp", appContext.packageName)

        val latch = CountDownLatch(1)
        val database = Firebase.database.reference
        database.child("gate_token").child("token").addValueEventListener(object  : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                error(p0)
            }

            override fun onDataChange(p0: DataSnapshot) {
                assertNotNull(p0.value)
                println(p0.value)

                latch.countDown()
            }

        })

        latch.await(10, TimeUnit.SECONDS)
    }
}