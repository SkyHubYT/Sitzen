package till.messager.data.source.local

import kotlinx.coroutines.delay
import till.messager.data.model.UserProfile
import till.messager.data.repository.ContactRepository

class DemoContactRepository : ContactRepository {
    override suspend fun loadContacts(excludeUid: String): Result<List<UserProfile>> {
        delay(200)
        return Result.success(
            listOf(
                UserProfile("alice", "Alice Secure", "alice@demo.local", "Signal-grade mindset", "Verified", isAddedContact = true, proPlan = "monthly"),
                UserProfile("milo", "Milo Privacy", "milo@demo.local", "No ads. No trackers.", "Trusted", isAddedContact = false, proPlan = "yearly"),
                UserProfile("nora", "Nora Cipher", "nora@demo.local", "Metadata minimized", "Verified", isAddedContact = true, proPlan = "lifetime")
            )
        )
    }

    override suspend fun addContact(ownerUid: String, contact: UserProfile): Result<Unit> {
        delay(150)
        return Result.success(Unit)
    }
}
