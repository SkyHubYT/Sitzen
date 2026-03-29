package till.messager.data.source.local

import android.content.Context
import till.messager.data.model.AuthSession

class SessionManager(context: Context) {
    private val prefs = SecurePrefs.create(context, "session_store")

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_UID, session.uid)
            .putString(KEY_NAME, session.displayName)
            .putString(KEY_EMAIL, session.email)
            .putBoolean(KEY_DEMO, session.isDemo)
            .apply()
    }

    fun read(): AuthSession? {
        val uid = prefs.getString(KEY_UID, null) ?: return null
        val displayName = prefs.getString(KEY_NAME, null).orEmpty()
        val email = prefs.getString(KEY_EMAIL, null).orEmpty()
        val isDemo = prefs.getBoolean(KEY_DEMO, false)
        return AuthSession(uid, displayName, email, isDemo)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun hasActiveSession(): Boolean = read() != null

    private companion object {
        const val KEY_UID = "uid"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_DEMO = "demo"
    }
}
