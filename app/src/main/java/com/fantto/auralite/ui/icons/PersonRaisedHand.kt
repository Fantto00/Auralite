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
public val person_raised_hand: ImageVector
  get() {
    if (_person_raised_hand != null) {
      return _person_raised_hand!!
    }
    _person_raised_hand =
      ImageVector.Builder(
          name = "person_raised_hand",
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
            moveTo(2f, 23f)
            verticalLineTo(21f)
            horizontalLineTo(22f)
            verticalLineToRelative(2f)
            horizontalLineTo(2f)
            close()
            moveTo(4f, 20f)
            verticalLineTo(14f)
            quadTo(3.18f, 12.65f, 2.73f, 11.14f)
            reflectiveQuadTo(2.28f, 8.05f)
            quadToRelative(0f, -1.53f, 0.39f, -3f)
            quadTo(3.05f, 3.57f, 3.58f, 2.15f)
            quadTo(3.78f, 1.63f, 4.23f, 1.31f)
            reflectiveQuadTo(5.23f, 1f)
            quadTo(6f, 1f, 6.55f, 1.52f)
            reflectiveQuadTo(7f, 2.77f)
            lineTo(6.73f, 5.05f)
            quadTo(6.58f, 6.25f, 6.94f, 7.32f)
            reflectiveQuadTo(8.03f, 9.21f)
            reflectiveQuadToRelative(1.75f, 1.3f)
            reflectiveQuadTo(12f, 11f)
            quadToRelative(1.5f, 0f, 3.01f, 0.31f)
            reflectiveQuadToRelative(2.64f, 0.89f)
            reflectiveQuadToRelative(1.74f, 1.46f)
            reflectiveQuadTo(20f, 15.85f)
            verticalLineTo(20f)
            horizontalLineTo(4f)
            close()
            moveTo(6f, 18f)
            horizontalLineTo(18f)
            verticalLineTo(15.85f)
            quadToRelative(0f, -0.6f, -0.3f, -1.06f)
            reflectiveQuadTo(16.85f, 14.05f)
            quadToRelative(-1.03f, -0.5f, -2.38f, -0.78f)
            reflectiveQuadTo(12f, 13f)
            quadTo(10.35f, 13f, 8.94f, 12.33f)
            quadTo(7.53f, 11.65f, 6.54f, 10.51f)
            reflectiveQuadTo(5.05f, 7.89f)
            reflectiveQuadTo(4.75f, 4.8f)
            quadTo(4.5f, 5.55f, 4.39f, 6.4f)
            reflectiveQuadTo(4.28f, 8.05f)
            quadToRelative(0f, 1.45f, 0.51f, 2.79f)
            quadTo(5.3f, 12.18f, 6f, 13.45f)
            verticalLineTo(18f)
            close()
            moveTo(9.18f, 8.82f)
            quadTo(8f, 7.65f, 8f, 6f)
            reflectiveQuadTo(9.18f, 3.17f)
            reflectiveQuadTo(12f, 2f)
            reflectiveQuadToRelative(2.83f, 1.17f)
            reflectiveQuadTo(16f, 6f)
            reflectiveQuadTo(14.83f, 8.82f)
            reflectiveQuadTo(12f, 10f)
            reflectiveQuadTo(9.18f, 8.82f)
            close()
            moveTo(13.41f, 7.41f)
            quadTo(14f, 6.82f, 14f, 6f)
            reflectiveQuadTo(13.41f, 4.59f)
            reflectiveQuadTo(12f, 4f)
            reflectiveQuadTo(10.59f, 4.59f)
            quadTo(10f, 5.18f, 10f, 6f)
            reflectiveQuadToRelative(0.59f, 1.41f)
            reflectiveQuadTo(12f, 8f)
            reflectiveQuadTo(13.41f, 7.41f)
            close()
            moveTo(8f, 20f)
            verticalLineTo(19.08f)
            quadTo(8f, 17.4f, 9.16f, 16.2f)
            reflectiveQuadTo(12f, 15f)
            horizontalLineToRelative(4f)
            verticalLineToRelative(2f)
            horizontalLineTo(12f)
            quadToRelative(-0.85f, 0f, -1.42f, 0.61f)
            reflectiveQuadTo(10f, 19.08f)
            verticalLineTo(20f)
            horizontalLineTo(8f)
            close()
            moveToRelative(4f, -2f)
            close()
            moveTo(12f, 6f)
            close()
          }
        }
        .build()
    return _person_raised_hand!!
  }

private var _person_raised_hand: ImageVector? = null
