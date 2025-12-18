package com.lightningshop.jobtrack

import android.app.Application
import com.lightningshop.jobtrack.di.AppGraph

class JobTrackApp : Application() {
  override fun onCreate() {
    super.onCreate()
    AppGraph.init(this)
  }
}
