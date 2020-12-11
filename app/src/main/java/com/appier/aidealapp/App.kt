@file:Suppress("unused")

package com.appier.aidealapp

import android.app.Application
import com.appier.aidealsdk.AiDeal

class App : Application() {
    companion object {
        private const val APIKEY = "aideal-mobile-qa"
    }

    private val aiDeal = AiDeal.getInstance()

    override fun onCreate() {
        super.onCreate()
        aiDeal.configure(this, APIKEY)
    }
}
