package com.kahomesl.allergenradar.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.kahomesl.allergenradar.AppContainer
import com.kahomesl.allergenradar.notifications.RiskAlertSettings
import com.kahomesl.allergenradar.ui.screens.*
import com.kahomesl.allergenradar.ui.viewmodel.*
import kotlinx.coroutines.launch

private enum class AppScreen(val label: String) { HOME("首页"), LOCATION("位置"), HISTORY("历史"), MY("我的"), DATA_INFO("数据说明"), PRIVACY_INFO("隐私说明"), ABOUT("关于") }

@Composable
fun AllergenRadarApp(container: AppContainer) {
    val home: HomeViewModel = viewModel(factory = viewModelFactory { HomeViewModel(container.repository, container.locationPreference) })
    val location: LocationViewModel = viewModel(factory = viewModelFactory { LocationViewModel(container.repository, container.locationPreference, container.oneShotLocationClient) })
    val history: HistoryViewModel = viewModel(factory = viewModelFactory { HistoryViewModel(container.repository, container.locationPreference) })
    val mine: MyViewModel = viewModel(factory = viewModelFactory { MyViewModel(container.repository, container.locationPreference) })
    val homeState by home.state.collectAsStateWithLifecycle()
    val locationState by location.state.collectAsStateWithLifecycle()
    val historyState by history.state.collectAsStateWithLifecycle()
    val myState by mine.state.collectAsStateWithLifecycle()
    val alerts by container.riskAlertPreference.settings.collectAsStateWithLifecycle(RiskAlertSettings())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    var focusDistricts by remember { mutableStateOf(false) }
    var notificationDenied by remember { mutableStateOf(false) }
    var locationDenied by remember { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch { container.riskAlertPreference.update(alerts.copy(enabled = true)) } else notificationDenied = true
    }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) location.findNearbySupportedLocation() else locationDenied = true
    }
    when (screen) {
        AppScreen.DATA_INFO -> { PremiumDataInfoScreen { screen = AppScreen.MY }; return }
        AppScreen.PRIVACY_INFO -> { PremiumPrivacyScreen { screen = AppScreen.MY }; return }
        AppScreen.ABOUT -> { PremiumAboutScreen { screen = AppScreen.MY }; return }
        else -> Unit
    }
    Scaffold(bottomBar = { PremiumBottomNavigation(screen) { screen = it; if (it != AppScreen.LOCATION) focusDistricts = false } }) { padding ->
        when (screen) {
            AppScreen.HOME -> PremiumHomeScreen(homeState, home::refresh, { focusDistricts = true; screen = AppScreen.LOCATION }, { screen = AppScreen.DATA_INFO }, Modifier.padding(padding))
            AppScreen.LOCATION -> PremiumLocationScreen(
                state = locationState, onRefresh = location::refresh, onSelect = location::select,
                onUseCurrentLocation = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) { locationDenied = false; location.findNearbySupportedLocation() }
                    else locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }, onConfirmNearbyLocation = location::confirmNearbyLocation, permissionDenied = locationDenied,
                focusDistricts = focusDistricts, modifier = Modifier.padding(padding),
            )
            AppScreen.HISTORY -> PremiumHistoryScreen(historyState, history::refresh, history::setTaxon, history::setMeasurement, Modifier.padding(padding))
            AppScreen.MY -> PremiumMyScreen(
                state = myState, onRefresh = mine::refresh, onOpenLocations = { focusDistricts = false; screen = AppScreen.LOCATION },
                onOpenDataInfo = { screen = AppScreen.DATA_INFO }, onOpenPrivacy = { screen = AppScreen.PRIVACY_INFO }, onOpenAbout = { screen = AppScreen.ABOUT },
                alertSettings = alerts, permissionDenied = notificationDenied,
                onOpenNotificationSettings = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) },
                onSettingsChange = { updated -> scope.launch { container.riskAlertPreference.update(updated) } },
                onAlertsEnabled = { enabled -> if (!enabled) scope.launch { container.riskAlertPreference.update(alerts.copy(enabled = false)) } else if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else scope.launch { container.riskAlertPreference.update(alerts.copy(enabled = true)) } },
                modifier = Modifier.padding(padding),
            )
            else -> Unit
        }
    }
}

@Composable private fun PremiumBottomNavigation(selected: AppScreen, onSelect: (AppScreen) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavItem(AppScreen.HOME, Icons.Default.Home, selected, onSelect)
            NavItem(AppScreen.LOCATION, Icons.Default.LocationOn, selected, onSelect)
            NavItem(AppScreen.HISTORY, Icons.Default.History, selected, onSelect)
            NavItem(AppScreen.MY, Icons.Default.AccountCircle, selected, onSelect)
        }
    }
}

@Composable private fun RowScope.NavItem(target: AppScreen, icon: ImageVector, selected: AppScreen, onSelect: (AppScreen) -> Unit) {
    val tint = if (target == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(Modifier.weight(1f).clickable { onSelect(target) }.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, target.label, tint = tint); Text(target.label, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

fun measurementLabel(type: String): String = displayMeasurementLabel(type)
