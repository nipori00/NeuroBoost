package keio.nipori.neuroboost.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import keio.nipori.neuroboost.R
import keio.nipori.neuroboost.ui.theme.DarkBlue
import keio.nipori.neuroboost.ui.theme.White
import keio.nipori.neuroboost.ui.theme.AccentBlue

@Composable
fun TitleScreen(
    onStartClick: () -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf(
        "en" to R.string.language_english,
        "ja" to R.string.language_japanese,
        "ko" to R.string.language_korean
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Title
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center
            )

            // Subtitle
            Text(
                text = stringResource(R.string.title_subtitle),
                fontSize = 18.sp,
                color = White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Start Button
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.button_start),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.label_language),
                    fontSize = 16.sp,
                    color = White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .width(200.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = White
                        )
                    ) {
                        val currentLangName = languages.find { it.first == currentLanguage }?.second
                            ?: R.string.language_english
                        Text(
                            text = stringResource(currentLangName),
                            fontSize = 16.sp
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEach { (code, nameResId) ->
                            DropdownMenuItem(
                                text = { Text(stringResource(nameResId)) },
                                onClick = {
                                    onLanguageChange(code)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
