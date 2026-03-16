package com.example.anidate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Главное состояние навигации
            var currentScreen by remember { mutableStateOf("main") }

            // Общая подложка, чтобы не было белых вспышек
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorResource(id = R.color.background_dark)
            ) {
                // Вызываем твою новую оболочку из файла AniDateSidePanel.kt
                AniDateSideLayout(
                    currentScreen = currentScreen,
                    onNavigateToEvents = { currentScreen = "events" },
                    onBack = { currentScreen = "main" }
                ) {
                    // Внутри оболочки меняем только основной контент
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(500),
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "main" -> MainContent()
                            "events" -> EventsScreen() // Должен быть в EventsScreen.kt
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    // Контент главного экрана (текст AniDate)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 96.dp, top = 40.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )
    }
}