package com.fantto.auralite.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val ai_icon: ImageVector
  get() {
    if (_ai_icon != null) {
      return _ai_icon!!
    }
    _ai_icon =
      ImageVector.Builder(
          name = "cognition_2",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(9.5f, 15f)
            quadToRelative(1.05f, 0f, 1.78f, -0.73f)
            lineToRelative(4f, -4f)
            quadTo(16f, 9.55f, 16f, 8.5f)
            reflectiveQuadTo(15.28f, 6.72f)
            reflectiveQuadTo(13.5f, 6f)
            reflectiveQuadTo(11.73f, 6.72f)
            quadTo(10.8f, 6.4f, 9.9f, 6.57f)
            reflectiveQuadTo(8.38f, 7.38f)
            reflectiveQuadTo(7.58f, 8.9f)
            reflectiveQuadToRelative(0.15f, 1.83f)
            quadTo(7f, 11.45f, 7f, 12.5f)
            reflectiveQuadToRelative(0.73f, 1.77f)
            reflectiveQuadTo(9.5f, 15f)
            close()
            moveTo(6f, 22f)
            verticalLineTo(17.7f)
            quadTo(4.58f, 16.4f, 3.79f, 14.66f)
            reflectiveQuadTo(3f, 11f)
            quadTo(3f, 7.25f, 5.63f, 4.63f)
            reflectiveQuadTo(12f, 2f)
            quadToRelative(3.13f, 0f, 5.54f, 1.84f)
            quadToRelative(2.41f, 1.84f, 3.14f, 4.79f)
            lineToRelative(1.3f, 5.13f)
            quadToRelative(0.13f, 0.47f, -0.18f, 0.86f)
            reflectiveQuadTo(21f, 15f)
            horizontalLineTo(19f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(17f, 20f)
            horizontalLineTo(15f)
            verticalLineToRelative(2f)
            horizontalLineTo(13f)
            verticalLineTo(18f)
            horizontalLineToRelative(4f)
            verticalLineTo(13f)
            horizontalLineToRelative(2.7f)
            lineTo(18.75f, 9.13f)
            quadTo(18.18f, 6.85f, 16.3f, 5.43f)
            reflectiveQuadTo(12f, 4f)
            quadTo(9.1f, 4f, 7.05f, 6.02f)
            reflectiveQuadTo(5f, 10.95f)
            quadToRelative(0f, 1.5f, 0.61f, 2.85f)
            reflectiveQuadToRelative(1.74f, 2.4f)
            lineTo(8f, 16.8f)
            verticalLineTo(22f)
            horizontalLineTo(6f)
            close()
            moveToRelative(6.35f, -9f)
            close()
          }
        }
        .build()
    return _ai_icon!!
  }

private var _ai_icon: ImageVector? = null
