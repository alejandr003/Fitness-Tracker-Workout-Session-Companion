package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.ui.dashboard.WorkoutDashboardScreen
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.ui.theme.FitnessTrackerWorkoutSessionCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessTrackerWorkoutSessionCompanionTheme {
                WorkoutDashboardScreen()
            }
        }
    }
}
