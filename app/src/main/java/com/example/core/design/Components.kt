package com.example.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalStrings
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun BasluneCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun BasluneDarkHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = DeepEspresso,
        border = BorderStroke(1.dp, RichBrown),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeepEspresso, RichBrown)
                    )
                )
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    tone: BadgeTone = BadgeTone.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone) {
        BadgeTone.SUCCESS -> SuccessContainer to SuccessGreen
        BadgeTone.WARNING -> WarningContainer to WarningAmber
        BadgeTone.ERROR -> ErrorContainer to ErrorRed
        BadgeTone.GOLD -> ChampagneGold.copy(alpha = 0.28f) to RichBrown
        BadgeTone.DARK -> DeepEspresso to ChampagneGold
        BadgeTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(0.5.dp, fg.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class BadgeTone {
    NEUTRAL,
    SUCCESS,
    WARNING,
    ERROR,
    GOLD,
    DARK
}

@Composable
fun SampleOrStarterBadge(
    isSample: Boolean,
    isStarter: Boolean = false
) {
    val strings = LocalStrings.current
    if (isSample) {
        StatusBadge(text = strings.sampleBadge, tone = BadgeTone.WARNING)
    } else if (isStarter) {
        StatusBadge(text = strings.starterBadge, tone = BadgeTone.GOLD)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun KpiStatCard(
    title: String,
    primaryValue: String,
    secondaryValue: String? = null,
    icon: ImageVector,
    tone: BadgeTone = BadgeTone.NEUTRAL,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val accentColor = when (tone) {
        BadgeTone.SUCCESS -> SuccessGreen
        BadgeTone.WARNING -> WarningAmber
        BadgeTone.ERROR -> ErrorRed
        BadgeTone.GOLD -> ChampagneGold
        BadgeTone.DARK -> DeepEspresso
        BadgeTone.NEUTRAL -> RichBrown
    }
    BasluneCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = primaryValue,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!secondaryValue.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = secondaryValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Outlined.Info,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BasluneCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ChampagneGold.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = RichBrown,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("empty_state_action")
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ConfirmDestructiveDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                modifier = Modifier.testTag("confirm_destructive_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun <T> FilterChipRow(
    items: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(item) },
                label = { Text(labelOf(item), maxLines = 1) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeepEspresso,
                    selectedLabelColor = ChampagneGold,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = DeepEspresso
                )
            )
        }
    }
}

data class BarChartPoint(
    val label: String,
    val primaryValue: Float,
    val secondaryValue: Float = 0f
)

@Composable
fun BasluneDualBarChart(
    data: List<BarChartPoint>,
    primaryColor: Color = SuccessGreen,
    secondaryColor: Color = ErrorRed,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    val maxVal = max(
        1f,
        data.maxOf { max(it.primaryValue, it.secondaryValue) }
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            val slotWidth = size.width / data.size
            val barWidth = (slotWidth * 0.28f).coerceIn(6f, 28f)
            val chartHeight = size.height - 12f
            // Baseline
            drawLine(
                color = SoftBorders,
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = 2f
            )
            data.forEachIndexed { idx, point ->
                val centerX = slotWidth * idx + slotWidth / 2f
                val h1 = (point.primaryValue / maxVal) * (chartHeight - 10f)
                val h2 = (point.secondaryValue / maxVal) * (chartHeight - 10f)

                if (h1 > 0f) {
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(centerX - barWidth - 2f, chartHeight - h1),
                        size = Size(barWidth, h1),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
                if (h2 > 0f) {
                    drawRoundRect(
                        color = secondaryColor,
                        topLeft = Offset(centerX + 2f, chartHeight - h2),
                        size = Size(barWidth, h2),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun BasluneDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(modifier = Modifier.size(110.dp)) {
            val strokeWidth = 24f
            var startAngle = -90f
            slices.forEach { slice ->
                if (slice.value > 0f) {
                    val sweep = (slice.value / total) * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2f).coerceAtLeast(1f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                    startAngle += sweep
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.filter { it.value > 0f }.forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    Text(
                        text = "${slice.label} (${slice.value.toInt()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BasluneTrendLineChart(
    points: List<Pair<String, Float>>,
    lineColor: Color = ChampagneGold,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    val maxVal = max(1f, points.maxOf { it.second })
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val stepX = if (points.size > 1) size.width / (points.size - 1) else size.width / 2f
            val chartHeight = size.height - 16f
            val path = Path()
            val coords = points.mapIndexed { idx, (_, v) ->
                val x = if (points.size > 1) idx * stepX else size.width / 2f
                val y = chartHeight - ((v / maxVal) * (chartHeight - 16f))
                Offset(x, y)
            }
            coords.forEachIndexed { idx, pt ->
                if (idx == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
            coords.forEach { pt ->
                drawCircle(color = DeepEspresso, radius = 8f, center = pt)
                drawCircle(color = lineColor, radius = 5f, center = pt)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
