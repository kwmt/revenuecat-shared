package io.github.kwmt.revenuecat.paywall.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.kwmt.revenuecat.core.PackageInfo
import io.github.kwmt.revenuecat.core.RevenueCatClient
import io.github.kwmt.revenuecat.paywall.PaywallState
import io.github.kwmt.revenuecat.paywall.PaywallViewModel

/**
 * Paywall のテーマ設定。アプリごとにカスタマイズ可能。
 */
data class PaywallTheme(
    val title: String = "Unlock Premium",
    val subtitle: String = "Get access to all features",
    val features: List<String> = emptyList(),
    val purchaseButtonText: String = "Subscribe",
    val restoreButtonText: String = "Restore Purchases",
)

/**
 * すぐに使えるデフォルトPaywall画面。
 * 見た目をカスタマイズしたい場合は PaywallTheme で調整するか、自前で実装する。
 */
@Composable
fun DefaultPaywall(
    client: RevenueCatClient,
    theme: PaywallTheme = PaywallTheme(),
    purchaseParams: Any,
    onDismiss: () -> Unit = {},
    onPurchaseSuccess: () -> Unit = {},
    viewModel: PaywallViewModel = remember { PaywallViewModel(client) },
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOfferings() }

    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            viewModel.clearPurchaseSuccess()
            onPurchaseSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.isPremium -> AlreadyPremiumContent(onDismiss)
            else -> PaywallContent(state, theme, viewModel::selectPackage, { viewModel.purchase(purchaseParams) }, viewModel::restore)
        }

        state.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK") } }
            ) { Text(error) }
        }
    }
}

@Composable
private fun PaywallContent(
    state: PaywallState,
    theme: PaywallTheme,
    onSelectPackage: (PackageInfo) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        Text(theme.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(theme.subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

        if (theme.features.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            theme.features.forEach { feature ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("✓ ", color = MaterialTheme.colorScheme.primary)
                    Text(feature, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        state.packages.forEach { pkg ->
            PackageCard(pkg, pkg.identifier == state.selectedPackage?.identifier) { onSelectPackage(pkg) }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onPurchase,
            enabled = !state.isPurchasing && state.selectedPackage != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (state.isPurchasing) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("${theme.purchaseButtonText} ${state.selectedPackage?.localizedPriceString.orEmpty()}", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onRestore, enabled = !state.isPurchasing) { Text(theme.restoreButtonText) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PackageCard(packageInfo: PackageInfo, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(packageInfo.identifier, style = MaterialTheme.typography.titleMedium)
            Text(packageInfo.localizedPriceString, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AlreadyPremiumContent(onDismiss: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text("You're already Premium!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onDismiss) { Text("Close") }
    }
}
