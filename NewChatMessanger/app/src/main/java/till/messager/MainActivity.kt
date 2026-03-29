package till.messager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import till.messager.databinding.ActivityLauncherBinding
import till.messager.ui.home.HomeActivity
import till.messager.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        val container = (application as TillMessagerApp).container
        val next = if (container.sessionManager.hasActiveSession()) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(next)
        finish()
    }
}
