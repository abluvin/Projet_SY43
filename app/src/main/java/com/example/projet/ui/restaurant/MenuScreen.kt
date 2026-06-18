package com.example.projet.ui.restaurant

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.restaurant.AvailableRestaurants
import com.example.projet.data.restaurant.MenuDay
import com.example.projet.data.restaurant.RestaurantInfo
import com.example.projet.ui.components.UtbmLogo
import com.example.projet.ui.theme.BadgeGlutenBg
import com.example.projet.ui.theme.BadgeGlutenText
import com.example.projet.ui.theme.BadgeVeganBg
import com.example.projet.ui.theme.BadgeVeganText
import com.example.projet.ui.theme.UtbmBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(vm: RestaurantViewModel = viewModel()) {
    val selectedResto by vm.selectedRestaurant.collectAsState()
    val menuState by vm.menuState.collectAsState()
    val gpsDetected by vm.gpsDetected.collectAsState()
    val context = LocalContext.current

    fun detectLocation() {
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        val location = providers.firstNotNullOfOrNull { provider ->
            try { locationManager.getLastKnownLocation(provider) } catch (e: SecurityException) { null }
        }
        location?.let { vm.detectNearestRestaurant(it.latitude, it.longitude) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) detectLocation() }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) detectLocation()
    }

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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                selectedResto.city.uppercase() + " CAMPUS",
                                fontSize = 11.sp,
                                color = if (gpsDetected) Color(0xFF2ECC71) else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) detectLocation()
                        else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Détecter campus",
                            tint = if (gpsDetected) Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { vm.fetchMenu() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (gpsDetected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F8EF))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Campus détecté automatiquement", fontSize = 12.sp, color = Color(0xFF27AE60), fontWeight = FontWeight.Medium)
                }
            }

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
                    MenuContent(state.menuDays, selectedResto)
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
fun MenuContent(menuDays: List<MenuDay>, restaurant: RestaurantInfo) {
    val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfFr = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    val today = Date()
    val todayIso = sdfIso.format(today)
    val todayFr = sdfFr.format(today)
    
    // On cherche le menu d'aujourd'hui
    val currentDayMenu = menuDays.find { it.date == todayIso || it.date == todayFr } 
                        ?: menuDays.firstOrNull()

    if (currentDayMenu == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun menu disponible pour le moment.", textAlign = TextAlign.Center, color = Color.Gray)
        }
        return
    }

    // Tentative de parsing propre de la date pour le badge
    val parsedDate: Date? = try {
        if (currentDayMenu.date.contains("-")) {
            val parts = currentDayMenu.date.split("-")
            if (parts[0].length == 4) {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(currentDayMenu.date)
            } else {
                SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(currentDayMenu.date)
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    val displayDate = parsedDate?.let { 
        SimpleDateFormat("dd MMMM", Locale.FRANCE).format(it).uppercase() 
    } ?: currentDayMenu.date

    val displayYear = parsedDate?.let { 
        SimpleDateFormat("yyyy", Locale.FRANCE).format(it) 
    } ?: ""

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
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Menu du ${currentDayMenu.date}", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(Modifier.width(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = UtbmBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(displayDate, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        if (displayYear.isNotEmpty()) {
                            Text(displayYear, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        if (currentDayMenu.repas.isEmpty()) {
            item {
                Text(
                    "Détails du menu non disponibles.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    color = Color.Gray
                )
            }
        } else {
            currentDayMenu.repas.forEach { repas ->
                item {
                    Text(
                        text = repas.type.uppercase(),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = UtbmBlue
                    )
                    HorizontalDivider(color = UtbmBlue.copy(alpha = 0.2f))
                }

                repas.categories.forEach { category ->
                    item {
                        SectionTitle(title = category.libelle)
                    }
                    items(category.plats) { plat ->
                        MenuRowItem(
                            title = plat.libelle,
                            price = "Tarif CROUS",
                            badgeText = category.libelle,
                            isVegan = plat.libelle.contains("Vegan", ignoreCase = true) || 
                                     plat.libelle.contains("Végé", ignoreCase = true) ||
                                     plat.libelle.contains("Falafel", ignoreCase = true)
                        )
                        Spacer(Modifier.height(12.dp))
                    }
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
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun MenuRowItem(title: String, price: String, badgeText: String, isVegan: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        color = MaterialTheme.colorScheme.onSurface,
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
