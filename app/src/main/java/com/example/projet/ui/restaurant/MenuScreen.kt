package com.example.projet.ui.restaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.restaurant.AvailableRestaurants
import com.example.projet.data.restaurant.RestaurantInfo
import com.example.projet.data.restaurant.RestaurantMenu
import com.example.projet.ui.components.UtbmLogo
import com.example.projet.ui.theme.BadgeGlutenBg
import com.example.projet.ui.theme.BadgeGlutenText
import com.example.projet.ui.theme.BadgeVeganBg
import com.example.projet.ui.theme.BadgeVeganText
import com.example.projet.ui.theme.UtbmBlue
import com.example.projet.ui.theme.UtbmDarkBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(vm: RestaurantViewModel = viewModel()) {
    val selectedResto by vm.selectedRestaurant.collectAsState()
    val menuState by vm.menuState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UtbmLogo(iconSize = 32.dp, showText = false)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Restaurant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = UtbmDarkBlue
                            )
                            Text(
                                selectedResto.city.uppercase() + " CAMPUS",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.fetchMenu() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = UtbmDarkBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Sélecteur de Restaurant
            RestaurantSelector(
                selected = selectedResto,
                onSelected = { vm.selectRestaurant(it) }
            )

            when (val state = menuState) {
                is MenuState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UtbmBlue)
                    }
                }
                is MenuState.Success -> {
                    MenuContent(state.menu, selectedResto)
                }
                is MenuState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Oops!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, textAlign = TextAlign.Center, color = Color.Gray)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.fetchMenu() }, colors = ButtonDefaults.buttonColors(containerColor = UtbmBlue)) {
                                Text("Réessayer")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RestaurantSelector(selected: RestaurantInfo, onSelected: (RestaurantInfo) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AvailableRestaurants) { resto ->
            FilterChip(
                selected = selected.id == resto.id,
                onClick = { onSelected(resto) },
                label = { Text(resto.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UtbmBlue,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected.id == resto.id,
                    borderColor = Color.LightGray,
                    selectedBorderColor = UtbmBlue
                )
            )
        }
    }
}

@Composable
fun MenuContent(menu: RestaurantMenu, restaurant: RestaurantInfo) {
    val dateStr = SimpleDateFormat("dd MMMM", Locale.FRANCE).format(Date()).uppercase()
    val yearStr = SimpleDateFormat("yyyy", Locale.FRANCE).format(Date())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        restaurant.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Menu du jour • ${restaurant.city}", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(Modifier.width(8.dp))
                // Bloc Date
                Card(
                    colors = CardDefaults.cardColors(containerColor = UtbmBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            dateStr,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            yearStr,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        val categories = menu.midi ?: emptyList()
        
        if (categories.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pas de menu disponible pour ce restaurant aujourd'hui.", textAlign = TextAlign.Center, color = Color.Gray)
                }
            }
        } else {
            categories.forEach { category ->
                item {
                    SectionTitle(title = category.categorie)
                }
                items(category.plats) { plat ->
                    MenuRowItem(title = plat, price = "Tarif CROUS", badgeText = category.categorie.uppercase(), isVegan = plat.contains("Vegan", ignoreCase = true))
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = UtbmDarkBlue,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun MenuRowItem(title: String, price: String, badgeText: String, isVegan: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image du plat (Placeholder)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                 Text("🍴", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = price,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = UtbmBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                CustomBadge(
                    text = badgeText,
                    bgColor = if (isVegan) BadgeVeganBg else BadgeGlutenBg,
                    textColor = if (isVegan) BadgeVeganText else BadgeGlutenText
                )
            }
        }
    }
}

@Composable
fun CustomBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
