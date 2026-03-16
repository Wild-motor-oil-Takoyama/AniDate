package com.example.anidate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults



// В файле EventsScreen.kt
@Composable
fun EventsScreen() { // Скобки пустые
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_dark))
            .padding(start = 96.dp, top = 40.dp, end = 16.dp)
    ) {
        // КНОПКУ "НАЗАД" ОТСЮДА УДАЛЯЕМ

        Text(
            text = "События",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(5) { index ->
                NotificationItem(index)
            }
        }
    }
}

@Composable
fun NotificationItem(index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.panel_dark))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Уведомление #${index + 1}",
                color = colorResource(id = R.color.menu_purple),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Здесь будет текст важного события или новости из мира аниме.",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}