package keio.nipori.neuroboost.models

enum class ProblemType {
    MATH,
    PUZZLE,
    CARD,
    NBACK,
    SWIFT_VISION,
    PATHFINDER
}

data class Problem(
    val id: String,
    val title: String,
    val type: ProblemType,
    val description: String
)
