package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkNavyShield
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonTeal

@Composable
fun GlowingShield(
  modifier: Modifier = Modifier,
  size: Dp = 190.dp,
  isScanning: Boolean = false,
  isProtected: Boolean = true
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  val primaryAccent = if (isProtected) NeonEmerald else Color(0xFFEF4444)
  val secondaryAccent = if (isProtected) NeonCyan else Color(0xFFF59E0B)

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val w = this.size.width
      val h = this.size.height
      val cx = w / 2f
      val cy = h / 2f

      // 1. Outer ambient glow ring
      val currentPulse = if (isScanning) pulseScale else 1f
      val currentAlpha = if (isScanning) pulseAlpha else 0.5f

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            primaryAccent.copy(alpha = currentAlpha * 0.45f),
            secondaryAccent.copy(alpha = currentAlpha * 0.15f),
            Color.Transparent
          ),
          center = Offset(cx, cy),
          radius = (w * 0.48f) * currentPulse
        ),
        radius = (w * 0.48f) * currentPulse,
        center = Offset(cx, cy)
      )

      // 2. Tech grid concentric ring
      drawCircle(
        color = primaryAccent.copy(alpha = 0.25f),
        radius = w * 0.42f,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5.dp.toPx())
      )

      // 3. Shield Outer Path
      val shieldPath = Path().apply {
        moveTo(cx, cy - h * 0.34f)
        lineTo(cx + w * 0.30f, cy - h * 0.22f)
        cubicTo(
          cx + w * 0.30f, cy + h * 0.15f,
          cx + w * 0.16f, cy + h * 0.34f,
          cx, cy + h * 0.40f
        )
        cubicTo(
          cx - w * 0.16f, cy + h * 0.34f,
          cx - w * 0.30f, cy + h * 0.15f,
          cx - w * 0.30f, cy - h * 0.22f
        )
        close()
      }

      // Draw shield background
      drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
          colors = listOf(
            DarkNavyShield,
            Color(0xFF07121E)
          ),
          startY = cy - h * 0.34f,
          endY = cy + h * 0.40f
        )
      )

      // Draw shield neon stroke
      drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
          colors = listOf(primaryAccent, secondaryAccent),
          startY = cy - h * 0.34f,
          endY = cy + h * 0.40f
        ),
        style = Stroke(width = 3.5.dp.toPx())
      )

      // 4. Inner Shield Contour
      val innerShield = Path().apply {
        moveTo(cx, cy - h * 0.28f)
        lineTo(cx + w * 0.24f, cy - h * 0.18f)
        cubicTo(
          cx + w * 0.24f, cy + h * 0.12f,
          cx + w * 0.13f, cy + h * 0.27f,
          cx, cy + h * 0.32f
        )
        cubicTo(
          cx - w * 0.13f, cy + h * 0.27f,
          cx - w * 0.24f, cy + h * 0.12f,
          cx - w * 0.24f, cy - h * 0.18f
        )
        close()
      }
      drawPath(
        path = innerShield,
        color = NeonTeal.copy(alpha = 0.3f),
        style = Stroke(width = 1.dp.toPx())
      )

      // 5. Stylized Central "X" Emblem
      val xSpan = w * 0.12f
      val xTop = cy - h * 0.10f
      val xBottom = cy + h * 0.10f

      // Main X strokes with rounded caps
      drawLine(
        brush = Brush.linearGradient(listOf(primaryAccent, secondaryAccent)),
        start = Offset(cx - xSpan, xTop),
        end = Offset(cx + xSpan, xBottom),
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
      )
      drawLine(
        brush = Brush.linearGradient(listOf(secondaryAccent, primaryAccent)),
        start = Offset(cx + xSpan, xTop),
        end = Offset(cx - xSpan, xBottom),
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
      )
    }
  }
}
