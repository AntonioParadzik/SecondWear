package hr.ferit.antonioparadzik

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.navigation.RootNav
import hr.ferit.antonioparadzik.ui.theme.SecondWearTheme
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel
import android.app.AlertDialog
import android.net.Uri

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
            // Check if the location settings were modified after returning from settings
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

        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        permissionsRequestLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun promptUserToEnableLocation() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location Services")
            .setMessage("Please enable location service in settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                settingsLauncher.launch(intent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                showLocationNotEnabledDialog()
            }
            .show()
    }

    private fun showLocationNotEnabledDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Services Required")
            .setMessage("This app requires location services to function. Please enable location services in settings.")
            .setPositiveButton("Retry") { _, _ ->
                promptUserToEnableLocation()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish() // Exit the app or handle accordingly
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Camera and location permissions are required for this app to function. Please grant the necessary permissions.")
            .setPositiveButton("Retry") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish() // Exit the app or handle accordingly
            }
            .show()
    }

    private fun showPermissionDeniedPermanentlyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Denied")
            .setMessage("You have denied permissions permanently. Please enable them in app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${packageName}")
                }
                startActivity(intent)
            }
            .setNegativeButton("Exit") { _, _ ->
                finish() // Exit the app or handle accordingly
            }
            .show()
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        // Check if we should show an explanation to the user
        return arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).any { shouldShowRequestPermissionRationale(it) }
    }

    private fun setRootNav() {
        setContent {
            val currentUser = FirebaseAuth.getInstance().currentUser

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