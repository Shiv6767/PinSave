package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PinItem
import com.example.viewmodel.MainViewModel

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val historyPins by viewModel.historyPins.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "PinSave",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.clearHistory() }) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("Extraction Log", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("${historyPins.size} ENTRIES", style = MaterialTheme.typography.bodySmall, fontFamily = com.example.ui.theme.CodeFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Divider(color = MaterialTheme.colorScheme.surfaceContainer)
            Spacer(modifier = Modifier.height(24.dp))

            if (historyPins.isNotEmpty()) {
                Text("TODAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(historyPins) { pin ->
                        HistoryItemView(pin)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No extraction history.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun HistoryItemView(pin: PinItem) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh)
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                try {
                    val urlToOpen = if (pin.domain.startsWith("http")) pin.domain else "https://${pin.domain}"
                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlToOpen)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.surfaceContainer)
        ) {
            AsyncImage(model = pin.src, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(modifier = Modifier.weight(1f).padding(12.dp).align(Alignment.CenterVertically)) {
            Text(pin.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, maxLines = 1)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(pin.domain, style = MaterialTheme.typography.bodySmall, fontFamily = com.example.ui.theme.CodeFont, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                Box(modifier = Modifier.padding(horizontal = 8.dp).size(4.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, androidx.compose.foundation.shape.CircleShape))
                Text("10:42 AM", style = MaterialTheme.typography.bodySmall, fontFamily = com.example.ui.theme.CodeFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
