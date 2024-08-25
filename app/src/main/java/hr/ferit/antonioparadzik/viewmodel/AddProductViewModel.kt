package hr.ferit.antonioparadzik.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("MissingPermission")
class AddProductViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    fun uploadImageAndSavePost(
        context: Context,
        uri: Uri,
        name: String,
        size: String,
        price: String,
        userId: String
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
                    addPost(context, name, size, price, imageUrl.toString(), location, userId)
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
            // Ensure the client is initialized
            if (fusedLocationClient != null) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationRetrieved(location)
                    } else {
                        // Request location updates if lastLocation is null
                        requestLocationUpdates(context, onLocationRetrieved, fusedLocationClient)
                    }
                }?.addOnFailureListener { exception ->
                    Log.e("Location", "Failed to get location", exception)
                    Toast.makeText(context, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
                    onLocationRetrieved(null)
                }
            } else {
                Log.e("Location", "Location client is not initialized")
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
                    p0?.locations?.firstOrNull()?.let { location ->
                        Log.d("Location", "Updated Location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
                        // Stop location updates after getting the location
                        fusedLocationClient?.removeLocationUpdates(this)
                        onLocationRetrieved(location)
                    } ?: run {
                        Log.e("Location", "Location result is null")
                        onLocationRetrieved(null)
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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
        imageUrl: String,
        location: Location?,
        userId: String
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val username = user?.displayName
        if (name.isBlank() || size.isBlank() || price.isBlank() || imageUrl.isBlank()) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        val post = hashMapOf(
            "name" to name,
            "size" to size,
            "price" to price,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "username" to username,
            "latitude" to location?.latitude,  // Store latitude as Double or null
            "longitude" to location?.longitude // Store longitude as Double or null

        ).filterValues { it != null }

        Log.d("Firestore", "Adding post: $post")

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(context, "Post added successfully", Toast.LENGTH_SHORT).show()
                Log.d("Firestore", "DocumentSnapshot added with ID: ${it.id}")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error adding document", exception)
                Toast.makeText(context, "Failed to add post: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
