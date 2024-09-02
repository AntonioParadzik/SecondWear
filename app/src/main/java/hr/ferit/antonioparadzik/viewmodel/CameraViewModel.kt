package hr.ferit.antonioparadzik.viewmodel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Locale

class CameraViewModel: ViewModel() {
    fun captureImage(navHostController: NavHostController, imageCapture: ImageCapture, context: Context) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val name = "IMG_$timestamp.jpeg"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = outputFileResults.savedUri
                    uri?.let {
                        navHostController.previousBackStackEntry?.savedStateHandle?.set("imageUri", uri.toString())
                        navHostController.popBackStack()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    println("Failed $exception")
                }
            }
        )
    }
}