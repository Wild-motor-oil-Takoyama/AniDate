package com.example.anidate

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AniDateSideLayout(
    currentScreen: String,
    onNavigateToEvents: () -> Unit,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val panelWidth by animateDpAsState(
        targetValue = if (isExpanded) dimensionResource(id = R.dimen.panel_expanded)
        else dimensionResource(id = R.dimen.panel_collapsed),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PanelWidth"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount > 50) isExpanded = true
                    if (dragAmount < -50) isExpanded = false
                }
            }
    ) {
        // 1. ОСНОВНОЙ КОНТЕНТ (Нижний слой)
        content()

        // 2. БОКОВАЯ ПАНЕЛЬ (Верхний слой)
        Column(
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .background(colorResource(id = R.color.panel_dark))
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 40.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ExpandableMenuItem(
                icon = Icons.Default.Person,
                label = stringResource(id = R.string.menu_account),
                color = colorResource(id = R.color.menu_red),
                isExpanded = isExpanded
            )

            Spacer(modifier = Modifier.height(20.dp))

            ExpandableMenuItem(
                icon = if (currentScreen == "main") Icons.Default.DateRange else Icons.Default.ArrowBack,
                label = if (currentScreen == "main") stringResource(id = R.string.menu_events) else "Назад",
                color = colorResource(id = R.color.menu_purple),
                isExpanded = isExpanded,
                onClick = {
                    if (currentScreen == "main") onNavigateToEvents() else onBack()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
            ExpandableMenuItem(Icons.Default.Favorite, stringResource(id = R.string.menu_collections), colorResource(id = R.color.menu_green), isExpanded)
            Spacer(modifier = Modifier.height(20.dp))
            ExpandableMenuItem(Icons.Default.Settings, stringResource(id = R.string.menu_settings), colorResource(id = R.color.menu_blue), isExpanded)
        }
    }
}

// ЭТУ ФУНКЦИЮ МЫ ДОБАВИЛИ СЮДА, ЧТОБЫ ОШИБКИ ИСЧЕЗЛИ:
@Composable
fun ExpandableMenuItem(
    icon: ImageVector,
    label: String,
    color: Color,
    isExpanded: Boolean,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.menu_item_height))
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius)),
        color = color
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInHorizontally { -40 } + fadeIn(),
                exit = slideOutHorizontally { -40 } + fadeOut()
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 15.dp),
                    maxLines = 1
                )
            }
        }
    }
}