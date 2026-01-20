package keio.nipori.neuroboost.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.ui.theme.AccentBlue
import keio.nipori.neuroboost.viewmodels.CardGameViewModel
import keio.nipori.neuroboost.viewmodels.CardItem
import keio.nipori.neuroboost.viewmodels.CardShape
import keio.nipori.neuroboost.viewmodels.CardColor
import keio.nipori.neuroboost.viewmodels.GamePhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardGameScreen(
    problemId: String,
    onGameCompleted: (keio.nipori.neuroboost.models.GameResult) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: CardGameViewModel = viewModel()
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
                title = { Text("Memory Sequence", color = White) },
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
                Text("Time: ${gameState.timeRemaining}s", color = White, fontSize = 20.sp)
                Text("Score: ${gameState.correctCount}", color = White, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(gameState.message, color = White, fontSize = 24.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Display Sequence Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gameState.phase == GamePhase.MEMORIZE) {
                    gameState.sequence.forEach { card ->
                        CardView(card)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                } else if (gameState.phase == GamePhase.RECALL || gameState.phase == GamePhase.FEEDBACK) {
                    // Show placeholders or user choices?
                    // Let's show empty slots getting filled
                    gameState.sequence.indices.forEach { index ->
                        if (index < gameState.userSequence.size) {
                            CardView(gameState.userSequence[index])
                        } else {
                            // Empty slot
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(2.dp, White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Options
            if (gameState.phase == GamePhase.RECALL) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    gameState.options.forEach { option ->
                        CardView(option, onClick = { viewModel.onOptionSelected(option) })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun CardView(card: CardItem, onClick: (() -> Unit)? = null) {
    val color = when(card.color) {
        CardColor.RED -> Color(0xFFEF5350)
        CardColor.GREEN -> Color(0xFF66BB6A)
        CardColor.BLUE -> Color(0xFF42A5F5)
        CardColor.YELLOW -> Color(0xFFFFEE58)
    }
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .run {
                if (onClick != null) clickable { onClick() } else this
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path()
            val w = size.width
            val h = size.height
            
            when(card.shape) {
                CardShape.SQUARE -> {
                    path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
                }
                CardShape.CIRCLE -> {
                    path.addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
                }
                CardShape.TRIANGLE -> {
                    path.moveTo(w/2, 0f)
                    path.lineTo(w, h)
                    path.lineTo(0f, h)
                    path.close()
                }
            }
            
            drawPath(path, color)
        }
    }
}
