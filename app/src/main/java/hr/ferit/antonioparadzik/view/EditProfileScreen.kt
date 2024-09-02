package hr.ferit.antonioparadzik.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.R
import hr.ferit.antonioparadzik.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(navHostController: NavHostController, profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val user by profileViewModel.user.collectAsState()
    var newUsername by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            profileViewModel.fetchUser(currentUserId)
        }
    }
    if (user == null) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text("Loading user information...")
        }
        return
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it
            }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top=42.dp)

    ){
        Box(modifier = Modifier.size(200.dp)){
            AsyncImage(
                model = imageUri ?: user?.profileImageUrl ?: R.drawable.ic_user,
                contentDescription = "User profile image",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add Image",
                        modifier = Modifier.size(56.dp),

                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            label = { Text("New username") },
            modifier = Modifier.width(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            uploading = true
            val updatedUsername = newUsername.ifBlank { user!!.username }
            if (imageUri != null) {
                profileViewModel.uploadImageToFirebase(imageUri!!) { downloadUrl ->
                    profileViewModel.updateUserProfile(navHostController, context, updatedUsername, downloadUrl) {
                        uploading = false
                    }
                }
            }
             else {
                profileViewModel.updateUserProfile(navHostController, context, updatedUsername, user?.profileImageUrl ?: "") {
                    uploading = false
                }
            }
        },
            enabled = !uploading
        ) {
            Text(text = "Save")
        }
    }
}