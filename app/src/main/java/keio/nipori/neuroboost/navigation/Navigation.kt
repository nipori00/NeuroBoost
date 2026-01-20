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
import keio.nipori.neuroboost.models.GameResult
import keio.nipori.neuroboost.models.GameState

sealed class Screen(val route: String) {
    object Title : Screen("title")
    object MathGame : Screen("math_game")
    object Result : Screen("result")
    object History : Screen("history")
    object AllProblems : Screen("all_problems")
    object PuzzleGame : Screen("puzzle_game/{problemId}") {
        fun createRoute(problemId: String) = "puzzle_game/$problemId"
    }
    object CardGame : Screen("card_game/{problemId}") {
        fun createRoute(problemId: String) = "card_game/$problemId"
    }
    object NBackGame : Screen("nback_game/{problemId}") {
        fun createRoute(problemId: String) = "nback_game/$problemId"
    }
    object SwiftVisionGame : Screen("swift_vision_game/{problemId}") {
        fun createRoute(problemId: String) = "swift_vision_game/$problemId"
    }
    object PathfinderGame : Screen("pathfinder_game/{problemId}") {
        fun createRoute(problemId: String) = "pathfinder_game/$problemId"
    }
}

@Composable
fun NeuroBoostNavigation(
    navController: NavHostController,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val mathGameViewModel: MathGameViewModel = viewModel()
    val mathGameState by mathGameViewModel.gameState.collectAsState()
    val mathGameResult by mathGameViewModel.gameResult.collectAsState(initial = null)
    
    // Shared state for displaying results from any game
    var sharedGameResult by remember { mutableStateOf<GameResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Title.route
    ) {
        composable(Screen.Title.route) {
            TitleScreen(
                onStartClick = { problem ->
                    // Logic to navigate to specific game based on problem type
                    when (problem.type) {
                        keio.nipori.neuroboost.models.ProblemType.MATH -> {
                            mathGameViewModel.startGame(problem.id)
                            navController.navigate(Screen.MathGame.route)
                        }
                        keio.nipori.neuroboost.models.ProblemType.PUZZLE -> {
                            navController.navigate(Screen.PuzzleGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.CARD -> {
                            navController.navigate(Screen.CardGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.NBACK -> {
                            navController.navigate(Screen.NBackGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.SWIFT_VISION -> {
                            navController.navigate(Screen.SwiftVisionGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.PATHFINDER -> {
                            navController.navigate(Screen.PathfinderGame.createRoute(problem.id))
                        }
                    }
                },
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                },
                onAllProblemsClick = {
                    navController.navigate(Screen.AllProblems.route)
                },
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }
        
        composable(Screen.History.route) {
            keio.nipori.neuroboost.screens.HistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AllProblems.route) {
            keio.nipori.neuroboost.screens.AllProblemsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onProblemClick = { problem ->
                    when (problem.type) {
                        keio.nipori.neuroboost.models.ProblemType.MATH -> {
                            mathGameViewModel.startGame(problem.id)
                            navController.navigate(Screen.MathGame.route)
                        }
                        keio.nipori.neuroboost.models.ProblemType.PUZZLE -> {
                            navController.navigate(Screen.PuzzleGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.CARD -> {
                            navController.navigate(Screen.CardGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.NBACK -> {
                            navController.navigate(Screen.NBackGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.SWIFT_VISION -> {
                            navController.navigate(Screen.SwiftVisionGame.createRoute(problem.id))
                        }
                        keio.nipori.neuroboost.models.ProblemType.PATHFINDER -> {
                            navController.navigate(Screen.PathfinderGame.createRoute(problem.id))
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.PuzzleGame.route,
            arguments = listOf(androidx.navigation.navArgument("problemId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId") ?: ""
            keio.nipori.neuroboost.screens.PuzzleGameScreen(
                problemId = problemId,
                onGameCompleted = { result ->
                    sharedGameResult = result
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.PuzzleGame.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.CardGame.route,
             arguments = listOf(androidx.navigation.navArgument("problemId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId") ?: ""
            keio.nipori.neuroboost.screens.CardGameScreen(
                problemId = problemId,
                onGameCompleted = { result ->
                    sharedGameResult = result
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.CardGame.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.NBackGame.route,
            arguments = listOf(androidx.navigation.navArgument("problemId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId") ?: ""
            keio.nipori.neuroboost.screens.NBackGameScreen(
                problemId = problemId,
                onGameCompleted = { result ->
                    sharedGameResult = result
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.NBackGame.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.SwiftVisionGame.route,
            arguments = listOf(androidx.navigation.navArgument("problemId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId") ?: ""
            keio.nipori.neuroboost.screens.SwiftVisionGameScreen(
                problemId = problemId,
                onGameCompleted = { result ->
                    sharedGameResult = result
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.SwiftVisionGame.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.PathfinderGame.route,
            arguments = listOf(androidx.navigation.navArgument("problemId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val problemId = backStackEntry.arguments?.getString("problemId") ?: ""
            keio.nipori.neuroboost.screens.PathfinderGameScreen(
                problemId = problemId,
                onGameCompleted = { result ->
                    sharedGameResult = result
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.PathfinderGame.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MathGame.route) {
            // Navigate to result when game ends (Using shared mechanism or keeping explicit)
            // Keeping explicit observation for MathGame for now as it uses ViewModel scoped to this NavGraph builder (or rather, scoped above)
            // But we should unify. Use LaunchedEffect here to set shared Result.
            
            LaunchedEffect(mathGameResult) {
                if (mathGameResult != null) {
                    sharedGameResult = mathGameResult
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.MathGame.route) { inclusive = true }
                    }
                }
            }

            MathGameScreen(
                gameState = mathGameState,
                onAnswerSelected = { answer ->
                    mathGameViewModel.submitAnswer(answer)
                }
            )
        }

        composable(Screen.Result.route) {
            sharedGameResult?.let { result ->
                ResultScreen(
                    gameResult = result,
                    onBackToHome = {
                        mathGameViewModel.resetGame() // Safe to call? Yes
                        // Also clear shared result?
                        sharedGameResult = null
                        navController.navigate(Screen.Title.route) {
                            popUpTo(Screen.Title.route) { inclusive = true }
                        }
                    },
                    onPlayAgain = {
                        // For Math game we can restart. For others, we just go to Title for now.
                        if (result.gameType == "Math Challenge") {
                             mathGameViewModel.startGame()
                             navController.navigate(Screen.MathGame.route) {
                                 popUpTo(Screen.Result.route) { inclusive = true }
                             }
                        } else {
                             // Default fallback
                             sharedGameResult = null
                             navController.navigate(Screen.Title.route)
                        }
                    }
                )
            }
        }
    }
}
