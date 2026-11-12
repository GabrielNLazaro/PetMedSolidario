package com.petmedsolidario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petmedsolidario.data.Medicine

private val green = Color(0xFF147D64)
private val ink = Color(0xFF173B30)
private val mint = Color(0xFFE9F5EF)
val amber = Color(0xFFF4B860)

@Composable
fun PetMedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = green, onPrimary = Color.White, primaryContainer = mint, onPrimaryContainer = ink,
            secondary = green, secondaryContainer = mint, onSecondaryContainer = ink,
            background = Color(0xFFF6F8F7), onBackground = ink, surface = Color.White, onSurface = ink,
            surfaceContainer = Color.White, error = Color(0xFFB3261E)
        ),
        shapes = Shapes(medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(16.dp)),
        content = content
    )
}

@Composable
fun Page(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
fun Action(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = enabled,
        shape = MaterialTheme.shapes.medium) { Text(label) }
}

@Composable
fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun MedicineCard(item: Medicine, available: Int, open: () -> Unit) {
    ElevatedCard(onClick = open, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.form.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            Text("${item.point.name} · ${item.point.district}")
            Text("$available un. disponíveis", style = MaterialTheme.typography.labelLarge)
        }
    }
}
