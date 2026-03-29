package till.messager.data.source.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import till.messager.data.model.UserProfile
import till.messager.data.repository.ContactRepository

class FirebaseContactRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ContactRepository {

    override suspend fun loadContacts(excludeUid: String): Result<List<UserProfile>> = runCatching {
        val myContacts = firestore.collection("users")
            .document(excludeUid)
            .collection("contacts")
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("uid") }
            .toSet()

        firestore.collection("users")
            .orderBy("registrationDate", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val uid = doc.getString("uid") ?: doc.id
                if (uid == excludeUid) return@mapNotNull null

                val email = doc.getString("email") ?: ""
                val isAdmin = email.lowercase() == "tillscheidegget@gmail.com"
                val storedPlan = doc.getString("proPlan") ?: if (isAdmin) "lifetime" else "free"

                UserProfile(
                    uid = uid,
                    displayName = doc.getString("displayName") ?: "Unknown",
                    email = email,
                    status = doc.getString("status") ?: "Sicher verschlüsselt",
                    trustLabel = if (isAdmin) "Administrator" else (doc.getString("trustLabel") ?: "Benutzer"),
                    lastSeenMillis = doc.getLong("lastSeenMillis") ?: 0L,
                    isAdmin = isAdmin,
                    registrationDate = doc.getLong("registrationDate") ?: 0L,
                    isAddedContact = uid in myContacts,
                    proPlan = storedPlan
                )
            }
    }

    override suspend fun addContact(ownerUid: String, contact: UserProfile): Result<Unit> = runCatching {
        val payload = hashMapOf(
            "uid" to contact.uid,
            "displayName" to contact.displayName,
            "email" to contact.email,
            "savedAt" to System.currentTimeMillis()
        )
        firestore.collection("users")
            .document(ownerUid)
            .collection("contacts")
            .document(contact.uid)
            .set(payload)
            .await()
    }
}
