package com.shared.revenuecat.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shared.revenuecat.core.RevenueCatManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(onNavigateToPaywall: () -> Unit) {
    val status by RevenueCatManager.entitlementStatus.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Entitlement Status",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (status.isActive) "Premium: Active" else "Premium: Inactive",
            style = MaterialTheme.typography.titleLarge,
            color = if (status.isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Will Renew: ${status.willRenew}")

        status.expirationDateMillis?.let { millis ->
            val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(Date(millis))
            Text(text = "Expires: $formatted")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!status.isActive) {
            Button(onClick = onNavigateToPaywall) {
                Text("Show Paywall")
            }
        }
    }
}
