package hr.ferit.antonioparadzik.view.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.R
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel, authenticationViewModel: AuthenticationViewModel) {

    MaterialTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            item {
                Header(authenticationViewModel, navController)
            }
            items(2) { index ->
                ProductCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
val user = FirebaseAuth.getInstance().currentUser
val username = user?.displayName
@Composable
fun Header(authenticationViewModel: AuthenticationViewModel, navController: NavController) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Hello, $username!", fontSize = 24.sp)
        Image(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { authenticationViewModel.logout(context, navController) }

        )
    }
}

@Composable
fun ProductCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Username", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(R.drawable.ic_home),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Location", color = Color.Gray)
        Text(text = "Product", fontWeight = FontWeight.Bold)
        Text(text = "Size:", fontWeight = FontWeight.Bold)
        Text(text = "€XX.YY", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO: Handle contact action */ }, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Contact me")
        }
    }
}

