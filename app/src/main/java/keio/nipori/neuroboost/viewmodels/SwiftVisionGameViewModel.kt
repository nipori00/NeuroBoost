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
import kotlin.random.Random

enum class CentralObject { CAR, TRUCK }
enum class PeripheralLocation { TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT }

data class SwiftVisionState(
    val centralObject: CentralObject? = null,
    val peripheralLocation: PeripheralLocation? = null,
    val phase: SwiftVisionPhase = SwiftVisionPhase.SHOW, // SHOW -> INPUT -> FEEDBACK
    val totalProblems: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val timeRemaining: Int = 30,
    val isGameActive: Boolean = false,
    val message: String = ""
)

enum class SwiftVisionPhase {
    SHOW,
    WAIT, // Blank screen ("empty background" as requested)
    INPUT_CENTER,
    INPUT_PERIPHERAL,
    FEEDBACK
}

class SwiftVisionGameViewModel(application: Application) : AndroidViewModel(application) {
    private val historyManager = HistoryManager(application)
    private val progressManager = ProgressManager(application)
    
    private val _gameState = MutableStateFlow(SwiftVisionState())
    val gameState: StateFlow<SwiftVisionState> = _gameState.asStateFlow()
    
    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentProblemId: String? = null
    private var currentCentral: CentralObject = CentralObject.CAR
    private var currentPeripheral: PeripheralLocation = PeripheralLocation.TOP
    
    fun startGame(problemId: String) {
        currentProblemId = problemId
        _gameState.value = SwiftVisionState(
            isGameActive = true,
            timeRemaining = 30
        )
        generateNewProblem()
        startTimer()
    }
    
    private fun generateNewProblem() {
        currentCentral = CentralObject.values().random()
        currentPeripheral = PeripheralLocation.values().random()
        
        viewModelScope.launch {
            // SHOW
            _gameState.value = _gameState.value.copy(
                phase = SwiftVisionPhase.SHOW,
                centralObject = currentCentral,
                peripheralLocation = currentPeripheral,
                message = ""
            )
            // Show for short duration (e.g. 500ms? Swift vision is usually fast)
            delay(500)
            
            // WAIT (Empty background)
            _gameState.value = _gameState.value.copy(
                phase = SwiftVisionPhase.WAIT,
                centralObject = null,
                peripheralLocation = null
            )
            delay(1000) // "Dummy screen" removed, now just blank wait.
            
            // INPUT 1: Center
            _gameState.value = _gameState.value.copy(
                phase = SwiftVisionPhase.INPUT_CENTER,
                message = "What was in the center?"
            )
        }
    }
    
    fun submitCenter(obj: CentralObject) {
         if (_gameState.value.phase != SwiftVisionPhase.INPUT_CENTER) return
         
         if (obj == currentCentral) {
             _gameState.value = _gameState.value.copy(phase = SwiftVisionPhase.INPUT_PERIPHERAL, message = "Where was the dot?")
         } else {
             handleResult(false)
         }
    }
    
    fun submitPeripheral(loc: PeripheralLocation) {
        if (_gameState.value.phase != SwiftVisionPhase.INPUT_PERIPHERAL) return
        
        if (loc == currentPeripheral) {
            handleResult(true)
        } else {
            handleResult(false)
        }
    }
    
    private fun handleResult(isCorrect: Boolean) {
        val currentState = _gameState.value
        if (!currentState.isGameActive) return
        
        if (isCorrect) {
             _gameState.value = currentState.copy(
                 correctCount = currentState.correctCount + 1,
                 totalProblems = currentState.totalProblems + 1,
                 phase = SwiftVisionPhase.FEEDBACK,
                 message = "Correct!"
             )
        } else {
             _gameState.value = currentState.copy(
                 incorrectCount = currentState.incorrectCount + 1,
                 totalProblems = currentState.totalProblems + 1,
                 phase = SwiftVisionPhase.FEEDBACK,
                 message = "Wrong!"
             )
        }
        
        viewModelScope.launch {
            delay(1000)
            if (_gameState.value.isGameActive) {
                generateNewProblem()
            }
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
            gameType = "Swift Vision",
            totalProblems = currentState.totalProblems,
            correctAnswers = currentState.correctCount,
            incorrectAnswers = currentState.incorrectCount,
            timestamp = System.currentTimeMillis()
        )
        _gameResult.value = result
        historyManager.saveGameResult(result)
        
        if (currentProblemId != null && currentState.correctCount > currentState.incorrectCount && currentState.correctCount > 0) {
             progressManager.markProblemSolved(currentProblemId!!)
        }
        
        timerJob?.cancel()
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
