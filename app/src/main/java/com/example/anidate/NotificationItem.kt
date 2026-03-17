package com.example.anidate

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anidate.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class AniDateEvent(val title: String, val date: String, val time: String, val type: String)
data class Particle(val vx: Float, val vy: Float, val color: Color)

fun formatDisplayDate(raw: String): String = if (raw.length == 8) "${raw.substring(0, 2)}.${raw.substring(2, 4)}.${raw.substring(4)}" else raw
fun formatDisplayTime(raw: String): String = if (raw.length == 4) "${raw.substring(0, 2)}:${raw.substring(2)}" else raw

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(event: AniDateEvent, onEdit: () -> Unit, onDelete: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isExploding by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, spring(0.5f, Spring.StiffnessLow), label = "")
    val shakeAnim by animateFloatAsState(if (isExploding) 5f else 0f, repeatable(6, tween(40, easing = LinearEasing), RepeatMode.Reverse), label = "")

    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { showDeleteDialog = true; false } else false })

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colorResource(id = R.color.panel_dark),
            title = { Text("Удалить?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch { isExploding = true; delay(1200); onDelete(); isExploding = false }
                }) { Text("Удалить", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = Color.White) } }
        )
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (isExploding) {
            ExplosionEffect(colorResource(id = R.color.menu_purple))
        } else {
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {},
                content = {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = shakeAnim)
                            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { scope.launch { isPressed = true; onEdit(); delay(100); isPressed = false } }) },
                        shape = RoundedCornerShape(16.dp),
                        color = colorResource(id = R.color.panel_dark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
                            Text(event.type.uppercase(), color = colorResource(id = R.color.menu_purple).copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(event.title, color = colorResource(id = R.color.menu_purple), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Text(text = formatDisplayDate(event.date), color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = formatDisplayTime(event.time), color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ExplosionEffect(color: Color) {
    val particles = remember { List(45) { Particle(Random.nextFloat() * 40 - 20, Random.nextFloat() * 40 - 20, color) } }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { anim.animateTo(1f, tween(1200, easing = FastOutSlowInEasing)) }
    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        particles.forEach { p ->
            drawCircle(color = p.color.copy(alpha = 1f - anim.value), radius = 12f * (1f - anim.value), center = Offset(center.x + p.vx * anim.value * 25, center.y + p.vy * anim.value * 25))
        }
    }
}