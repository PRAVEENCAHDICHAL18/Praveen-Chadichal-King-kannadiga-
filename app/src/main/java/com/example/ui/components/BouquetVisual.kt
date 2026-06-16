package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BouquetVisual(
    stems: Map<String, Int>,
    wrapping: String,
    ribbon: String,
    modifier: Modifier = Modifier
) {
    // Generate a repeatable random seed based on the bouquet name characteristics
    // so the exact same bouquet always renders identically!
    val bouquetSeed = remember(stems, wrapping, ribbon) {
        val hash = (stems.keys.joinToString("") + wrapping + ribbon).hashCode()
        Random(hash)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF0EF),
                        Color(0xFFFFE0DF),
                        Color(0xFFFFF7F6)
                    )
                )
            )
            .border(2.dp, Color(0xFFFBECEC), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Central origin for the stem bouquet assembly
            val centerX = width / 2f
            val centerY = height / 2f
            
            // 1. Draw Stems in a bundle
            // Stems meet at bottom center and fan out towards top-middle
            val stemColor = Color(0xFF6E8E56) // Leaf green
            val totalStemsCount = stems.values.sum().coerceAtMost(30)
            
            for (i in 0 until totalStemsCount) {
                // Fan angles pointing upwards: from 200° (left-up) to 340° (right-up)
                val angleRad = Math.toRadians((210 + (120f / (totalStemsCount + 1)) * (i + 1)).toDouble())
                val endX = centerX + (width * 0.35f * cos(angleRad)).toFloat()
                val endY = centerY + (height * 0.35f * sin(angleRad)).toFloat()
                
                // Draw stem line fanning out
                drawLine(
                    color = stemColor,
                    start = Offset(centerX, height * 0.72f),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // 2. Draw Foliage & Leaves
            for (i in 0 until totalStemsCount) {
                val angleRad = Math.toRadians((215 + (110f / (totalStemsCount + 1)) * (i + 1)).toDouble())
                val midX = centerX + (width * 0.22f * cos(angleRad)).toFloat()
                val midY = centerY + (height * 0.22f * sin(angleRad)).toFloat()
                
                // Draw a beautiful small leaf
                drawCircle(
                    color = Color(0xFF86A36F),
                    radius = 8.dp.toPx(),
                    center = Offset(midX, midY)
                )
                drawCircle(
                    color = Color(0xFF5E794A),
                    radius = 4.dp.toPx(),
                    center = Offset(midX, midY)
                )
            }

            // 3. Draw Flowers based on actual stem contents
            var flowerIndex = 0
            stems.forEach { (flowerName, count) ->
                val countToDraw = count.coerceAtMost(10)
                
                // Determine color based on flower name keyword
                val flowerColor = when {
                    flowerName.contains("Rose", ignoreCase = true) && flowerName.contains("White", ignoreCase = true) -> Color(0xFFF9F5F0)
                    flowerName.contains("Rose", ignoreCase = true) && flowerName.contains("Crimson", ignoreCase = true) -> Color(0xFF9E0B33)
                    flowerName.contains("Rose", ignoreCase = true) && flowerName.contains("Pink", ignoreCase = true) -> Color(0xFFF07E8E)
                    flowerName.contains("Rose", ignoreCase = true) -> Color(0xFFD32F2F) // Ruby Red Roses
                    
                    flowerName.contains("Lily", ignoreCase = true) || flowerName.contains("Lilies", ignoreCase = true) -> Color(0xFFFCEFF1)
                    flowerName.contains("Sunflower", ignoreCase = true) -> Color(0xFFFFB300) // Golden Sunflower
                    flowerName.contains("Daisy", ignoreCase = true) || flowerName.contains("Daisies", ignoreCase = true) -> Color(0xFFFAF6F0)
                    
                    flowerName.contains("Lavender", ignoreCase = true) || flowerName.contains("Lilac", ignoreCase = true) -> Color(0xFFB39DDB) // Light purple
                    flowerName.contains("Orchid", ignoreCase = true) -> Color(0xFFBA68C8)
                    flowerName.contains("Tulip", ignoreCase = true) && flowerName.contains("Yellow", ignoreCase = true) -> Color(0xFFFFEB3B)
                    flowerName.contains("Tulip", ignoreCase = true) -> Color(0xFFF48FB1)
                    
                    else -> Color(0xFFFFAB91) // Coral/Pastel Peach default
                }
                
                for (c in 0 until countToDraw) {
                    // Spread coordinates
                    val angleOffset = bouquetSeed.nextDouble(-10.0, 10.0)
                    val distOffset = bouquetSeed.nextDouble(height * 0.15, height * 0.38)
                    
                    // Angle points upwards as a semi-dome: 210 to 330 degrees
                    val angleStep = 120f / (stems.values.sum().coerceAtLeast(1))
                    val baseAngle = 210 + (flowerIndex * angleStep) + angleOffset
                    val angleRad = Math.toRadians(baseAngle)
                    
                    val fx = centerX + (distOffset * cos(angleRad)).toFloat()
                    val fy = centerY + (distOffset * sin(angleRad)).toFloat()

                    // Render specific flower shapes
                    when {
                        flowerName.contains("Rose", ignoreCase = true) -> {
                            // Layered Rose petals
                            drawCircle(color = flowerColor, radius = 16.dp.toPx(), center = Offset(fx, fy))
                            drawCircle(color = flowerColor.copy(alpha = 0.85f), radius = 11.dp.toPx(), center = Offset(fx, fy))
                            drawCircle(color = Color(0x22000000), radius = 11.dp.toPx(), center = Offset(fx, fy), style = Stroke(width = 1.dp.toPx()))
                            drawCircle(color = flowerColor.darken(0.15f), radius = 6.dp.toPx(), center = Offset(fx, fy))
                        }
                        flowerName.contains("Sunflower", ignoreCase = true) -> {
                            // Center disc + golden petal spikes
                            val petals = 12
                            for (p in 0 until petals) {
                                val pAngle = Math.toRadians((360f / petals * p).toDouble())
                                val px = fx + (14.dp.toPx() * cos(pAngle)).toFloat()
                                val py = fy + (14.dp.toPx() * sin(pAngle)).toFloat()
                                drawCircle(color = Color(0xFFFFD54F), radius = 6.dp.toPx(), center = Offset(px, py))
                            }
                            drawCircle(color = Color(0xFF5D4037), radius = 10.dp.toPx(), center = Offset(fx, fy)) // Brown center
                        }
                        flowerName.contains("Lily", ignoreCase = true) || flowerName.contains("Lilies", ignoreCase = true) -> {
                            // Elegant 5-point star-like starburst
                            val lilyPath = Path().apply {
                                val points = 5
                                var outer = true
                                for (p in 0..points * 2) {
                                    val rRad = Math.toRadians((360f / (points * 2) * p - 90).toDouble())
                                    val currentRadius = if (outer) 18.dp.toPx() else 6.dp.toPx()
                                    val lx = fx + (currentRadius * cos(rRad)).toFloat()
                                    val ly = fy + (currentRadius * sin(rRad)).toFloat()
                                    if (p == 0) moveTo(lx, ly) else lineTo(lx, ly)
                                    outer = !outer
                                }
                                close()
                            }
                            drawPath(path = lilyPath, color = flowerColor)
                            drawCircle(color = Color(0xFFE57373), radius = 3.dp.toPx(), center = Offset(fx, fy)) // Center ovary
                        }
                        flowerName.contains("Daisy", ignoreCase = true) || flowerName.contains("Daisies", ignoreCase = true) || flowerName.contains("Chrysanthemum", ignoreCase = true) -> {
                            // White daisies with yellow center
                            val daisyPetals = 8
                            for (dp in 0 until daisyPetals) {
                                val dpAngle = Math.toRadians((360f / daisyPetals * dp).toDouble())
                                val dpx = fx + (10.dp.toPx() * cos(dpAngle)).toFloat()
                                val dpy = fy + (10.dp.toPx() * sin(dpAngle)).toFloat()
                                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(dpx, dpy))
                            }
                            drawCircle(color = Color(0xFFFFCA28), radius = 6.dp.toPx(), center = Offset(fx, fy)) // Yellow pistil
                        }
                        flowerName.contains("Lavender", ignoreCase = true) || flowerName.contains("Lilac", ignoreCase = true) -> {
                            // Stack of purple circles along a tall stem
                            val stalkAngle = Math.toRadians((baseAngle - 5).toDouble())
                            for (j in 0..4) {
                                val sOffset = j * 8.dp.toPx()
                                val sx = fx - (sOffset * cos(stalkAngle)).toFloat()
                                val sy = fy - (sOffset * sin(stalkAngle)).toFloat()
                                drawCircle(color = flowerColor, radius = 5.dp.toPx(), center = Offset(sx, sy))
                                drawCircle(color = flowerColor.lighten(0.15f), radius = 3.dp.toPx(), center = Offset(sx - 2f, sy - 2f))
                            }
                        }
                        else -> {
                            // Universal lovely 5-petal wildflower
                            val wildPetals = 5
                            for (wp in 0 until wildPetals) {
                                val wpAngle = Math.toRadians((360f / wildPetals * wp).toDouble())
                                val wpx = fx + (9.dp.toPx() * cos(wpAngle)).toFloat()
                                val wpy = fy + (9.dp.toPx() * sin(wpAngle)).toFloat()
                                drawCircle(color = flowerColor, radius = 6.dp.toPx(), center = Offset(wpx, wpy))
                            }
                            drawCircle(color = Color(0xFFFFF176), radius = 4.dp.toPx(), center = Offset(fx, fy))
                        }
                    }
                    flowerIndex++
                }
            }

            // 4. Draw Bouquet Wrapping
            // Cone shape from center meeting at bottom center fanning out
            val wrapColor = when {
                wrapping.contains("Teal", ignoreCase = true) -> Color(0xFF26A69A)
                wrapping.contains("Pink", ignoreCase = true) -> Color(0xFFF48FB1)
                wrapping.contains("Kraft", ignoreCase = true) -> Color(0xFFD7CCC8) // Soft Brown Kraft
                wrapping.contains("Waxed", ignoreCase = true) -> Color(0xFFBCAAA4)
                wrapping.contains("Burlap", ignoreCase = true) -> Color(0xFFE0D4C3) // Rustic Burlap Tan
                wrapping.contains("Slate", ignoreCase = true) || wrapping.contains("Gray", ignoreCase = true) -> Color(0xFF90A4AE)
                else -> Color(0xFFEF9A9A) // Default romantic soft red wrap
            }

            val pathWrap = Path().apply {
                // Pinched bottom point where hand holds
                moveTo(centerX, height * 0.75f)
                // Left wrap wing
                lineTo(centerX - width * 0.38f, centerY + height * 0.15f)
                // Left drapery arch
                quadraticBezierTo(centerX - width * 0.2f, centerY + height * 0.05f, centerX, centerY + height * 0.12f)
                // Right drapery arch
                quadraticBezierTo(centerX + width * 0.2f, centerY + height * 0.05f, centerX + width * 0.38f, centerY + height * 0.15f)
                // Close back to pinched bottom point
                lineTo(centerX, height * 0.75f)
            }
            // Draw wrapping paper with solid + translucent shading overlay
            drawPath(path = pathWrap, color = wrapColor)
            drawPath(path = pathWrap, color = Color(0x33FFFFFF)) // Shine highlight overlay
            
            // Draw wrapping outlines
            drawPath(path = pathWrap, color = wrapColor.darken(0.12f), style = Stroke(width = 1.5.dp.toPx()))

            // Draw a subtle dark shadow inside fold lines of wrapping paper
            val foldPath = Path().apply {
                moveTo(centerX, height * 0.75f)
                lineTo(centerX - width * 0.12f, centerY + height * 0.12f)
                moveTo(centerX, height * 0.75f)
                lineTo(centerX + width * 0.12f, centerY + height * 0.12f)
            }
            drawPath(path = foldPath, color = wrapColor.darken(0.2f), style = Stroke(width = 1.dp.toPx()))

            // 5. Draw Tied Ribbon & Bow
            val ribbonColor = when {
                ribbon.contains("Gold", ignoreCase = true) -> Color(0xFFFFD54F)
                ribbon.contains("White", ignoreCase = true) || ribbon.contains("Ivory", ignoreCase = true) -> Color(0xFFFFFDF9)
                ribbon.contains("Satin", ignoreCase = true) && ribbon.contains("Coral", ignoreCase = true) -> Color(0xFFFF8A80)
                ribbon.contains("Organza", ignoreCase = true) -> Color(0xBBD4E157)
                ribbon.contains("Jute", ignoreCase = true) || ribbon.contains("Twine", ignoreCase = true) -> Color(0xFFA1887F)
                else -> Color(0xFFFF4081) // Hot Pink Ribbon
            }

            val ribbonY = height * 0.72f
            
            // Hanging bow strand paths
            val leftStrand = Path().apply {
                moveTo(centerX - 3.dp.toPx(), ribbonY)
                quadraticBezierTo(centerX - width * 0.15f, ribbonY + height * 0.12f, centerX - width * 0.10f, ribbonY + height * 0.20f)
            }
            val rightStrand = Path().apply {
                moveTo(centerX + 3.dp.toPx(), ribbonY)
                quadraticBezierTo(centerX + width * 0.15f, ribbonY + height * 0.12f, centerX + width * 0.10f, ribbonY + height * 0.20f)
            }
            drawPath(path = leftStrand, color = ribbonColor, style = Stroke(width = 3.dp.toPx()))
            drawPath(path = rightStrand, color = ribbonColor, style = Stroke(width = 3.dp.toPx()))

            // Draw Bow Loops
            // Draw left loop ellipse
            drawOval(
                color = ribbonColor,
                topLeft = Offset(centerX - 24.dp.toPx(), ribbonY - 8.dp.toPx()),
                size = Size(20.dp.toPx(), 16.dp.toPx())
            )
            // Draw right loop ellipse
            drawOval(
                color = ribbonColor,
                topLeft = Offset(centerX + 4.dp.toPx(), ribbonY - 8.dp.toPx()),
                size = Size(20.dp.toPx(), 16.dp.toPx())
            )
            
            // Draw tie knot in the center
            drawCircle(color = ribbonColor.darken(0.12f), radius = 5.dp.toPx(), center = Offset(centerX, ribbonY))
        }
    }
}

// Color modification helper extensions to keep it beautifully shades
private fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
