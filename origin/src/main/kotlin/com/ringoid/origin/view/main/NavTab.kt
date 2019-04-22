package com.ringoid.origin.view.main

enum class NavTab {
    EXPLORE, LMM, PROFILE;  // order matters

    companion object {
        val values: Array<NavTab> = NavTab.values()

        fun get(index: Int): NavTab = values[index]
    }
}
