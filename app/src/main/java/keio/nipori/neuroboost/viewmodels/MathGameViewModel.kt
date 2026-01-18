package keio.nipori.neuroboost.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import keio.nipori.neuroboost.models.GameResult
import keio.nipori.neuroboost.models.GameState
import keio.nipori.neuroboost.models.MathOperation
import keio.nipori.neuroboost.models.MathProblem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class MathGameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()

    private var timerJob: Job? = null

    fun startGame() {
        // Reset game state
        _gameState.value = GameState(
            isGameActive = true,
            timeRemaining = 30,
            correctCount = 0,
            incorrectCount = 0,
            totalProblems = 0
        )
        _gameResult.value = null
        
        // Generate first problem
        generateNewProblem()
        
        // Start timer
        startTimer()
    }

    fun submitAnswer(answer: Int) {
        val currentState = _gameState.value
        val currentProblem = currentState.currentProblem ?: return

        if (answer == currentProblem.correctAnswer) {
            _gameState.value = currentState.copy(
                correctCount = currentState.correctCount + 1,
                totalProblems = currentState.totalProblems + 1
            )
        } else {
            _gameState.value = currentState.copy(
                incorrectCount = currentState.incorrectCount + 1,
                totalProblems = currentState.totalProblems + 1
            )
        }

        // Generate next problem
        if (_gameState.value.isGameActive) {
            generateNewProblem()
        }
    }

    private fun generateNewProblem() {
        val operation = MathOperation.values().random()
        val problem = when (operation) {
            MathOperation.ADDITION -> generateAddition()
            MathOperation.SUBTRACTION -> generateSubtraction()
            MathOperation.MULTIPLICATION -> generateMultiplication()
            MathOperation.DIVISION -> generateDivision()
        }

        _gameState.value = _gameState.value.copy(currentProblem = problem)
    }

    private fun generateAddition(): MathProblem {
        val num1 = Random.nextInt(10, 100)
        val num2 = Random.nextInt(10, 100)
        val correctAnswer = num1 + num2
        val options = generateOptions(correctAnswer)
        
        return MathProblem(num1, num2, MathOperation.ADDITION, correctAnswer, options)
    }

    private fun generateSubtraction(): MathProblem {
        val num1 = Random.nextInt(50, 200)
        val num2 = Random.nextInt(10, num1)
        val correctAnswer = num1 - num2
        val options = generateOptions(correctAnswer)
        
        return MathProblem(num1, num2, MathOperation.SUBTRACTION, correctAnswer, options)
    }

    private fun generateMultiplication(): MathProblem {
        val num1 = Random.nextInt(2, 13)
        val num2 = Random.nextInt(2, 13)
        val correctAnswer = num1 * num2
        val options = generateOptions(correctAnswer)
        
        return MathProblem(num1, num2, MathOperation.MULTIPLICATION, correctAnswer, options)
    }

    private fun generateDivision(): MathProblem {
        val num2 = Random.nextInt(2, 13)
        val quotient = Random.nextInt(2, 13)
        val num1 = num2 * quotient
        val correctAnswer = quotient
        val options = generateOptions(correctAnswer)
        
        return MathProblem(num1, num2, MathOperation.DIVISION, correctAnswer, options)
    }

    private fun generateOptions(correctAnswer: Int): List<Int> {
        val options = mutableSetOf(correctAnswer)
        
        while (options.size < 4) {
            val offset = Random.nextInt(-20, 21)
            val wrongAnswer = correctAnswer + offset
            if (wrongAnswer > 0 && wrongAnswer != correctAnswer) {
                options.add(wrongAnswer)
            }
        }
        
        return options.shuffled()
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
        _gameState.value = currentState.copy(isGameActive = false)
        
        _gameResult.value = GameResult(
            totalProblems = currentState.totalProblems,
            correctAnswers = currentState.correctCount,
            incorrectAnswers = currentState.incorrectCount
        )
        
        timerJob?.cancel()
    }

    fun resetGame() {
        timerJob?.cancel()
        _gameState.value = GameState()
        _gameResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
