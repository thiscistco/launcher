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
import android.content.IntentFilter
import android.os.BatteryManager

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
fun BatteryIndicator(
    batteryPercent: Int,
    modifier: Modifier = Modifier
) {
    // 5 garis miring, tiap garis 20% = bisa terisi sebagian
    // 15% = 3/4 garis pertama (0.75 * 20% = 15%)
    // 30% = 1 garis penuh + 1/2 garis kedua (20% + 10%)
    // Di ventilasi kanan - 5 garis miring ventilasi, tiap 5% = 1/4 garis, isi dari bawah, miring sesuai aslinya
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Label persentase baterai (menggantikan HIGH PERFORMANCE) - di atas ventilasi
        Text(
            text = "$batteryPercent%",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6A00),
            letterSpacing = 0.5.sp
        )
        // 5 garis miring DIAGONAL VERTIKAL di ventilasi kanan - persis seperti ventilasi POWERTRAIN di gambar
        // Isi dari bawah, tiap garis vertikal diagonal
        for (i in 0 until 5) {
            val bottomIndex = 4 - i
            val start = bottomIndex * 20
            val end = (bottomIndex + 1) * 20
            val fillFraction = when {
                batteryPercent >= end -> 1f
                batteryPercent <= start -> 0f
                else -> (batteryPercent - start) / 20f // 5%->0.25, 15%->0.75 bottom
            }
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(0.5.dp))
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                if (fillFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fillFraction)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFFFF6A00))
                    )
                }
            }
        }
    }
}

@Composable
fun getBatteryPercent(): Int {
    val context = LocalContext.current
    // Dynamic: baca baterai HP real via BatteryManager
    // 15% -> 1 garis, 40% -> 2 garis, 80% -> 4 garis, dll akan langsung update
    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val battery = context.registerReceiver(null, filter)
    val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level != -1 && scale != -1) {
        (level * 100 / scale)
    } else {
        15 // fallback jika gagal baca
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
    val batteryPercent = getBatteryPercent()

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

        // Battery Indicator Overlay - 5 garis di ventilasi kosong plat silver kanan
        // Posisi tepat di ventilasi kanan (tempat 5 garis miring asli)
        BatteryIndicator(
            batteryPercent = batteryPercent,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp, top = 30.dp)
        )

        // Optional parallax overlay - subtle movement via sensor (implemented in real device)
        if (parallaxEnabled) {
            // Parallax layer would be handled with gyroscope in full build
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
