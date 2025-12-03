package com.example.passwordgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passwordgenerator.ui.theme.PasswordGeneratorTheme
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/* ------------------------------------------------------------------
   Entry point
--------------------------------------------------------------------*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordGeneratorTheme(dynamicColor = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen()
                }
            }
        }
    }
}

/* ------------------------------------------------------------------
   Data model
--------------------------------------------------------------------*/
data class Depo(
    var depoId: String = "0",
    var password: String = ""
)

/* ------------------------------------------------------------------
   Screen
--------------------------------------------------------------------*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scaffold = remember { SnackbarHostState() }
    var message by rememberSaveable { mutableStateOf("") }
    var downloaded by rememberSaveable { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    fun snack(msg: String) = scope.launch { scaffold.showSnackbar(msg) }

    val firebaseRef = remember {
        FirebaseDatabase.getInstance("https://depopassword-default-rtdb.firebaseio.com/").reference
    }

    /* --- root box so we can align the snack-bar --- */
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Password Generator",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Version / message") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ElevatedButton(
                onClick = {
                    val list = memberGenerate().apply { this[0].password = message }
                    firebaseRef.setValue(list)
                        .addOnSuccessListener { snack("Password uploaded ✔") }
                        .addOnFailureListener { snack(it.message ?: "Error") }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Upload message") }

            ElevatedButton(
                onClick = {
                    val list = memberGenerate()
                    firebaseRef.setValue(list)
                        .addOnSuccessListener { snack("Random list uploaded ✔") }
                        .addOnFailureListener { snack(it.message ?: "Error") }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generate random passwords") }

            ElevatedButton(
                onClick = {
                    val list = freezeMemberGenerate()
                    firebaseRef.setValue(list)
                        .addOnSuccessListener { snack("Frozen list uploaded ✔") }
                        .addOnFailureListener { snack(it.message ?: "Error") }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Freeze passwords (neofetch)") }

            Spacer(Modifier.height(12.dp))

            downloaded = passwordDownloader(firebaseRef)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = downloaded,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        /* --- snack-bar anchored to the bottom-center of the Box --- */
        SnackbarHost(
            hostState = scaffold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/* ------------------------------------------------------------------
   Business logic
--------------------------------------------------------------------*/
private fun generateRandomPassword(): String =
    (('A'..'Z') + ('0'..'9'))
        .shuffled()
        .take(5)
        .joinToString("")

private fun memberGenerate(): MutableList<Depo> =
    MutableList(98) { Depo(it.toString(), generateRandomPassword()) }
        .apply { this[0].password = "" }

private fun freezeMemberGenerate(): MutableList<Depo> =
    MutableList(98) { Depo(it.toString(), "neofetch") }

/* ------------------------------------------------------------------
   Real-time downloader (Composable)
--------------------------------------------------------------------*/
@Composable
private fun passwordDownloader(ref: DatabaseReference): String {
    var text by remember { mutableStateOf("Nothing downloaded yet…") }

    DisposableEffect(ref) {
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                text = s.children.joinToString("\n") {
                    "${it.child("depoId").value} = ${it.child("password").value}"
                }
            }

            override fun onCancelled(e: DatabaseError) {
                text = "Cancelled: ${e.message}"
            }
        })
        onDispose { ref.removeEventListener(listener) }
    }
    return text
}

/* ------------------------------------------------------------------
   Preview
--------------------------------------------------------------------*/
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewHome() {
    PasswordGeneratorTheme { HomeScreen() }
}