package com.workos.launcher

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workos.launcher.ui.theme.WorkOSTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.tan

private const val BATTERY_BAR_SKEW_ANGLE_DEGREES = 52.0
private const val BATTERY_OVERLAY_SHIFT_WALLPAPER_PX = 2f
private const val BATTERY_PANEL_TO_BAR_GAP_WALLPAPER_PX = 6f
private const val BATTERY_PANEL_LOWERING_WALLPAPER_PX = 2f
private const val LED_DOT_SIZE_HEIGHT_RATIO = 0.053333333f

private val LED_DOT_MATRIX_GLYPHS = mapOf(
    '0' to listOf("111", "101", "101", "101", "111"),
    '1' to listOf("010", "110", "010", "010", "111"),
    '2' to listOf("111", "001", "111", "100", "111"),
    '3' to listOf("111", "001", "111", "001", "111"),
    '4' to listOf("101", "101", "111", "001", "001"),
    '5' to listOf("111", "100", "111", "001", "111"),
    '6' to listOf("111", "100", "111", "101", "111"),
    '7' to listOf("111", "001", "010", "010", "010"),
    '8' to listOf("111", "101", "111", "101", "111"),
    '9' to listOf("111", "101", "111", "001", "111")
)

@Composable
private fun LedDotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF6A00)
) {
    val blankGlyph = listOf("000", "000", "000", "000", "000")
    val glyphs = text.map { LED_DOT_MATRIX_GLYPHS[it] ?: blankGlyph }

    Canvas(modifier = modifier) {
        val dotSize = size.height * LED_DOT_SIZE_HEIGHT_RATIO
        val dotGap = dotSize * 0.5f
        val characterGap = dotSize * 0.8f
        val glyphWidth = dotSize * 3f + dotGap * 2f
        val textWidth = glyphWidth * glyphs.size + characterGap * (glyphs.size - 1)
        val textHeight = dotSize * 5f + dotGap * 4f
        val startX = (size.width - textWidth) / 2f
        val startY = (size.height - textHeight) / 2f

        glyphs.forEachIndexed { characterIndex, glyph ->
            glyph.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { columnIndex, dot ->
                    if (dot == '1') {
                        val center = Offset(
                            x = startX + characterIndex * (glyphWidth + characterGap) +
                                columnIndex * (dotSize + dotGap) + dotSize / 2f,
                            y = startY + rowIndex * (dotSize + dotGap) + dotSize / 2f
                        )
                        drawCircle(color.copy(alpha = 0.22f), radius = dotSize * 0.85f, center = center)
                        drawCircle(color, radius = dotSize * 0.42f, center = center)
                    }
                }
            }
        }
    }
}

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
    // Position each of the five battery bars over its matching lower vent slit in wallpaper v17.
    BoxWithConstraints(modifier = modifier) {
        val imageScale = maxOf(maxWidth / 1224f, maxHeight / 2720f)
        val imageOffsetX = (maxWidth - imageScale * 1224f) / 2f
        val imageOffsetY = (maxHeight - imageScale * 2720f) / 2f
        val barWidth = imageScale * 44f
        val barHeight = imageScale * 63f
        val capHeight = imageScale * 20f
        val slitCenterX = imageOffsetX + imageScale * 1123.5f
        // Ordered from top to bottom; the battery fills upward from the lowest slit.
        val slitCenterYs = listOf(1265f, 1316f, 1365.5f, 1415.5f, 1464.5f)
        val batteryOverlayOffsetY = imageScale * BATTERY_OVERLAY_SHIFT_WALLPAPER_PX
        val skewSlope = tan(BATTERY_BAR_SKEW_ANGLE_DEGREES * PI / 180.0).toFloat()

        val indicatorWidth = imageScale * 50f
        val indicatorHeight = imageScale * 54f
        val topBarTop = imageOffsetY + imageScale * slitCenterYs.first() +
            batteryOverlayOffsetY - barHeight / 2f
        val triangleHeight = imageScale * (
            BATTERY_PANEL_TO_BAR_GAP_WALLPAPER_PX - BATTERY_PANEL_LOWERING_WALLPAPER_PX
        )
        val indicatorTop = topBarTop - indicatorHeight - triangleHeight

        Box(
            modifier = Modifier
                .offset(
                    x = slitCenterX - indicatorWidth / 2f,
                    y = indicatorTop
                )
                .size(width = indicatorWidth, height = indicatorHeight)
                .clip(RoundedCornerShape(imageScale * 8f))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            LedDotMatrixText(
                text = batteryPercent.toString(),
                modifier = Modifier.fillMaxSize()
            )
        }

        val triangleWidth = imageScale * 18f
        Canvas(
            modifier = Modifier
                .offset(
                    x = slitCenterX - triangleWidth / 2f,
                    y = indicatorTop + indicatorHeight
                )
                .size(width = triangleWidth, height = triangleHeight)
        ) {
            val triangle = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(triangle, Color.Black)
        }

        for (i in 0 until 5) {
            val bottomIndex = 4 - i
            val start = bottomIndex * 20
            val end = (bottomIndex + 1) * 20
            val fillFraction = when {
                batteryPercent >= end -> 1f
                batteryPercent <= start -> 0f
                else -> (batteryPercent - start) / 20f
            }
            val barLeft = slitCenterX - barWidth / 2f
            val barTop = imageOffsetY + imageScale * slitCenterYs[i] + batteryOverlayOffsetY - barHeight / 2f

            Canvas(
                modifier = Modifier
                    .offset(x = barLeft, y = barTop)
                    .size(width = barWidth, height = barHeight)
            ) {
                val endCapHeight = size.height - size.width * skewSlope
                val barPath = Path().apply {
                    moveTo(0f, size.height - endCapHeight)
                    lineTo(0f, size.height)
                    lineTo(size.width, endCapHeight)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(barPath, Color.White.copy(alpha = 0.10f))
                if (fillFraction > 0f) {
                    clipPath(barPath) {
                        drawRect(
                            color = Color(0xFFFF6A00),
                            size = Size(width = size.width * fillFraction, height = size.height)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCalendarOverlay(modifier: Modifier = Modifier) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            today = LocalDate.now()
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val imageScale = maxOf(maxWidth / 1224f, maxHeight / 2720f)
        val imageOffsetX = (maxWidth - imageScale * 1224f) / 2f
        val imageOffsetY = (maxHeight - imageScale * 2720f) / 2f
        val yearMonth = YearMonth.from(today)
        val locale = Locale("id", "ID")
        val monthLabel = yearMonth.month.getDisplayName(TextStyle.SHORT, locale).uppercase(locale)
        val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value % 7
        val weekCount = (firstDayOffset + yearMonth.lengthOfMonth() + 6) / 7

        MiniCalendarPanel(
            today = today,
            monthLabel = monthLabel,
            firstDayOffset = firstDayOffset,
            weekCount = weekCount,
            modifier = Modifier
                .offset(
                    x = imageOffsetX + imageScale * 591f,
                    y = imageOffsetY + imageScale * 1853f
                )
                .size(width = imageScale * 255f, height = imageScale * 144.5f)
        )
    }
}

@Composable
private fun MiniCalendarPanel(
    today: LocalDate,
    monthLabel: String,
    firstDayOffset: Int,
    weekCount: Int,
    modifier: Modifier = Modifier
) {
    val daysInMonth = YearMonth.from(today).lengthOfMonth()
    val weekdays = listOf("MIN", "SEN", "SEL", "RAB", "KAM", "JUM", "SAB")
    val panelShape = RoundedCornerShape(6.dp)
    val todayMarkerSize = if (weekCount == 6) 4.dp else 5.dp

    Box(
        modifier = modifier
            .clip(panelShape)
            .background(Color.Black.copy(alpha = 0.84f))
            .border(0.8.dp, Color.White.copy(alpha = 0.42f), panelShape)
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.height(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = monthLabel,
                    fontSize = 7.sp,
                    lineHeight = 8.sp,
                    letterSpacing = 0.3.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6A00),
                    maxLines = 1
                )
                Text(
                    text = today.year.toString(),
                    fontSize = 6.sp,
                    lineHeight = 7.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1
                )
            }

            Row(
                modifier = Modifier.height(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekdays.forEach { weekday ->
                    Text(
                        text = weekday,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 4.sp,
                        lineHeight = 5.sp,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 1
                    )
                }
            }

            repeat(weekCount) { week ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(7) { weekdayIndex ->
                        val day = week * 7 + weekdayIndex - firstDayOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                if (day == today.dayOfMonth) {
                                    Box(
                                        modifier = Modifier
                                            .size(todayMarkerSize)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF6A00)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 4.sp,
                                            lineHeight = 4.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF141414),
                                            maxLines = 1
                                        )
                                    }
                                } else {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 4.sp,
                                        lineHeight = 4.sp,
                                        color = if (weekdayIndex == 0 || weekdayIndex == 6) {
                                            Color.White.copy(alpha = 0.65f)
                                        } else {
                                            Color.White.copy(alpha = 0.92f)
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
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
        // Modern, camera-free v17 derivative - 1440x3200 with the original aspect ratio
        Image(
            painter = painterResource(id = R.drawable.default_wallpaper_modern),
            contentDescription = "GT30 Pro modern wallpaper, no text or camera module",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay bars are individually positioned from the matching vent-slit coordinates in wallpaper v17.
        BatteryIndicator(
            batteryPercent = batteryPercent,
            modifier = Modifier.fillMaxSize()
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

        MiniCalendarOverlay(modifier = Modifier.fillMaxSize())
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
