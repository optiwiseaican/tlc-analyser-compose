package com.aican.tlcanalyzer.ui.navigation

import  kotlinx.serialization.Serializable



sealed class RegisterRoute {

    @Serializable
    data object ROUTE_SPLASH_SCREEN : RegisterRoute()

    @Serializable
    data object ROUTE_GET_STARTED : RegisterRoute()

    @Serializable
    data object ROUTE_SIGN_IN : RegisterRoute()

    @Serializable
    data object ROUTE_SIGN_UP : RegisterRoute()


}

sealed class DashboardRoute {
    @Serializable
    data object ROUTE_DASHBOARD : DashboardRoute()
}