package com.example.projet.ui.restaurant

import androidx.compose.foundation.BorderStroke
import com.example.projet.ui.components.UtbmLogo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet.ui.theme.BadgeGlutenBg
import com.example.projet.ui.theme.BadgeGlutenText
import com.example.projet.ui.theme.BadgeVeganBg
import com.example.projet.ui.theme.BadgeVeganText
import com.example.projet.ui.theme.UtbmBlue
import com.example.projet.ui.theme.UtbmDarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen() {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UtbmLogo(iconSize = 32.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Restaurant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF0A192F)
                            )
                            Text(
                                "SEVENANS CAMPUS",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }


                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = UtbmDarkBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("HOME") })
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Outlined.CalendarMonth, "Agenda") },
                    label = { Text("AGENDA") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = UtbmBlue,
                        selectedTextColor = UtbmBlue,
                        indicatorColor = BadgeVeganBg
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Outlined.ChatBubbleOutline, "Chat") },
                    label = { Text("CHAT") })
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.People, "Groups") },
                    label = { Text("GROUPS") })
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Menu, "Menu") },
                    label = { Text("MENU") })
            }
        }

    ) {
        innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Restaurant Universitaire",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text("Menu du jour • Belfort", fontSize = 14.sp, color = Color.Gray)
                    }
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
                                "14 AVRIL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "2026",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.verticalGradient(listOf(UtbmBlue, UtbmDarkBlue)))
                ) {
                    // Ici tu mettras ton Image de fond avec un Painter (ex: le chien ou le plat)
                    // Modifier.matchParentSize() pour remplir le fond

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CustomBadge(text = "LE CHOIX DU CHEF", bgColor = Color.White.copy(alpha = 0.2f), textColor = Color.White)
                                CustomBadge(text = "SANS GLUTEN", bgColor = BadgeGlutenBg.copy(alpha = 0.8f), textColor = BadgeGlutenText)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pavé de Saumon à l'Unilatérale",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Servi avec un écrasé de pommes de terre à l'huile de truffe et ses asperges croquantes.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column {
                            Text(
                                "PRIX ÉTUDIANT",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "3,30 €",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            item {
                 SectionTitle(title = "Entrées")
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                     MenuRowItem(title = "Salade Croquante du Jardin", price = "0,80 €", badgeText = "VEGAN", isVegan = true)
                    MenuRowItem(title = "Velouté de Saison", price = "0,80 €", badgeText = "SANS GLUTEN", isVegan = false)
                }
            }

            // Section Plats de Résistance
            item {
                 SectionTitle(title = "Plats de Résistance")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column {
                        // Image Placeholder pour le Poke bowl
                        Box(
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                                .background(Color.LightGray)
                        )

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Poke Bowl Vegan aux Falafels",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    "3,30 €",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = UtbmBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Quinoa, avocat, fèves edamame et sauce sésame.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CustomBadge(text = "VEGAN", bgColor = BadgeVeganBg, textColor = BadgeVeganText)
                                CustomBadge(text = "ÉQUILIBRÉ", bgColor = BadgeGlutenBg, textColor = BadgeGlutenText)
                            }
                        }
                    }
                }
            }

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
        border = CardStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Placeholder Image du plat miniature
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text(price, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = UtbmBlue)
                }
                Spacer(modifier = Modifier.height(4.dp))
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
fun CardStroke(width: Dp, color: Color): BorderStroke {
    return BorderStroke(width, color)
}

@Composable
fun CustomBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bgColor).padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}



