package keio.nipori.neuroboost.utils

import android.content.Context
import android.content.SharedPreferences

class ProgressManager(context: Context) {
    private val PREFS_NAME = "NeuroBoostProgress"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isProblemSolved(problemId: String): Boolean {
        return prefs.getBoolean("solved_$problemId", false)
    }

    fun markProblemSolved(problemId: String) {
        prefs.edit().putBoolean("solved_$problemId", true).apply()
    }
}
