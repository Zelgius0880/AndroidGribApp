package zelgius.com.gribapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import zelgius.com.gribapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       /* setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Test"*/

        val navController = findNavController(R.id.nav_host)
        setupActionBarWithNavController(navController)
        //binding.toolbar.setupWithNavController(navController, AppBarConfiguration(navController.graph))

        /*navController.addOnDestinationChangedListener {
                controller, destination, arguments ->
            supportActionBar?.title = navController.currentDestination?.label
        }*/
    }

    override fun onSupportNavigateUp(): Boolean {
        return (Navigation.findNavController(this, R.id.nav_host).navigateUp()
                || super.onSupportNavigateUp())
    }
}
