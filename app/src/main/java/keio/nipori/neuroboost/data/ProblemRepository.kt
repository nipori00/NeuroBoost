package keio.nipori.neuroboost.data

import keio.nipori.neuroboost.models.Problem
import keio.nipori.neuroboost.models.ProblemType

object ProblemRepository {
    val problems = listOf(
        Problem(
            id = "math_1",
            title = "Math Challenge",
            type = ProblemType.MATH,
            description = "Solve simple arithmetic problems to win."
        ),
        Problem(
            id = "puzzle_1",
            title = "Shape Puzzle",
            type = ProblemType.PUZZLE,
            description = "Fit the correct shape into the empty space."
        ),
        Problem(
            id = "card_1",
            title = "Memory Sequence",
            type = ProblemType.CARD,
            description = "Remember the sequence of cards."
        ),
        Problem(
            id = "nback_1",
            title = "Memory Match: 2-Back",
            type = ProblemType.NBACK,
            description = "Match the item from 2 steps ago"
        ),
        Problem(
            id = "swift_1",
            title = "Swift Vision: Car/Truck",
            type = ProblemType.SWIFT_VISION,
            description = "Identify center object + peripheral location"
        ),
        Problem(
            id = "path_1",
            title = "Pathfinder: Trail A & B",
            type = ProblemType.PATHFINDER,
            description = "Connect nodes in correct sequence"
        )
    )

    fun getProblemById(id: String): Problem? {
        return problems.find { it.id == id }
    }
}
