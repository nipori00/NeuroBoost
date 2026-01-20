package keio.nipori.neuroboost.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import keio.nipori.neuroboost.ui.theme.AccentBlue
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.viewmodels.CentralObject
import keio.nipori.neuroboost.viewmodels.PeripheralLocation
import keio.nipori.neuroboost.viewmodels.SwiftVisionGameViewModel
import keio.nipori.neuroboost.viewmodels.SwiftVisionPhase
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwiftVisionGameScreen(
    problemId: String,
    onGameCompleted: (keio.nipori.neuroboost.models.GameResult) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: SwiftVisionGameViewModel = viewModel()
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
                title = { Text("Swift Vision", color = White) },
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
            // Stats (Top Left/Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time: ${gameState.timeRemaining}s", color = White, fontSize = 20.sp)
                Text("Score: ${gameState.correctCount}", color = White, fontSize = 20.sp)
            }

            // Game Area (Center)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp), // Push down below stats
                contentAlignment = Alignment.Center
            ) {
                if (gameState.phase == SwiftVisionPhase.SHOW) {
                    // Central Object
                    val icon = if (gameState.centralObject == CentralObject.CAR) Icons.Default.DirectionsCar else Icons.Default.LocalShipping
                    Icon(
                        imageVector = icon,
                        contentDescription = "Center Object",
                        tint = White,
                        modifier = Modifier.size(60.dp)
                    )
                    
                    // Peripheral Dot
                    // Dot needs to be "farther from center".
                    // Let's use a large radius relative to screen, likely via Canvas.
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = center
                        // Use 80% of min dimension radius
                        val radius = (size.minDimension / 2) * 0.8f
                        
                        val angle = when(gameState.peripheralLocation) {
                            PeripheralLocation.TOP -> -90.0
                            PeripheralLocation.TOP_RIGHT -> -45.0
                            PeripheralLocation.RIGHT -> 0.0
                            PeripheralLocation.BOTTOM_RIGHT -> 45.0
                            PeripheralLocation.BOTTOM -> 90.0
                            PeripheralLocation.BOTTOM_LEFT -> 135.0
                            PeripheralLocation.LEFT -> 180.0
                            PeripheralLocation.TOP_LEFT -> 225.0
                            null -> 0.0
                        }
                        
                        val rad = Math.toRadians(angle)
                        val dotX = center.x + radius * cos(rad).toFloat()
                        val dotY = center.y + radius * sin(rad).toFloat()
                        
                        drawCircle(
                            color = AccentBlue,
                            radius = 15.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }
                } else if (gameState.phase == SwiftVisionPhase.INPUT_CENTER) {
                    // Central Input Dialog (Overlay)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("What was in the center?", color = White, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            // Car Button
                            IconButton(
                                onClick = { viewModel.submitCenter(CentralObject.CAR) },
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(White.copy(alpha=0.1f), CircleShape)
                                    .border(2.dp, White, CircleShape)
                            ) {
                                Icon(Icons.Default.DirectionsCar, "Car", tint = White, modifier = Modifier.size(40.dp))
                            }
                            
                            // Truck Button
                            IconButton(
                                onClick = { viewModel.submitCenter(CentralObject.TRUCK) },
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(White.copy(alpha=0.1f), CircleShape)
                                    .border(2.dp, White, CircleShape)
                            ) {
                                Icon(Icons.Default.LocalShipping, "Truck", tint = White, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                } else if (gameState.phase == SwiftVisionPhase.INPUT_PERIPHERAL) {
                    // Peripheral Input (Radial buttons)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("Where was the dot?", color = White, fontSize = 24.sp, modifier = Modifier.align(Alignment.Center))
                         
                         // 8 Buttons
                         PeripheralLocation.values().forEach { loc ->
                            val angle = when(loc) {
                                PeripheralLocation.TOP -> 270.0
                                PeripheralLocation.TOP_RIGHT -> 315.0
                                PeripheralLocation.RIGHT -> 0.0
                                PeripheralLocation.BOTTOM_RIGHT -> 45.0
                                PeripheralLocation.BOTTOM -> 90.0
                                PeripheralLocation.BOTTOM_LEFT -> 135.0
                                PeripheralLocation.LEFT -> 180.0
                                PeripheralLocation.TOP_LEFT -> 225.0
                            }
                            // Convert standard angle (Right=0) to visual placement.
                            // In Canvas logic above: TOP was -90. Math.cos(-90) = 0, sin(-90) = -1. Correct (Up).
                            // Here we use Layout modification (offset).
                            // Let's use BoxWithConstraints or just static offsets? Static is hard on different screens.
                            // Better: Alignment with biases? Or just a box with absolute offsets from center calculated in px?
                            
                            // Let's use a Box with Modifier.align + offset, but align pushes to edges. 
                            // Easier: Box(Modifier.fillMaxSize()) { Box(Modifier.align(BiasAlignment(x,y))) }
                            
                            val rad = Math.toRadians(angle)
                            val biasX = cos(rad).toFloat() * 0.8f // 0.8 to give some padding
                            val biasY = sin(rad).toFloat() * 0.8f
                            
                            Box(modifier = Modifier.align(androidx.compose.ui.BiasAlignment(biasX, biasY))) {
                                Button(
                                    onClick = { viewModel.submitPeripheral(loc) },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = White.copy(alpha = 0.2f)),
                                    modifier = Modifier.size(50.dp)
                                ) {}
                            }
                         }
                    }
                }
                
                // Feedback overlay
                if (gameState.phase == SwiftVisionPhase.FEEDBACK) {
                     Text(gameState.message, color = White, fontSize = 32.sp)
                }
            }
        }
    }
}
