package com.neilb.synapcart

import android.app.Application
import com.neilb.synapcart.util.PriceAlertNotifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SynapCartApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PriceAlertNotifications.createChannel(this)
    }
}
