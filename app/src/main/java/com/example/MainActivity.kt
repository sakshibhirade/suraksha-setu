package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.auth.AuthService
import com.example.data.database.AppDatabase
import com.example.data.repository.SurakshaRepository
import com.example.ui.AppTab
import com.example.ui.SurakshaViewModel
import com.example.ui.SurakshaViewModelFactory
import com.example.ui.components.SurakshaBottomNav
import com.example.ui.components.SurakshaHeader
import com.example.ui.screens.AboutModal
import com.example.ui.screens.AuthModal
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.theme.SurakshaSutraTheme
import com.example.ui.theme.SurakshaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SurakshaViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repo = SurakshaRepository(db, AuthService(db, applicationContext))
        SurakshaViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemInDark = isSystemInDarkTheme()
            val userThemePref by viewModel.userThemePreference.collectAsState()
            val isDarkMode = userThemePref ?: systemInDark

            val authState by viewModel.authState.collectAsState()
            val currentTab by viewModel.currentTab.collectAsState()
            val isAboutModalOpen by viewModel.isAboutModalOpen.collectAsState()

            var showAuthModal by remember { mutableStateOf(false) }

            SurakshaSutraTheme(darkTheme = isDarkMode) {
                val colors = SurakshaTheme.colors

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = colors.getBackgroundBrush())
                ) {
                    Scaffold(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        topBar = {
                            SurakshaHeader(
                                currentUser = authState.currentUser,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode(isDarkMode) },
                                onAuthClick = { showAuthModal = true },
                                onOpenAbout = { viewModel.openAboutModal() }
                            )
                        },
                        bottomBar = {
                            SurakshaBottomNav(
                                selectedTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                                when (tab) {
                                    AppTab.HOME -> HomeScreen(viewModel = viewModel)
                                    AppTab.REPORT -> ReportScreen(viewModel = viewModel)
                                    AppTab.REGISTER -> RegisterScreen(viewModel = viewModel)
                                    AppTab.LEARN -> LearnScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }

                    if (showAuthModal) {
                        AuthModal(
                            viewModel = viewModel,
                            onDismiss = { showAuthModal = false }
                        )
                    }

                    if (isAboutModalOpen) {
                        AboutModal(
                            onDismiss = { viewModel.closeAboutModal() }
                        )
                    }
                }
            }
        }
    }
}
