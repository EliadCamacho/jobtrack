package com.lightningshop.jobtrack

import android.app.Application
import com.lightningshop.jobtrack.di.AppGraph

class JobTrackApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    AppGraph.init(this)
  }
}
