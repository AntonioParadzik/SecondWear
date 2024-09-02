package hr.ferit.antonioparadzik.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.R
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.model.Post
import hr.ferit.antonioparadzik.model.User
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel
import hr.ferit.antonioparadzik.viewmodel.ProfileViewModel

@Composable
fun FeedScreen(
    navHostController: NavHostController,
    homeViewModel: HomeViewModel,
    authenticationViewModel: AuthenticationViewModel,
    profileViewModel: ProfileViewModel,
    rootNavController: NavHostController
) {
    val user by profileViewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            profileViewModel.fetchUser(currentUserId)
        }
    }


    val posts by homeViewModel.filteredPosts.collectAsState()

    MaterialTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            item {
                Header(
                    user = user,
                    onFilterClick = {
                        navHostController.navigate(ScreenRoutes.FilterScreen.route) {
                            popUpTo(ScreenRoutes.FeedScreen.route)
                            {
                                inclusive = true
                            }
                        }
                    }

                )
            }
            if (posts.isEmpty()) {
                item {
                    Text(text = "")
                }
            } else {
                items(posts.size) { index ->
                    val (post, thisUser) = posts[index]
                    ProductCard(post = post, user = thisUser, homeViewModel = homeViewModel)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

}

@Composable
fun Header(
    user: User?,
    onFilterClick: () -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (user != null) {
            Text(text = "Hello, ${user.username}!", fontSize = 24.sp)
        }
        Image(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { onFilterClick() }
        )
    }
}


@Composable
fun ProductCard(post: Post, user: User?, homeViewModel: HomeViewModel) {
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
            user?.profileImageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "User profile image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            } ?: Icon(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Placeholder",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = user?.username ?: "Unknown User", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Product image",
            modifier = Modifier
                .fillMaxSize()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = "Location icon",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = cityName ?: "Fetching location...",
                color = Color.Gray
            )
        }
        Text(text = post.name, fontWeight = FontWeight.Bold)
        Text(text = "Size: ${post.size}", fontWeight = FontWeight.Bold)
        Text(text = "Gender: ${post.gender}", fontWeight = FontWeight.Bold)
        Text(text = "€${post.price}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (user != null) {
            if(user.userId != FirebaseAuth.getInstance().currentUser?.uid) {
                Button(
                    onClick = {
                        homeViewModel.composeEmail(context, user.email, "Inquiry about your post: ${post.name}")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Contact me")
                }
            }
        }

    }
}
