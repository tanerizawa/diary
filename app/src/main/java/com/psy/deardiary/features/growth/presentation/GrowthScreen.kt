// File: app/src/main/java/com/psy/deardiary/features/growth/GrowthScreen.kt
// VERSI DIPERBARUI: Menambahkan impor yang hilang untuk toArgb.

package com.psy.deardiary.features.growth.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb // PERBAIKAN: Impor ditambahkan di sini
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.ui.theme.DearDiaryTheme
import com.psy.deardiary.ui.theme.Primary
import com.psy.deardiary.ui.theme.PrimaryContainer
import com.psy.deardiary.ui.theme.Secondary
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    viewModel: GrowthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pertumbuhan & Pencapaian") })
        }
    ) { paddingValues ->
        if (state.isLoading && state.totalJournals == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.totalJournals == 0) {
            EmptyGrowthState(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                CalmnessTree(streak = state.writingStreak)
                Divider(modifier = Modifier.padding(vertical = 24.dp))
                MoodCalendar(
                    yearMonth = state.currentDisplayMonth,
                    moodData = state.moodCalendarData,
                    onMonthChange = { offset -> viewModel.changeDisplayMonth(offset) }
                )
                Divider(modifier = Modifier.padding(vertical = 24.dp))

                MoodTrendChart(data = state.moodTrendData)

                Divider(modifier = Modifier.padding(vertical = 24.dp))
                Text("Statistik Kamu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(title = "Total Jurnal", value = state.totalJournals.toString(), modifier = Modifier.weight(1f))
                    StatisticCard(title = "Runtutan Menulis", value = "${state.writingStreak} hari", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                StatisticCard(title = "Mood Paling Sering", value = state.mostFrequentMood, modifier = Modifier.fillMaxWidth())
                Divider(modifier = Modifier.padding(vertical = 24.dp))
                Text("Pencapaian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                AchievementBadge(title = "Refleksi Pertama", description = "Memulai perjalanan menulismu.", achieved = state.totalJournals > 0)
                AchievementBadge(title = "Penulis 7 Hari", description = "Menulis jurnal selama 7 hari berturut-turut.", achieved = state.writingStreak >= 7)
            }
        }
    }
}

@Composable
private fun MoodTrendChart(data: List<MoodDataPoint>) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = onSurfaceVariant.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tren Mood 7 Hari Terakhir",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (data.isEmpty()) {
            Text(
                "Tulis jurnal beberapa hari untuk melihat tren mood-mu di sini.",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 8.dp)
            ) {
                val path = Path()
                val (minY, maxY) = 0f to 5f // Skor mood dari 0 (tidak ada data) sampai 5
                val xStep = if (data.size > 1) size.width / (data.size - 1) else size.width / 2

                data.forEachIndexed { index, point ->
                    val x = if (data.size > 1) index * xStep else xStep
                    val y = size.height - ((point.averageMood - minY) / (maxY - minY) * size.height).coerceIn(0f, size.height)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    if (point.averageMood > 0) {
                        drawCircle(color = Primary, radius = 8f, center = Offset(x, y))
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        point.label,
                        x,
                        size.height + textPaint.textSize + 12.dp.toPx(),
                        textPaint
                    )
                }

                drawPath(
                    path = path,
                    color = Primary,
                    style = Stroke(width = 5f)
                )
            }
        }
    }
}

@Composable
private fun EmptyGrowthState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌱📊", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum Ada Data Pertumbuhan",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tulis jurnal pertamamu untuk mulai melihat bagaimana kamu tumbuh dan mencapai tujuanmu di sini.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MoodCalendar(
    yearMonth: YearMonth,
    moodData: Map<Int, String>,
    onMonthChange: (Long) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value % 7
    val days = (1..daysInMonth).toList()
    val dayLabels = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    Column {
        MonthHeader(yearMonth = yearMonth, onMonthChange = onMonthChange)
        Spacer(modifier = Modifier.height(16.dp))
        CalendarGrid(dayLabels, firstDayOfMonth, days, moodData)
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onMonthChange: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(-1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Bulan Sebelumnya")
        }
        Text(
            text = "${yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("id"))} ${yearMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onMonthChange(1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Bulan Berikutnya")
        }
    }
}

@Composable
private fun CalendarGrid(
    dayLabels: List<String>,
    firstDayOfMonth: Int,
    days: List<Int>,
    moodData: Map<Int, String>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.heightIn(max = 320.dp),
        userScrollEnabled = false
    ) {
        items(dayLabels) { day ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(text = day, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        items(firstDayOfMonth) { Box {} }

        items(days) { day ->
            val mood = moodData[day]
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(if (mood != null) PrimaryContainer else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (mood != null) {
                    Text(text = mood, style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CalmnessTree(streak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Yard,
                contentDescription = "Pohon Ketenangan",
                modifier = Modifier.size(48.dp),
                tint = Secondary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Pohon Ketenanganmu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (streak > 0) "Kamu telah menulis $streak hari berturut-turut. Terus rawat pikiranmu!"
                    else "Mulai tulis jurnal setiap hari untuk menumbuhkan pohonmu.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun StatisticCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AchievementBadge(title: String, description: String, achieved: Boolean = false) {
    val color = if (achieved) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = if (achieved) 0.3f else 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = "Lencana Pencapaian",
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
