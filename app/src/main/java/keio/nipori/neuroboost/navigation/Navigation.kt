package keio.nipori.neuroboost.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import keio.nipori.neuroboost.screens.MathGameScreen
import keio.nipori.neuroboost.screens.ResultScreen
import keio.nipori.neuroboost.screens.TitleScreen
import keio.nipori.neuroboost.viewmodels.MathGameViewModel

sealed class Screen(val route: String) {
    object Title : Screen("title")
    object MathGame : Screen("math_game")
    object Result : Screen("result")
}

@Composable
fun NeuroBoostNavigation(
    navController: NavHostController,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val mathGameViewModel: MathGameViewModel = viewModel()
    val gameState by mathGameViewModel.gameState.collectAsState()
    val gameResult by mathGameViewModel.gameResult.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Title.route
    ) {
        composable(Screen.Title.route) {
            TitleScreen(
                onStartClick = {
                    mathGameViewModel.startGame()
                    navController.navigate(Screen.MathGame.route) {
                        popUpTo(Screen.Title.route) { inclusive = false }
                    }
                },
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }

        composable(Screen.MathGame.route) {
            // Navigate to result when game ends
            LaunchedEffect(gameResult) {
                if (gameResult != null) {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.MathGame.route) { inclusive = true }
                    }
                }
            }

            MathGameScreen(
                gameState = gameState,
                onAnswerSelected = { answer ->
                    mathGameViewModel.submitAnswer(answer)
                }
            )
        }

        composable(Screen.Result.route) {
            gameResult?.let { result ->
                ResultScreen(
                    gameResult = result,
                    onBackToHome = {
                        mathGameViewModel.resetGame()
                        navController.navigate(Screen.Title.route) {
                            popUpTo(Screen.Title.route) { inclusive = true }
                        }
                    },
                    onPlayAgain = {
                        mathGameViewModel.startGame()
                        navController.navigate(Screen.MathGame.route) {
                            popUpTo(Screen.Result.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
