package net.avantica.whereisthetruck

import android.content.*
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.nav_header_main.view.*
import net.avantica.whereisthetruck.databinding.NavHeaderMainBinding
import net.avantica.whereisthetruck.utilities.IMAGE_UPDATED_BROADCAST


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            if (intent?.action == IMAGE_UPDATED_BROADCAST) {
                val auth: FirebaseAuth = FirebaseAuth.getInstance()
                val user = auth.currentUser
                loadUserImage(user?.uid ?: "")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_configuration, R.id.nav_profile), drawerLayout)

        // Bind the header
        val header: View = navView.getHeaderView(0)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val binding: NavHeaderMainBinding? = DataBindingUtil.bind<NavHeaderMainBinding>(header)
        binding?.user = user
        loadUserImage(user?.uid ?: "")

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadCastReceiver, IntentFilter(IMAGE_UPDATED_BROADCAST))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadCastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_settings) {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm_Message))
                .setPositiveButton(getString(R.string.yes),
                    DialogInterface.OnClickListener { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    })
                .setNegativeButton(getString(R.string.cancel),null)
            builder.create()
                .show()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadUserImage(userId: String) {
        val ONE_MEGABYTE: Long = 1024 * 1024
        val imageReference: StorageReference? = FirebaseStorage.getInstance().reference.child("images").child(userId)
        imageReference?.getBytes(ONE_MEGABYTE)?.addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            val navView: NavigationView = findViewById(R.id.nav_view)
            val header: View = navView.getHeaderView(0)
            header.profileImageView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }
    }
}
