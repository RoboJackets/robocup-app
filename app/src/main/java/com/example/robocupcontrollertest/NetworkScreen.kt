package com.example.robocupcontrollertest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NetGold = Color(0xFFFFC72C)
private val NetWhite = Color(0xFFF5F3ED)
private val NetMuted = Color(0xFFAAA59B)
private val NetLine = NetGold.copy(alpha = 0.55f)

// The networking layer can populate these with real data later.
// Null means "not measured", rather than an invented zero.
data class NetworkStats(
    val connected: Boolean = false,
    val destination: String = "Not configured",
    val port: Int? = null,
    val latencyMs: Int? = null,
    val packetLossPercent: Float? = null,
    val txPerSecond: Int? = null,
    val rxPerSecond: Int? = null,
    val lastResponse: String = "Never",
    val signalDbm: Int? = null,
    val lossHistory: List<Float> = emptyList(),
    val packets: List<PacketEntry> = emptyList()
)

data class PacketEntry(val direction: String, val payload: String, val timestamp: String)

// The shared header and DRIVER / NETWORK footer are owned by DriverStationScreen.
@Composable
fun NetworkScreen(
    robot: Int,
    stats: NetworkStats = NetworkStats()
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 320.dp
        val metricHeight = if (compact) 42.dp else 56.dp
        val labelHeight = if (compact) 22.dp else 28.dp

        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                // Match the gold robot banner on DRIVER; compact connection rows fit without scrolling.
                Column(Modifier.weight(0.36f).fillMaxHeight().clipToBounds()) {
                    Column(
                        Modifier.fillMaxWidth().height(56.dp).background(NetGold),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SELECTED ROBOT", color = Color(0xFF4A3A18),
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.7.sp, lineHeight = 14.sp,
                            textAlign = TextAlign.Center, maxLines = 1)
                        Text("ROBOT %02d".format(robot), color = Color(0xFF171717),
                            fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 23.sp,
                            textAlign = TextAlign.Center, maxLines = 1)
                    }
                    // Keep connection details together near the top of the left panel.
                    Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.TopCenter) {
                        Column(Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth().height(29.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("CONNECTION", color = NetGold, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Canvas(Modifier.size(8.dp)) {
                                    drawCircle(if (stats.connected) Color(0xFF70D69A)
                                    else Color(0xFFFF8A7A))
                                }
                                Spacer(Modifier.width(7.dp))
                                Text(if (stats.connected) "Connected" else "Disconnected",
                                    color = NetWhite, fontSize = 11.sp, maxLines = 1)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(NetLine))
                            ConnectionDetail("TRANSPORT", "UDP")
                            ConnectionDetail("DESTINATION", stats.destination)
                            ConnectionDetail("PORT", stats.port?.toString() ?: "—")
                            ConnectionDetail("LAST RESPONSE", stats.lastResponse)
                            ConnectionDetail("WI-FI SIGNAL",
                                stats.signalDbm?.let { "$it dBm" } ?: "—")
                        }
                    }
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(NetLine))

                // Right side has flexible graph and log heights; neither extends past the footer.
                Column(Modifier.weight(0.64f).fillMaxHeight().clipToBounds()) {
                    Row(Modifier.fillMaxWidth().height(metricHeight),
                        verticalAlignment = Alignment.CenterVertically) {
                        Metric("LOSS", stats.packetLossPercent?.let { "%.1f%%".format(it) } ?: "—", compact)
                        MetricDivider()
                        Metric("LATENCY", stats.latencyMs?.let { "$it ms" } ?: "—", compact)
                        MetricDivider()
                        Metric("TX/s", stats.txPerSecond?.toString() ?: "—", compact)
                        MetricDivider()
                        Metric("RX/s", stats.rxPerSecond?.toString() ?: "—", compact)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(NetLine))
                    SectionHeading("PACKET LOSS HISTORY", "0–100%", labelHeight)
                    Box(Modifier.weight(0.42f).fillMaxWidth()
                        .padding(start = 12.dp, end = 15.dp, bottom = 5.dp)) {
                        LossGraph(stats.lossHistory, Modifier.fillMaxSize())
                        if (stats.lossHistory.isEmpty()) Text("No measurements yet",
                            color = NetMuted, fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.Center))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(NetLine))
                    SectionHeading("PACKET LOG", "TX / RX", labelHeight)
                    Box(Modifier.weight(0.58f).fillMaxWidth()) {
                        if (stats.packets.isEmpty()) Text("No packets recorded yet",
                            color = NetMuted, fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.Center))
                        else LazyColumn(Modifier.fillMaxSize().padding(horizontal = 10.dp),
                            contentPadding = PaddingValues(vertical = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(stats.packets) { packet ->
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(packet.timestamp, color = NetMuted, fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace, maxLines = 1)
                                    Text(packet.direction,
                                        color = if (packet.direction == "TX") NetGold
                                        else Color(0xFF71C7E8),
                                        fontSize = 9.sp, fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold)
                                    Text(packet.payload, color = NetWhite, fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace, maxLines = 2,
                                        overflow = TextOverflow.Ellipsis)
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
private fun SectionHeading(title: String, suffix: String, height: androidx.compose.ui.unit.Dp) {
    Row(Modifier.fillMaxWidth().height(height).padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("●", color = NetGold, fontSize = 9.sp)
        Spacer(Modifier.width(6.dp))
        Text(title, color = NetWhite, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.weight(1f))
        Text(suffix, color = NetMuted, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun ConnectionDetail(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = NetMuted, fontSize = 11.sp, maxLines = 1,
            modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(5.dp))
        Text(value, color = NetWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, modifier = Modifier.weight(1f), textAlign = TextAlign.End,
            overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun RowScope.Metric(label: String, value: String, compact: Boolean) {
    Column(Modifier.weight(1f).padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.Center) {
        Text(label, color = NetMuted, fontSize = if (compact) 8.sp else 9.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = NetWhite, fontSize = if (compact) 15.sp else 18.sp,
            fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MetricDivider() {
    Box(Modifier.width(1.dp).fillMaxHeight().background(NetLine.copy(alpha = 0.65f)))
}

@Composable
private fun LossGraph(history: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        for (step in 0..4) {
            val y = size.height * step / 4f
            drawLine(NetWhite.copy(alpha = 0.12f), Offset(0f, y),
                Offset(size.width, y), 1.dp.toPx())
        }
        val values = history.takeLast(60)
        if (values.isNotEmpty()) {
            val plot = Path()
            values.forEachIndexed { index, percent ->
                val x = if (values.size == 1) size.width / 2 else
                    index.toFloat() / (values.size - 1) * size.width
                val y = size.height * (1f - percent.coerceIn(0f, 100f) / 100f)
                if (index == 0) plot.moveTo(x, y) else plot.lineTo(x, y)
            }
            drawPath(plot, NetGold, style = Stroke(width = 2.dp.toPx()))
            if (values.size == 1) drawCircle(NetGold, 3.dp.toPx(),
                Offset(size.width / 2, size.height *
                        (1f - values[0].coerceIn(0f, 100f) / 100f)))
        }
    }
}
