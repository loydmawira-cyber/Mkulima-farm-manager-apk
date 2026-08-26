package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.util.CountryCodeItem
import com.example.util.DEFAULT_COUNTRY_CODES
import com.example.util.DEFAULT_KENYA_COUNTRY
import com.example.util.WORLD_COUNTRY_CODES

enum class AuthMode {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD
}

@Composable
fun getAuthFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF000000),
    unfocusedTextColor = Color(0xFF000000),
    focusedLabelColor = ForestGreenPrimary,
    unfocusedLabelColor = Color(0xFF000000),
    focusedBorderColor = ForestGreenPrimary,
    unfocusedBorderColor = Color(0xFF334155),
    cursorColor = Color(0xFF000000),
    focusedPlaceholderColor = Color(0xFF475569),
    unfocusedPlaceholderColor = Color(0xFF475569),
    focusedLeadingIconColor = ForestGreenPrimary,
    unfocusedLeadingIconColor = Color(0xFF0F172A),
    focusedTrailingIconColor = ForestGreenPrimary,
    unfocusedTrailingIconColor = Color(0xFF0F172A),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: (emailOrPhone: String, pass: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (name: String, emailOrPhone: String, pass: String, farmName: String, countryCode: String, phoneNumber: String, onError: (String) -> Unit) -> Unit,
    onForgotPassword: (emailOrPhone: String, onComplete: (String) -> Unit) -> Unit,
    onCompletePasswordReset: (emailOrPhone: String, newPass: String, onResult: (Boolean) -> Unit) -> Unit = { _, _, cb -> cb(true) },
    previewFarmId: String = "FARM-82K9"
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Sign Up

    var name by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var rawPhoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(DEFAULT_KENYA_COUNTRY) }
    var showCountryPicker by remember { mutableStateOf(false) }

    // Sign in specific phone state & toggle
    var loginMethodPhone by remember { mutableStateOf(true) } // true: Phone with country code, false: Email or Worker ID
    var loginPhoneNumber by remember { mutableStateOf("") }
    var loginSelectedCountry by remember { mutableStateOf(DEFAULT_KENYA_COUNTRY) }
    var showLoginCountryPicker by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var duplicateAccountAlertPhone by remember { mutableStateOf<String?>(null) }

    // Password reset step state
    var resetStep by remember { mutableIntStateOf(1) } // 1: enter identifier, 2: enter OTP and new pass
    var resetOtpCode by remember { mutableStateOf("") }
    var newResetPassword by remember { mutableStateOf("") }
    var confirmResetPassword by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            ForestGreenDark,
            ForestGreenPrimary,
            Color(0xFFF8F9FA)
        ),
        startY = 0f,
        endY = 1200f
    )

    if (showCountryPicker) {
        CountryCodePickerDialog(
            selectedCountry = selectedCountry,
            onSelectCountry = { country ->
                selectedCountry = country
                showCountryPicker = false
            },
            onDismiss = { showCountryPicker = false }
        )
    }

    if (showLoginCountryPicker) {
        CountryCodePickerDialog(
            selectedCountry = loginSelectedCountry,
            onSelectCountry = { country ->
                loginSelectedCountry = country
                showLoginCountryPicker = false
            },
            onDismiss = { showLoginCountryPicker = false }
        )
    }

    // Single Registration Alert Dialog
    if (duplicateAccountAlertPhone != null) {
        AlertDialog(
            onDismissRequest = { duplicateAccountAlertPhone = null },
            title = {
                Text(
                    text = "Account Already Exists",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "An account with the phone number ($duplicateAccountAlertPhone) is already registered on Mkulima Farm. Please sign in to continue or use a different phone number.",
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = duplicateAccountAlertPhone ?: ""
                        duplicateAccountAlertPhone = null
                        selectedTab = 0
                        mode = AuthMode.LOGIN
                        loginMethodPhone = true
                        loginPhoneNumber = num.removePrefix(loginSelectedCountry.dialCode).removePrefix("+")
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Go to Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicateAccountAlertPhone = null }) {
                    Text("Use Different Number", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(10.dp))

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
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (mode == AuthMode.FORGOT_PASSWORD) {
                        // ==========================================
                        // FORGOT PASSWORD / PASSWORD RESET FLOW
                        // ==========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                mode = AuthMode.LOGIN
                                resetStep = 1
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

                        if (resetStep == 1) {
                            Text(
                                text = "Enter the real recovery email registered with your account. We will send a secure password reset link to that inbox.",
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = emailOrPhone,
                                onValueChange = {
                                    emailOrPhone = it
                                    errorMessage = null
                                },
                                label = { Text("Recovery Email Address") },
                                placeholder = { Text("you@example.com") },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_pass_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (!emailOrPhone.contains("@") || !emailOrPhone.contains(".")) {
                                        errorMessage = "Enter the real recovery email registered with your account."
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
                                    Text("Send Password Reset Link", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                }
                            }
                        } else {
                            // Step 2: Enter Verification Code & New Password
                            Text(
                                text = "Enter the verification code dispatched to $emailOrPhone and set your new password.",
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = resetOtpCode,
                                onValueChange = {
                                    resetOtpCode = it
                                    errorMessage = null
                                },
                                label = { Text("6-Digit Verification Code") },
                                placeholder = { Text("e.g. 123456") },
                                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reset_otp_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newResetPassword,
                                onValueChange = {
                                    newResetPassword = it
                                    errorMessage = null
                                },
                                label = { Text("New Password") },
                                leadingIcon = { Icon(Icons.Filled.LockReset, contentDescription = null) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Toggle visibility"
                                        )
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmResetPassword,
                                onValueChange = {
                                    confirmResetPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm New Password") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_new_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (newResetPassword.length < 6) {
                                        errorMessage = "Password must be at least 6 characters."
                                        return@Button
                                    }
                                    if (newResetPassword != confirmResetPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    onCompletePasswordReset(emailOrPhone, newResetPassword) { success ->
                                        isLoading = false
                                        if (success) {
                                            successMessage = "Password successfully reset! You can now sign in."
                                            mode = AuthMode.LOGIN
                                            resetStep = 1
                                            password = newResetPassword
                                        } else {
                                            errorMessage = "Failed to update password. Please verify your contact identifier."
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_save_new_pass"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Save New Password & Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                }
                            }
                        }

                    } else {
                        // ==========================================
                        // LOGIN / SIGN UP TABS
                        // ==========================================
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

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedTab == 1) {
                            // SIGN UP (Farm Owner) Fields
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Full Name") },
                                placeholder = { Text("e.g. David Kimani") },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = farmName,
                                onValueChange = { farmName = it },
                                label = { Text("Farm Enterprise Name") },
                                placeholder = { Text("e.g. Green Pastures Farm") },
                                leadingIcon = { Icon(Icons.Filled.Storefront, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_farm_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Phone Number with Country Code Dropdown Selector (Requirement 2)
                            Text(
                                text = "Phone Number (International Code)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Country Code Selector Dropdown Button
                                Surface(
                                    modifier = Modifier
                                        .height(56.dp)
                                        .clickable { showCountryPicker = true }
                                        .testTag("country_code_selector"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${selectedCountry.flagEmoji} ${selectedCountry.dialCode}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Select Country Code",
                                            tint = Color(0xFF475569)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Phone number local input field
                                OutlinedTextField(
                                    value = rawPhoneNumber,
                                    onValueChange = {
                                        rawPhoneNumber = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text(selectedCountry.samplePlaceholder) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("signup_phone_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = getAuthFieldColors()
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Required email recovery address for every new account.
                            OutlinedTextField(
                                value = emailOrPhone,
                                onValueChange = {
                                    emailOrPhone = it
                                    errorMessage = null
                                },
                                label = { Text("Recovery Email Address") },
                                placeholder = { Text("you@example.com") },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_email_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

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

                            Spacer(modifier = Modifier.height(10.dp))

                        } else {
                            // SIGN IN Fields (Owner or Worker)
                            // Login Mode Switch: Phone with Country Code vs Email / Worker ID
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { loginMethodPhone = true },
                                    color = if (loginMethodPhone) ForestGreenPrimary else Color.Transparent
                                ) {
                                    Text(
                                        text = "Phone Number",
                                        color = if (loginMethodPhone) Color.White else Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { loginMethodPhone = false },
                                    color = if (!loginMethodPhone) ForestGreenPrimary else Color.Transparent
                                ) {
                                    Text(
                                        text = "Email / Worker ID",
                                        color = if (!loginMethodPhone) Color.White else Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (loginMethodPhone) {
                                // Country Code selector alongside phone field on Login (Requirement 2)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .height(56.dp)
                                            .clickable { showLoginCountryPicker = true }
                                            .testTag("login_country_code_selector"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${loginSelectedCountry.flagEmoji} ${loginSelectedCountry.dialCode}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.ArrowDropDown,
                                                contentDescription = "Select Country Code",
                                                tint = Color(0xFF475569)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedTextField(
                                        value = loginPhoneNumber,
                                        onValueChange = {
                                            loginPhoneNumber = it
                                            errorMessage = null
                                        },
                                        placeholder = { Text(loginSelectedCountry.samplePlaceholder) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Phone,
                                            imeAction = ImeAction.Next
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("login_phone_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = getAuthFieldColors()
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = emailOrPhone,
                                    onValueChange = {
                                        emailOrPhone = it
                                        errorMessage = null
                                    },
                                    label = { Text("Email Address or Worker ID") },
                                    placeholder = { Text("e.g. owner@mkulima.farm or WRK-1001") },
                                    leadingIcon = {
                                        Icon(
                                            if (emailOrPhone.contains("@")) Icons.Filled.Email else Icons.Filled.Person,
                                            contentDescription = null
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_email_phone_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = getAuthFieldColors(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // PASSWORD FIELD
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
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
                            colors = getAuthFieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done
                            )
                        )

                        if (selectedTab == 1) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // CONFIRM PASSWORD FIELD
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_confirm_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = getAuthFieldColors(),
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
                                        resetStep = 1
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
                                    val loginIdentifier = if (loginMethodPhone) {
                                        if (loginPhoneNumber.isBlank()) {
                                            errorMessage = "Please enter your phone number."
                                            return@Button
                                        }
                                        val cleanCode = loginSelectedCountry.dialCode
                                        val cleanNum = loginPhoneNumber.trim().removePrefix("0").replace(Regex("[^0-9]"), "")
                                        "$cleanCode$cleanNum"
                                    } else {
                                        emailOrPhone.trim()
                                    }

                                    if (loginIdentifier.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter your credentials and password."
                                        return@Button
                                    }
                                    isLoading = true
                                    onLogin(loginIdentifier, password) { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                } else {
                                    if (name.isBlank()) {
                                        errorMessage = "Please enter your name."
                                        return@Button
                                    }
                                    val finalFullPhone = if (rawPhoneNumber.isNotBlank()) {
                                        val cleanDigits = rawPhoneNumber.trim().replace(Regex("[^0-9]"), "").removePrefix("0")
                                        "${selectedCountry.dialCode}$cleanDigits"
                                    } else {
                                        ""
                                    }
                                    val recoveryEmail = emailOrPhone.trim()
                                    if (!recoveryEmail.contains("@") || !recoveryEmail.contains(".")) {
                                        errorMessage = "Enter a valid recovery email. Phone-number accounts use it to reset their password."
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
                                    onSignUp(
                                        name,
                                        recoveryEmail,
                                        password,
                                        farmName,
                                        selectedCountry.dialCode,
                                        rawPhoneNumber.trim()
                                    ) { err ->
                                        isLoading = false
                                        if (err.contains("already exists", ignoreCase = true)) {
                                            duplicateAccountAlertPhone = finalFullPhone.ifBlank { recoveryEmail }
                                        }
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
                                    fontSize = 15.sp,
                                    color = Color.White
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
                                color = Color(0xFFFEE2E2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
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
                                color = Color(0xFFDCFCE7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
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


@Composable
fun CountryCodePickerDialog(
    selectedCountry: CountryCodeItem,
    onSelectCountry: (CountryCodeItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            WORLD_COUNTRY_CODES
        } else {
            val q = searchQuery.trim().lowercase()
            val cleanCodeQuery = q.replace("+", "")
            WORLD_COUNTRY_CODES.filter {
                it.countryName.lowercase().contains(q) ||
                it.dialCode.contains(q) ||
                (cleanCodeQuery.isNotBlank() && it.dialCode.replace("+", "").contains(cleanCodeQuery))
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Country Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${filteredCountries.size} countries available",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country or dial code (e.g. 254, Kenya)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = getAuthFieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredCountries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No countries match \"$searchQuery\"",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredCountries, key = { it.countryName + it.dialCode }) { country ->
                            val isSelected = country.dialCode == selectedCountry.dialCode && country.countryName == selectedCountry.countryName
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFF0FDF4) else Color.Transparent)
                                    .clickable { onSelectCountry(country) }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = country.flagEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = country.countryName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = country.dialCode,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = ForestGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Divider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}
