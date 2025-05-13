// Favorites.kt
package com.example.groupproject

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ValueEventListener
import java.util.*

class Favorites {

    private val auth   = FirebaseAuth.getInstance()
    private val db     = FirebaseDatabase.getInstance().reference

    // ensure user signed in
    private val uid: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated")

    // reference to /users/{uid}/favorites
    private val favRef: DatabaseReference
        get() = db.child("users").child(uid).child("favorites")

    /** Add or update a favorite stop */
    fun addFavorite(
        id: String,
        name: String,
        onComplete: ((DatabaseError?) -> Unit)? = null
    ) {
        val data = mapOf<String, Any>(
            "name" to name,
            "timestamp" to ServerValue.TIMESTAMP
        )
        favRef.child(id)
            .setValue(data) { error, _ ->
                onComplete?.invoke(error)
            }
    }

    /** Remove one favorite */
    fun removeFavorite(
        id: String,
        onComplete: ((Boolean, DatabaseError?) -> Unit)? = null
    ) {
        favRef.child(id)
            .removeValue { error, _ ->
                onComplete?.invoke(error == null, error)
            }
    }

    /** Delete all favorites in a single update */
    fun clearAllFavorites(onComplete: ((DatabaseError?) -> Unit)? = null) {
        // Setting the entire favorites node to null deletes it
        favRef
            .removeValue { error, _ ->
                onComplete?.invoke(error)
            }
    }

    /**
     * Listen for live updates, ordered by timestamp desc.
     * Returns the ValueEventListener so you can remove it in onPause().
     */
    fun getFavorites(
        onUpdate: (List<FavoriteItem>) -> Unit,
        onError: ((DatabaseError) -> Unit)? = null
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // collect items and sort by timestamp descending
                val items = snapshot.children.mapNotNull { child ->
                    val id   = child.key ?: return@mapNotNull null
                    val name = child.child("name").getValue(String::class.java)
                        ?: return@mapNotNull null
                    val ts   = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    FavoriteItem(id, name, ts)
                }.sortedByDescending { it.timestamp }

                onUpdate(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onError?.invoke(error)
            }
        }

        favRef.addValueEventListener(listener)
        return listener
    }

    data class FavoriteItem(
        val id: String,
        val name: String,
        val timestamp: Long
    )
}
