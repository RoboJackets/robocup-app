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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private val Gold = Color(0xFFFFC72C)
private val White = Color(0xFFF5F3ED)
private val Gray = Color(0xFFAAA59B)
private val StationGradient = Brush.horizontalGradient(
    listOf(Color(0xFF2B2822), Color(0xFF262522), Color(0xFF232426))
)
private val Green = Color(0xFF238C50)
private val Red = Color(0xFFC03939)

@Composable
fun DriverStationScreen() {
    var robot by remember { mutableIntStateOf(1) }
    var controlState by remember { mutableIntStateOf(0) } // 0 = stopped, 1 = initialized, 2 = running
    var robotMenu by remember { mutableStateOf(false) }
    val connected = false

    val activity = LocalContext.current as? Activity

    DisposableEffect(activity) {
        if (activity != null) {
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val params = window.attributes
            params.layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= 30)
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                else
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = params

            WindowInsetsControllerCompat(window, window.decorView).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        onDispose {
            activity?.let {
                WindowInsetsControllerCompat(it.window, it.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

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
        // Header background spans the whole screen.
        Row(
            Modifier.fillMaxWidth().height(54.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF493719), Color(0xFF26221B), Color(0xFF19191B))
                    )
                )
                .padding(start = 30.dp, end = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.img),
                contentDescription = "RoboJackets Logo",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(11.dp))

            Column(Modifier.weight(1f)) {
                Text("ROBOCUP", color = White, fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold, lineHeight = 19.sp)
                Text("DRIVER STATION", color = Gold, fontSize = 9.sp,
                    letterSpacing = 1.2.sp, lineHeight = 12.sp)
            }

            Box(
                Modifier.size(8.dp).background(
                    if (connected) Green else Red, CircleShape
                )
            )
            Spacer(Modifier.width(7.dp))

            Text(
                if (connected) "Robots Connected" else "Robots Disconnected",
                color = White, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1
            )

            // Settings icon intentionally inactive.
            Text("⋮", color = Gold, fontSize = 26.sp,
                modifier = Modifier.padding(start = 17.dp))
        }

        Box(Modifier.fillMaxWidth().height(1.dp)
            .background(Gold.copy(alpha = 0.55f)))

        Row(
            Modifier.weight(1f).fillMaxWidth()
        ) {
            // Robot selection and start/stop.
            BoxWithConstraints(
                Modifier.weight(0.36f).fillMaxHeight()
            ) {
                val controlSize = minOf(maxWidth * 0.48f, maxHeight * 0.34f, 105.dp)
                val selectorWidth = minOf(maxWidth - 24.dp, 196.dp)

                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Full-width gold selector; tap anywhere to choose a robot.
                    Box(
                        Modifier.fillMaxWidth().height(56.dp)
                            .background(Gold)
                            .clickable { robotMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        // The popup stays anchored to the centered label.
                        Box(Modifier.width(selectorWidth).fillMaxHeight()) {
                            Column(
                                Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
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
                                modifier = Modifier.width(selectorWidth).background(Gold)
                            ) {
                                (1..6).forEach { id ->
                                    val selected = id == robot
                                    DropdownMenuItem(
                                        modifier = Modifier.height(42.dp).background(
                                            if (selected) Color(0xFF302B20) else Gold
                                        ),
                                        text = {
                                            Box(Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center) {
                                                Text("ROBOT %02d".format(id),
                                                    color = if (selected) Gold else Color(0xFF171717), fontSize = 16.sp,
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
                        // One borderless button cycles INIT → PLAY → STOP.
                        val buttonFill = when (controlState) {
                            0 -> Color(0xFF363A3C)  // INIT: neutral charcoal
                            1 -> Color(0xFF363A3C) // PLAY: same charcoal as INIT
                            else -> Gold             // STOP: RoboJackets gold
                        }
                        Box(
                            Modifier.size(controlSize)
                                .background(buttonFill, CircleShape)
                                .clickable { controlState = (controlState + 1) % 3 },
                            contentAlignment = Alignment.Center
                        ) {
                            when (controlState) {
                                0 -> Text(
                                    "INIT", color = Gold, fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )
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
                                    drawRoundRect(
                                        color = Color(0xFF242424),
                                        topLeft = Offset((size.width - side) / 2,
                                            (size.height - side) / 2),
                                        size = Size(side, side),
                                        cornerRadius = CornerRadius(5.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                Modifier.fillMaxHeight().width(1.dp)
                    .background(Gold.copy(alpha = 0.55f))
            )

            Column(Modifier.weight(0.64f).fillMaxHeight()) {
                // Compact horizontal readings leave more room for telemetry.
                Row(
                    Modifier.fillMaxWidth().height(56.dp)
                        .background(Color.White.copy(alpha = 0.045f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier.weight(1f).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatIcon(battery = true)
                            Spacer(Modifier.width(12.dp))
                            Text("0 V", color = White, fontSize = 21.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(Modifier.width(1.dp).fillMaxHeight()
                        .background(Gold.copy(alpha = 0.65f)))
                    Row(
                        Modifier.weight(1f).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatIcon(battery = false)
                            Spacer(Modifier.width(12.dp))
                            Text("0 m/s", color = White, fontSize = 21.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Thin gold separation between stats and terminal.
                Box(Modifier.fillMaxWidth().height(1.dp)
                    .background(Gold.copy(alpha = 0.55f)))

                Row(
                    Modifier.fillMaxWidth().height(30.dp)
                        .background(Color.Black.copy(alpha = 0.14f))
                        .padding(start = 10.dp, end = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            Text(
                                level, modifier = Modifier.width(48.dp),
                                color = when (level) {
                                    "SYSTEM" -> Gold
                                    "INPUT" -> Color(0xFF71C7E8)
                                    "ROBOT" -> Color(0xFFC4A1EF)
                                    "STATE" -> if (controlState == 2) Color(0xFF70D69A) else Gold
                                    "WAIT" -> Color(0xFFFFB866)
                                    else -> Gray
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp, lineHeight = 13.sp
                            )
                            Text(
                                message, color = White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp, lineHeight = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(1.dp)
            .background(Gold.copy(alpha = 0.65f)))

        Row(
            Modifier.fillMaxWidth().height(32.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF19191B), Color(0xFF26221B), Color(0xFF493719))
                    )
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("DRIVER", color = Gold, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("NETWORK", color = Gray.copy(alpha = 0.5f),
                    fontSize = 11.sp)
            }
        }
    }
}


@Composable
private fun StatIcon(battery: Boolean) {
    Canvas(Modifier.size(26.dp)) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        if (battery) {
            drawRoundRect(Gold, Offset(size.width * 0.08f, size.height * 0.27f),
                Size(size.width * 0.76f, size.height * 0.46f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f), style = stroke)
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

