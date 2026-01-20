package keio.nipori.neuroboost.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import keio.nipori.neuroboost.models.GameResult
import keio.nipori.neuroboost.utils.HistoryManager
import keio.nipori.neuroboost.utils.ProgressManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Internal state for Puzzle Game
data class PuzzleState(
    val currentShapeIndices: List<Int> = emptyList(), // Indices representing shapes
    val correctShapeIndex: Int = 0, // The index of the shape that fits
    val targetShapeIndex: Int = 0, // The "hole" shape index
    val options: List<Int> = emptyList(),
    val totalProblems: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val timeRemaining: Int = 30,
    val isGameActive: Boolean = false,
    val message: String = ""
)

class PuzzleGameViewModel(application: Application) : AndroidViewModel(application) {
    private val historyManager = HistoryManager(application)
    private val progressManager = ProgressManager(application)
    
    private val _gameState = MutableStateFlow(PuzzleState())
    val gameState: StateFlow<PuzzleState> = _gameState.asStateFlow()
    
    // Add _gameResult if it's missing or re-add it cleanly
    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentProblemId: String? = null

    // Shape definitions (conceptual, mapped to UI by index)
    // 0: Perfect Square
    // 1: Circle
    // 2: Triangle
    // ...
    
    fun startGame(problemId: String) {
        currentProblemId = problemId
        _gameState.value = PuzzleState(
            isGameActive = true,
            timeRemaining = 30,
            message = ""
        )
        _gameResult.value = null
        generateNewProblem()
        startTimer()
    }
    
    private fun generateNewProblem() {
        // Shapes logic:
        // Target is simple shape (0, 1, 2)
        val targetId = (0..2).random() 
        val correctKeyId = targetId
        
        // Options: The 3 basic shapes
        val options = listOf(0, 1, 2).shuffled()
        
        _gameState.value = _gameState.value.copy(
            targetShapeIndex = targetId,
            options = options,
            correctShapeIndex = correctKeyId
        )
    }
    
    fun submitAnswer(selectedShapeIndex: Int) {
        val currentState = _gameState.value
        if (!currentState.isGameActive) return
        
        val isCorrect = selectedShapeIndex == currentState.correctShapeIndex
        
        if (isCorrect) {
             _gameState.value = currentState.copy(
                 correctCount = currentState.correctCount + 1,
                 totalProblems = currentState.totalProblems + 1,
                 message = "Correct!"
             )
        } else {
             _gameState.value = currentState.copy(
                 incorrectCount = currentState.incorrectCount + 1,
                 totalProblems = currentState.totalProblems + 1,
                 message = "Wrong!"
             )
        }
        
        if (currentState.isGameActive) {
            generateNewProblem()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value.timeRemaining > 0 && _gameState.value.isGameActive) {
                delay(1000)
                val newTime = _gameState.value.timeRemaining - 1
                _gameState.value = _gameState.value.copy(timeRemaining = newTime)
                
                if (newTime <= 0) {
                    endGame()
                }
            }
        }
    }
    
    private fun endGame() {
        val currentState = _gameState.value
        _gameState.value = currentState.copy(isGameActive = false, message = "Time's Up!")
        
        val result = GameResult(
            gameType = "Shape Puzzle",
            totalProblems = currentState.totalProblems,
            correctAnswers = currentState.correctCount,
            incorrectAnswers = currentState.incorrectCount,
            timestamp = System.currentTimeMillis()
        )
        
        _gameResult.value = result
        historyManager.saveGameResult(result)
        
        if (currentProblemId != null && currentState.correctCount > currentState.incorrectCount && currentState.correctCount > 5) {
             progressManager.markProblemSolved(currentProblemId!!)
        }
        
        timerJob?.cancel()
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
