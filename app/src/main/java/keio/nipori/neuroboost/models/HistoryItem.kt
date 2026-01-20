package keio.nipori.neuroboost.models

data class HistoryItem(
    val question: String,
    val userAnswer: Int,
    val correctAnswer: Int,
    val isCorrect: Boolean,
    val timestamp: Long
)
