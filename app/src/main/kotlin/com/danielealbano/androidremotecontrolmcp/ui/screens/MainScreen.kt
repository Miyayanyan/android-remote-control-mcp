@file:Suppress("FunctionNaming", "LongMethod")

package com.danielealbano.androidremotecontrolmcp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danielealbano.androidremotecontrolmcp.R
import com.danielealbano.androidremotecontrolmcp.ui.components.UpdateAvailableBanner
import com.danielealbano.androidremotecontrolmcp.ui.glass.GlassPolicy
import com.danielealbano.androidremotecontrolmcp.ui.glass.GlassTabBar
import com.danielealbano.androidremotecontrolmcp.ui.glass.GlassTabItem
import com.danielealbano.androidremotecontrolmcp.ui.glass.StopServerShortcut
import com.danielealbano.androidremotecontrolmcp.ui.navigation.SettingsRoute
import com.danielealbano.androidremotecontrolmcp.ui.navigation.TopLevelRoute
import com.danielealbano.androidremotecontrolmcp.ui.viewmodels.MainViewModel
import com.danielealbano.androidremotecontrolmcp.ui.viewmodels.UpdateViewModel

private const val BOTTOM_BAR_SIDE_PADDING_DP = 16
private const val BOTTOM_BAR_VERTICAL_PADDING_DP = 10
private const val BOTTOM_BAR_SPACING_DP = 10
private const val TAB_BAR_MAX_WIDTH_DP = 520

@Composable
fun MainScreen(
    onRequestNotificationPermission: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
) {
    var selectedTabRoute by rememberSaveable { mutableStateOf(TopLevelRoute.Server.route) }
    var pendingSettingsRoute by rememberSaveable { mutableStateOf<String?>(null) }
    val availableUpdate by updateViewModel.availableUpdate.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Back on the Settings/About tab returns to the Server tab instead of leaving the app. The
    // settings NavHost registers its own back callback AFTER this one, so it wins while its back
    // stack is non-empty: back inside a settings sub-screen still pops to the settings index first.
    BackHandler(enabled = selectedTabRoute != TopLevelRoute.Server.route) {
        selectedTabRoute = TopLevelRoute.Server.route
    }

    Scaffold(
        topBar = {
            availableUpdate?.let { update ->
                UpdateAvailableBanner(
                    versionName = update.versionName,
                    onClick = {
                        // No-op if the device has no browser to handle ACTION_VIEW (never crash).
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.releaseUrl))) }
                    },
                )
            }
        },
        bottomBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = BOTTOM_BAR_SIDE_PADDING_DP.dp,
                            vertical = BOTTOM_BAR_VERTICAL_PADDING_DP.dp,
                        ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(BOTTOM_BAR_SPACING_DP.dp),
            ) {
                if (GlassPolicy.showStopServerShortcut(serverStatus)) {
                    StopServerShortcut(onClick = { viewModel.stopServer(context) })
                }
                GlassTabBar(
                    items =
                        listOf(
                            GlassTabItem(
                                TopLevelRoute.Server.route,
                                Icons.Default.Dns,
                                stringResource(R.string.tab_server),
                            ),
                            GlassTabItem(
                                TopLevelRoute.Settings.route,
                                Icons.Default.Settings,
                                stringResource(R.string.tab_settings),
                            ),
                            GlassTabItem(
                                TopLevelRoute.About.route,
                                Icons.Default.Info,
                                stringResource(R.string.tab_about),
                            ),
                        ),
                    selectedKey = selectedTabRoute,
                    onSelect = { selectedTabRoute = it },
                    modifier = Modifier.widthIn(max = TAB_BAR_MAX_WIDTH_DP.dp).fillMaxWidth(),
                )
            }
        },
    ) { paddingValues ->
        when (selectedTabRoute) {
            TopLevelRoute.Server.route -> {
                ServerTabScreen(
                    onNavigateToPermissions = {
                        pendingSettingsRoute = SettingsRoute.Permissions.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    onNavigateToNetworkSettings = {
                        pendingSettingsRoute = SettingsRoute.General.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    onNavigateToTunnelSettings = {
                        pendingSettingsRoute = SettingsRoute.Tunnel.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    onOpenPrivacySettings = {
                        pendingSettingsRoute = SettingsRoute.Privacy.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    modifier = Modifier.padding(paddingValues),
                    viewModel = viewModel,
                )
            }

            TopLevelRoute.Settings.route -> {
                SettingsScreen(
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onRequestCameraPermission = onRequestCameraPermission,
                    onRequestMicrophonePermission = onRequestMicrophonePermission,
                    onRequestLocationPermission = onRequestLocationPermission,
                    pendingRoute = pendingSettingsRoute,
                    onPendingRouteConsumed = { pendingSettingsRoute = null },
                    modifier = Modifier.padding(paddingValues),
                    viewModel = viewModel,
                )
            }

            TopLevelRoute.About.route -> {
                AboutScreen(modifier = Modifier.padding(paddingValues))
            }

            else -> {
                ServerTabScreen(
                    onNavigateToPermissions = {
                        pendingSettingsRoute = SettingsRoute.Permissions.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    onNavigateToNetworkSettings = {
                        pendingSettingsRoute = SettingsRoute.General.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    onNavigateToTunnelSettings = {
                        pendingSettingsRoute = SettingsRoute.Tunnel.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    onOpenPrivacySettings = {
                        pendingSettingsRoute = SettingsRoute.Privacy.route
                        selectedTabRoute = TopLevelRoute.Settings.route
                    },
                    modifier = Modifier.padding(paddingValues),
                    viewModel = viewModel,
                )
            }
        }
    }
}
