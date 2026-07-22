package com.jbn.yololab_22200019

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.jbn.yololab_22200019.ui.CameraScreen
import com.jbn.yololab_22200019.ui.theme.YoloTheme

class MainActivity : ComponentActivity() {
    private var cameraPermissionGranted by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraPermissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraPermissionGranted = hasCameraPermission()
        if (!cameraPermissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            YoloTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (cameraPermissionGranted) {
                            CameraScreen()
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Se requiere permiso de cámara",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        private fun hasCameraPermission(): Boolean =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        YoloTheme {
            Greeting("Android")
        }
    }