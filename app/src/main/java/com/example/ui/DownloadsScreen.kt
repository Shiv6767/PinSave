package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PinItem
import com.example.ui.components.PinSaveTopBar
import com.example.viewmodel.MainViewModel

@Composable
fun DownloadsScreen(viewModel: MainViewModel) {
    val activePins by viewModel.activePins.collectAsState()
    val downloadedPins by viewModel.downloadedPins.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        PinSaveTopBar(showMenu = false)

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (activePins.isNotEmpty()) {
                item {
                    Text(
                        "Active Operations",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(activePins) { pin ->
                    ActiveDownloadItem(pin, onDelete = { viewModel.deletePin(pin.id) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (downloadedPins.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("Completed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Clear All",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = com.example.ui.theme.CodeFont,
                            modifier = Modifier.clickable { viewModel.clearDownloaded() }.padding(4.dp)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(downloadedPins) { pin ->
                    CompletedDownloadItem(pin, onDelete = { viewModel.deletePin(pin.id) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ActiveDownloadItem(pin: PinItem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(1.dp, MaterialTheme.colorScheme.surfaceContainer)
        ) {
            AsyncImage(model = pin.src, contentDescription = null, modifier = Modifier.fillMaxSize().padding(1.dp), contentScale = ContentScale.Crop, alpha = 0.4f)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(pin.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Downloading...", style = MaterialTheme.typography.bodySmall, fontFamily = com.example.ui.theme.CodeFont, color = MaterialTheme.colorScheme.secondary)
            }
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh).border(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.38f).background(MaterialTheme.colorScheme.primary))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
    }
}

@Composable
fun CompletedDownloadItem(pin: PinItem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(1.dp, MaterialTheme.colorScheme.surfaceContainer)
        ) {
            AsyncImage(model = pin.src, contentDescription = null, modifier = Modifier.fillMaxSize().padding(1.dp), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(pin.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Saved", style = MaterialTheme.typography.bodySmall, fontFamily = com.example.ui.theme.CodeFont, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        val context = androidx.compose.ui.platform.LocalContext.current
        Button(
            onClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(pin.src))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.height(32.dp).border(1.dp, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
        ) {
            Text("OPEN", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
    }
}
