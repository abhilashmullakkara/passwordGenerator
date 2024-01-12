package com.example.passwordgenerator

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.passwordgenerator.ui.theme.PasswordGeneratorTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordGeneratorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting( modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pass by rememberSaveable {
        mutableStateOf("")
    }
    var dDepo: MutableList<Depo>
    Column {


    Text(
        text = "Greetings!",
        modifier = modifier
    )
    TextButton(onClick = {
        val dataBase2 =
            FirebaseDatabase.getInstance("https://depopassword-default-rtdb.firebaseio.com/")
        val myRef2 = dataBase2.reference
        dDepo= memberGenerate()
        myRef2.setValue(dDepo).addOnSuccessListener {
            Toast.makeText(context, "Password uploaded Successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, it.toString(), Toast.LENGTH_LONG).show()

        }
    }) {
        Text(text = "Password Generator")

    }
        pass= passwordDownloader()
        Text("Password: $pass")
//        val pass= test()
//        Text("Password from 0 th position: $pass")
}

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PasswordGeneratorTheme {
        Greeting()
    }
}


fun generateRandomPassword(): String {
    val charset = ('A'..'Z') + ('0'..'9') // Use the characters you want for the password
    val passwordLength = 5

    val randomPassword = buildString {
        val uniqueChars = mutableSetOf<Char>()

        while (uniqueChars.size < passwordLength) {
            val randomChar = charset.random()
            uniqueChars.add(randomChar)
        }

        uniqueChars.forEach { append(it) }
    }

    return randomPassword
}

fun memberGenerate(): MutableList<Depo> {
    val dDepo= mutableListOf<Depo>()
    for (i in 0 until 98) {
        val depo = Depo()
        depo.depoId = i.toString()
        depo.password= generateRandomPassword()
       // println("Generated Password: ${depo.password}")
        dDepo.add(depo)
    }
    return dDepo
}

class Depo(var depoId:String="0",var password:String="")
@Composable
fun passwordDownloader(depoNumber: String = "0"): String {
    var passwordResult by remember { mutableStateOf("nothingrecovered") }

    val dataBase = FirebaseDatabase.getInstance("https://depopassword-default-rtdb.firebaseio.com/")
    val myRef = dataBase.reference

    DisposableEffect(depoNumber) {
        val listener = myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = StringBuffer()

                snapshot.children.forEach { childSnapshot ->
                    data.append("  " + childSnapshot.child("depoId").value)
                    data.append("=    " + childSnapshot.child("password").value)
                }

                passwordResult = if (data.isNotEmpty()) data.toString() else "nothingrecovered"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })

        // Cleanup the listener when the effect is disposed
        onDispose {
            myRef.removeEventListener(listener)
        }
    }

    return passwordResult
}



//@Composable
//fun passwordDownloader(depoNumber: String = "0"): String {
//    var passwordResult by remember { mutableStateOf("") }
//    val dataBase = FirebaseDatabase.getInstance("https://depopassword-default-rtdb.firebaseio.com/")
//    val myRef = dataBase.reference
//
//    // Use a StringBuilder to efficiently append data
//    // val data = StringBuilder()
//    val data = StringBuffer()
//    myRef.get().addOnSuccessListener { dataSnapshot ->
//        if (dataSnapshot != null) {
//            dataSnapshot.children.forEach { childSnapshot ->
//               data.append("  " + childSnapshot.child("depoId").value)
//                data.append("=    " + childSnapshot.child("password").value)
//            }
//            passwordResult = data.toString()
//        }
//
//
//        // After iterating through the data, update the passwordResult
//        // Note: You may want to handle the StringBuilder differently based on your use case
//      //  passwordResult = data.toString()
//        if (passwordResult.isEmpty()) passwordResult = "nothingrecovered"
//
//        // Trigger a recomposition to reflect the updated passwordResult
//        // (This depends on where this function is used)
//
//
//        // This will return an empty string until the database operation is completed.
//
//    }
//        return passwordResult
//    }



