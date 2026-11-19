package com.example.ui.screens.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.LocalStrings
import com.example.domain.model.NotificationType
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun NotificationsScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("notifications_screen")
    ) {
        if (state.notifications.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.markAllNotificationsRead() }) {
                    Icon(Icons.Outlined.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.markAllRead)
                }
            }
        }

        if (state.notifications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.NotificationsNone,
                title = strings.notifications,
                description = strings.noNotifications
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.notifications, key = { it.id }) { notif ->
                    BasluneCard(
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            notif.targetRoute?.let(onNavigate)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, style = MaterialTheme.typography.titleMedium)
                                Text(notif.message, style = MaterialTheme.typography.bodySmall)
                            }
                            if (!notif.isRead) {
                                StatusBadge(
                                    text = "•",
                                    tone = when (notif.type) {
                                        NotificationType.OVERDUE_TASK, NotificationType.UNPAID_INVOICE -> BadgeTone.ERROR
                                        NotificationType.UPCOMING_DEADLINE, NotificationType.FOLLOW_UP_DUE -> BadgeTone.WARNING
                                        else -> BadgeTone.GOLD
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
