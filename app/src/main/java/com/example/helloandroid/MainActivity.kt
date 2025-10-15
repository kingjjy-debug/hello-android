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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    val earnWebFallback: String?,
    val convertWebFallback: String?,
    val officialDocs: List<String>
)

class MainActivity : ComponentActivity() {

    // 1차: H.Point만. 이후 OK캐쉬백/네이버페이/L.POINT/KB포인트리/Hyatt 추가 예정
    private val partners = listOf(
        Partner(
            id = "hpoint",
            displayName = "H.Point",
            brandColorArgb = 0xFF007AFF, // 파란 악센트
            ratioLabel = "22P → 1마일",
            limitLabel = "1일 1회",
            earnTips = listOf("출석체크", "광고보기", "설문 참여"),
            warningText = "현금충전·선물·타사전환 포인트는 전환 불가",
            packageName = "kr.co.hpoint.hdgm",
            playStoreUrl = "https://play.google.com/store/apps/details?id=kr.co.hpoint.hdgm",
            earnWebFallback = "https://www.h-point.co.kr/introduce/trip.nhd",
            convertWebFallback = "https://www.h-point.co.kr/introduce/trip.nhd",
            officialDocs = listOf("https://www.h-point.co.kr/introduce/trip.nhd")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    MainScreen(
                        partners = partners,
                        onEarnClick = { openPartnerAppOrWeb(it, isConvert = false) },
                        onConvertClick = { openPartnerAppOrWeb(it, isConvert = true) }
                    )
                }
            }
        }
    }

    private fun openPartnerAppOrWeb(
        partner: Partner,
        isConvert: Boolean
    ) {
        // 1) 앱 실행 우선
        partner.packageName?.let { pkg ->
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                startActivity(launchIntent)
                return
            }
        }
        // 2) 미설치 → Play 스토어
        partner.packageName?.let { pkg ->
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                return
            } catch (_: ActivityNotFoundException) {
                // 마켓 앱 없으면 웹 스토어
                partner.playStoreUrl?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                return
            }
        }
        // 3) 최후: 공식 안내 웹(전환/모으기 각각)
        val url = if (isConvert) partner.convertWebFallback else partner.earnWebFallback
        url?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
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
        TopAppBar(
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
                AssistBadge(partner.ratioLabel)
                Spacer(Modifier.width(6.dp))
                partner.limitLabel?.let { AssistBadge(it) }
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
                            // 공식 링크 열기
                            // AlertDialog 내에서는 Intent 전달이 어려워 상위에서 처리하지 않고
                            // 간단한 VIEW Intent로 열기
                            try {
                                val ctx = LocalContext.current
                                ctx.startActivity(
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

@Composable
fun AssistBadge(text: String) {
    AssistChip(
        onClick = { /* no-op */ },
        label = { Text(text) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: @Composable () -> Unit, actions: @Composable RowScope.() -> Unit) {
    SmallTopAppBar(title = title, actions = actions)
}
