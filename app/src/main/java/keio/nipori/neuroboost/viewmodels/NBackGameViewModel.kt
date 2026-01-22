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

data class NBackState(
    val grid: List<Boolean> = List(9) { false },
    val targetIndex: Int? = null,
    val phase: NBackPhase = NBackPhase.SHOW_A,
    val rounds: Int = 1,
    val maxRounds: Int = 10,
    val score: Int = 0,
    val isGameActive: Boolean = false,
    val message: String = "",
    val firstPos: Int? = null,
    val secondPos: Int? = null
)

enum class NBackPhase {
    SHOW_A,
    SHOW_B,
    INPUT,
    FEEDBACK
}

class NBackGameViewModel(application: Application) : AndroidViewModel(application) {
    private val historyManager = HistoryManager(application)
    private val progressManager = ProgressManager(application)
    
    private val _gameState = MutableStateFlow(NBackState())
    val gameState: StateFlow<NBackState> = _gameState.asStateFlow()
    
    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    
    private var currentProblemId: String? = null
    
    fun startGame(problemId: String) {
        currentProblemId = problemId
        _gameState.value = NBackState(
            isGameActive = true,
            rounds = 1,
            score = 0
        )
        startRound()
    }
    
    private fun startRound() {
        if (_gameState.value.rounds > _gameState.value.maxRounds) {
            endGame()
            return
        }
        
        val pos1 = (0..8).random()
        var pos2 = (0..8).random()
        
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(
                phase = NBackPhase.SHOW_A,
                targetIndex = pos1,
                message = "",
                firstPos = pos1,
                secondPos = pos2
            )
            delay(1000)
            
            _gameState.value = _gameState.value.copy(targetIndex = null)
            delay(200)

            _gameState.value = _gameState.value.copy(
                phase = NBackPhase.SHOW_B,
                targetIndex = pos2
            )
            delay(1000)
            
            _gameState.value = _gameState.value.copy(
                phase = NBackPhase.INPUT,
                targetIndex = null,
                message = "Select where the square FIRST appeared"
            )
        }
    }
    
    fun onGridClicked(index: Int) {
        val currentState = _gameState.value
        if (currentState.phase != NBackPhase.INPUT || !currentState.isGameActive) return
        
        val isCorrect = index == currentState.firstPos
        
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score
        
        _gameState.value = currentState.copy(
            phase = NBackPhase.FEEDBACK,
            message = if (isCorrect) "Correct!" else "Incorrect!",
            score = newScore
        )
        
        viewModelScope.launch {
            delay(1000)
            if (_gameState.value.isGameActive) {
                _gameState.value = _gameState.value.copy(
                    rounds = currentState.rounds + 1
                )
                startRound()
            }
        }
    }
    
    private fun endGame() {
        val currentState = _gameState.value
        _gameState.value = currentState.copy(isGameActive = false, message = "Game Over")
        
        val result = GameResult(
            gameType = "Memory Match",
            totalProblems = currentState.maxRounds,
            correctAnswers = currentState.score,
            incorrectAnswers = currentState.maxRounds - currentState.score,
            timestamp = System.currentTimeMillis()
        )
        _gameResult.value = result
        historyManager.saveGameResult(result)
        
        if (currentProblemId != null && currentState.score >= (currentState.maxRounds * 0.7).toInt()) {
             progressManager.markProblemSolved(currentProblemId!!)
        }
    }
}
