package app.kaaj.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import app.kaaj.android.ui.KaajRoot
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.theme.KaajTheme

/**
 * The only Activity. It is an AppCompatActivity so the in-app language switch (বাংলা / English)
 * works on every Android version; everything on screen is Jetpack Compose.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as KaajApp).container
        setContent {
            KaajTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    KaajRoot()
                }
            }
        }
    }
}
