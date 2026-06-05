package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.PinSaveTopBar
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel, onSignOut: () -> Unit) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isAccountLinked by viewModel.isAccountLinked.collectAsState()
    val pinterestUsername by viewModel.pinterestUsername.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        PinSaveTopBar(showMenu = false)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Manage your app preferences and account details.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            SettingsSection("Account") {
                val accountSubtitle = if (isAccountLinked) "Linked as @$pinterestUsername" else "Not Linked"
                SettingsItem("Pinterest Account", accountSubtitle)
                SettingsItem("Data Usage", "Manage caching and storage")
            }

            SettingsSection("Download Preferences") {
                SettingsItem("Default Quality", "High (Original Resolution)")
                SettingsItem("Save Location", "/Downloads/PinSave", isCode = true)
            }

            SettingsSection("Appearance") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Theme", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Switch between light and dark mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isDarkTheme, onCheckedChange = { viewModel.setDarkTheme(it) })
                }
            }

            SettingsSection("About") {
                SettingsItem("Privacy Policy", null)
                SettingsItem("Terms of Service", null)
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                    Text("PinSave v1.0.4 (Build 492)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        content()
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String?, isCode: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            if (subtitle != null) {
                if (isCode) {
                    Box(modifier = Modifier.padding(top = 4.dp).background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.small).border(1.dp, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small).padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = com.example.ui.theme.CodeFont)
                    }
                } else {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        if (subtitle != null && !isCode || subtitle == null) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
