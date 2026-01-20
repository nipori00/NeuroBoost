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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.ui.theme.AccentBlue
import keio.nipori.neuroboost.viewmodels.PuzzleGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleGameScreen(
    problemId: String,
    onGameCompleted: (keio.nipori.neuroboost.models.GameResult) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: PuzzleGameViewModel = viewModel()
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
                title = { Text("Shape Puzzle", color = White) },
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Target Spot (The "Hole")
            // Target Spot (The "Big Object" with a "Hole")
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentBlue) // The "Filled Part" (Big Object) - matches pieces
                    .border(2.dp, White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                 // The "Missing Spot" (Small Hole)
                 Box(modifier = Modifier.size(80.dp)) {
                    ShapeCanvas(shapeIndex = gameState.targetShapeIndex, color = White, isHole = false)
                 }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("What fits in the missing spot?", color = White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))
            
            // Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gameState.options.forEach { shapeId ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(White)
                            .clickable { viewModel.submitAnswer(shapeId) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShapeCanvas(shapeIndex = shapeId, color = AccentBlue, isHole = false)
                    }
                }
            }
        }
    }
}

@Composable
fun ShapeCanvas(shapeIndex: Int, color: Color, isHole: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        val path = Path()
        
        when (shapeIndex) {
            0 -> { // Square
                path.addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
            }
            1 -> { // Circle
                path.addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
            }
            2 -> { // Triangle
                path.moveTo(w/2, 0f)
                path.lineTo(w, h)
                path.lineTo(0f, h)
                path.close()
            }
            3 -> { // Hexagon
                // Approx
                path.moveTo(w*0.25f, 0f)
                path.lineTo(w*0.75f, 0f)
                path.lineTo(w, h*0.5f)
                path.lineTo(w*0.75f, h)
                path.lineTo(w*0.25f, h)
                path.lineTo(0f, h*0.5f)
                path.close()
            }
            // Imperfect Shapes (Indices 10+)
            10 -> { // Notch Top Right
                path.moveTo(0f, 0f)
                path.lineTo(w*0.7f, 0f) // Cut
                path.lineTo(w*0.7f, h*0.3f) // Cut in
                path.lineTo(w, h*0.3f) // Cut out
                path.lineTo(w, h)
                path.lineTo(0f, h)
                path.close()
            }
            11 -> { // Notch Bottom Left
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w, h)
                path.lineTo(h*0.3f, h)
                path.lineTo(h*0.3f, h*0.7f)
                path.lineTo(0f, h*0.7f)
                path.close()
            }
            12 -> { // Notch Top Left
                 path.moveTo(w*0.3f, 0f)
                 path.lineTo(w, 0f)
                 path.lineTo(w, h)
                 path.lineTo(0f, h)
                 path.lineTo(0f, h*0.3f)
                 path.lineTo(w*0.3f, h*0.3f)
                 path.close()
            }
            13 -> { // Notch Bottom Right
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w, h*0.7f)
                path.lineTo(w*0.7f, h*0.7f)
                path.lineTo(w*0.7f, h)
                path.lineTo(0f, h)
                path.close()
            }
            else -> path.addOval(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
        }
        
        drawPath(
            path = path,
            color = color,
            style = if (isHole) Stroke(width = 5f) else Fill
        )
    }
}
