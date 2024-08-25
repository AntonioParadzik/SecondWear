package hr.ferit.antonioparadzik.view

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.R
import hr.ferit.antonioparadzik.model.Post
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun FeedScreen(navHostController: NavHostController, homeViewModel: HomeViewModel, authenticationViewModel: AuthenticationViewModel, rootNavController: NavHostController) {
    val context = LocalContext.current
    homeViewModel.fetchPosts(context)
    val posts by homeViewModel.posts.collectAsState()

    MaterialTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            item {
                Header(authenticationViewModel, navHostController, rootNavController)
            }
            items(posts.size) { index ->
                ProductCard( post = posts[index], homeViewModel=homeViewModel)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
val user = FirebaseAuth.getInstance().currentUser
val username = user?.displayName
@Composable
fun Header(authenticationViewModel: AuthenticationViewModel, navController: NavController, rootNavController: NavHostController) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Hello, $username!", fontSize = 24.sp)
        Image(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { authenticationViewModel.logout(context, navController, rootNavController) }

        )
    }
}

@Composable
fun ProductCard( post: Post, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    var cityName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(post.latitude, post.longitude) {
        homeViewModel.fetchCityName(context, post.latitude.toDouble(), post.longitude.toDouble()) { city ->
            cityName = city ?: "Unknown"
        }
    }

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
            Text(text = post.username, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = cityName ?: "Fetching location...", color = Color.Gray)
        Text(text = post.name, fontWeight = FontWeight.Bold)
        Text(text = "Size: ${post.size}", fontWeight = FontWeight.Bold)
        Text(text = "€${post.price}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { /* TODO: Handle contact action */ }, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Contact me")
        }
    }
}

