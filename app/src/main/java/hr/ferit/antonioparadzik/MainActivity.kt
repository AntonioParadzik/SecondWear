package hr.ferit.antonioparadzik

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.navigation.RootNav
import hr.ferit.antonioparadzik.ui.theme.SecondWearTheme
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel
import android.app.AlertDialog
import android.util.Log
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private var permissionsRequested = false

    private val permissionsRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionsRequested = true
            if (permissions.all { it.value }) {
                if (isLocationEnabled()) {
                    setRootNav()
                } else {
                    promptUserToEnableLocation()
                }
            } else {
                if (shouldShowRequestPermissionRationale()) {
                    showPermissionDeniedDialog()
                } else {
                    showPermissionDeniedPermanentlyDialog()
                }
            }
        }

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (isLocationEnabled()) {
                setRootNav()
            } else {
                showLocationNotEnabledDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                setRootNav()
            } else {
                promptUserToEnableLocation()
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        var notificationPermissionGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                notificationPermissionGranted
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionsRequestLauncher.launch(permissions.toTypedArray())
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptUserToEnableLocation() {
        showDialog(
            title = "Enable Location Services",
            message = "Please enable location service in settings.",
            positiveButtonText = "Settings",
            positiveButtonAction = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                settingsLauncher.launch(intent)
            },
            negativeButtonText = "Cancel",
            negativeButtonAction = {
                showLocationNotEnabledDialog()
            }
        )
    }

    private fun showLocationNotEnabledDialog() {
        showDialog(
            title = "Location Services Required",
            message = "This app requires location services to function. Please enable location services in settings.",
            positiveButtonText = "Retry",
            positiveButtonAction = {
                promptUserToEnableLocation()
            },
            negativeButtonText = "Exit",
            negativeButtonAction = {
                finish()
            }
        )
    }

    private fun showPermissionDeniedDialog() {
        showDialog(
            title = "Permissions Required",
            message = "Camera, location, and notification permissions are required for this app to function. Please grant the necessary permissions.",
            positiveButtonText = "Retry",
            positiveButtonAction = {
                requestPermissions()
            },
            negativeButtonText = "Exit",
            negativeButtonAction = {
                finish()
            }
        )
    }

    private fun showPermissionDeniedPermanentlyDialog() {
        showDialog(
            title = "Permissions Denied",
            message = "You have denied permissions permanently. Please enable them in app settings.",
            positiveButtonText = "Open Settings",
            positiveButtonAction = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${packageName}")
                }
                startActivity(intent)
            },
            negativeButtonText = "Exit",
            negativeButtonAction = {
                finish()
            }
        )
    }

    private fun showDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        positiveButtonAction: () -> Unit,
        negativeButtonText: String,
        negativeButtonAction: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> positiveButtonAction() }
            .setNegativeButton(negativeButtonText) { _, _ -> negativeButtonAction() }
            .show()
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.any { shouldShowRequestPermissionRationale(it) }
    }

    private fun setRootNav() {
        setContent {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                Log.e("MainActivity", "currentUser UID: ${currentUser.uid}")
                Log.e("MainActivity", "currentUser Provider ID: ${currentUser.providerId}")
                Log.e("MainActivity", "currentUser Email: ${currentUser.email}")
            } else {
                Log.e("MainActivity", "currentUser is null")
            }
            SecondWearTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNav(currentUser, authenticationViewModel, homeViewModel)
                }
            }
        }
    }
}
