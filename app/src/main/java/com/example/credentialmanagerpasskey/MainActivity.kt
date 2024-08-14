package com.example.credentialmanagerpasskey

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.example.credentialmanagerpasskey.ui.theme.CredentialManagerPasskeyTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.security.SecureRandom

class MainActivity : ComponentActivity() {
    private val usernameString: MutableState<String> = mutableStateOf("")
    private val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CredentialManagerPasskeyTheme {
                CenteredColumn()
            }
        }
    }

    @Composable
    fun CenteredColumn() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = usernameString.value,
                    onValueChange = { usernameString.value = it },
                    label = { Text("Username") },
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(8.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onClick(this@MainActivity, usernameString.value) },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Login",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewColumn() {
        CenteredColumn()
    }

    private fun onClick(context: Context, username: String) {
        lifecycleScope.launch {

            val credentialManager = CredentialManager.create(context)

            var response = context.resources.assets.open("RegFromServer").bufferedReader().use(BufferedReader::readText);

            //Update userId, name and Display name in the mock
            response = response.replace("<userId>", getEncodedUserId()).replace("<challenge>", getEncodedChallenge())
                .replace("<userName>", username).replace("<userDisplayName>", username)

            var responseRequest = CreatePublicKeyCredentialRequest(response)

            try {
                credentialManager.createCredential(context, responseRequest) as CreatePublicKeyCredentialResponse
            } catch (e: Exception) {
                Log.d("MainActivity", "Error: $e")
            }

//        val request = Request.Builder()
//            .url("https://api.example.com/login")
//            .build()
//
//        okHttpClient.newCall(request).execute().use { response ->
//            if (!response.isSuccessful) throw Exception("Unexpected code $response")
//            Log.d("MainActivity", response.body!!.string())
//        }
            Log.d("MainActivity", "Username: $username")
        }
    }

    private fun getEncodedUserId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    private fun getEncodedChallenge(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }
}
