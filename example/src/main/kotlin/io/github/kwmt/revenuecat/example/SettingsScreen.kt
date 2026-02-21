package io.github.kwmt.revenuecat.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kwmt.revenuecat.core.PurchaseResult
import io.github.kwmt.revenuecat.core.RevenueCatManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    var userId by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("User ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Button(
            onClick = {
                scope.launch {
                    try {
                        RevenueCatManager.login(userId)
                        statusMessage = "Logged in as: $userId"
                    } catch (e: Exception) {
                        statusMessage = "Login failed: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = userId.isNotBlank(),
        ) {
            Text("Login")
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    try {
                        RevenueCatManager.logout()
                        statusMessage = "Logged out"
                    } catch (e: Exception) {
                        statusMessage = "Logout failed: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Logout")
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    when (val result = RevenueCatManager.restore()) {
                        is PurchaseResult.Success -> {
                            statusMessage = if (result.isActive) {
                                "Restore successful: Premium active"
                            } else {
                                "Restore successful: No active entitlements"
                            }
                        }
                        is PurchaseResult.Error -> {
                            statusMessage = "Restore failed: ${result.message}"
                        }
                        is PurchaseResult.Cancelled -> {
                            statusMessage = "Restore cancelled"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Restore Purchases")
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
