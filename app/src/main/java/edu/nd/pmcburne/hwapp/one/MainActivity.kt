package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.room.Room
import edu.nd.pmcburne.hwapp.one.data.database.AppDatabase
import edu.nd.pmcburne.hwapp.one.data.database.GameEntity
import edu.nd.pmcburne.hwapp.one.data.repository.GameRepo
import java.time.LocalDate
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ncaa-db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val dao = db.gameDao()

        val repository = GameRepo(dao)

        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                ScoreboardScreen(repository)
            }
        }
    }
}

@Composable
fun ScoreboardScreen(repository: GameRepo) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var gender by remember { mutableStateOf("men") }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val dateString = selectedDate.toString()
    val games by repository.getGames(dateString, gender).collectAsState(initial = emptyList())

    LaunchedEffect(selectedDate, gender) {
        isLoading = true
        repository.refreshGames(
            gender,
            selectedDate.year.toString(),
            selectedDate.monthValue.toString().padStart(2, '0'),
            selectedDate.dayOfMonth.toString().padStart(2, '0')
        )
        isLoading = false
    }

    Scaffold(
        topBar = {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Date: ${selectedDate.format(DateTimeFormatter.ofLocalizedDate(
                            FormatStyle.MEDIUM))}")
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { gender = "men" },
                            modifier = Modifier.weight(1f),
                            colors = if (gender == "men") ButtonDefaults.buttonColors()
                            else ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Men's")
                        }

                        Button(
                            onClick = { gender = "women" },
                            modifier = Modifier.weight(1f),
                            colors = if (gender == "women") ButtonDefaults.buttonColors()
                            else ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text("Women's")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
//            if (isLoading) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            }

            if (games.isNotEmpty()) {
                LazyColumn {
                    items(games) { game ->
                        GameCard(game)
                    }
                }
            }
            else if (!isLoading) {
                Text(
                    text = "No game data available.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun GameCard(game: GameEntity) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ScoreRow(teamName = "Away: ${game.awayTeam}", score = game.awayScore)
                ScoreRow(teamName = "Home: ${game.homeTeam}", score = game.homeScore)
            }

            Column(horizontalAlignment = Alignment.End) {
                val displayStatus = when (game.status.lowercase()) {
                    "pre" -> "Upcoming: ${game.startTime ?: "TBD"}"
                    "final" -> "Final"
                    "live" -> {
                        "Currently Playing: ${game.clock ?: ""} ${game.period}"
                    }
                    else -> game.status
                }

                Text(
                    text = displayStatus,
                    style =  MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                if (game.status.lowercase() == "final" && game.winner != null) {
                    Text(
                        text = "Winner: ${game.winner}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF388E3C)
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreRow(teamName: String, score: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 100.dp)
        )
        Text(
            text = score?.toString() ?: "-",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

fun getPeriodText(period: Int?, gender: String): String {
    if (period == null) return ""
    return when {
        gender.lowercase().contains("men") -> when (period) {
            1 -> "1st Half"
            2 -> "2nd Half"
            else -> "OT${period - 2}"
        }
        gender.lowercase().contains("women") -> when (period) {
            in 1..4 -> "Q$period"
            else -> "OT${period - 4}"
        }
        else -> "P$period"
    }
}