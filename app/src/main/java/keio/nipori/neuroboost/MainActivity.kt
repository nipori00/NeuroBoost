package keio.nipori.neuroboost

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import keio.nipori.neuroboost.navigation.NeuroBoostNavigation
import keio.nipori.neuroboost.ui.theme.NeuroBoostTheme
import java.util.*


class MainActivity : ComponentActivity() {
    private val PREFS_NAME = "NeuroBoostPrefs"
    private val KEY_LANGUAGE = "language"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
        
        setContent {
            var currentLanguage by remember { mutableStateOf(savedLanguage) }
            
            // Update locale when language changes
            LaunchedEffect(currentLanguage) {
                updateLocale(currentLanguage)
                sharedPrefs.edit().putString(KEY_LANGUAGE, currentLanguage).apply()
            }
            
            NeuroBoostTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    NeuroBoostNavigation(
                        navController = navController,
                        currentLanguage = currentLanguage,
                        onLanguageChange = { newLanguage ->
                            currentLanguage = newLanguage
                        }
                    )
                }
            }
        }
    }
    
    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}