package com.workos.launcher

import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workos.launcher.ui.theme.WorkOSTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wallpaper v17 is set as default via theme + Image background
        // Parallax option is handled via sensor in HomeScreen

        setContent {
            WorkOSTheme {
                HomeScreen(
                    onOpenSuperApp = {
                        startActivity(Intent(this, SuperAppActivity::class.java))
                    },
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Prevent exit launcher with back
    }
}

@Composable
fun HomeScreen(
    onOpenSuperApp: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    var parallaxEnabled by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Get installed apps for drawer
    val launcherApps = remember {
        val pm = context.packageManager
        pm.getInstalledApplications(0)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }
            .take(24) // limit for preview
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wallpaper v17 - 1224x2720 440ppi - edge-to-edge
        Image(
            painter = painterResource(id = R.drawable.default_wallpaper),
            contentDescription = "GT30 Pro Wallpaper v17",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Optional parallax overlay - subtle movement via sensor (implemented in real device)
        if (parallaxEnabled) {
            // Parallax layer would be handled with gyroscope in full build
            // For now static with slight blur for depth
        }

        // Status bar spacer (hidden per user request - no status bar)
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top - Focus Today Widget (Productivity)
            Column {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FOCUS TODAY",
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Good Morning, Rembangan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()).format(Date()) + " • 3 tasks",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Super App Quick Access
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSuperApp() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "SUPER APP",
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = Color.White.copy(0.6f)
                            )
                            Text(
                                text = "Notes • Todo • Calendar • Focus",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(text = "→", color = Color.White, fontSize = 18.sp)
                    }
                }
            }

            // Bottom - App Grid + Dock
            Column {
                // Search bar (universal search for productivity)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍  Search apps, notes, tasks...",
                            fontSize = 13.sp,
                            color = Color.White.copy(0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // App Grid (productivity categorized)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(launcherApps.size) { idx ->
                        val app = launcherApps[idx]
                        val pm = context.packageManager
                        val label = pm.getApplicationLabel(app).toString()
                        val icon = pm.getApplicationIcon(app)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                pm.getLaunchIntentForPackage(app.packageName)?.let {
                                    context.startActivity(it)
                                }
                            }
                        ) {
                            // Icon placeholder - real would use icon drawable
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = label.first().toString(), color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label.take(8),
                                fontSize = 10.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dock (4 primary apps)
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("📞", "💬", "🌐", "📷").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Settings hint
                Text(
                    text = "Long press for settings • Parallax ${if (parallaxEnabled) "ON" else "OFF"}",
                    fontSize = 10.sp,
                    color = Color.White.copy(0.4f),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { parallaxEnabled = !parallaxEnabled }
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.35f))
            // blur is available on Android 12+
            .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(12.dp) else Modifier)
    ) {
        content()
    }
}
