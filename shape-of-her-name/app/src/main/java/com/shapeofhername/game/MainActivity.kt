package com.shapeofhername.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shapeofhername.game.ui.ShapeOfHerNameApp
import com.shapeofhername.game.ui.theme.ShapeOfHerNameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShapeOfHerNameTheme {
                ShapeOfHerNameApp()
            }
        }
    }
}
