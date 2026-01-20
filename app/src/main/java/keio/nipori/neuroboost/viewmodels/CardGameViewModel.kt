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

// Card Shape Types
enum class CardShape { SQUARE, CIRCLE, TRIANGLE }
enum class CardColor { RED, GREEN, BLUE, YELLOW }

data class CardItem(
    val id: Int,
    val shape: CardShape,
    val color: CardColor
)

data class CardGameState(
    val sequence: List<CardItem> = emptyList(),
    val options: List<CardItem> = emptyList(),
    val currentStep: Int = 0, // Which item in the sequence we are asking for?
    // Actually, usually Memory Sequence shows a sequence, then asks to repeat it.
    // Simplifying for "30s continuous":
    // 1. Show a sequence (length 3, 4, 5...) for X seconds.
    // 2. Hide.
    // 3. User taps options to reconstruct.
    // 4. If correct, +Score, Next problem.
    // Let's stick to sequence length 3 for speed in 30s mode, or adaptive.
    
    val phase: GamePhase = GamePhase.MEMORIZE, // MEMORIZE -> RECALL -> FEEDBACK
    val userSequence: MutableList<CardItem> = mutableListOf(),
    
    val totalProblems: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val timeRemaining: Int = 30,
    val isGameActive: Boolean = false,
    val message: String = ""
)

enum class GamePhase { MEMORIZE, RECALL, FEEDBACK }

class CardGameViewModel(application: Application) : AndroidViewModel(application) {
    private val historyManager = HistoryManager(application)
    private val progressManager = ProgressManager(application)
    
    private val _gameState = MutableStateFlow(CardGameState())
    val gameState: StateFlow<CardGameState> = _gameState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentProblemId: String? = null
    
    fun startGame(problemId: String) {
        currentProblemId = problemId
        _gameState.value = CardGameState(
            isGameActive = true,
            timeRemaining = 30
        )
        _gameResult.value = null
        generateNewProblem()
        startTimer()
    }
    
    private fun generateNewProblem() {
        // Generate sequence of 3 cards
        // Mix of shapes and colors
        val length = 3
        val sequence = List(length) { 
            CardItem(
                id = it, 
                shape = CardShape.values().random(), 
                color = CardColor.values().random()
            ) 
        }
        
        // Options available for user to click to add to their sequence.
        // We need 4 options? Or just buttons for all possibilities?
        // "Update the four options accordingly." -> imply 4 choices.
        // If the sequence consists of shapes/colors, the 4 options must be the possible allowed inputs?
        // Or 4 possible next items?
        // Let's interpret: Display the sequence. Use 4 options that ARE the sequence items (shuffled) plus one distractor? 
        // Or just the sequence items shuffled? "Remember the sequence".
        // Let's provide 4 options which are distinctive cards, and the sequence is made of them.
        
        val pool = sequence.toMutableList()
        while(pool.size < 4) {
            pool.add(CardItem(-1, CardShape.values().random(), CardColor.values().random()))
        }
        val options = pool.shuffled()
        
        _gameState.value = _gameState.value.copy(
            sequence = sequence,
            options = options,
            phase = GamePhase.MEMORIZE,
            userSequence = mutableListOf(),
            message = "Memorize!"
        )
        
        // Auto switch to Recall after 2 seconds
        viewModelScope.launch {
            delay(2000)
            if (_gameState.value.isGameActive) {
                _gameState.value = _gameState.value.copy(
                    phase = GamePhase.RECALL,
                    message = "Repeat the sequence"
                )
            }
        }
    }
    
    fun onOptionSelected(card: CardItem) {
        val currentState = _gameState.value
        if (currentState.phase != GamePhase.RECALL || !currentState.isGameActive) return
        
        val newUserSeq = currentState.userSequence.toMutableList()
        newUserSeq.add(card)
        
        // Check immediate validity? or wait?
        // Let's check immediate for "Simon Says" feel or standard memory?
        // Standard: Wait until full length?
        // Let's check matching index.
        val currentIndex = newUserSeq.lastIndex
        if (currentIndex < currentState.sequence.size) {
            // Check if this card matches the one at that index in original sequence
            // Note: Options might have duplicates if we are just picking shapes/colors?
            // "CardItem" equality includes ID. If we use shuffled items from sequence, IDs match.
            // If the user tracks "Red Square" and there are two "Red Squares", handled by ID? 
            // Ideally visual appearance matches.
            
            // To be fair: The user taps the option.
            
            val expected = currentState.sequence[currentIndex]
            // Relaxed check: Shape and Color match (ignore ID if distinct options visually identical)
            if (card.shape == expected.shape && card.color == expected.color) {
                // Correct so far
                _gameState.value = currentState.copy(userSequence = newUserSeq)
                
                if (newUserSeq.size == currentState.sequence.size) {
                    // Full sequence correct
                    handleResult(true)
                }
            } else {
                // Wrong step
                handleResult(false)
            }
        }
    }
    
    private fun handleResult(isCorrect: Boolean) {
        val currentState = _gameState.value
        if (isCorrect) {
             _gameState.value = currentState.copy(
                 correctCount = currentState.correctCount + 1,
                 totalProblems = currentState.totalProblems + 1,
                 message = "Correct!",
                 phase = GamePhase.FEEDBACK
             )
        } else {
             _gameState.value = currentState.copy(
                 incorrectCount = currentState.incorrectCount + 1,
                 totalProblems = currentState.totalProblems + 1,
                 message = "Wrong!",
                 phase = GamePhase.FEEDBACK
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
            gameType = "Memory Sequence",
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
