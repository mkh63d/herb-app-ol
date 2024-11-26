package com.empty

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.empty.databinding.ActivityMainBinding
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import io.github.cdimascio.dotenv.dotenv

@Serializable
data class User(
    val id: Int,
    val firstName: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val mainBinding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val dotenv = dotenv {
        directory = "/assets"
        filename = "env"
    }

    //region onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupabaseApp()
        }
        setContentView(R.layout.activity_main)
    }
    //endregion

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.navHostFragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //region Supabase client initialization

    private val supabaseUrl = dotenv["SUPABASE_URL"]
    private val supabaseAnonKey = dotenv["SUPABASE_ANON_KEY"]

    val supabase = if (!supabaseUrl.isNullOrEmpty() && !supabaseAnonKey.isNullOrEmpty()) {
        createSupabaseClient(
            supabaseUrl,
            supabaseAnonKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    } else {
        //TODO Uncomment when database filled with data
        //throw NullPointerException("Missing environment variables!")
        null
    }

    //endregion

    //region Supabase setup

    suspend fun getUsers(): List<User> {
        return try {
            Log.d("Supabase", "Attempting to fetch users from Supabase...")

            supabase?.from("users") ?.select(columns = Columns.list("id,firstName"))
                ?.decodeList<User>()
                ?.also { users ->Log.d("Supabase", "Users fetched: ${users.size}")
                } ?: emptyList()

        } catch (e: Exception) {
            Log.e("Supabase", "Error fetching users: ${e.message}", e)
            emptyList()
        }
    }


    @Composable
    fun SupabaseApp() {
        var users by remember { mutableStateOf(listOf<User>()) } // Holds the list of users

        // Fetch users when the composable first loads
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                users = getUsers() // Fetch users from Supabase
            }
        }

        // Display the list of users
        Surface(modifier = Modifier.fillMaxSize()) {
            if (users.isEmpty()) {
                Text(text = "No Users Found", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(users) { user ->
                        Text(text = "ID: ${user.id} - Name: ${user.firstName}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SupabaseAppPreview() {
        SupabaseApp()
    }

    //endregion

    //region Permissions management
    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 34) {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            //TODO Add more permissions
        )
    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // TODO Add what happens if all permissions granted successfully
                    // here all permission granted successfully
                    //startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    //endregion


}
