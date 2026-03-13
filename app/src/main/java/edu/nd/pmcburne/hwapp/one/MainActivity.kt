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
import androidx.compose.foundation.layout.fillMaxWidth
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
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "ncaa-db"
        ).build()

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
                // TODO: add date picker button
                // TODO: add switch button for gender
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyColumn {
                items(games) { game ->
                    GameCard(game)
                }
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
                ScoreRow(teamName = game.awayTeam, score = game.awayScore)
                ScoreRow(teamName = game.homeTeam, score = game.homeScore)
            }

            Column(horizontalAlignment = Alignment.End) {
                val displayStatus = when (game.status.lowercase()) {
                    "pre" -> game.startTime ?: "TBD"
                    "final" -> "Final"
                    "live" -> "${game.clock ?: ""} ${getPeriodText(game.period, game.gender)}"
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
    return when (gender.lowercase()) {
        "men" -> when (period) {
            1 -> "1st Half"
            2 -> "2nd Half"
            else -> "OT$ {period - 2}"
        }
        "women" -> when (period) {
            in 1..4 -> "Q$period"
            else -> "OT${period - 4}"
        }
        else -> "P$period"
    }
}