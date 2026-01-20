package keio.nipori.neuroboost.models

data class GameState(
    val currentProblem: MathProblem? = null,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val timeRemaining: Int = 30,
    val isGameActive: Boolean = false,
    val totalProblems: Int = 0
)

data class GameResult(
    val gameType: String = "Math",
    val totalProblems: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val timeTakenSeconds: Long = 0
) {
    val accuracy: Int
        get() = if (totalProblems > 0) {
            ((correctAnswers.toFloat() / totalProblems.toFloat()) * 100).toInt()
        } else {
            0
        }
    
    fun getEncouragementMessage(): String {
        return when {
            accuracy >= 90 -> "result_excellent"
            accuracy >= 75 -> "result_great"
            accuracy >= 50 -> "result_good"
            else -> "result_keep_practicing"
        }
    }
}
