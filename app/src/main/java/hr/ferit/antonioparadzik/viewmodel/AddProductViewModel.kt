package hr.ferit.antonioparadzik.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

class AddProductViewModel:ViewModel() {
    private val storage = Firebase.storage

    fun uploadImage(context: Context, uri: Uri) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${uri.lastPathSegment}")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Upload failed", exception)
            Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUrlToFirestore(context, uri.toString())
            }.addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Failed to get download URL", exception)
                Toast.makeText(context, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun saveImageUrlToFirestore(context: Context, imageUrl: String) {

    }
}