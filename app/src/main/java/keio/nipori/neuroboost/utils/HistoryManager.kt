package keio.nipori.neuroboost.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import keio.nipori.neuroboost.models.GameResult

class HistoryManager(private val context: Context) {
    private val PREFS_NAME = "NeuroBoostHistory"
    private val KEY_GAME_HISTORY = "game_history_list" // Changed key to avoid conflict/ensure fresh start
    private val gson = Gson()

    fun saveGameResult(result: GameResult) {
        val history = getHistory().toMutableList()
        history.add(0, result) // Add to top
        saveHistoryList(history)
    }

    fun getHistory(): List<GameResult> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_GAME_HISTORY, null)
        return if (json != null) {
            val type = object : TypeToken<List<GameResult>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    
    fun clearHistory() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_GAME_HISTORY).apply()
    }

    private fun saveHistoryList(history: List<GameResult>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_GAME_HISTORY, json).apply()
    }
}
