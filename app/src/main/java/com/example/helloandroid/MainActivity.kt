package com.example.helloandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.os.Bundle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HPointCard()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HPointCard() {
    val context = LocalContext.current
    Scaffold(
        topBar = { TopAppBar(title = { Text("대한항공 마일리지 모으기") }) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("H.Point 포인트 관리", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        openAppOrPlayStore(
                            context,
                            packageName = "com.hpoint",
                            playStoreUrl = "https://play.google.com/store/apps/details?id=com.hpoint"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("H.Point 포인트 모으기")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        openAppOrPlayStore(
                            context,
                            packageName = "com.hpoint",
                            playStoreUrl = "https://play.google.com/store/apps/details?id=com.hpoint"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("H.Point → 대한항공 마일리지 전환하기")
                }
            }
        }
    )
}

fun openAppOrPlayStore(context: android.content.Context, packageName: String, playStoreUrl: String) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        try {
            context.startActivity(launchIntent)
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            context.startActivity(intent)
        }
    } else {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
        context.startActivity(intent)
    }
}
