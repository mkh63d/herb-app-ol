package com.empty

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.empty.ui.theme.EmptyTheme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

// Supabase client initialization
val supabase = createSupabaseClient(
    supabaseUrl = "https://wjfuzjxiiytaaexsdngf.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndqZnV6anhpaXl0YWFleHNkbmdmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjg5OTUzODIsImV4cCI6MjA0NDU3MTM4Mn0.1eY-WWib2CX6TlEIxuGevSlq_Av7nqw8dbbnVPfvD9E"
) {
    install(Auth)
    install(Postgrest)
}

@Serializable
data class User(
    val id: Int,
    val firstName: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupabaseApp()
        }
    }
}


suspend fun getUsers(): List<User> {
    return try {
        Log.d("Supabase", "Attempting to fetch users from Supabase...")

        val users = supabase.from("users")
            .select(columns = Columns.list("id,firstName"))
            .decodeList<User>()

        Log.d("Supabase", "Users fetched: ${users.size}")

        users
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
