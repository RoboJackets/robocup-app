package com.example.robocupcontrollertest

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private val Gold = Color(0xFFFFC72C)
private val White = Color(0xFFF5F3ED)
private val Gray = Color(0xFFAAA59B)
private val Green = Color(0xFF238C50)
private val Red = Color(0xFFC03939)
private val Line = Gold.copy(alpha = 0.55f)
private val StationGradient = Brush.horizontalGradient(
    listOf(Color(0xFF2B2822), Color(0xFF262522), Color(0xFF232426))
)
private val HeaderGradient = Brush.horizontalGradient(
    listOf(Color(0xFF493719), Color(0xFF26221B), Color(0xFF19191B))
)
private val FooterGradient = Brush.horizontalGradient(
    listOf(Color(0xFF19191B), Color(0xFF26221B), Color(0xFF493719))
)

// The controller mapping will update these values later. This UI doesn't send commands.
// The names mirror the fields in the team's message structure.
data class RobotCommandPreview(
    val team: String? = null,
    val dribblerSpeed: String? = null,
    val kickStrength: String? = null,
    val shootMode: String? = null,
    val triggerMode: String? = null,
    val bodyX: String? = null,
    val bodyY: String? = null,
    val bodyW: String? = null
)

@Composable
fun DriverStationScreen(
    networkStats: NetworkStats = NetworkStats(),
    command: RobotCommandPreview = RobotCommandPreview()
) {
    var robot by remember { mutableIntStateOf(1) }
    var selectedTeam by remember { mutableStateOf(if (command.team.equals("yellow", true)) "YELLOW" else "BLUE") }
    var controlState by remember { mutableIntStateOf(0) } // 0: INIT, 1: PLAY, 2: STOP
    var robotMenu by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Driver, 1 = Network
    val connected = false // Replace with real robot connection feedback later.

    // Make the Activity immersive. The header itself still protects curved edges.
    val activity = LocalContext.current as? Activity
    DisposableEffect(activity) {
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val params = window.attributes
            params.layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= 30)
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            else WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params
            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        onDispose {
            activity?.window?.let { window ->
                WindowInsetsControllerCompat(window, window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Placeholder log entries until real telemetry is available.
    val logs = listOf(
        "SYSTEM" to "Driver station ready",
        "INPUT" to "Awaiting controller input",
        "ROBOT" to "Robot %02d selected".format(robot),
        "STATE" to when (controlState) {
            1 -> "Initialized"
            2 -> "Running"
            else -> "Stopped"
        },
        "WAIT" to "No robot telemetry received"
    )

    Column(Modifier.fillMaxSize().background(StationGradient)) {
        // Header: branding, connection indicator and inactive settings icon.
        Row(
            Modifier.fillMaxWidth().height(54.dp).background(HeaderGradient)
                .padding(start = 30.dp, end = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.img),
                contentDescription = "RoboJackets Logo",
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text("ROBOCUP", color = White, fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold, lineHeight = 19.sp)
                Text("DRIVER STATION", color = Gold, fontSize = 9.sp,
                    letterSpacing = 1.2.sp, lineHeight = 12.sp)
            }
            Box(Modifier.size(8.dp).background(if (connected) Green else Red, CircleShape))
            Spacer(Modifier.width(7.dp))
            Text(if (connected) "Robots Connected" else "Robots Disconnected",
                color = White, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("⋮", color = Gold, fontSize = 26.sp,
                modifier = Modifier.padding(start = 17.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Line))

        if (activeTab == 1) {
            Box(Modifier.weight(1f).fillMaxWidth()) { NetworkScreen(robot, networkStats) }
        } else Row(Modifier.weight(1f).fillMaxWidth()) {
            // Left: full-width robot selector and one three-state control button.
            BoxWithConstraints(Modifier.weight(0.36f).fillMaxHeight()) {
                val controlSize = minOf(maxWidth * 0.48f, maxHeight * 0.34f, 105.dp)
                val selectorWidth = minOf(maxWidth - 24.dp, 196.dp)

                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.fillMaxWidth().height(56.dp).background(Gold)
                            .clickable { robotMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        // Anchor the dropdown to the centered selector content.
                        Box(Modifier.width(selectorWidth).fillMaxHeight()) {
                            Column(Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Text("SELECT ROBOT", color = Color(0xFF4A3A18),
                                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.7.sp, lineHeight = 14.sp)
                                Text("ROBOT %02d".format(robot), color = Color(0xFF171717),
                                    fontSize = 19.sp, fontWeight = FontWeight.Bold,
                                    lineHeight = 23.sp)
                            }
                            Text("▼", color = Color(0xFF29251C), fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.CenterStart))
                            Text("▼", color = Color(0xFF29251C), fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.CenterEnd))
                            DropdownMenu(
                                expanded = robotMenu,
                                onDismissRequest = { robotMenu = false },
                                modifier = Modifier.width(selectorWidth)
                                    .background(Color(0xFF302E2A))
                            ) {
                                (1..6).forEach { id ->
                                    val selected = id == robot
                                    DropdownMenuItem(
                                        modifier = Modifier.height(42.dp).background(
                                            if (selected) Color(0xFF48402C) else Color(0xFF302E2A)),
                                        text = {
                                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text("ROBOT %02d".format(id),
                                                    color = if (selected) Gold else White,
                                                    fontSize = 16.sp,
                                                    fontWeight = if (selected) FontWeight.ExtraBold
                                                    else FontWeight.Medium)
                                            }
                                        },
                                        onClick = { robot = id; robotMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // Each tap advances INIT -> PLAY -> STOP -> INIT (UI only).
                        val fill = if (controlState == 2) Gold else Color(0xFF363A3C)
                        Box(
                            Modifier.size(controlSize).background(fill, CircleShape)
                                .clickable { controlState = (controlState + 1) % 3 },
                            contentAlignment = Alignment.Center
                        ) {
                            when (controlState) {
                                0 -> Text("INIT", color = Gold, fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                                1 -> Canvas(Modifier.fillMaxSize()) {
                                    val triangle = Path().apply {
                                        moveTo(size.width * 0.39f, size.height * 0.29f)
                                        lineTo(size.width * 0.74f, size.height * 0.47f)
                                        quadraticBezierTo(size.width * 0.79f, size.height * 0.50f,
                                            size.width * 0.74f, size.height * 0.53f)
                                        lineTo(size.width * 0.39f, size.height * 0.71f)
                                        quadraticBezierTo(size.width * 0.35f, size.height * 0.73f,
                                            size.width * 0.35f, size.height * 0.68f)
                                        lineTo(size.width * 0.35f, size.height * 0.32f)
                                        close()
                                    }
                                    drawPath(triangle, Gold)
                                }
                                else -> Canvas(Modifier.fillMaxSize()) {
                                    val side = size.minDimension * 0.32f
                                    drawRoundRect(Color(0xFF242424),
                                        Offset((size.width - side) / 2, (size.height - side) / 2),
                                        Size(side, side), cornerRadius = CornerRadius(5.dp.toPx()))
                                }
                            }
                        }
                    }
                }
            }

            Box(Modifier.fillMaxHeight().width(1.dp).background(Line))

            // Right: compact readings and independently scrolling terminal.
            Column(Modifier.weight(0.64f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    listOf(true, false).forEach { battery ->
                        Row(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            StatIcon(battery)
                            Spacer(Modifier.weight(1f))
                            Text(if (battery) "0 V" else "0 m/s", color = White,
                                fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        if (battery) Box(Modifier.width(1.dp).fillMaxHeight()
                            .background(Gold.copy(alpha = 0.40f)))
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                Row(Modifier.fillMaxWidth().height(30.dp)
                    .background(Color.Black.copy(alpha = 0.14f))
                    .padding(start = 10.dp, end = 22.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("●", color = Gold, fontSize = 10.sp)
                    Spacer(Modifier.width(7.dp))
                    Text("TELEMETRY", color = White, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)
                }
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.14f))
                        .padding(start = 10.dp, end = 20.dp),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(logs) { (level, message) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            val tagColor = when (level) {
                                "SYSTEM" -> Gold
                                "INPUT" -> Color(0xFF71C7E8)
                                "ROBOT" -> Color(0xFFC4A1EF)
                                "STATE" -> if (controlState == 2) Color(0xFF70D69A) else Gold
                                "WAIT" -> Color(0xFFFFB866)
                                else -> Gray
                            }
                            Text(level, modifier = Modifier.width(48.dp), color = tagColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp, lineHeight = 13.sp)
                            Text(message, color = White, fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp, lineHeight = 13.sp, maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Display the current command fields; controller mapping can be added later.
        if (activeTab == 0) {
            DriverCommandStrip(robot, command, selectedTeam) {
                selectedTeam = if (selectedTeam == "BLUE") "YELLOW" else "BLUE"
            }
        }

        // Shared tab bar. Keep the header and frame when switching screens.
        Box(Modifier.fillMaxWidth().height(1.dp).background(Gold.copy(alpha = 0.65f)))
        Row(Modifier.fillMaxWidth().height(32.dp).background(FooterGradient),
            verticalAlignment = Alignment.CenterVertically) {
            listOf("DRIVER", "NETWORK").forEachIndexed { index, label ->
                Box(Modifier.weight(1f).fillMaxHeight().clickable { activeTab = index },
                    contentAlignment = Alignment.Center) {
                    Text(label, color = if (activeTab == index) Gold else Gray.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

// Both icons share an identical 26 dp drawing area and right-aligned readout.
@Composable
private fun StatIcon(battery: Boolean) {
    Canvas(Modifier.size(26.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        if (battery) {
            drawRoundRect(Gold, Offset(size.width * 0.08f, size.height * 0.27f),
                Size(size.width * 0.76f, size.height * 0.46f),
                cornerRadius = CornerRadius(3f), style = stroke)
            drawRect(Gold, Offset(size.width * 0.85f, size.height * 0.40f),
                Size(size.width * 0.10f, size.height * 0.20f))
        } else {
            drawArc(Gold, 180f, 180f, false,
                topLeft = Offset(size.width * 0.08f, size.height * 0.12f),
                size = Size(size.width * 0.87f, size.height * 0.87f), style = stroke)
            drawLine(Gold, Offset(size.width * 0.51f, size.height * 0.78f),
                Offset(size.width * 0.78f, size.height * 0.35f), 2.dp.toPx())
        }
    }
}

// Keep related command fields together instead of placing nine tiny cells in one row.
// TEAM is selectable; other fields will be driven by controller input later.
@Composable
private fun DriverCommandStrip(
    robot: Int, command: RobotCommandPreview, team: String, onTeamToggle: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.08f))) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
        Row(
            Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 14.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Robot selection and team color; dividers separate the field groups.
            CommandGroup(Modifier.weight(0.26f)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CommandReadout("TEAM", team, Modifier.weight(1f).clickable(onClick = onTeamToggle),
                        if (team == "BLUE") Color(0xFF71C7E8) else Gold)
                    CommandReadout("ID", "%02d".format(robot), Modifier.weight(1f))
                }
            }
            CommandDivider()
            // Kick/dribbler settings are currently just displayed, not transmitted.
            CommandGroup(Modifier.weight(0.43f)) {
                Row(Modifier.fillMaxWidth()) {
                    listOf(
                        "DRIBBLE" to command.dribblerSpeed,
                        "KICK" to command.kickStrength,
                        "SHOOT" to command.shootMode,
                        "TRIGGER" to command.triggerMode
                    ).forEach { (label, value) ->
                        CommandReadout(label, value, Modifier.weight(1f))
                    }
                }
            }
            CommandDivider()
            // Body-frame velocity commands (x, y, rotation).
            CommandGroup(Modifier.weight(0.31f)) {
                Row(Modifier.fillMaxWidth()) {
                    listOf(
                        "X" to command.bodyX,
                        "Y" to command.bodyY,
                        "W" to command.bodyW
                    ).forEach { (label, value) ->
                        CommandReadout(label, value, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandDivider() {
    Box(Modifier.width(1.dp).height(34.dp).background(White.copy(alpha = 0.12f)))
}

@Composable
private fun CommandGroup(modifier: Modifier, content: @Composable () -> Unit) {
    Column(modifier.padding(horizontal = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        content()
    }
}

@Composable
private fun CommandReadout(
    label: String, value: String?, modifier: Modifier = Modifier, valueColor: Color = White
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Gray, fontSize = 10.sp, lineHeight = 13.sp,
            textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value ?: "—", color = valueColor, fontSize = 14.sp, lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
