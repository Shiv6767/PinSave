package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ui.PinSaveApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val app = application as PinSaveApplication
    viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(app.repository) as T
      }
    }).get(MainViewModel::class.java)

    handleIntent(intent)

    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()

      MyApplicationTheme(darkTheme = isDarkTheme) {
        PinSaveApp(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: android.content.Intent?) {
    val action = intent?.action
    val data = intent?.data
    if (android.content.Intent.ACTION_VIEW == action && data != null) {
      if (data.scheme == "pinsave" && data.host == "oauth-callback") {
        val token = data.getQueryParameter("token")
        if (token != null) {
          viewModel.setAccessToken(token)
        }
      }
    }
  }
}

