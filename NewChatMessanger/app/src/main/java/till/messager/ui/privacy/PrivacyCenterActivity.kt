package till.messager.ui.privacy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import till.messager.TillMessagerApp
import till.messager.core.secureWindow
import till.messager.databinding.ActivityPrivacyCenterBinding

class PrivacyCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyCenterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureWindow()
        binding = ActivityPrivacyCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topBar.setNavigationOnClickListener { finish() }

        val mode = (application as TillMessagerApp).container.backendMode
        binding.textBackend.text = "Backend-Modus: $mode"
        binding.textSummary.text = "V2.1 Professional Update: Echte Login-Verifizierung, lokale Room-Datenbank zur Sicherung und Firebase Cloud-Sync sind aktiv."
    }
}
