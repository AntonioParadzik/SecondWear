package hr.ferit.antonioparadzik.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AuthenticationViewModel: ViewModel() {
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
                    navController.navigate("app_scaffold") {
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
                                Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    navController.navigate("login_screen")
                } else {
                    Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun logout(context: Context, navController: NavController) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            navController.navigate("login_screen") {
                popUpTo("app_scaffold") { inclusive = true }
            }
        } else {
            Toast.makeText(context, "Logout Failed", Toast.LENGTH_SHORT).show()
        }
    }
}