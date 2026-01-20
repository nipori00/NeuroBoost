package keio.nipori.neuroboost.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import keio.nipori.neuroboost.ui.theme.AccentBlue
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.viewmodels.NBackGameViewModel
import keio.nipori.neuroboost.viewmodels.NBackPhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NBackGameScreen(
    problemId: String,
    onGameCompleted: (keio.nipori.neuroboost.models.GameResult) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: NBackGameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState(initial = null)

    LaunchedEffect(problemId) {
        viewModel.startGame(problemId)
    }
    
    LaunchedEffect(gameResult) {
        gameResult?.let { result ->
            onGameCompleted(result)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Match", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        },
        containerColor = DarkBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Round: ${gameState.rounds}/${gameState.maxRounds}", color = White, fontSize = 20.sp)
                Text("Score: ${gameState.score}", color = White, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // 3x3 Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val isTarget = index == gameState.targetIndex
                            
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = if (isTarget) AccentBlue else White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(2.dp, White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (gameState.phase == NBackPhase.INPUT) {
                                            viewModel.onGridClicked(index)
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Fixed height container for message to prevent layout shift
            Box(
                 modifier = Modifier.height(72.dp),
                 contentAlignment = Alignment.Center
            ) {
                Text(gameState.message, color = White, fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
