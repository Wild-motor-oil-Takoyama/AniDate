package com.example.anidate

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Вспомогательный класс для эффекта взрыва
data class LibParticle(val vx: Float, val vy: Float, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionItem(
    collection: AniCollection,
    onEditConfirmed: (String) -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isExploding by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Состояние для редактирования названия
    var editedName by remember(showEditDialog) { mutableStateOf(collection.name) }

    // Анимации нажатия и тряски
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LibScale"
    )

    val shakeAnim by animateFloatAsState(
        targetValue = if (isExploding) 6f else 0f,
        animationSpec = repeatable(8, tween(40, easing = LinearEasing), RepeatMode.Reverse),
        label = "LibShake"
    )

    // --- ДИАЛОГ УДАЛЕНИЯ ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colorResource(id = R.color.panel_dark),
            title = { Text("Удалить библиотеку?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        isExploding = true
                        delay(1100)
                        onDelete()
                        isExploding = false
                    }
                }) { Text("Удалить", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = Color.White) } }
        )
    }

    // --- ДИАЛОГ РЕДАКТИРОВАНИЯ ---
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            containerColor = colorResource(id = R.color.panel_dark),
            title = {
                Text(
                    "Редактировать название",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        label = { Text("Название коллекции") },
                        isError = editedName.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = colorResource(id = R.color.menu_green),
                            errorBorderColor = Color.Red,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = colorResource(id = R.color.menu_green)
                        )
                    )
                    if (editedName.isBlank()) {
                        Text(
                            "Поле не может быть пустым",
                            color = Color.Red,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            onEditConfirmed(editedName)
                            showEditDialog = false
                        }
                    },
                    enabled = editedName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.menu_green),
                        disabledContainerColor = colorResource(id = R.color.menu_green).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Готово", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Отмена", color = Color.White)
                }
            }
        )
    }

    // --- ОСНОВНОЙ КОНТЕНТ КАРТОЧКИ ---
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (isExploding) {
            LibExplosionEffect(colorResource(id = R.color.menu_green))
        } else {
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        showDeleteDialog = true
                        false
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = { Box(Modifier.fillMaxSize()) },
                content = {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = shakeAnim)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scope.launch {
                                            isPressed = true
                                            delay(150)
                                            showEditDialog = true
                                            isPressed = false
                                        }
                                    }
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = colorResource(id = R.color.panel_dark)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = collection.name,
                                    color = colorResource(id = R.color.menu_green),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "${collection.count} произведений",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LibExplosionEffect(color: Color) {
    val particles = remember { List(50) { LibParticle(Random.nextFloat() * 40 - 20, Random.nextFloat() * 40 - 20, color) } }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { anim.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
    Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = 1f - anim.value),
                radius = 12f * (1f - anim.value),
                center = Offset(center.x + p.vx * anim.value * 25, center.y + p.vy * anim.value * 25)
            )
        }
    }
}