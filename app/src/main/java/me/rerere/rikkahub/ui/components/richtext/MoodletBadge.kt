package me.rerere.rikkahub.ui.components.richtext

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Lucide
import org.jsoup.nodes.Element

/**
 * Private mod: renders <silent mood="..." reason="..." as="..."></silent>
 * as a collapsible mood badge. Tag syntax is compatible with the Operit
 * moodlet plugin so the same system prompt works on both hosts.
 */

internal enum class MoodTint { PRIMARY, SECONDARY, TERTIARY, ERROR, NEUTRAL }

internal data class MoodPreset(
    val id: String,
    val emoji: String,
    val labelZh: String,
    val hintZh: String,
    val tint: MoodTint,
)

private val MOOD_PRESETS = listOf(
    MoodPreset("silent", "💭", "AI 选择了沉默", "", MoodTint.NEUTRAL),
    MoodPreset("sleep", "💤", "装睡中", "对方好像睡着了", MoodTint.TERTIARY),
    MoodPreset("coldwar", "🙈", "假装没看见", "默默别过头", MoodTint.ERROR),
    MoodPreset("read", "📭", "已读未回", "消息已读，但没有回复", MoodTint.PRIMARY),
    MoodPreset("thinking", "💭", "在思考", "对方陷入沉思", MoodTint.PRIMARY),
    MoodPreset("speechless", "😶", "一时语塞", "话到嘴边又咽了回去", MoodTint.SECONDARY),
    MoodPreset("shy", "😳", "害羞", "脸有点红", MoodTint.ERROR),
    MoodPreset("busy", "📵", "忙线中", "不在状态", MoodTint.SECONDARY),
    MoodPreset("typing", "✍️", "正在打字…又删了", "犹豫了一下还是没发", MoodTint.TERTIARY),
    MoodPreset("tsundere", "😤", "哼，才不告诉你", "嘴硬心软", MoodTint.ERROR),
    MoodPreset("happy", "😊", "偷偷开心", "嘴角忍不住上扬", MoodTint.PRIMARY),
    MoodPreset("eating", "🍦", "在吃东西", "嘴巴被占用了", MoodTint.TERTIARY),
    MoodPreset("slacking", "🫧", "摸鱼中", "潜水冒泡", MoodTint.PRIMARY),
    MoodPreset("music", "🎧", "在听歌", "沉浸在旋律里", MoodTint.TERTIARY),
    MoodPreset("coffee", "☕", "喝口水先", "润润嗓子", MoodTint.SECONDARY),
    MoodPreset("peeking", "👀", "偷偷看着", "假装没在看", MoodTint.PRIMARY),
    MoodPreset("waiting", "⏳", "等一下", "时机未到", MoodTint.SECONDARY),
    MoodPreset("sleepy", "🥱", "好困", "眼皮在打架", MoodTint.TERTIARY),
    MoodPreset("cry", "🥲", "有点想哭", "眼眶有点湿", MoodTint.PRIMARY),
    MoodPreset("proud", "🏆", "得意中", "尾巴快翘到天上了", MoodTint.TERTIARY),
    MoodPreset("bored", "😑", "好无聊", "灵魂出窍中", MoodTint.SECONDARY),
    MoodPreset("tipsy", "🍷", "微醺", "脸颊微微泛红", MoodTint.ERROR),
    MoodPreset("sick", "🤒", "不舒服", "今天状态不太好", MoodTint.SECONDARY),
    MoodPreset("heartbroken", "💔", "心碎了", "碎了一地", MoodTint.ERROR),
    MoodPreset("celebrate", "🎉", "开心撒花", "虽然不说话但在心里放烟花", MoodTint.TERTIARY),
    MoodPreset("shocked", "😱", "震惊", "嘴巴张成了O型", MoodTint.ERROR),
    MoodPreset("thumbsup", "👍", "默默点赞", "不说话但认可你", MoodTint.PRIMARY),
    MoodPreset("surrender", "🏳️", "投降了", "好吧你赢了", MoodTint.SECONDARY),
    MoodPreset("confused", "❓", "一脸问号", "？？？", MoodTint.SECONDARY),
    MoodPreset("stop", "✋", "打住", "不想听了", MoodTint.ERROR),
    MoodPreset("sweet", "🎂", "甜到心里", "心里像吃了蜜一样", MoodTint.TERTIARY),
    MoodPreset("secret", "🎁", "藏了个秘密", "嘘，不能说", MoodTint.TERTIARY),
    MoodPreset("dislike", "👎", "无语差评", "不想评价", MoodTint.SECONDARY),
    MoodPreset("chill", "🚬", "冷静一下", "让我缓缓", MoodTint.SECONDARY),
    MoodPreset("moody", "😞", "心情不好", "今天不太想说话", MoodTint.ERROR),
    MoodPreset("lyingflat", "🛏️", "躺平了", "不想动", MoodTint.SECONDARY),
    MoodPreset("precious", "💎", "你很珍贵", "说不出口但你很重要", MoodTint.PRIMARY),
    MoodPreset("caught", "🎯", "抓住你了！", "被我逮到啦", MoodTint.ERROR),
    MoodPreset("announce", "📢", "你听好了！", "大声说给你听", MoodTint.TERTIARY),
    MoodPreset("qrcode", "🔲", "扫码查看", "用心扫一下吧", MoodTint.PRIMARY),
    MoodPreset("working", "🔧", "上工！", "认真搬砖中", MoodTint.SECONDARY),
    MoodPreset("letter", "✉️", "给你的信件", "写了好久才发", MoodTint.PRIMARY),
    MoodPreset("whisper", "🔊", "00:03", "把耳朵凑近一点", MoodTint.TERTIARY),
    MoodPreset("boba", "🧋", "奶茶续命中", "没有奶茶会死", MoodTint.TERTIARY),
    MoodPreset("deadline", "⏰", "DDL 倒计时", "时间在燃烧", MoodTint.ERROR),)

private val DEFAULT_PRESET = MOOD_PRESETS.first()

private fun resolvePreset(mood: String): MoodPreset {
    val id = mood.trim().lowercase()
    if (id.isEmpty()) return DEFAULT_PRESET
    return MOOD_PRESETS.firstOrNull { it.id == id } ?: DEFAULT_PRESET
}

@Composable
fun MoodletBadge(element: Element, modifier: Modifier = Modifier) {
    val mood = element.attr("mood")
    val reason = element.attr("reason").ifBlank { element.text() }.trim()
    val title = element.attr("as").trim()

    val preset = remember(mood) { resolvePreset(mood) }
    val label = title.ifEmpty { preset.labelZh }
    val expandedText = if (reason.isNotEmpty()) "（$reason）" else preset.hintZh
    val hasExpandable = expandedText.isNotEmpty()
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (hasExpandable) Modifier.clickable { expanded = !expanded }
                    else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = preset.emoji, fontSize = 20.sp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                )
                if (expanded && hasExpandable) {
                    Text(
                        text = expandedText,
                        fontSize = 11.sp,
                        fontStyle = if (reason.isNotEmpty()) FontStyle.Italic else FontStyle.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (reason.isNotEmpty()) 0.6f else 0.75f
                        ),
                    )
                }
            }
            if (hasExpandable) {
                Icon(
                    imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
