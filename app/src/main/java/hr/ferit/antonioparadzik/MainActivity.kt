package hr.ferit.antonioparadzik

import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.navigation.RootNav
import hr.ferit.antonioparadzik.ui.theme.SecondWearTheme
import hr.ferit.antonioparadzik.view.LoginScreen
import hr.ferit.antonioparadzik.view.RegisterScreen
import hr.ferit.antonioparadzik.viewmodel.AddProductViewModel
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Implement camera related  code
                setRootNav()
            } else {
                // Camera permission denied
            }

        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                setRootNav()
            }
            else -> {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun setRootNav(){
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


