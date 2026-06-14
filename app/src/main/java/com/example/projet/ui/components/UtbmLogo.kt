package com.example.projet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IconBg   = Color(0xFF0E2A5C)
private val UtbmBlue = Color(0xFF1A57A0)

@Composable
fun UtbmLogo(modifier: Modifier = Modifier, iconSize: Dp = 40.dp) {
    val barHeight = (iconSize.value * 0.09f).dp
    val cornerRadius = (iconSize.value * 0.22f).dp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Icône carrée
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(cornerRadius))
                .background(IconBg)
        ) {
            // Lettre U — grande et centrée
            Text(
                text = "U",
                color = Color.White,
                fontSize = (iconSize.value * 0.72f).sp,
                fontWeight = FontWeight.Black,
                lineHeight = (iconSize.value * 0.72f).sp,
                modifier = Modifier.align(Alignment.Center)
            )
            // Barre tricolore en bas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .align(Alignment.BottomCenter)
            ) {
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4A90D9)))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF2ECC71)))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF8B5CF6)))
            }
        }

        // Wordmark : UTBM + trait + connect
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "UTBM",
                color = UtbmBlue,
                fontSize = (iconSize.value * 0.50f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                lineHeight = (iconSize.value * 0.52f).sp
            )
            HorizontalDivider(
                color = UtbmBlue,
                thickness = 1.5.dp,
                modifier = Modifier.width((iconSize.value * 1.5f).dp)
            )
            Text(
                text = "connect",
                color = UtbmBlue,
                fontSize = (iconSize.value * 0.20f).sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 3.sp,
                lineHeight = (iconSize.value * 0.24f).sp
            )
        }
    }
}
