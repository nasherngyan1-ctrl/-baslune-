package com.example.ui.screens.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.design.*
import com.example.core.localization.LocalStrings
import com.example.core.security.BusinessPermission
import com.example.core.security.RolePermissions
import com.example.data.local.UserMemberEntity
import com.example.domain.model.UserRole
import com.example.ui.theme.ErrorRed
import com.example.ui.viewmodel.BasluneMainViewModel
import com.example.ui.viewmodel.BasluneUiState

@Composable
fun TeamScreen(
    state: BasluneUiState,
    viewModel: BasluneMainViewModel
) {
    val strings = LocalStrings.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_member_fab")
            ) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = strings.addMember)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
        ) {
            item {
                BasluneCard {
                    Text(strings.switchActiveRoleHint, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChipRow(
                        items = UserRole.entries,
                        selected = state.settings.activeRole,
                        labelOf = { strings.label(it) },
                        onSelect = { role -> viewModel.setActiveRole(role) }
                    )
                }
            }

            item {
                SectionHeader(title = "${strings.navTeam} (${state.teamMembers.size})")
            }

            items(state.teamMembers, key = { it.id }) { member ->
                BasluneCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${strings.label(member.role)} • ${member.department.ifBlank { strings.studioName }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { viewModel.setActiveRole(member.role, member.name) }
                            ) {
                                Text(strings.activeRole)
                            }
                            if (member.role != UserRole.OWNER || state.teamMembers.count { it.role == UserRole.OWNER } > 1) {
                                IconButton(onClick = { viewModel.deleteTeamMember(member.id) }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = strings.delete, tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }

            item {
                BasluneCard {
                    Text(strings.permissionsMatrix, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    UserRole.entries.forEach { role ->
                        val perms = RolePermissions.permissionsFor(role)
                        Text(
                            text = "${strings.label(role)}: ${perms.size}/${BusinessPermission.entries.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(UserRole.STAFF) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.addMember) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("${strings.memberName} *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(strings.email) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text(strings.memberDepartment) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text(strings.memberRole, style = MaterialTheme.typography.labelMedium)
                    FilterChipRow(items = UserRole.entries, selected = role, labelOf = { strings.label(it) }, onSelect = { role = it })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank()) return@Button
                    viewModel.saveTeamMember(UserMemberEntity(name = name.trim(), email = email.trim(), role = role, department = dept.trim()))
                    showAddDialog = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text(strings.cancel) } }
        )
    }
}
