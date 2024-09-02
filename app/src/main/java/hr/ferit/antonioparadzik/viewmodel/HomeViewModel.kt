package hr.ferit.antonioparadzik.viewmodel

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import hr.ferit.antonioparadzik.model.Post
import hr.ferit.antonioparadzik.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var postsListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null

    private val _posts = MutableStateFlow<List<Pair<Post, User?>>>(emptyList())
    val posts: StateFlow<List<Pair<Post, User?>>> = _posts

    private val _filteredPosts = MutableStateFlow<List<Pair<Post, User?>>>(emptyList())
    val filteredPosts: StateFlow<List<Pair<Post, User?>>> = _filteredPosts

    private val _sortByDateNewest = MutableStateFlow<Boolean?>(null)
    val sortByDateNewest: StateFlow<Boolean?> = _sortByDateNewest

    private val _minPrice = MutableStateFlow<Float?>(null)
    val minPrice: StateFlow<Float?> = _minPrice

    private val _maxPrice = MutableStateFlow<Float?>(null)
    val maxPrice: StateFlow<Float?> = _maxPrice

    private val _selectedGender = MutableStateFlow<String?>(null)
    val selectedGender: StateFlow<String?> = _selectedGender

    private val _isDataFetched = MutableStateFlow(false)

    init {
        setupPostsListener()
        setupUsersListener()
    }

    private fun setupPostsListener() {
        postsListener = firestore.collection("posts")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val postList = mutableListOf<Post>()
                    val userIds = mutableSetOf<String>()

                    snapshot.forEach { document ->
                        val post = document.toObject(Post::class.java)
                        userIds.add(post.userId)
                        postList.add(post)
                    }

                    fetchUsers(userIds) { userMap ->
                        val postsWithUserData = postList.map { post ->
                            val user = userMap[post.userId]
                            post to user
                        }

                        val sortedPostsWithUserData = postsWithUserData.sortedByDescending { it.first.timestamp }

                        _posts.value = sortedPostsWithUserData
                        _filteredPosts.value = sortedPostsWithUserData
                        _isDataFetched.value = true
                    }
                } else {
                    Log.d("Firestore", "Current data: null")
                }
            }
    }

    private fun setupUsersListener() {
        usersListener = firestore.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val userMap = it.associateBy({ it.id }, { it.toObject(User::class.java) })

                    val updatedPosts = _posts.value.map { (post, _) ->
                        post to userMap[post.userId]
                    }

                    _posts.value = updatedPosts
                    _filteredPosts.value = updatedPosts
                }
            }
    }

    private fun fetchUsers(userIds: Set<String>, onUsersFetched: (Map<String, User?>) -> Unit) {
        if (userIds.isEmpty()) {
            Log.e("HomeViewModel", "No user IDs provided for query.")
            return
        }
        firestore.collection("users")
            .whereIn(FieldPath.documentId(), userIds.toList())
            .get()
            .addOnSuccessListener { userResult ->
                val userMap = userResult.associateBy({ it.id }, { it.toObject(User::class.java) })
                onUsersFetched(userMap)
            }
            .addOnFailureListener { userException ->
                Log.e("Firestore", "Error fetching users", userException)
            }
    }

    fun applyFilters(
        sortByDate: Boolean?,
        minPrice: Float?,
        maxPrice: Float?,
        gender: String?
    ) {
        var filteredPosts = _posts.value

        _sortByDateNewest.value = sortByDate
        _minPrice.value = minPrice
        _maxPrice.value = maxPrice
        _selectedGender.value = gender

        filteredPosts = filteredPosts.filter { post ->
            val price = post.first.price.toFloatOrNull() ?: return@filter false
            val matchesPrice = (minPrice == null || price >= minPrice) && (maxPrice == null || price <= maxPrice)
            val matchesGender = gender == null || post.first.gender == gender
            matchesPrice && matchesGender
        }

        sortByDate?.let { isNewestFirst ->
            filteredPosts = if (isNewestFirst) {
                filteredPosts.sortedByDescending { (post, _) -> post.timestamp }
            } else {
                filteredPosts.sortedBy { (post, _) -> post.timestamp }
            }
        }

        _filteredPosts.value = filteredPosts
    }

    fun getCityNameFromLocation(context: Context, latitude: Double, longitude: Double): String? {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(context, Locale.getDefault())
            return try {
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].locality
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            Log.e("Geocoder", "Geocoder not available on this device")
            return null
        }
    }

    fun fetchCityName(context: Context, latitude: Double, longitude: Double, onCityFetched: (String?) -> Unit) {
        viewModelScope.launch {
            val cityName = withContext(Dispatchers.IO) {
                getCityNameFromLocation(context, latitude, longitude)
            }
            onCityFetched(cityName)
        }
    }

    fun composeEmail(context: Context, recipient: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("ProductCard", "Error launching email client: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()

        postsListener?.remove()
        usersListener?.remove()
    }
}

