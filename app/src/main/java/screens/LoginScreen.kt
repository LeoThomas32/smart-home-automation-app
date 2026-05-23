package com.example.homeautomation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeautomation.model.User
import com.example.homeautomation.network.RetrofitClient
import com.example.homeautomation.ui.theme.Accent
import com.example.homeautomation.ui.theme.Surface
import com.example.homeautomation.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    // Validation check for special symbols
    val containsSpecialSymbols = remember(username) {
        username.isNotEmpty() && !username.all { it.isLetterOrDigit() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUp) "Create Account" else "Welcome Back",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isSignUp) "Sign up for a new smart home" else "Login to your smart home",
            color = TextSoft,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                error = null 
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            isError = containsSpecialSymbols,
            supportingText = {
                if (containsSpecialSymbols) {
                    Text("No special symbols allowed (only letters & numbers)", color = Color.Red)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = if (containsSpecialSymbols) Color.Red else Accent,
                unfocusedBorderColor = if (containsSpecialSymbols) Color.Red else Surface,
                focusedLabelColor = if (containsSpecialSymbols) Color.Red else Accent,
                unfocusedLabelColor = TextSoft
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Surface,
                focusedLabelColor = Accent,
                unfocusedLabelColor = TextSoft
            )
        )

        if (isSignUp) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; error = null },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Surface,
                    focusedLabelColor = Accent,
                    unfocusedLabelColor = TextSoft
                )
            )
        }

        if (error != null) {
            Text(
                text = error!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    error = "Please fill all fields"
                    return@Button
                }
                
                if (containsSpecialSymbols) {
                    error = "Username contains unsupported characters"
                    return@Button
                }

                if (isSignUp && password != confirmPassword) {
                    error = "Passwords do not match"
                    return@Button
                }
                
                isLoading = true
                scope.launch {
                    try {
                        val api = RetrofitClient.instance
                        val lowerUsername = username.lowercase().trim()
                        if (isSignUp) {
                            val resp = api.register(mapOf("username" to lowerUsername, "password" to password))
                            if (resp.isSuccessful) {
                                isSignUp = false
                                error = "Registration successful! Please login."
                                username = ""
                                password = ""
                                confirmPassword = ""
                            } else {
                                val errorMsg = resp.errorBody()?.string()
                                error = if (errorMsg?.contains("Invalid username") == true) "Only letters and numbers allowed" else "Registration failed"
                            }
                        } else {
                            val resp = api.login(mapOf("username" to lowerUsername, "password" to password))
                            if (resp.isSuccessful) {
                                onLoginSuccess(User(id = lowerUsername, name = lowerUsername.capitalize(), houses = emptyList()))
                            } else {
                                error = "Invalid username or password"
                            }
                        }
                    } catch (e: Exception) {
                        error = "Connection error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isSignUp) "Sign Up" else "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up",
            color = Accent,
            modifier = Modifier.clickable { isSignUp = !isSignUp; error = null }
        )
    }
}
