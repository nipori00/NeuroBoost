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
import kotlin.math.pow
import kotlin.math.sqrt

data class PathNode(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    var state: NodeState = NodeState.NORMAL
)

enum class NodeState {
    NORMAL,
    CORRECT,
    WRONG
}

data class PathfinderState(
    val nodes: List<PathNode> = emptyList(),
    val completedPath: List<PathNode> = emptyList(),
    val nextIndex: Int = 0,
    val fullSequence: List<String> = emptyList(),
    val isGameActive: Boolean = false,
    val isSolved: Boolean = false,
    val timeElapsed: Long = 0,
    val message: String = ""
)

class PathfinderGameViewModel(application: Application) : AndroidViewModel(application) {
    private val historyManager = HistoryManager(application)
    private val progressManager = ProgressManager(application)
    
    private val _gameState = MutableStateFlow(PathfinderState())
    val gameState: StateFlow<PathfinderState> = _gameState.asStateFlow()
    
    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    
    private var timerJob: Job? = null
    private var currentProblemId: String? = null

    fun startGame(problemId: String) {
        currentProblemId = problemId
        initLevel2()
    }

    private fun initLevel2() {
        val sequence = mutableListOf<String>()
        val numbers = (1..10).map { it.toString() }
        val letters = ('A'..'J').map { if (it == 'I') "Ⅰ" else it.toString() }
        
        for (i in 0 until 10) {
            sequence.add(numbers[i])
            sequence.add(letters[i])
        }
        
        val nodes = generateNodes(sequence)
        
        _gameState.value = PathfinderState(
            nodes = nodes,
            fullSequence = sequence,
            isGameActive = true,
            isSolved = false,
            message = "Connect: 1 -> A -> 2 -> B..."
        )
        
        startTimer()
    }
    
    private fun generateNodes(sequence: List<String>): List<PathNode> {
        val nodes = mutableListOf<PathNode>()
        
        for (item in sequence) {
            var validPosition = false
            var attempts = 0
            while (!validPosition && attempts < 100) {
                val x = (10..90).random() / 100f
                val y = (15..85).random() / 100f
                
                
                var collision = false
                for (existing in nodes) {
                    val dist = sqrt((x - existing.x).pow(2) + (y - existing.y).pow(2))
                    if (dist < 0.12f) {
                        collision = true
                        break
                    }
                }
                
                if (!collision) {
                    nodes.add(PathNode(item, item, x, y))
                    validPosition = true
                }
                attempts++
            }
        }
        return nodes
    }
    
    fun onNodeTap(node: PathNode) {
        val currentState = _gameState.value
        if (!currentState.isGameActive || currentState.isSolved) return
        
        val expected = currentState.fullSequence[currentState.nextIndex]
        if (node.text == expected) {
            val updatedNodes = currentState.nodes.map { 
                if (it.text == node.text) it.copy(state = NodeState.CORRECT) else it 
            }
            
            val updatedPath = currentState.completedPath + node
            
            _gameState.value = currentState.copy(
                nodes = updatedNodes,
                completedPath = updatedPath,
                nextIndex = currentState.nextIndex + 1
            )
            
            if (currentState.nextIndex + 1 >= currentState.fullSequence.size) {
                _gameState.value = _gameState.value.copy(
                    isGameActive = false,
                    isSolved = true,
                    message = "Complete!"
                )
                
                val result = keio.nipori.neuroboost.models.GameResult(
                    gameType = "Pathfinder",
                    totalProblems = 1,
                    correctAnswers = 1,
                    incorrectAnswers = 0,
                    timestamp = System.currentTimeMillis(),
                    timeTakenSeconds = currentState.timeElapsed
                )
                _gameResult.value = result
                historyManager.saveGameResult(result)
                
                currentProblemId?.let { progressManager.markProblemSolved(it) }
            }
        } else {
        }
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value.isGameActive) {
                delay(1000)
                _gameState.value = _gameState.value.copy(timeElapsed = _gameState.value.timeElapsed + 1)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
