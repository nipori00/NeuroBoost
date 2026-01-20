package keio.nipori.neuroboost.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import keio.nipori.neuroboost.R
import keio.nipori.neuroboost.models.GameResult
import keio.nipori.neuroboost.ui.theme.*

@Composable
fun ResultScreen(
    gameResult: GameResult,
    onBackToHome: () -> Unit,
    onPlayAgain: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.result_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center
            )

            // Encouragement Message
            val messageKey = gameResult.getEncouragementMessage()
            val messageResId = when (messageKey) {
                "result_excellent" -> R.string.result_excellent
                "result_great" -> R.string.result_great
                "result_good" -> R.string.result_good
                else -> R.string.result_keep_practicing
            }
            
            Text(
                text = stringResource(messageResId),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = GoldenYellow,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (gameResult.gameType == "Pathfinder") {
                        StatRow(
                            label = "Time Taken",
                            value = "${gameResult.timeTakenSeconds}s",
                            color = White
                        )
                    } else {
                        // Total Problems
                        StatRow(
                            label = stringResource(R.string.result_problems_solved),
                            value = gameResult.totalProblems.toString(),
                            color = White
                        )

                        Divider(color = White.copy(alpha = 0.2f))

                        // Correct Answers
                        StatRow(
                            label = stringResource(R.string.result_correct_answers),
                            value = gameResult.correctAnswers.toString(),
                            color = CorrectGreen
                        )

                        Divider(color = White.copy(alpha = 0.2f))

                        // Incorrect Answers
                        StatRow(
                            label = stringResource(R.string.result_incorrect_answers),
                            value = gameResult.incorrectAnswers.toString(),
                            color = IncorrectRed
                        )

                        Divider(color = White.copy(alpha = 0.2f))

                        // Accuracy
                        StatRow(
                            label = stringResource(R.string.result_accuracy),
                            value = "${gameResult.accuracy}%",
                            color = AccentBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play Again Button
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.button_play_again),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Back to Home Button
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.button_back_home),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = White.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
