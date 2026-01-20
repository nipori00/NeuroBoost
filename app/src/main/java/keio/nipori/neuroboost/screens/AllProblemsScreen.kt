package keio.nipori.neuroboost.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import keio.nipori.neuroboost.data.ProblemRepository
import keio.nipori.neuroboost.models.Problem
import keio.nipori.neuroboost.models.ProblemType
import keio.nipori.neuroboost.ui.theme.AccentBlue
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.utils.ProgressManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProblemsScreen(
    onBackClick: () -> Unit,
    onProblemClick: (Problem) -> Unit
) {
    val context = LocalContext.current
    val progressManager = remember { ProgressManager(context) }
    val problems = remember { ProblemRepository.problems }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Problems", color = White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue
                )
            )
        },
        containerColor = DarkBlue
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(problems) { problem ->
                val isSolved = progressManager.isProblemSolved(problem.id)
                ProblemItemRow(problem, isSolved, onProblemClick)
            }
        }
    }
}

@Composable
fun ProblemItemRow(
    problem: Problem,
    isSolved: Boolean,
    onClick: (Problem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(problem) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSolved) AccentBlue.copy(alpha = 0.3f) else White.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (problem.type) {
                ProblemType.MATH -> Icons.Filled.Add
                ProblemType.PUZZLE -> Icons.Filled.Build // Using Build as placeholder for Puzzle
                ProblemType.CARD -> Icons.Filled.Menu // Using Menu as placeholder
                ProblemType.NBACK -> Icons.Filled.Refresh
                ProblemType.SWIFT_VISION -> Icons.Filled.Star
                ProblemType.PATHFINDER -> Icons.Filled.Share
            }

            val backgroundColor = when (problem.type) {
                ProblemType.MATH -> Color(0xFFE3F2FD) // Light Blue
                ProblemType.PUZZLE -> Color(0xFFF3E5F5) // Light Purple
                ProblemType.CARD -> Color(0xFFFFF3E0) // Light Orange
                ProblemType.NBACK -> Color(0xFFE8F5E9) // Light Green
                ProblemType.SWIFT_VISION -> Color(0xFFFFF8E1) // Light Yellow
                ProblemType.PATHFINDER -> Color(0xFFFBE9E7) // Light Deep Orange
            }

            Icon(
                imageVector = icon,
                contentDescription = problem.type.name,
                tint = DarkBlue,
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor, MaterialTheme.shapes.small)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = problem.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = problem.description,
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Spacer(modifier = Modifier.width(16.dp))

            if (isSolved) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Solved",
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
            } else {
               Spacer(modifier = Modifier.size(24.dp))
            }

        }
    }
}
