package com.example.bookeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bookeeper.ui.BookeeperApp
import com.example.bookeeper.ui.theme.BookeeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookeeperTheme {
                BookeeperApp()
            }
        }
    }
}
