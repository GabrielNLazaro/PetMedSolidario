package com.petmedsolidario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.petmedsolidario.ui.PetMedTheme
import com.petmedsolidario.ui.PetMedApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { PetMedTheme { PetMedApp() } }
    }
}
