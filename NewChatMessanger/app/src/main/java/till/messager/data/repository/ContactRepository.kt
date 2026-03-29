package till.messager.data.repository

import till.messager.data.model.UserProfile

interface ContactRepository {
    suspend fun loadContacts(excludeUid: String): Result<List<UserProfile>>
    suspend fun addContact(ownerUid: String, contact: UserProfile): Result<Unit>
}
