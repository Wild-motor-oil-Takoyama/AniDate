package com.example.anidate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anidate.R

// Модель данных остается здесь, так как экран владеет состоянием списка
data class AniCollection(val name: String, val count: Int = 0)

@Composable
fun CollectionsScreen(onBack: () -> Unit) {
    val collectionsList = remember {
        mutableStateListOf(
            AniCollection("Смотрю", 12),
            AniCollection("В планах", 45),
            AniCollection("Просмотрено", 120)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.background_dark))
            .padding(start = 96.dp, top = 40.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Библиотеки",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Кнопка создания (функционал реализуется здесь же в будущем через отдельный диалог)
        Button(
            onClick = { /* TODO: Реализовать диалог создания новой коллекции */ },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.menu_green)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Создать библиотеку", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(collectionsList) { index, item ->
                CollectionItem(
                    collection = item,
                    // Передаем логику сохранения изменений в список
                    onEditConfirmed = { newName ->
                        collectionsList[index] = collectionsList[index].copy(name = newName)
                    },
                    onDelete = { collectionsList.removeAt(index) }
                )
            }
        }
    }
}