package com.fantto.auralite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fantto.auralite.ui.navigation.AuraliteNavGraph
import com.fantto.auralite.ui.theme.AuraliteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraliteTheme {
                AuraliteNavGraph()
            }
        }
    }
}

