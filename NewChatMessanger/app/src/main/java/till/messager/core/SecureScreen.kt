package till.messager.core

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.secureWindow() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )
}
