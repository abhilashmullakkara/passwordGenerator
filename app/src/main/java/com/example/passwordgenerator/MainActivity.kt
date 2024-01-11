package com.example.passwordgenerator

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.passwordgenerator.ui.theme.PasswordGeneratorTheme
import com.google.firebase.database.FirebaseDatabase

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
    for (i in 1 until 98) {
        val depo = Depo()
        depo.depoId = i
        depo.password= generateRandomPassword()
       // println("Generated Password: ${depo.password}")
        dDepo.add(depo)
    }
    return dDepo
}

class Depo(var depoId:Int=0,var password:String="")

