package com.example.anidate

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage

@Composable
fun WorksScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val worksList = remember {
        mutableStateListOf<WorkData>().apply { addAll(loadWorksList(context)) }
    }

    var selectedWork by remember { mutableStateOf<WorkData?>(null) }

    LaunchedEffect(worksList.toList()) {
        saveWorksList(context, worksList.toList())
    }

    AnimatedContent(
        targetState = selectedWork,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        }, label = "WorkNavigation"
    ) { work ->
        if (work == null) {
            WorkListContent(
                worksList = worksList,
                onBack = onBack,
                onAddClick = { newWork: WorkData -> worksList.add(newWork) },
                onEditWork = { updated: WorkData ->
                    val index = worksList.indexOfFirst { it.id == updated.id }
                    if (index != -1) worksList[index] = updated
                },
                onWorkClick = { selectedWork = it },
                onImageSelected = { index, path ->
                    worksList[index] = worksList[index].copy(imageUri = path)
                },
                onDeleteWork = { index ->
                    if (index < worksList.size) worksList.removeAt(index)
                }
            )
        } else {
            WorkDetailScreen(
                work = work,
                onBack = { selectedWork = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkListContent(
    worksList: List<WorkData>,
    onBack: () -> Unit,
    onAddClick: (WorkData) -> Unit,
    onEditWork: (WorkData) -> Unit,
    onWorkClick: (WorkData) -> Unit,
    onImageSelected: (Int, String) -> Unit,
    onDeleteWork: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWork by remember { mutableStateOf<WorkData?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredWorks = remember(searchQuery, worksList.toList()) {
        if (searchQuery.isBlank()) worksList
        else worksList.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    if (showAddDialog) {
        WorkEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { data: WorkData -> onAddClick(data); showAddDialog = false }
        )
    }

    if (editingWork != null) {
        WorkEditDialog(
            initialWork = editingWork,
            onDismiss = { editingWork = null },
            onConfirm = { updated: WorkData ->
                onEditWork(updated)
                editingWork = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(colorResource(id = R.color.background_dark))
            .padding(start = 96.dp, top = 40.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Text("Произведения", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = colorResource(id = R.color.menu_blue),
                cursorColor = colorResource(id = R.color.menu_blue)
            )
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.menu_blue)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Добавить произведение", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(filteredWorks) { _, item ->
                val originalIndex = worksList.indexOfFirst { it.id == item.id }
                WorkItem(
                    work = item,
                    onClick = { onWorkClick(item) },
                    onDoubleTap = { editingWork = item },
                    onImageSelected = { path -> if (originalIndex != -1) onImageSelected(originalIndex, path) },
                    onDelete = { if (originalIndex != -1) onDeleteWork(originalIndex) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkEditDialog(
    initialWork: WorkData? = null,
    onDismiss: () -> Unit,
    onConfirm: (WorkData) -> Unit
) {
    var title by remember { mutableStateOf(initialWork?.title ?: "") }
    var type by remember { mutableStateOf(initialWork?.type ?: "") }
    var status by remember { mutableStateOf(initialWork?.status ?: "") }
    var rating by remember { mutableStateOf(initialWork?.rating ?: "") }
    var description by remember { mutableStateOf(initialWork?.description ?: "") }

    var triedToSubmit by remember { mutableStateOf(false) }

    val ratingValue = rating.toIntOrNull()
    val isRatingError = rating.isNotEmpty() && (ratingValue == null || ratingValue > 10)

    val types = listOf("Мультфильм", "Сериал", "Аниме", "Фильм", "Аниме фильм", "Аниме сериал")
    val statuses = listOf("Смотрю", "Бросил", "Буду смотреть")
    var typeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().imePadding(),
        containerColor = colorResource(id = R.color.panel_dark),
        title = { Text(if (initialWork == null) "Новое" else "Правка", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomInputField(value = title, onValueChange = { title = it }, label = "Название *", isError = triedToSubmit && title.isBlank())

                Box {
                    CustomInputField(value = type, onValueChange = {}, label = "Тип *", readOnly = true, isError = triedToSubmit && type.isBlank(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) })
                    Box(Modifier.matchParentSize().clickable { typeExpanded = true })
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }, modifier = Modifier.background(colorResource(id = R.color.panel_dark))) {
                        types.forEach { t -> DropdownMenuItem(text = { Text(t, color = Color.White) }, onClick = { type = t; typeExpanded = false }) }
                    }
                }

                Box {
                    CustomInputField(value = status, onValueChange = {}, label = "Статус *", readOnly = true, isError = triedToSubmit && status.isBlank(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) })
                    Box(Modifier.matchParentSize().clickable { statusExpanded = true })
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }, modifier = Modifier.background(colorResource(id = R.color.panel_dark))) {
                        statuses.forEach { s -> DropdownMenuItem(text = { Text(s, color = Color.White) }, onClick = { status = s; statusExpanded = false }) }
                    }
                }

                CustomInputField(value = rating, onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) rating = it }, label = "Оценка (0-10)", isError = isRatingError, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                CustomInputField(value = description, onValueChange = { description = it }, label = "Описание", isError = false, modifier = Modifier.height(110.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    triedToSubmit = true
                    if (title.isNotBlank() && type.isNotBlank() && status.isNotBlank() && !isRatingError) {
                        onConfirm(initialWork?.copy(title = title, type = type, status = status, rating = rating, description = description)
                            ?: WorkData(title = title, type = type, status = status, rating = rating, description = description))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.menu_blue)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Готово", color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = Color.White) } }
    )
}

@Composable
fun CustomInputField(value: String, onValueChange: (String) -> Unit, label: String, isError: Boolean, readOnly: Boolean = false, modifier: Modifier = Modifier, trailingIcon: @Composable (() -> Unit)? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange, label = { Text(label) }, readOnly = readOnly, isError = isError, modifier = Modifier.fillMaxWidth(),
            trailingIcon = trailingIcon, keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White, focusedTextColor = Color.White,
                unfocusedBorderColor = Color.Gray, focusedBorderColor = colorResource(id = R.color.menu_blue),
                errorBorderColor = Color.Red, unfocusedLabelColor = Color.Gray, focusedLabelColor = colorResource(id = R.color.menu_blue)
            )
        )
    }
}

@Composable
fun WorkDetailScreen(work: WorkData, onBack: () -> Unit) {
    val baseColor = colorResource(id = R.color.panel_dark)
    val highlightColor = remember(baseColor) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(baseColor.toArgb(), hsl)
        hsl[2] = (hsl[2] + 0.06f).coerceAtMost(1.0f)
        Color(ColorUtils.HSLToColor(hsl))
    }

    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    Column(modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.background_dark)).padding(start = 96.dp, top = 40.dp, end = 16.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Spacer(Modifier.width(12.dp))
            AnimatedVisibility(visible = startAnim, enter = fadeIn(tween(600)) + slideInHorizontally(tween(600)) { -20 }) {
                Text(text = work.title, color = colorResource(id = R.color.menu_blue), fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Surface(color = highlightColor, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.size(110.dp, 160.dp).clip(RoundedCornerShape(12.dp)).background(colorResource(id = R.color.background_dark))) {
                        if (work.imageUri != null) AsyncImage(model = work.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Тип: ${work.type}", color = Color.White.copy(0.9f), fontSize = 15.sp)
                        Text("Статус: ${work.status}", color = Color.White.copy(0.9f), fontSize = 15.sp)

                        // ЛОГИКА ОТОБРАЖЕНИЯ ОЦЕНКИ: если пусто, выводим ?
                        Spacer(Modifier.height(10.dp))
                        val scoreText = if (work.rating.isBlank()) "?" else work.rating
                        Text(
                            text = "$scoreText/10",
                            color = Color(0xFFFFD700),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("Описание", color = colorResource(id = R.color.menu_blue), fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            Spacer(Modifier.height(12.dp))
            Text(text = if (work.description.isNotEmpty()) work.description else "Нет описания...", color = Color.White.copy(0.85f), lineHeight = 24.sp, fontSize = 16.sp)
        }
    }
}