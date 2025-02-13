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

sealed class ImageAnalysisRoute {
    @Serializable
    data object ROUTE_IMAGE_ANALYSIS : ImageAnalysisRoute()

    @Serializable
    data object ROUTE_IMAGE_ANALYSIS_SETTINGS : ImageAnalysisRoute()

    @Serializable
    data object ROUTE_CROP_SETTINGS : ImageAnalysisRoute()


    @Serializable
    data object ROUTE_INTENSITY_PLOT : ImageAnalysisRoute()


    @Serializable
    data object ROUTE_PLOT_ON_GRAPH : ImageAnalysisRoute()


    @Serializable
    data object ROUTE_AUTO_DETECTION : ImageAnalysisRoute()

    @Serializable
    data object ROUTE_VOLUME_PLOT : ImageAnalysisRoute()

    @Serializable
    data object ROUTE_TABLE_PLOT : ImageAnalysisRoute()

    @Serializable
    data object ROUTE_REPORT : ImageAnalysisRoute()

}

sealed class SplitImageRoute {
    @Serializable
    data object ROUTE_SPLIT_IMAGE : SplitImageRoute()

    @Serializable
    data object ROUTE_MULTIPLE_IMAGE_ANALYSIS : SplitImageRoute()


    @Serializable
    data object ROUTE_MULTIPLE_INTENSITY_ANALYSIS : SplitImageRoute()

}