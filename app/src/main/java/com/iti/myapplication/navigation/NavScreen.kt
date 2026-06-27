package com.iti.myapplication.navigation

sealed class NavScreen(val route: String) {
    object UnknownScreen : NavScreen("unknown_screen")
}