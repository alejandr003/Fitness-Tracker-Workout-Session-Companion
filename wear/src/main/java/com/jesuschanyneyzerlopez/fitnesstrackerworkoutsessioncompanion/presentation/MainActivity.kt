package com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.theme.FitnessTrackerWorkoutSessionCompanionTheme
import com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.presentation.workout.WearWorkoutRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitnessTrackerWorkoutSessionCompanionTheme {
                WearWorkoutRoute()
            }
        }
    }
}
