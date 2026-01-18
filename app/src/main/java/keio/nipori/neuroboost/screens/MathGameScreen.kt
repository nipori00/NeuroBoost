package keio.nipori.neuroboost.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import keio.nipori.neuroboost.R
import keio.nipori.neuroboost.models.GameState
import keio.nipori.neuroboost.ui.theme.*

@Composable
fun MathGameScreen(
    gameState: GameState,
    onAnswerSelected: (Int) -> Unit
) {
    val currentProblem = gameState.currentProblem

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Timer and Score
            Column {
                // Score Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Correct Score
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.label_correct),
                                fontSize = 14.sp,
                                color = CorrectGreen
                            )
                            Text(
                                text = "${gameState.correctCount}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = CorrectGreen
                            )
                        }
                    }

                    // Incorrect Score
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.label_incorrect),
                                fontSize = 14.sp,
                                color = IncorrectRed
                            )
                            Text(
                                text = "${gameState.incorrectCount}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncorrectRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer - Centered
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.wrapContentWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceDark
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 40.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = "Timer",
                                    tint = White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.label_time_remaining),
                                    fontSize = 16.sp,
                                    color = White.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "${gameState.timeRemaining}",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gameState.timeRemaining <= 10) IncorrectRed else White
                            )
                            Text(
                                text = stringResource(R.string.label_seconds),
                                fontSize = 14.sp,
                                color = White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

            }

            // Middle Section: Problem Display
            currentProblem?.let { problem ->
                AnimatedContent(
                    targetState = problem,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                    },
                    label = "problem_animation"
                ) { animatedProblem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Text(
                            text = animatedProblem.getQuestionText(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp)
                        )
                    }
                }
            }

            // Bottom Section: Answer Options
            currentProblem?.let { problem ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    problem.options.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowOptions.forEach { option ->
                                Button(
                                    onClick = { onAnswerSelected(option) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(4.dp)
                                ) {
                                    Text(
                                        text = option.toString(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
