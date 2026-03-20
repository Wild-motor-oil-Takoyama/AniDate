package com.example.anidate

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

// --- МОДЕЛЬ ДАННЫХ ---
data class WorkData(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val type: String = "",
    val status: String = "",
    val rating: String = "",
    val description: String = "",
    val imageUri: String? = null
)

data class WorkParticle(val vx: Float, val vy: Float, val color: Color)

// --- ЛОГИКА СОХРАНЕНИЯ ---
fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "work_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        file.absolutePath
    } catch (e: Exception) { null }
}

fun saveWorksList(context: Context, works: List<WorkData>) {
    val prefs = context.getSharedPreferences("anidate_storage", Context.MODE_PRIVATE)
    val json = Gson().toJson(works)
    prefs.edit().putString("works_json", json).apply()
}

fun loadWorksList(context: Context): List<WorkData> {
    val prefs = context.getSharedPreferences("anidate_storage", Context.MODE_PRIVATE)
    val json = prefs.getString("works_json", null)
    return try {
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<WorkData>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } else emptyList()
    } catch (e: Exception) {
        prefs.edit().remove("works_json").apply()
        emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkItem(
    work: WorkData,
    onClick: () -> Unit,
    onDoubleTap: () -> Unit,
    onImageSelected: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isExploding by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "WorkScale"
    )

    val shakeAnim by animateFloatAsState(
        targetValue = if (isExploding) 8f else 0f,
        animationSpec = repeatable(10, tween(40, easing = LinearEasing), RepeatMode.Reverse),
        label = "WorkShake"
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val path = saveImageToInternalStorage(context, it)
            path?.let { p -> onImageSelected(p) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colorResource(id = R.color.panel_dark),
            title = { Text("Удалить произведение?", color = Color.White) },
            text = { Text("Это действие нельзя отменить.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch {
                        isExploding = true
                        delay(1000)
                        onDelete()
                        isExploding = false
                    }
                }) { Text("Удалить", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = Color.White) } }
        )
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (isExploding) {
            WorkExplosionEffect(colorResource(id = R.color.menu_blue))
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
                            .height(140.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = shakeAnim
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { onClick() },
                                    onDoubleTap = {
                                        scope.launch {
                                            isPressed = true
                                            delay(150)
                                            isPressed = false
                                            onDoubleTap()
                                        }
                                    }
                                )
                            },
                        color = colorResource(id = R.color.panel_dark),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(90.dp).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                                    .background(colorResource(id = R.color.background_dark).copy(alpha = 0.8f))
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (work.imageUri != null) {
                                    AsyncImage(model = work.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Add, null, tint = Color.White.copy(0.4f), modifier = Modifier.size(36.dp))
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = work.title, color = colorResource(id = R.color.menu_blue), fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "${work.type} • ${work.status}", color = Color.Gray, fontSize = 12.sp)

                                // ИСПРАВЛЕННАЯ ЛОГИКА ОЦЕНКИ
                                val displayRating = if (work.rating.isBlank()) "?" else work.rating
                                Text(
                                    text = "Оценка: $displayRating/10",
                                    color = Color(0xFFFFD700).copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                if (work.description.isNotEmpty()) {
                                    Text(text = work.description, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun WorkExplosionEffect(color: Color) {
    val particles = remember { List(60) { WorkParticle(Random.nextFloat() * 50 - 25, Random.nextFloat() * 40 - 20, color) } }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { anim.animateTo(1f, tween(1000, easing = FastOutSlowInEasing)) }
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = 1f - anim.value),
                radius = 14f * (1f - anim.value),
                center = Offset(center.x + p.vx * anim.value * 30, center.y + p.vy * anim.value * 30)
            )
        }
    }
}