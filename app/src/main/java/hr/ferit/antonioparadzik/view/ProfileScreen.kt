package hr.ferit.antonioparadzik.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
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
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.antonioparadzik.R
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.model.Post
import hr.ferit.antonioparadzik.model.User
import hr.ferit.antonioparadzik.viewmodel.AuthenticationViewModel
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel
import hr.ferit.antonioparadzik.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    profileViewModel: ProfileViewModel,
    homeViewModel: HomeViewModel,
    authenticationViewModel: AuthenticationViewModel,
    rootNavController: NavHostController
){
    val context = LocalContext.current
    val posts by profileViewModel.posts.collectAsState()
    val user by profileViewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            profileViewModel.fetchUser(currentUserId)
        }
    }
    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            profileViewModel.fetchPostsForCurrentUser(context)
        }
    }

    if (user == null) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Text("")
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ProfileHeader(authenticationViewModel, navHostController, rootNavController, user)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "My Posts",
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (posts.isEmpty()) {
            Text("")
        }else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts.size) { index ->
                    val (post, thisUser) = posts[index]
                    val postId = posts[index].third
                    PostCard(
                        post = post,
                        profileViewModel = profileViewModel,
                        homeViewModel = homeViewModel,
                        postId = postId
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
fun ProfileHeader( authenticationViewModel: AuthenticationViewModel, navController: NavController, rootNavController: NavHostController, user: User?) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Profile Image
                AsyncImage(
                    model = user?.profileImageUrl ?: R.drawable.ic_user,
                    contentDescription = "User profile image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)

            ) {
                user?.let {
                    Text(
                        text = it.username,
                        style = MaterialTheme.typography.h6,
                        color = Color.Black
                    )
                } ?: run {
                    Text(
                        text = "No user available",
                        style = MaterialTheme.typography.h6,
                        color = Color.Black
                    )
                }

                Button(onClick = { navController.navigate(ScreenRoutes.EditProfileScreen.route){
                    popUpTo(ScreenRoutes.ProfileScreen.route){
                        inclusive = true
                    }
                    launchSingleTop = true
                } }) {
                    Text("Edit Profile")
                }

            }
        }
         Column(
             verticalArrangement = Arrangement.Top
         ) {
             IconButton(onClick = { authenticationViewModel.logout(
                 context,
                 navController,
                 rootNavController
             )}) {
                 Icon(
                     painter = painterResource(id = R.drawable.ic_logout),
                     contentDescription = "Logout",
                     tint = Color.Black,
                     modifier = Modifier
                         .size(32.dp)
                 )
             }
         }
    }
}

@Composable
fun PostCard(post: Post, profileViewModel: ProfileViewModel, homeViewModel: HomeViewModel, postId: String) {
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
            .padding(vertical = 6.dp)
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ){
            DeleteDropdown(profileViewModel, postId)
        }
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Product image",
            modifier = Modifier
                .fillMaxSize()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = cityName ?: "Fetching location...", color = Color.Gray, fontSize = 16.sp)
        Text(text = post.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = "Size: ${post.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = "Gender: ${post.gender}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = "€${post.price}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun DeleteDropdown(profileViewModel: ProfileViewModel, postId: String) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Box(modifier = Modifier.wrapContentSize()) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.Black
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                        profileViewModel.deletePost(postId = postId, context = context)
                },
                text = { Text("Delete") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                }
            )
        }
    }
}