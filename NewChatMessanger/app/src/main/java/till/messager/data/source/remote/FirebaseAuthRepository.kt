package till.messager.data.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import till.messager.data.model.AuthSession
import till.messager.data.repository.AuthRepository
import till.messager.data.source.local.SessionManager

class FirebaseAuthRepository(
    private val sessionManager: SessionManager,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override fun currentSession(): AuthSession? {
        val current = auth.currentUser
        return if (current != null) {
            val session = AuthSession(
                uid = current.uid,
                displayName = current.displayName ?: sessionManager.read()?.displayName.orEmpty(),
                email = current.email.orEmpty(),
                isDemo = false,
                isEmailVerified = current.isEmailVerified
            )
            sessionManager.save(session)
            session
        } else {
            sessionManager.read()?.takeIf { it.isDemo.not() }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthSession> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim().lowercase(), password).await()
        val user = result.user ?: error("No user returned")

        if (!user.isEmailVerified) {
            user.sendEmailVerification().await()
            auth.signOut()
            throw Exception("Bitte verifiziere deine E-Mail. Ein Link wurde an ${email.trim()} gesendet.")
        }

        val profileRef = firestore.collection("users").document(user.uid)
        val profile = profileRef.get().await()
        val displayName = profile.getString("displayName") ?: user.displayName ?: email.substringBefore('@')
        val session = AuthSession(user.uid, displayName, user.email.orEmpty(), false, true)
        sessionManager.save(session)

        profileRef.update(
            mapOf(
                "lastSeenMillis" to System.currentTimeMillis(),
                "isOnline" to true
            )
        ).await()
        session
    }

    override suspend fun signUp(displayName: String, email: String, password: String): Result<AuthSession> = runCatching {
        val normalizedEmail = email.trim().lowercase()
        val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
        val user = result.user ?: error("No user returned")

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
        ).await()
        user.sendEmailVerification().await()

        val now = System.currentTimeMillis()
        val isAdmin = normalizedEmail == "tillscheidegget@gmail.com"
        val payload = hashMapOf(
            "uid" to user.uid,
            "displayName" to displayName,
            "email" to normalizedEmail,
            "status" to if (isAdmin) "System-Administrator" else "Willkommen in Till Messager Pro Beta v6",
            "trustLabel" to if (isAdmin) "Administrator" else "Verifizierbarer Benutzer",
            "lastSeenMillis" to now,
            "registrationDate" to now,
            "isAdmin" to isAdmin,
            "role" to if (isAdmin) "admin" else "member",
            "canDistributePro" to isAdmin,
            "isOnline" to false,
            "isPro" to isAdmin,
            "proPlan" to if (isAdmin) "lifetime" else "free",
            "billingPreview" to mapOf(
                "monthlyChf" to 4,
                "yearlyChf" to 40,
                "lifetimeChf" to 50
            ),
            "appMeta" to mapOf(
                "channel" to "beta",
                "versionName" to "6.0-beta",
                "firebaseConnected" to true
            )
        )
        firestore.collection("users").document(user.uid).set(payload).await()

        if (isAdmin) {
            firestore.collection("users")
                .document(user.uid)
                .collection("meta")
                .document("distribution")
                .set(
                    mapOf(
                        "enabled" to true,
                        "updatedAt" to now,
                        "message" to "Admin kann Messager Pro Versionen verteilen.",
                        "allowedPlans" to listOf("monthly", "yearly", "lifetime")
                    )
                )
                .await()
        }

        val session = AuthSession(user.uid, displayName, normalizedEmail, false, false)
        sessionManager.save(session)
        auth.signOut()
        throw Exception("Account erstellt. Bitte bestätige jetzt deine E-Mail und logge dich danach erneut ein.")
    }

    override suspend fun signOut() {
        auth.currentUser?.uid?.let { uid ->
            runCatching {
                firestore.collection("users").document(uid).update(
                    mapOf(
                        "lastSeenMillis" to System.currentTimeMillis(),
                        "isOnline" to false
                    )
                ).await()
            }
        }
        auth.signOut()
        sessionManager.clear()
    }
}
