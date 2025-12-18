package com.lightningshop.jobtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lightningshop.jobtrack.di.AppGraph
import com.lightningshop.jobtrack.ui.JobTrackRoot
import com.lightningshop.jobtrack.ui.theme.JobTrackTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val prefs = AppGraph.prefs

    setContent {
      val themeState by prefs.themeState.collectAsState(initial = prefs.defaultThemeState())
      JobTrackTheme(themeState = themeState) {
        JobTrackRoot(
          prefs = prefs,
          jobsRepo = AppGraph.jobsRepo,
          invoicesRepo = AppGraph.invoicesRepo,
          export = AppGraph.export,
        )
      }
    }
  }
}
