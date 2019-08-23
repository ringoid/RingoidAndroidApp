package com.ringoid.base.navigation

object NavigationRegistry {

    private val screenHistory = mutableListOf<AppScreen>()
        .apply {
            add(AppScreen.STUB)
            add(AppScreen.STUB)
        }

    fun lastScreen(): AppScreen = screenHistory.last()
    fun beforeLastScreen(): AppScreen = screenHistory[screenHistory.size - 2]

    internal fun recordCurrentScreen(screen: AppScreen) {
        if (screenHistory.last() == screen && screen.idempotent) {
            return
        }
        screenHistory.add(screen)
    }
}
