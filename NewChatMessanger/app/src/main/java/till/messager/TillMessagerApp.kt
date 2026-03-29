package till.messager

import android.app.Application
import com.google.firebase.FirebaseApp
import till.messager.core.AppContainer

class TillMessagerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val firebaseEnabled = try {
            FirebaseApp.initializeApp(this) != null
        } catch (_: Throwable) {
            false
        }
        container = AppContainer(this, firebaseEnabled)
    }
}
