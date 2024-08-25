package hr.ferit.antonioparadzik.viewmodel

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import hr.ferit.antonioparadzik.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeViewModel: ViewModel() {
    private val firestore = Firebase.firestore

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts


    fun fetchPosts(context: Context) {
        viewModelScope.launch {
            firestore.collection("posts")
                .get()
                .addOnSuccessListener { result ->
                    try {
                        val postList = result.map { document ->
                            Log.d("Firestore", "Document data: ${document.data}") // Add this line
                            document.toObject<Post>()
                        }
                        _posts.value = postList
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error converting document to Post", e) // Add this line
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error fetching document", exception)
                    Toast.makeText(context, "Failed to fetch posts: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun getCityNameFromLocation(context: Context, latitude: Double, longitude: Double): String? {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(context, Locale.getDefault())
            return try {
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    addresses[0].locality // You can use addresses[0].getAddressLine(0) for full address
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


}