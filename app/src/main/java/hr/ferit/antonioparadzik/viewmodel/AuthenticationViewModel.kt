package hr.ferit.antonioparadzik.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore
import hr.ferit.antonioparadzik.ScreenRoutes

class AuthenticationViewModel: ViewModel() {
    private val firestore = Firebase.firestore
    fun signIn(context: Context, email: String, password: String, navController: NavController) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Prijavljeno uspješno
                    Toast.makeText(context, "Logged in successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(ScreenRoutes.HomeNav.route) {
                        popUpTo("login_screen") { inclusive = true }
                    }
                } else {
                    // Prijavljivanje neuspješno
                    Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun register(context: Context, username: String, email: String, password: String, navController: NavController) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Username, email and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Create user document in Firestore
                                createUserDocument(user.uid, username)

                                Toast.makeText(
                                    context,
                                    "Registered successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("login_screen")
                            } else {
                                Toast.makeText(context, "Profile update failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserDocument(userId: String, username: String) {
        val userDocument = hashMapOf(
            "username" to username,
            "userId" to userId,
            "profileImageUrl" to "" // Initialize as empty; can be updated later
        )

        firestore.collection("users").document(userId)
            .set(userDocument)
            .addOnSuccessListener {
                // User document created successfully
            }
            .addOnFailureListener { exception ->
                // Handle failure
                Log.e("Firestore", "Error creating user document", exception)
            }
    }

    fun logout(context: Context, navController: NavController, rootNavController: NavHostController) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            rootNavController.navigate(ScreenRoutes.AuthNav.route) {
                popUpTo(ScreenRoutes.HomeNav.route) {
                    inclusive = true
                }
            }
        } else {
            Toast.makeText(context, "Logout Failed", Toast.LENGTH_SHORT).show()
        }
    }
}