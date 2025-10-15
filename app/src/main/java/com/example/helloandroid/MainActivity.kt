package com.example.helloandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope

data class Partner(
    val id: String,
    val displayName: String,
    val brandColorArgb: Long,
    val ratioLabel: String,
    val limitLabel: String?,
    val earnTips: List<String>,
    val warningText: String,
    val packageName: String?,
    val playStoreUrl: String?,
    val earnDeepLink: String?,
    val convertDeepLink: String?,
    val earnWebFallback: String?,
    val convertWebFallback: String?,
    val officialDocs: List<String>
)

class MainActivity : ComponentActivity() {

    private val partners = listOf(
        Partner(
            id = "hpoint",
            displayName = "H.Point",
            brandColorArgb = 0xFF007AFF,
            ratioLabel = "22P → 1마일",
            limitLabel = "1일 1회",
            earnTips = listOf("출석룰렛", "포인트워크", "미션/퀴즈"),
            warningText = "현금충전·선물·타사전환 포인트는 전환 불가",
            packageName = "kr.co.hpoint.hdgm",
            playStoreUrl = "https://play.google.com/store/apps/details?id=kr.co.hpoint.hdgm",
            earnDeepLink = "https://www.h-point.co.kr/stack/joy.nhd",
            convertDeepLink = "https://www.h-point.co.kr/stack/change.nhd",
            earnWebFallback = "https://www.h-point.co.kr/",
            convertWebFallback = "https://www.h-point.co.kr/",
            officialDocs = listOf("https://www.h-point.co.kr/")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainScreen(
                        partners = partners,
                        onEarnClick = { p -> openHPointLink(isConvert = false, p) },
                        onConvertClick = { p -> openHPointLink(isConvert = true, p) }
                    )
                }
            }
        }
    }

    private fun openInAppWeb(url: String) {
        startActivity(
            Intent(this, InAppWebActivity::class.java).putExtra("url", url)
        )
    }

    private fun openHPointLink(isConvert: Boolean, partner: Partner) {
        val pkg = partner.packageName
        val firstUrl = if (isConvert) partner.convertDeepLink else partner.earnDeepLink
        val lastUrl = if (isConvert) partner.convertWebFallback else partner.earnWebFallback

        // 1) 앱 딥링크 시도 (앱이 인식하면 바로 해당 화면)
        if (pkg != null && firstUrl != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(firstUrl)).setPackage(pkg)
                startActivity(intent)
                return
            } catch (_: Exception) { /* 다음 단계 */ }
        }

        // 2) 인앱 웹뷰로 URL 열기 (앱이 못 가로채도 우리 앱 안에서 표시)
        if (firstUrl != null) {
            openInAppWeb(firstUrl)
            return
        }

        // 3) 앱 메인 실행
        if (pkg != null) {
            try {
                packageManager.getLaunchIntentForPackage(pkg)?.let {
                    startActivity(it)
                    return
                }
            } catch (_: Exception) { /* 다음 */ }
        }

        // 4) 마켓 앱 상세 (외부)
        if (pkg != null) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                return
            } catch (_: ActivityNotFoundException) { /* 다음 */ }
        }

        // 5) 플레이 스토어 웹 (인앱 웹뷰)
        partner.playStoreUrl?.let { openInAppWeb(it); return }

        // 6) 최후 웹 폴백 (인앱 웹뷰)
        lastUrl?.let { openInAppWeb(it) }
    }
}

@Composable
fun MainScreen(
    partners: List<Partner>,
    onEarnClick: (Partner) -> Unit,
    onConvertClick: (Partner) -> Unit
) {
    var showNotice by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(
            title = { Text("대한항공 마일리지 모으기") },
            actions = {}
        )
        if (showNotice) {
            NoticeBanner(
                text = "전환 비율·한도는 변경될 수 있어요. 각 카드의 ⓘ에서 공식 안내를 확인하세요.",
                onDismiss = { showNotice = false }
            )
        }
        Column(Modifier.padding(16.dp)) {
            partners.forEach { p ->
                PartnerCard(p, onEarnClick, onConvertClick)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun NoticeBanner(text: String, onDismiss: () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onDismiss) { Text("닫기") }
        }
    }
}

@Composable
fun PartnerCard(
    partner: Partner,
    onEarnClick: (Partner) -> Unit,
    onConvertClick: (Partner) -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    partner.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                AssistChip(onClick = {}, label = { Text(partner.ratioLabel) })
                Spacer(Modifier.width(6.dp))
                partner.limitLabel?.let {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "오늘의 무료 미션: ${partner.earnTips.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "⚠ ${partner.warningText}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onEarnClick(partner) },
                    modifier = Modifier.weight(1f)
                ) { Text("포인트 모으기") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onConvertClick(partner) },
                    modifier = Modifier.weight(1f)
                ) { Text("마일리지로 전환") }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { showInfo = true },
                modifier = Modifier.align(Alignment.End)
            ) { Text("ⓘ 안내") }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("${partner.displayName} 전환 안내") },
            text = {
                Column {
                    Text("• 전환 비율: ${partner.ratioLabel}")
                    partner.limitLabel?.let { Text("• 한도: $it") }
                    Text("• 전환 불가: ${partner.warningText}")
                    if (partner.officialDocs.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("• 공식 링크는 버튼을 눌러 확인하세요.")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("닫기") }
            },
            dismissButton = {
                if (partner.officialDocs.isNotEmpty()) {
                    val first = partner.officialDocs.first()
                    TextButton(
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(first))
                                )
                            } catch (_: Exception) { }
                        }
                    ) { Text("공식 링크") }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(title = title, actions = actions)
}
