package com.example.proyectocompose.common

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.proyectocompose.ui.theme.ProyectoComposeTheme

@Composable
fun TitleText(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 30.sp, textDecoration = TextDecoration.Underline)
}

@Composable
fun Subtitle(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 40.sp)
}

@Composable
fun BodyText(text: String) {
    Text(text = text)
}

@Composable
fun ClickableText(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.clickable { onClick() },
        color = MaterialTheme.colorScheme.tertiary,
        style = TextStyle(textDecoration = TextDecoration.Underline)
    )
}