package app.grapheneos.pdfviewer

import android.app.Application
import app.grapheneos.pdfviewer.recent.RecentFilesRepository
import com.google.android.material.color.DynamicColors

class App : Application() {
    lateinit var recentFilesRepository: RecentFilesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        recentFilesRepository = RecentFilesRepository(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
