package hr.ferit.antonioparadzik.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.model.Post
import hr.ferit.antonioparadzik.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _posts = MutableStateFlow<List<Triple<Post, User?, String>>>(emptyList())
    val posts: StateFlow<List<Triple<Post, User?, String>>> = _posts

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun fetchPostsForCurrentUser(context: Context) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(context, "No logged-in user found.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            try {
                val postList = fetchPostsForUser(currentUserId)

                val postsWithUserData = postList.map { (docId, post) ->
                    Triple(post, _user.value, docId)
                }

                val sortedPostsWithUserData = postsWithUserData.sortedByDescending { it.first.timestamp }
                _posts.value = sortedPostsWithUserData

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching data", e)
                Toast.makeText(context, "Failed to fetch data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = fetchUserFromFirestore(userId)
                _user.value = user
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching user", e)
            }
        }
    }

    fun deletePost(postId: String, context: Context) {
        viewModelScope.launch {
            try {
                deletePostFromFirestore(postId)
                Toast.makeText(context, "Post deleted successfully.", Toast.LENGTH_SHORT).show()
                fetchPostsForCurrentUser(context)
            }
            catch (
                e: Exception
            ) {
                Log.e("ProfileViewModel", "Error deleting post", e)
            }
        }
    }

    private suspend fun fetchPostsForUser(userId: String): List<Pair<String,Post> >{
        return try {
            val result = firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                val post = document.toObject(Post::class.java)
                if (post != null) {
                    document.id to post
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error fetching posts", e)
            emptyList()
        }
    }

    private suspend fun fetchUserFromFirestore(userId: String): User? {
        return try {
            val userDocument = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            userDocument.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error fetching user", e)
            null
        }
    }

    fun uploadImageToFirebase(uri: Uri, onUploadSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/${uri.lastPathSegment}")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onUploadSuccess(downloadUri.toString())
            }.addOnFailureListener {
                Log.e("Firebase", "Failed to get download URL")
            }
        }.addOnFailureListener {
            Log.e("Firebase", "Image upload failed: ${it.message}")
        }
    }

    fun updateUserProfile(navHostController: NavHostController, context: Context,newUsername: String, newImageUrl: String, onComplete: () -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        val updates = hashMapOf<String, Any>(
            "username" to newUsername,
            "profileImageUrl" to newImageUrl
        )

        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("Firestore", "User profile updated successfully.")
                Toast.makeText(context, "User profile updated successfully.", Toast.LENGTH_SHORT).show()
                onComplete()
                navHostController.navigate(ScreenRoutes.ProfileScreen.route){
                    popUpTo(ScreenRoutes.EditProfileScreen.route){
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Failed to update user profile: ${exception.message}")
                onComplete()
            }
    }

    private suspend fun deletePostFromFirestore(postId: String) {
        try {
            firestore.collection("posts")
                .document(postId)
                .delete()
                .await()

        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error deleting post", e)
        }
    }

}
