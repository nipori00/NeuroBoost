package keio.nipori.neuroboost.models

enum class MathOperation {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION;

    fun getSymbol(): String {
        return when (this) {
            ADDITION -> "+"
            SUBTRACTION -> "-"
            MULTIPLICATION -> "×"
            DIVISION -> "÷"
        }
    }
}

data class MathProblem(
    val num1: Int,
    val num2: Int,
    val operation: MathOperation,
    val correctAnswer: Int,
    val options: List<Int>
) {
    fun getQuestionText(): String {
        return "$num1 ${operation.getSymbol()} $num2 = ?"
    }
}
