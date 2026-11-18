package app.kaaj.android

import android.app.Application
import app.kaaj.android.data.AppContainer
import org.osmdroid.config.Configuration
import java.io.File

class KaajApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // OpenStreetMap tiles: the tile servers ask every app to identify itself, and the tile
        // cache goes in the app's own cache folder (no storage permission needed).
        Configuration.getInstance().apply {
            load(this@KaajApp, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
        }
    }
}
