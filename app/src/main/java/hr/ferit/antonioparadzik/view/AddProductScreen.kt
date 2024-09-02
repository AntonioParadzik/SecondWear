package hr.ferit.antonioparadzik.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.viewmodel.AddProductViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun AddProductScreen(navHostController: NavHostController, addProductViewModel: AddProductViewModel){
    val context = LocalContext.current
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var productName by remember { mutableStateOf("") }
    var productSize by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedGender by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            imageUri = null // Reset imageUri when composable is disposed
        }
    }

    LaunchedEffect(Unit) {
        navHostController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("imageUri")?.observeForever { uriString ->
            imageUri = uriString?.let { Uri.parse(it) }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentScale = ContentScale.FillHeight
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {navHostController.navigate(ScreenRoutes.CameraScreen.route) {
                popUpTo(ScreenRoutes.AddProductScreen.route){
                    
                }
                launchSingleTop = true}}) {
                Text("Take a photo")
            }
            Button(onClick = {galleryLauncher.launch("image/*")
            }) {
                Text("Pick from gallery")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = productSize,
            onValueChange = { productSize = it },
            label = { Text("Size") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = productPrice,
            onValueChange = { productPrice = it },
            label = { Text("Price (in Euros)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Gender",
            modifier = Modifier
                .align(Alignment.Start),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GenderButton(
                label = "Men",
                isSelected = selectedGender == "Men",
                onClick = { selectedGender = "Men" },
                modifier = Modifier.weight(1f)
            )
            GenderButton(
                label = "Women",
                isSelected = selectedGender == "Women",
                onClick = { selectedGender = "Women" },
                modifier = Modifier.weight(1f)
            )
            GenderButton(
                label = "Unisex",
                isSelected = selectedGender == "Unisex",
                onClick = { selectedGender = "Unisex" },
                modifier = Modifier.weight(1f)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            uploading = true
            imageUri?.let {
                addProductViewModel.uploadImageAndSavePost(
                    navHostController,
                    context,
                    it,
                    productName,
                    productSize,
                    productPrice,
                    selectedGender,
                    userId
                )
            }
        },
            enabled = !uploading
        ) {
            Text("Post")
        }
    }
}
@Composable
fun GenderButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = if (isSelected) Color(0xFFE0F7FA) else Color.LightGray
    val textColor = if (isSelected) Color(0xFF00796B) else Color.DarkGray
    val borderColor = if (isSelected) Color.Transparent else Color.LightGray

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = buttonColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(40.dp)
    ) {
        Text(text = label, color = textColor)
    }
}