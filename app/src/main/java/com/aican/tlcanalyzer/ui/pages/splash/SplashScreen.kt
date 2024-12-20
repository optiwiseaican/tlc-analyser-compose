package com.aican.tlcanalyzer.ui.pages.splash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aican.tlcanalyzer.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onLogIn: () -> Unit,
    onNotLogIn: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()

        LaunchedEffect(Unit) {
            delay(500)
            val isLoggedIn = viewModel.isLoggedIn()
            if (isLoggedIn) {
                onLogIn.invoke()
            } else {
                onNotLogIn.invoke()
            }
        }
    }
}
