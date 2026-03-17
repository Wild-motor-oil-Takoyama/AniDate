package com.example.anidate

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anidate.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --- МАСКИ ВВОДА (БЕЗОПАСНЫЕ) ---
val dateMask = VisualTransformation { text ->
    val out = StringBuilder()
    for (i in text.indices) {
        out.append(text[i])
        if (i == 1 || i == 3) out.append(".")
    }
    val translator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 1) return offset
            if (offset <= 3) return offset + 1
            if (offset <= 8) return offset + 2
            return out.length // Фикс: не выходим за границы
        }
        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 2) return offset
            if (offset <= 5) return offset - 1
            if (offset <= 10) return offset - 2
            return text.length
        }
    }
    TransformedText(AnnotatedString(out.toString()), translator)
}

val timeMask = VisualTransformation { text ->
    val out = StringBuilder()
    for (i in text.indices) {
        out.append(text[i])
        if (i == 1) out.append(":")
    }
    val translator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = if (offset <= 1) offset else offset + 1
        override fun transformedToOriginal(offset: Int): Int = if (offset <= 2) offset else offset - 1
    }
    TransformedText(AnnotatedString(out.toString()), translator)
}

// --- ВАЛИДАЦИЯ ---
fun isValidDate(d: String): Boolean {
    if (d.length != 8) return false
    val day = d.substring(0, 2).toIntOrNull() ?: return false
    val month = d.substring(2, 4).toIntOrNull() ?: return false
    val year = d.substring(4).toIntOrNull() ?: return false
    return month in 1..12 && day in 1..31 && year in 1900..2100
}

fun isValidTime(t: String): Boolean {
    if (t.length != 4) return false
    val hour = t.substring(0, 2).toIntOrNull() ?: return false
    val min = t.substring(2, 4).toIntOrNull() ?: return false
    return hour in 0..23 && min in 0..59
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("AniDate_Prefs", Context.MODE_PRIVATE) }
    val gson = remember { Gson() }

    val eventsList = remember {
        val json = sharedPrefs.getString("saved_events", null)
        val typeToken = object : TypeToken<MutableList<AniDateEvent>>() {}.type
        val list: MutableList<AniDateEvent> = if (json != null) try { gson.fromJson(json, typeToken) } catch (e: Exception) { mutableListOf() } else mutableListOf()
        mutableStateListOf<AniDateEvent>().apply { addAll(list) }
    }

    var showDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableIntStateOf(-1) }
    var titleInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }

    val types = listOf("Мультфильм", "Сериал", "Аниме", "Фильм", "Аниме фильм", "Аниме сериал")
    var selectedType by remember { mutableStateOf(types[0]) }
    var expanded by remember { mutableStateOf(false) }

    val isDateError = dateInput.isNotEmpty() && !isValidDate(dateInput)
    val isTimeError = timeInput.isNotEmpty() && !isValidTime(timeInput)
    val isAllFieldsFilled = titleInput.isNotBlank() && dateInput.length == 8 && timeInput.length == 4
    val canSave = isAllFieldsFilled && !isDateError && !isTimeError

    val saveEvents = { sharedPrefs.edit().putString("saved_events", gson.toJson(eventsList.toList())).apply() }

    // Кастомный цвет выделения, чтобы видеть, что мы вводим
    val customTextSelectionColors = TextSelectionColors(
        handleColor = colorResource(id = R.color.menu_purple),
        backgroundColor = colorResource(id = R.color.menu_purple).copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Column(modifier = Modifier.fillMaxSize().background(colorResource(id = R.color.background_dark)).padding(start = 96.dp, top = 40.dp, end = 16.dp)) {
            Text("Уведомления", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { editingIndex = -1; titleInput = ""; dateInput = ""; timeInput = ""; selectedType = types[0]; showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.menu_purple)),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Создать")
            }
            Spacer(Modifier.height(20.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(eventsList) { index, item ->
                    NotificationItem(item, onEdit = { titleInput = item.title; dateInput = item.date; timeInput = item.time; selectedType = item.type; editingIndex = index; showDialog = true }, onDelete = { eventsList.removeAt(index); saveEvents() })
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = colorResource(id = R.color.panel_dark),
            title = { Text(if (editingIndex == -1) "Новое событие" else "Редактирование", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Название
                    OutlinedTextField(
                        value = titleInput, onValueChange = { titleInput = it },
                        label = { Text("Название", color = Color.LightGray) },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = colorResource(id = R.color.menu_purple)
                        )
                    )

                    // Категория
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedType, onValueChange = {}, readOnly = true,
                            label = { Text("Категория", color = Color.LightGray) },
                            textStyle = TextStyle(color = Color.White),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(colorResource(id = R.color.panel_dark))
                        ) {
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = Color.White) },
                                    onClick = { selectedType = type; expanded = false }
                                )
                            }
                        }
                    }

                    // ДАТА (С ПРИНУДИТЕЛЬНЫМ ЦВЕТОМ)
                    Column {
                        if (isDateError) Text("Некорректная дата", color = Color.Red, fontSize = 12.sp)
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= 8) dateInput = digits
                            },
                            label = { Text("Дата (ДДММГГГГ)", color = Color.LightGray) },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp), // ТЕКСТ БЕЛЫЙ
                            visualTransformation = dateMask,
                            isError = isDateError,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                errorTextColor = Color.White
                            )
                        )
                    }

                    // ВРЕМЯ (С ПРИНУДИТЕЛЬНЫМ ЦВЕТОМ)
                    Column {
                        if (isTimeError) Text("Некорректное время", color = Color.Red, fontSize = 12.sp)
                        OutlinedTextField(
                            value = timeInput,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= 4) timeInput = digits
                            },
                            label = { Text("Время (ЧЧММ)", color = Color.LightGray) },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp), // ТЕКСТ БЕЛЫЙ
                            visualTransformation = timeMask,
                            isError = isTimeError,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                errorTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (!isAllFieldsFilled) {
                        Text("Заполните поля", color = Color.Red, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    } else { Spacer(Modifier.weight(1f)) }

                    TextButton(
                        onClick = {
                            val ev = AniDateEvent(titleInput, dateInput, timeInput, selectedType)
                            if (editingIndex == -1) eventsList.add(ev) else eventsList[editingIndex] = ev
                            saveEvents(); showDialog = false
                        },
                        enabled = canSave
                    ) {
                        Text("Готово", color = if (canSave) colorResource(id = R.color.menu_purple) else Color.Gray)
                    }
                }
            }
        )
    }
}