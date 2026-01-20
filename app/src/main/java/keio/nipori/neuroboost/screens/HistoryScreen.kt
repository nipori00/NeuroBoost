package keio.nipori.neuroboost.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import keio.nipori.neuroboost.models.GameResult
import keio.nipori.neuroboost.ui.theme.AccentBlue
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.utils.HistoryManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val historyManager = remember { HistoryManager(context) }
    // Reverse list (already localized in manager, but ensure order)
    // Actually manager adds to top (index 0), so it is already newest first.
    val history = remember { historyManager.getHistory() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance History", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        },
        containerColor = DarkBlue
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No games played yet.", color = White.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { result ->
                    GameResultItem(result)
                }
            }
        }
    }
}

@Composable
fun GameResultItem(result: GameResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.gameType,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(result.timestamp)),
                    fontSize = 12.sp,
                    color = White.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (result.gameType == "Pathfinder") {
                Text("Time Taken: ${result.timeTakenSeconds}s", color = White)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                     Column {
                         Text("Score / Problems", fontSize = 12.sp, color = White.copy(alpha = 0.6f))
                         Text("${result.correctAnswers} / ${result.totalProblems}", fontWeight = FontWeight.Bold, color = White)
                     }
                     Column {
                         Text("Accuracy", fontSize = 12.sp, color = White.copy(alpha = 0.6f))
                         Text("${result.accuracy}%", fontWeight = FontWeight.Bold, color = White)
                     }
                }
            }
        }
    }
}
