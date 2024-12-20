package com.aican.tlcanalyzer.ui.navigation

import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aican.tlcanalyzer.ui.pages.dashboard.DashboardScreen
import com.aican.tlcanalyzer.ui.pages.getstarted.GetStartedScreen
import com.aican.tlcanalyzer.ui.pages.signin.SignInScreen
import com.aican.tlcanalyzer.ui.pages.signup.SignUpScreen
import com.aican.tlcanalyzer.ui.pages.splash.SplashScreen

import kotlin.reflect.KClass
import kotlin.reflect.typeOf

//https://github.com/himanshuGaur684/Type-Safe-Compose-Navigation/blob/main/app/src/main/java/gaur/himanshu/composenavigation/MainNavigation.kt


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = RegisterRoute.ROUTE_SPLASH_SCREEN
    ) {

        composable<RegisterRoute.ROUTE_SPLASH_SCREEN> {
            SplashScreen(onLogIn = {
                navController.navigate(DashboardRoute.ROUTE_DASHBOARD) {
                    popUpTo(RegisterRoute.ROUTE_SPLASH_SCREEN) { inclusive = true }
                }
            }) {
                navController.navigate(RegisterRoute.ROUTE_SIGN_IN) {
                    popUpTo(RegisterRoute.ROUTE_SPLASH_SCREEN) { inclusive = true }
                }
            }
        }

        composable<RegisterRoute.ROUTE_GET_STARTED> {
            GetStartedScreen() {
                navController.navigate(RegisterRoute.ROUTE_SIGN_IN)
            }
        }

        composable<RegisterRoute.ROUTE_SIGN_IN> {
            SignInScreen(onSignInSuccess = {
                navController.navigate(DashboardRoute.ROUTE_DASHBOARD)

            }) {
                navController.navigate(RegisterRoute.ROUTE_SIGN_UP)

            }

        }

        composable<RegisterRoute.ROUTE_SIGN_UP> {
            SignUpScreen(onSignUpSuccess = {
                navController.navigate(DashboardRoute.ROUTE_DASHBOARD)
            }) {
                navController.navigate(RegisterRoute.ROUTE_SIGN_IN)
            }

        }

        composable<DashboardRoute.ROUTE_DASHBOARD> {
            DashboardScreen()
        }

    }

}