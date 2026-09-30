package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.theme.StatusOnline

@Composable
fun LoginScreen(
    state: MainUiState,
    onLogin: (email: String, pass: String, apiUrl: String?) -> Unit,
    onRegister: (name: String, email: String, pass: String, passConfirm: String, apiUrl: String?) -> Unit,
    onConnectQuickTracker: (
        studentName: String,
        studentId: String,
        schoolName: String,
        grade: String,
        className: String,
        parentPhone: String,
        deviceName: String,
        apiUrl: String?
    ) -> Unit
) {
    // 0: Kết nối nhanh, 1: Đăng nhập, 2: Đăng ký
    var authMode by remember { mutableIntStateOf(0) }

    // Quick tracker fields
    var studentName by remember { mutableStateOf(state.studentName.ifEmpty { "Nguyễn Minh Quân" }) }
    var studentId by remember { mutableStateOf(state.studentId.ifEmpty { "HS1024" }) }
    var schoolName by remember { mutableStateOf(state.schoolName.ifEmpty { "THPT Chuyên Lê Hồng Phong" }) }
    var grade by remember { mutableStateOf(state.grade.ifEmpty { "Khối 10" }) }
    var className by remember { mutableStateOf(state.className.ifEmpty { "10A1" }) }
    var parentPhone by remember { mutableStateOf(state.parentPhone.ifEmpty { "0901234567" }) }
    var deviceName by remember { mutableStateOf(state.deviceName.ifEmpty { "Điện thoại Android" }) }

    // Account auth fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(state.userEmail.ifEmpty { "dungdaumoi223@gmail.com" }) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showServerConfig by remember { mutableStateOf(false) }
    var apiUrl by remember(state.apiBaseUrl) { mutableStateOf(state.apiBaseUrl) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Prominent Web Connection Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StatusOnline)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ĐỒNG BỘ MÁY CHỦ WEB",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusOnline
                        )
                        Text(
                            text = "https://qu-n-l-s1k1.onrender.com",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Hero Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Device Monitor Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Device Monitor",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = when (authMode) {
                    0 -> "Kết nối nhanh & gửi báo cáo học sinh trực tiếp lên Web Render"
                    1 -> "Đăng nhập tài khoản quản trị viên / phụ huynh"
                    else -> "Đăng ký tài khoản mới trên cơ sở dữ liệu Neon PostgreSQL"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Segmented Tabs: 0: KẾT NỐI NHANH, 1: ĐĂNG NHẬP, 2: ĐĂNG KÝ
                    TabRow(
                        selectedTabIndex = authMode,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = authMode == 0,
                            onClick = { authMode = 0 },
                            text = {
                                Text(
                                    "KẾT NỐI NHANH",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (authMode == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_quick")
                        )
                        Tab(
                            selected = authMode == 1,
                            onClick = { authMode = 1 },
                            text = {
                                Text(
                                    "ĐĂNG NHẬP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (authMode == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_login")
                        )
                        Tab(
                            selected = authMode == 2,
                            onClick = { authMode = 2 },
                            text = {
                                Text(
                                    "ĐĂNG KÝ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (authMode == 2) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_register")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // MODE 0: QUICK TRACKER
                    if (authMode == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { studentName = it },
                                label = { Text("Tên học sinh") },
                                placeholder = { Text("Nguyễn Minh Quân") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("quick_student_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = className,
                                    onValueChange = { className = it },
                                    label = { Text("Lớp") },
                                    placeholder = { Text("10A1") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("quick_class_name_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = grade,
                                    onValueChange = { grade = it },
                                    label = { Text("Khối") },
                                    placeholder = { Text("Khối 10") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("quick_grade_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = schoolName,
                                onValueChange = { schoolName = it },
                                label = { Text("Trường học") },
                                placeholder = { Text("THPT Chuyên Lê Hồng Phong") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("quick_school_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = parentPhone,
                                onValueChange = { parentPhone = it },
                                label = { Text("SĐT phụ huynh") },
                                placeholder = { Text("0901234567") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("quick_parent_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = deviceName,
                                onValueChange = { deviceName = it },
                                label = { Text("Tên thiết bị hiển thị") },
                                placeholder = { Text("Điện thoại Android") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("quick_device_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // MODE 1 & 2: LOGIN OR REGISTER
                    if (authMode == 1 || authMode == 2) {
                        Column {
                            // Registration Name Field
                            AnimatedVisibility(visible = authMode == 2) {
                                Column {
                                    OutlinedTextField(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        label = { Text("Họ và tên / Tên hiển thị") },
                                        placeholder = { Text("Nguyễn Văn A") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Tên") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Next
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("name_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            // Email Field
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email tài khoản") },
                                placeholder = { Text("nhap_email@domain.com") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("email_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Field
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Mật khẩu") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (authMode == 2) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (authMode == 1) {
                                        focusManager.clearFocus()
                                        onLogin(email, password, if (showServerConfig) apiUrl else null)
                                    }
                                }),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("password_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Confirm Password Field (for Registration)
                            AnimatedVisibility(visible = authMode == 2) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = { confirmPassword = it },
                                        label = { Text("Xác nhận lại mật khẩu") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirm Password") },
                                        trailingIcon = {
                                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Toggle confirm password visibility"
                                                )
                                            }
                                        },
                                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            focusManager.clearFocus()
                                            onRegister(fullName, email, password, confirmPassword, if (showServerConfig) apiUrl else null)
                                        }),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("confirm_password_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error & Success Message Banners
                    if (!state.errorMessage.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (!state.successMessage.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = state.successMessage,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Main Action Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            when (authMode) {
                                0 -> onConnectQuickTracker(
                                    studentName,
                                    studentId,
                                    schoolName,
                                    grade,
                                    className,
                                    parentPhone,
                                    deviceName,
                                    if (showServerConfig) apiUrl else null
                                )
                                1 -> onLogin(email, password, if (showServerConfig) apiUrl else null)
                                2 -> onRegister(fullName, email, password, confirmPassword, if (showServerConfig) apiUrl else null)
                            }
                        },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag(
                                when (authMode) {
                                    0 -> "quick_connect_button"
                                    1 -> "login_button"
                                    else -> "register_button"
                                }
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Đang đồng bộ với Render...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Icon(
                                imageVector = when (authMode) {
                                    0 -> Icons.Default.Bolt
                                    1 -> Icons.Default.CheckCircle
                                    else -> Icons.Default.HowToReg
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (authMode) {
                                    0 -> "⚡ Bắt đầu đồng bộ về Web"
                                    1 -> "Đăng nhập tài khoản"
                                    else -> "Đăng ký tài khoản"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Server URL config
            TextButton(onClick = { showServerConfig = !showServerConfig }) {
                Icon(
                    imageVector = if (showServerConfig) Icons.Default.Settings else Icons.Default.Dns,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showServerConfig) "Ẩn cấu hình máy chủ" else "Cấu hình địa chỉ máy chủ (Mặc định: Render)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(visible = showServerConfig) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ĐỊA CHỈ BACKEND (API_BASE_URL)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = apiUrl,
                            onValueChange = { apiUrl = it },
                            placeholder = { Text("https://qu-n-l-s1k1.onrender.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("api_url_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hệ thống kết nối trực tiếp với https://qu-n-l-s1k1.onrender.com và lưu trữ trên cơ sở dữ liệu Neon PostgreSQL.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
