package com.psy.deardiary.features.growth // Pastikan package ini sudah benar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.ui.theme.DearDiaryTheme
import com.psy.deardiary.ui.theme.Primary
import com.psy.deardiary.ui.theme.PrimaryContainer
import com.psy.deardiary.ui.theme.Secondary
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    viewModel: GrowthViewModel = hiltViewModel() // Pastikan ini mengacu pada GrowthViewModel
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

                // Pohon Ketenangan
                CalmnessTree(streak = state.writingStreak)

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                // Kalender Mood
                MoodCalendar(
                    yearMonth = state.currentDisplayMonth,
                    moodData = state.moodCalendarData,
                    onMonthChange = { offset -> viewModel.changeDisplayMonth(offset) }
                )

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                // Tren Emosi (masih dummy untuk visual)
                MoodTrendChart()

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                // Statistik
                Text("Statistik Kamu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pastikan ini memanggil fungsi StatisticCard yang sama
                    StatisticCard(title = "Total Jurnal", value = state.totalJournals.toString(), modifier = Modifier.weight(1f))
                    StatisticCard(title = "Runtutan Menulis", value = "${state.writingStreak} hari", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                StatisticCard(title = "Mood Paling Sering", value = state.mostFrequentMood, modifier = Modifier.fillMaxWidth())

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                // Pencapaian
                Text("Pencapaian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                // Pastikan ini memanggil fungsi AchievementBadge yang sama
                AchievementBadge(title = "Refleksi Pertama", description = "Memulai perjalanan menulismu.")
                AchievementBadge(title = "Penulis 7 Hari", description = "Menulis jurnal selama 7 hari berturut-turut.", achieved = state.writingStreak >= 7)
            }
        }
    }
}

// ================================================================
// Fungsi-fungsi pembantu yang hanya perlu didefinisikan SATU KALI
// ================================================================

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
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale("id"))} ${yearMonth.year}",
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
        Spacer(modifier = Modifier.height(8.dp)) // Menambahkan spacer di sini
        Text(
            text = "Tulis jurnal pertamamu untuk mulai melihat bagaimana kamu tumbuh dan mencapai tujuanmu di sini.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun MoodTrendChart() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tren Emosi Bulan Ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(Color.LightGray, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), pathEffect = pathEffect)
            drawLine(Color.LightGray, start = Offset(0f, 0f), end = Offset(size.width, 0f))
            drawLine(Color.LightGray, start = Offset(0f, size.height), end = Offset(size.width, size.height))
            val points = listOf(0.2f, 0.5f, 0.3f, 0.8f, 0.6f, 0.7f, 0.4f).mapIndexed { index, value ->
                Offset(x = size.width / 6 * index, y = size.height * (1 - value))
            }
            for (i in 0 until points.size - 1) {
                drawLine(Primary, start = points[i], end = points[i+1], strokeWidth = 5f)
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
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrowthScreenPreview() {
    DearDiaryTheme {
        GrowthScreen()
    }
}