package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary

enum class AuthMode {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (emailOrPhone: String, pass: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (name: String, emailOrPhone: String, pass: String, farmName: String, onError: (String) -> Unit) -> Unit,
    onForgotPassword: (emailOrPhone: String, onComplete: (String) -> Unit) -> Unit,
    previewFarmId: String = "FARM-82K9"
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Sign Up

    var name by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            ForestGreenDark,
            ForestGreenPrimary,
            Color(0xFFF8F9FA)
        ),
        startY = 0f,
        endY = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Branding Header
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Agriculture,
                        contentDescription = "Mkulima Farm",
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Mkulima Farm Manager",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Livestock, Yields & Workforce Management",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_main_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (mode == AuthMode.FORGOT_PASSWORD) {
                        // Forgot Password Subview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                mode = AuthMode.LOGIN
                                errorMessage = null
                                successMessage = null
                            }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Login", tint = ForestGreenPrimary)
                            }
                            Text(
                                text = "Reset Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Enter your registered email address or phone number to receive verification and reset instructions.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = {
                                emailOrPhone = it
                                errorMessage = null
                            },
                            label = { Text("Email or Phone Number") },
                            leadingIcon = {
                                Icon(
                                    if (emailOrPhone.contains("@")) Icons.Filled.Email else Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_pass_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (emailOrPhone.isBlank()) {
                                    errorMessage = "Please provide your email or phone number."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                onForgotPassword(emailOrPhone) { msg ->
                                    isLoading = false
                                    successMessage = msg
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_reset_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Send Reset Instructions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                    } else {
                        // Login / Sign Up Tabs
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            indicator = { tabPositions ->
                                if (selectedTab < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = ForestGreenPrimary,
                                        height = 3.dp
                                    )
                                }
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    mode = AuthMode.LOGIN
                                    errorMessage = null
                                    successMessage = null
                                },
                                text = {
                                    Text(
                                        "Sign In",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 0) ForestGreenPrimary else Color(0xFF64748B)
                                    )
                                },
                                modifier = Modifier.testTag("tab_sign_in")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    mode = AuthMode.SIGNUP
                                    errorMessage = null
                                    successMessage = null
                                },
                                text = {
                                    Text(
                                        "Register Farm",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == 1) ForestGreenPrimary else Color(0xFF64748B)
                                    )
                                },
                                modifier = Modifier.testTag("tab_register_farm")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (selectedTab == 1) {
                            // SIGN UP (Farm Owner) Fields
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Full Name") },
                                placeholder = { Text("e.g. David Kimani") },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = ForestGreenPrimary) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = farmName,
                                onValueChange = { farmName = it },
                                label = { Text("Farm Enterprise Name") },
                                placeholder = { Text("e.g. Green Pastures Farm") },
                                leadingIcon = { Icon(Icons.Filled.Storefront, contentDescription = null, tint = ForestGreenPrimary) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_farm_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Generated Farm ID Info Badge
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Key, contentDescription = null, tint = ForestGreenDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Unique Farm ID will be auto-generated",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenDark
                                        )
                                        Text(
                                            text = "Workers you add will link to your farm via this Farm ID.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // EMAIL OR PHONE
                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = {
                                emailOrPhone = it
                                errorMessage = null
                            },
                            label = { Text("Email or Phone Number") },
                            placeholder = { Text(if (selectedTab == 0) "owner@mkulima.farm or worker login" else "e.g. owner@mkulima.farm") },
                            leadingIcon = {
                                Icon(
                                    if (emailOrPhone.contains("@")) Icons.Filled.Email else Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = ForestGreenPrimary
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_phone_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (emailOrPhone.contains("@")) KeyboardType.Email else KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // PASSWORD
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = ForestGreenPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
                            )
                        )

                        if (selectedTab == 1) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // CONFIRM PASSWORD
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = ForestGreenPrimary) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }

                        if (selectedTab == 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        mode = AuthMode.FORGOT_PASSWORD
                                        errorMessage = null
                                        successMessage = null
                                    },
                                    modifier = Modifier.testTag("btn_forgot_password")
                                ) {
                                    Text("Forgot Password?", color = ForestGreenPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                errorMessage = null
                                successMessage = null
                                if (selectedTab == 0) {
                                    if (emailOrPhone.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter both identifier and password."
                                        return@Button
                                    }
                                    isLoading = true
                                    onLogin(emailOrPhone, password) { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                } else {
                                    if (name.isBlank()) {
                                        errorMessage = "Please enter your name."
                                        return@Button
                                    }
                                    if (emailOrPhone.isBlank()) {
                                        errorMessage = "Please enter an email or phone number."
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters."
                                        return@Button
                                    }
                                    if (password != confirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }
                                    isLoading = true
                                    onSignUp(name, emailOrPhone, password, farmName) { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_auth_submit"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    if (selectedTab == 0) "Sign In" else "Create Farm Account (Owner)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Alerts
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEE2E2)
                            ) {
                                Text(
                                    text = msg,
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = successMessage != null) {
                        successMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ForestGreenDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = msg,
                                        color = ForestGreenDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
