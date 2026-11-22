package com.example.robocupcontrollertest

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
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
private val StationGradient = Brush.horizontalGradient(
    listOf(Color(0xFF2B2822), Color(0xFF262522), Color(0xFF232426))
)
private val Green = Color(0xFF238C50)
private val Red = Color(0xFFC03939)

@Composable
fun DriverStationScreen() {
    var robot by remember { mutableIntStateOf(1) }
    var running by remember { mutableStateOf(false) }
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
        "SYSTEM" to "Driver station initialized",
        "INPUT" to "Waiting for controller input",
        "ROBOT" to "Robot %02d selected".format(robot),
        "STATE" to if (running) "Start selected" else "Stop selected",
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
                val buttonSize = minOf(
                    maxWidth * 0.48f, maxHeight * 0.34f, 105.dp
                )
                val gap = minOf(maxHeight * 0.11f, 28.dp)
                val selectorWidth = minOf(150.dp, maxWidth - 20.dp)

                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Compact selector centered above the play button.
                    Box(Modifier.width(selectorWidth)) {
                        Row(
                            Modifier.fillMaxWidth().height(42.dp)
                                .clickable { robotMenu = true },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ROBOT", color = Gold, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                            Spacer(Modifier.width(13.dp))
                            Text("%02d".format(robot), color = White, fontSize = 25.sp,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(9.dp))
                            Text("▾", color = Gold, fontSize = 19.sp)
                        }
                        DropdownMenu(
                            expanded = robotMenu,
                            onDismissRequest = { robotMenu = false },
                            modifier = Modifier.width(selectorWidth)
                                .background(StationGradient)
                        ) {
                            (1..6).forEach { id ->
                                val selected = id == robot
                                DropdownMenuItem(
                                    modifier = Modifier.height(42.dp).background(
                                        if (selected) Gold.copy(alpha = 0.13f)
                                        else Color.Transparent
                                    ),
                                    text = {
                                        Box(Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center) {
                                            Text("%02d".format(id),
                                                color = if (selected) Gold else White,
                                                fontSize = 17.sp,
                                                fontWeight = if (selected) FontWeight.Bold
                                                else FontWeight.Medium)
                                        }
                                    },
                                    onClick = { robot = id; robotMenu = false }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(gap))

                    Canvas(
                        Modifier.size(buttonSize)
                            .background(
                                if (running) Brush.verticalGradient(
                                    listOf(Color(0xFFFFD959), Gold, Color(0xFFE6AA10))
                                ) else Brush.verticalGradient(
                                    listOf(Color(0xFF36332A), Color(0xFF232321))
                                ), CircleShape
                            )
                            .border(2.dp, Gold.copy(alpha = if (running) 1f else 0.8f), CircleShape)
                            .clickable { running = !running }
                    ) {
                        if (running) {
                            val side = size.minDimension * 0.29f
                            drawRoundRect(Color(0xFF242424), Offset((size.width - side) / 2,
                                (size.height - side) / 2), Size(side, side),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                        } else {
                            val triangle = Path().apply {
                                moveTo(size.width * 0.39f, size.height * 0.30f)
                                lineTo(size.width * 0.69f, size.height * 0.50f)
                                lineTo(size.width * 0.39f, size.height * 0.70f)
                                close()
                            }
                            drawPath(triangle, Gold)
                        }
                    }

                    Spacer(Modifier.height(9.dp))

                    Text(
                        if (running) "● ON" else "○ OFF",
                        color = if (running) Gold else Gray,
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                Modifier.fillMaxHeight().width(1.dp)
                    .background(Gold.copy(alpha = 0.55f))
            )

            Column(Modifier.weight(0.64f).fillMaxHeight()) {
                // Compact horizontal readings leave more room for telemetry.
                Row(
                    Modifier.fillMaxWidth().height(48.dp)
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
                    Spacer(Modifier.weight(1f))
                    Text("DEBUG", color = Gray, fontSize = 9.sp)
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
                                    "STATE" -> if (running) Color(0xFF70D69A) else Gold
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
