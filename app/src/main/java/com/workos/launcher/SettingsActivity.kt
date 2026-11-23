package com.workos.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workos.launcher.ui.theme.WorkOSTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkOSTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    var parallax by remember { mutableStateOf(true) }
    var hideGamingApps by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Launcher Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Parallax 3D Wallpaper", color = Color.White)
            Switch(checked = parallax, onCheckedChange = { parallax = it })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Hide Gaming Apps in Focus Mode", color = Color.White)
            Switch(checked = hideGamingApps, onCheckedChange = { hideGamingApps = it })
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Wallpaper: v17 1224×2720 440ppi - GT30 Pro Cyber Mecha", color = Color.Gray)
        Text("Launcher transforms Gaming vibe → Productivity", color = Color.Gray)
    }
}
