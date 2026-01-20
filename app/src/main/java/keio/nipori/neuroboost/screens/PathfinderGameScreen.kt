package keio.nipori.neuroboost.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.viewmodels.NodeState
import keio.nipori.neuroboost.viewmodels.PathfinderGameViewModel
import keio.nipori.neuroboost.viewmodels.PathNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathfinderGameScreen(
    problemId: String,
    onGameCompleted: (keio.nipori.neuroboost.models.GameResult) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: PathfinderGameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState(initial = null)

    LaunchedEffect(problemId) {
        viewModel.startGame(problemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pathfinder: 1-A-2-B", color = White) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Text(gameState.message, color = White, fontSize = 16.sp)
                 Text("Time: ${gameState.timeElapsed}s", color = White)
            }
            
            // Game Area
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val width = maxWidth
                val height = maxHeight
                
                // Draw Connections Line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (gameState.completedPath.isNotEmpty()) {
                        val path = Path()
                        val first = gameState.completedPath.first()
                        path.moveTo(first.x * size.width, first.y * size.height)
                        
                        for (i in 1 until gameState.completedPath.size) {
                            val node = gameState.completedPath[i]
                            path.lineTo(node.x * size.width, node.y * size.height)
                        }
                        
                        drawPath(
                            path = path,
                            color = Color.Green,
                            style = Stroke(width = 5f)
                        )
                    }
                }
                
                // Draw Nodes
                gameState.nodes.forEach { node ->
                    NodeView(
                        node = node,
                        width = width.value, // Approximate conversion
                        height = height.value, // Approximate conversion
                        onClick = { viewModel.onNodeTap(node) }
                    )
                }
            }
            
            if (gameState.isSolved) {
                Button(
                    onClick = { gameResult?.let { onGameCompleted(it) } },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)
                ) {
                    Text("Finish Level (Time: ${gameState.timeElapsed}s)")
                }
            }
        }
    }
}

@Composable
fun NodeView(node: PathNode, width: Float, height: Float, onClick: () -> Unit) {
    // We use absolute offsets since viewModel uses 0.0-1.0 coords
    // Warning: width/height passed here are in Dp value from BoxWithConstraints
    // We need to assume the relative position works with Dp offsets directly
    
    val xOffset = (node.x * width).dp
    val yOffset = (node.y * height).dp
    
    val bgColor = when(node.state) {
        NodeState.NORMAL -> White
        NodeState.CORRECT -> Color.Green
        NodeState.WRONG -> Color.Red
    }
    
    Box(
        modifier = Modifier
            .offset(x = xOffset - 20.dp, y = yOffset - 20.dp) // Center the 40dp circle
            .size(40.dp)
            .background(bgColor, CircleShape)
            .border(2.dp, Color.Black, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = node.text,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}
