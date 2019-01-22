package com.ringoid.origin.view.main

import com.ringoid.utility.ICommunicator

interface IMainActivity : ICommunicator {

    fun openChat(peerId: String)

    fun popScreen()
}
