package com.workos.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workos.launcher.ui.theme.WorkOSTheme

class SuperAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkOSTheme {
                SuperAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAppScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Notes", "Todo", "Calendar", "Focus", "Files")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkOS Super App") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFF1A1A1A)) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(t, color = Color.White) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                item {
                    when (selectedTab) {
                        0 -> NotesTab()
                        1 -> TodoTab()
                        2 -> CalendarTab()
                        3 -> FocusTab()
                        4 -> FilesTab()
                    }
                }
            }
        }
    }
}

@Composable fun NotesTab() { Text("📝 Notes - All-in-One (local storage, sync to Drive optional)", color = Color.White) }
@Composable fun TodoTab() { Text("✓ Todo - Tasks with priority & due dates", color = Color.White) }
@Composable fun CalendarTab() { Text("📅 Calendar - Integrated with Google Calendar (via connected apps)", color = Color.White) }
@Composable fun FocusTab() { Text("⏱ Pomodoro + Habit Tracker", color = Color.White) }
@Composable fun FilesTab() { Text("📁 Files - Clean file manager", color = Color.White) }
