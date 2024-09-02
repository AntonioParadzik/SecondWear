package hr.ferit.antonioparadzik.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.android.gms.location.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import com.google.firebase.messaging.messaging
import hr.ferit.antonioparadzik.service.FcmApi
import hr.ferit.antonioparadzik.service.NotificationBody
import hr.ferit.antonioparadzik.service.NotificationState
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.service.SendMessageDto
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@SuppressLint("MissingPermission")
class AddProductViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    init {
        viewModelScope.launch {
            try {
                Firebase.messaging.subscribeToTopic("posts").await()
                Log.d("FCM", "Successfully subscribed to topic 'posts'")
            } catch (e: Exception) {
                Log.e("FCM", "Error subscribing to topic 'posts'", e)
            }
        }
    }

    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("https://secondwearbackend.onrender.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()

    private fun sendMessage() {
        viewModelScope.launch {
            val messageDto = SendMessageDto(
                notification = NotificationBody(
                    title = "New Post!",
                    body = "Check out the latest post on SecondWear!"
                )
            )
            try {
                Log.d("Notification", "Sending message: $messageDto")
                val response = api.broadcastMessage(messageDto)
                Log.d("Notification", "Message sent successfully, response: $response")
            } catch (e: HttpException) {
                Log.e("Notification", "HTTP exception occurred while sending message", e)
            } catch (e: IOException) {
                Log.e("Notification", "IO exception occurred while sending message", e)
            } catch (e: Exception) {
                Log.e("Notification", "Unexpected error occurred while sending message", e)
            }
        }
    }


    fun uploadImageAndSavePost(
        navHostController: NavHostController,
        context: Context,
        uri: Uri,
        name: String,
        size: String,
        price: String,
        gender: String,
        userId: String,
    ) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${uri.lastPathSegment}")
        val uploadTask = imageRef.putFile(uri)


        uploadTask.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Upload failed", exception)
            Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                Log.d("FirebaseStorage", "Download URL obtained: $imageUrl")
                getCurrentLocation(context) { location ->
                    addPost(context, name, size, price, gender, imageUrl.toString(), location, userId)
                }
                navHostController.navigate(ScreenRoutes.FeedScreen.route){
                    popUpTo(ScreenRoutes.AddProductScreen.route){
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }.addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Failed to get download URL", exception)
                Toast.makeText(context, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation(context: Context, onLocationRetrieved: (Location?) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (areLocationPermissionsGranted(context)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onLocationRetrieved(location)
                } else {
                    requestLocationUpdates(context, onLocationRetrieved, fusedLocationClient)
                }
            }.addOnFailureListener { exception ->
                Log.e("Location", "Failed to get location", exception)
                Toast.makeText(context, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
                onLocationRetrieved(null)
            }
        } else {
            Toast.makeText(context, "Location permissions not granted", Toast.LENGTH_SHORT).show()
            onLocationRetrieved(null)
        }
    }

    private fun requestLocationUpdates(context: Context, onLocationRetrieved: (Location?) -> Unit, fusedLocationClient: FusedLocationProviderClient) {
        if (areLocationPermissionsGranted(context)) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    p0.locations.firstOrNull()?.let { location ->
                        Log.d("Location", "Updated Location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
                        // Stop location updates after getting the location
                        fusedLocationClient.removeLocationUpdates(this)
                        onLocationRetrieved(location)
                    } ?: run {
                        Log.e("Location", "Location result is null")
                        onLocationRetrieved(null)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            Toast.makeText(context, "Location permissions not granted", Toast.LENGTH_SHORT).show()
            onLocationRetrieved(null)
        }
    }

    private fun areLocationPermissionsGranted(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun addPost(
        context: Context,
        name: String,
        size: String,
        price: String,
        gender: String,
        imageUrl: String,
        location: Location?,
        userId: String
    ) {
        if (name.isBlank() || size.isBlank() || price.isBlank() || imageUrl.isBlank() || gender.isBlank()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }
        val timestamp = System.currentTimeMillis()

        val post = hashMapOf(
            "name" to name,
            "size" to size,
            "price" to price,
            "gender" to gender,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "latitude" to location?.latitude,
            "longitude" to location?.longitude,
            "timestamp" to timestamp
        ).filterValues { it != null }

        Log.d("Firestore", "Adding post: $post")

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(context, "Post added successfully", Toast.LENGTH_SHORT).show()
                Log.d("Firestore", "DocumentSnapshot added with ID: ${it.id}")
                sendMessage()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error adding document", exception)
                Toast.makeText(context, "Failed to add post: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
