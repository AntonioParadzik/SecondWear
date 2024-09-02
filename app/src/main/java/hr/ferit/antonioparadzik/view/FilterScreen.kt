package hr.ferit.antonioparadzik.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import hr.ferit.antonioparadzik.ScreenRoutes
import hr.ferit.antonioparadzik.viewmodel.HomeViewModel

@Composable
fun FilterScreen(
    navHostController: NavController,
    homeViewModel: HomeViewModel
) {
    val sortByDateNewest by homeViewModel.sortByDateNewest.collectAsState()
    val minPriceFromViewModel by homeViewModel.minPrice.collectAsState()
    val maxPriceFromViewModel by homeViewModel.maxPrice.collectAsState()
    val selectedGender by homeViewModel.selectedGender.collectAsState()

    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf( "") }

    var isNewestToOldestSelected by remember { mutableStateOf(false) }
    var isOldestToNewestSelected by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 16.dp)
    ) {
        // Date sorting buttons
        Text(text = "Sort by Date")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isNewestToOldestSelected = !isNewestToOldestSelected
                    isOldestToNewestSelected = false
                    homeViewModel.applyFilters(if (isNewestToOldestSelected) true else null,  minPriceFromViewModel, maxPriceFromViewModel, selectedGender) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (sortByDateNewest == true) Color(0xFFE0F7FA) else Color.LightGray
                )
            ) {
                Text(text = "Newest to Oldest", color = if (isNewestToOldestSelected) Color(0xFF00796B) else Color.DarkGray)
            }
            Button(
                onClick = {
                    isOldestToNewestSelected = !isOldestToNewestSelected
                    isNewestToOldestSelected = false // Deselect the other button
                    homeViewModel.applyFilters(if (isOldestToNewestSelected) false else null, minPriceFromViewModel, maxPriceFromViewModel, selectedGender) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (sortByDateNewest == false) Color(0xFFE0F7FA) else Color.LightGray
                )
            ) {
                Text(text = "Oldest to Newest", color = if (isOldestToNewestSelected) Color(0xFF00796B) else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Filter by Gender")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenderButton(
                label = "Men",
                isSelected = selectedGender == "Men",
                onClick = { homeViewModel.applyFilters(sortByDateNewest, minPriceFromViewModel, maxPriceFromViewModel, if (selectedGender == "Men") null else "Men") }
            )
            GenderButton(
                label = "Women",
                isSelected = selectedGender == "Women",
                onClick = { homeViewModel.applyFilters(sortByDateNewest, minPriceFromViewModel, maxPriceFromViewModel, if (selectedGender == "Women") null else "Women") }
            )
            GenderButton(
                label = "Unisex",
                isSelected = selectedGender == "Unisex",
                onClick = { homeViewModel.applyFilters(sortByDateNewest, minPriceFromViewModel, maxPriceFromViewModel, if (selectedGender == "Unisex") null else "Unisex") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Filter by Price Range")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = minPrice,
                onValueChange = { minPrice = it },
                label = { Text("Min Price") },
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = maxPrice,
                onValueChange = { maxPrice = it },
                label = { Text("Max Price") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val minPriceValue = minPrice.toFloatOrNull()
            val maxPriceValue = maxPrice.toFloatOrNull()

            homeViewModel.applyFilters(sortByDateNewest, minPriceValue, maxPriceValue, selectedGender)
            navHostController.navigate(ScreenRoutes.FeedScreen.route){
                popUpTo(ScreenRoutes.FilterScreen.route){
                    inclusive = true
                }
            }
        }) {
            Text("Apply Filters")
        }
    }
}


