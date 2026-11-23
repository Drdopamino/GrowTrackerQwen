package com.algorythmstudio.growtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object C {
    val Bg = Color(0xFF141414)
    val Surface = Color(0xFF1E1E1E)
    val Surface2 = Color(0xFF262626)
    val Stroke = Color(0xFF333333)
    val Text = Color(0xFFFFFFFF)
    val Text2 = Color(0xFF8C8C8C)
    val Lime = Color(0xFFB4F000)
    val OnLime = Color(0xFF141414)
    val Green = Color(0xFF7ED957)
    val Yellow = Color(0xFFF7C948)
    val Purple = Color(0xFF8B7BF7)
    val Red = Color(0xFFFF5A5A)
}

val Snack = SnackbarHostState()

private val Head = FontFamily(Font(R.font.cy_grotesk_grand, FontWeight.Bold))
private val Body = FontFamily(
    Font(R.font.montserrat, FontWeight.Normal),
    Font(R.font.montserrat, FontWeight.Medium),
    Font(R.font.montserrat, FontWeight.SemiBold)
)

private val Typo = Typography(
    headlineLarge = TextStyle(fontFamily = Head, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = Head, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 25.sp),
    headlineSmall = TextStyle(fontFamily = Head, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontSize = 15.sp, lineHeight = 20.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = Body, fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Medium, fontSize = 10.sp)
)

val Metric = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.Lime, onPrimary = C.OnLime, background = C.Bg, onBackground = C.Text,
            surface = C.Surface, onSurface = C.Text, surfaceVariant = C.Surface2,
            onSurfaceVariant = C.Text2, outline = C.Stroke, error = C.Red,
            secondary = C.Purple, tertiary = C.Yellow
        ),
        typography = Typo,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(12.dp), small = RoundedCornerShape(16.dp),
            medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}

@Composable
fun Glow(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(C.Bg).drawBehind {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(C.Lime.copy(alpha = 0.18f), C.Lime.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(size.width * 0.08f, size.height * 1.02f),
                radius = size.maxDimension * 0.95f
            )
        )
    }, content = content)
}

@Composable
fun Pill(text: String, onClick: () -> Unit, mod: Modifier = Modifier, color: Color = C.Lime) {
    Row(
        mod.height(52.dp).clip(CircleShape).background(color).clickable(onClick = onClick).padding(start = 6.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(C.OnLime), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_arrow_right), null, tint = color, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = C.OnLime)
    }
}

@Composable
fun CircleBtn(icon: Int, label: String, onClick: () -> Unit, mod: Modifier = Modifier) {
    Column(mod.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp).clip(CircleShape).border(1.5.dp, C.Text.copy(alpha = 0.7f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), label, tint = C.Text, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = C.Text2, maxLines = 1)
    }
}

@Composable
fun BoxCard(mod: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val s = RoundedCornerShape(20.dp)
    Column(mod.fillMaxWidth().clip(s).background(C.Surface).border(1.dp, C.Stroke, s).padding(14.dp), content = content)
}

@Composable
fun AccentCard(color: Color, mod: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(mod.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(color).padding(14.dp), content = content)
}

@Composable
fun Header(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title.uppercase(), style = MaterialTheme.typography.headlineSmall, color = C.Text, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(action, style = MaterialTheme.typography.labelLarge, color = C.Lime,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onAction).padding(6.dp))
        }
    }
}

@Composable
fun Chip(text: String, active: Boolean, onClick: () -> Unit, color: Color = C.Lime) {
    Box(
        Modifier.clip(RoundedCornerShape(12.dp))
            .background(if (active) color else C.Surface2)
            .border(1.dp, if (active) color else C.Stroke, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = if (active) C.OnLime else C.Text2, maxLines = 1)
    }
}

@Composable
fun Tag(text: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(9.dp)).background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(9.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
    ) { Text(text, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1) }
}

@Composable
fun Ring(progress: Float, color: Color, label: String) {
    Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(76.dp)) {
            val w = 7.dp.toPx()
            drawArc(C.Stroke, 0f, 360f, false, topLeft = Offset(w / 2, w / 2), size = Size(size.width - w, size.height - w), style = androidx.compose.ui.graphics.drawscope.Stroke(w))
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, topLeft = Offset(w / 2, w / 2), size = Size(size.width - w, size.height - w), style = androidx.compose.ui.graphics.drawscope.Stroke(w, cap = androidx.compose.ui.graphics.StrokeCap.Round))
        }
        Text(label, style = MaterialTheme.typography.titleMedium, color = C.Text)
    }
}

@Composable
fun scrollMod(): Modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)

fun fmtDate(d: String): String = runCatching {
    LocalDate.parse(d).format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
}.getOrElse { d }

@Composable
fun HomeScreen(nav: androidx.navigation.NavHostController) {
    val evs by Store.events.collectAsState()
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))).replaceFirstChar { it.uppercase() }
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("Привет, Гровер", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
        Text("GROWTRACKER", style = MaterialTheme.typography.headlineLarge, color = C.Lime)
        Text(date, style = MaterialTheme.typography.bodySmall, color = C.Text2)
        Spacer(Modifier.height(14.dp))
        Header("Быстрые действия")
        Row(Modifier.fillMaxWidth()) {
            CircleBtn(R.drawable.ic_drop, "Полив", { nav.navigate("form/water/new") }, Modifier.weight(1f))
            CircleBtn(R.drawable.ic_flask, "Компот", { nav.navigate("form/feed/new") }, Modifier.weight(1f))
            CircleBtn(R.drawable.ic_spray, "Спрей", { nav.navigate("form/spray/new") }, Modifier.weight(1f))
            CircleBtn(R.drawable.ic_pencil, "Заметка", { nav.navigate("form/note/new") }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Header("Растения", "Все записи") { nav.navigate("journal") { launchSingleTop = true } }
        PLANTS.forEach { p ->
            val last = evs.filter { it.plant == p.id && (it.type == "water" || it.type == "feed" || it.type == "flush") }.maxByOrNull { it.date }
            val day = runCatching { java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(p.flip), LocalDate.now()).toInt() }.getOrElse { 0 }
            val next = runCatching { LocalDate.parse(last?.date ?: p.flip).plusDays(p.interval.toLong()).toString() }.getOrElse { "" }
            BoxCard(Modifier.padding(bottom = 10.dp).clickable { nav.navigate("plants") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ring(if (day > 0) day.toFloat() / p.max else 0f, p.color, if (day > 0) "$day" else "вега")
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium, color = C.Text)
                        Text("${p.gen} · ${p.pot} л · окно ${p.min}–${p.max} дн", style = MaterialTheme.typography.bodySmall, color = C.Text2)
                        Spacer(Modifier.height(6.dp))
                        val d = last?.delta
                        if (d != null) Tag("Δ ${d.toInt()} ppm", deltaColor(last) ?: C.Green) else Tag("нет замеров", C.Text2)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Следующий полив: ${fmtDate(next)}", style = MaterialTheme.typography.bodyMedium, color = C.Yellow)
            }
        }
        Spacer(Modifier.height(4.dp))
        Header("Последние события", "Смотреть все") { nav.navigate("journal") { launchSingleTop = true } }
        evs.sortedByDescending { it.date }.take(3).forEach { e ->
            BoxCard(Modifier.padding(bottom = 8.dp).clickable { nav.navigate("form/${e.type}/${e.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(plant(e.plant).color))
                    Spacer(Modifier.width(8.dp))
                    Text("${plant(e.plant).name} · ${typeName(e.type)}", style = MaterialTheme.typography.bodyLarge, color = C.Text, modifier = Modifier.weight(1f))
                    Text(fmtDate(e.date), style = MaterialTheme.typography.bodySmall, color = C.Text2)
                }
                if (e.inP != null || e.outP != null) {
                    Spacer(Modifier.height(6.dp))
                    Text("${e.inP?.toInt() ?: "—"} → ${e.outP?.toInt() ?: "—"} ppm", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                }
            }
        }
        Spacer(Modifier.height(110.dp))
    }
}

@Composable
fun PlantsScreen() {
    val evs by Store.events.collectAsState()
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("РАСТЕНИЯ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Spacer(Modifier.height(12.dp))
        PLANTS.forEach { p ->
            val list = evs.filter { it.plant == p.id }
            val day = runCatching { java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(p.flip), LocalDate.now()).toInt() }.getOrElse { 0 }
            val harvestA = runCatching { LocalDate.parse(p.flip).plusDays(p.min.toLong()).toString() }.getOrElse { "" }
            val harvestB = runCatching { LocalDate.parse(p.flip).plusDays(p.max.toLong()).toString() }.getOrElse { "" }
            BoxCard(Modifier.padding(bottom = 12.dp)) {
                Text(p.name, style = MaterialTheme.typography.headlineMedium, color = p.color)
                Text("${p.gen} · горшок ${p.pot} л", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                Spacer(Modifier.height(10.dp))
                Row {
                    Ring(if (day > 0) day.toFloat() / p.max else 0f, p.color, if (day > 0) "$day" else "0")
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Перевод на 12/12: ${fmtDate(p.flip)}", style = MaterialTheme.typography.bodyMedium, color = C.Text)
                        Text("Окно харвеста: ${fmtDate(harvestA)} — ${fmtDate(harvestB)}", style = MaterialTheme.typography.bodyMedium, color = C.Text)
                        Text("Интервал полива: ${p.interval} дня", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                        Text("Записей в журнале: ${list.size}", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CatalogScreen() {
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("КАТАЛОГ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Text("Дозы по умолчанию и вклад в PPM", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        Spacer(Modifier.height(12.dp))
        FERTS.groupBy { it.cat }.forEach { (cat, list) ->
            Header(cat)
            list.forEach { f ->
                BoxCard(Modifier.padding(bottom = 8.dp)) {
                    Text(f.name, style = MaterialTheme.typography.bodyLarge, color = C.Text)
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Tag("${f.dose} мл/л", C.Lime)
                        Spacer(Modifier.width(6.dp))
                        Tag(if (f.ppm > 0) "+${f.ppm.toInt()} ppm" else "вклад не задан", if (f.ppm > 0) C.Purple else C.Text2)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun DiaryScreen(nav: androidx.navigation.NavHostController) {
    val evs by Store.events.collectAsState()
    val list = evs.filter { it.err || it.type == "note" }.sortedByDescending { it.date }
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("ДНЕВНИК", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Text("Ошибки и заметки: ${list.size}", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        Spacer(Modifier.height(12.dp))
        list.forEach { e ->
            BoxCard(Modifier.padding(bottom = 10.dp).clickable { nav.navigate("form/${e.type}/${e.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plant(e.plant).name, style = MaterialTheme.typography.titleMedium, color = plant(e.plant).color, modifier = Modifier.weight(1f))
                    if (e.err) Tag("ОШИБКА", C.Red)
                }
                Text(fmtDate(e.date), style = MaterialTheme.typography.bodySmall, color = C.Text2)
                Spacer(Modifier.height(6.dp))
                Text(e.note, style = MaterialTheme.typography.bodyMedium, color = C.Text)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SettingsScreen() {
    val tds by Store.tds.collectAsState()
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("НАСТРОЙКИ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Spacer(Modifier.height(12.dp))
        Header("Шкала TDS")
        BoxCard {
            Text("EC = PPM / шкала. Влияет на все пересчёты в форме и журнале.", style = MaterialTheme.typography.bodySmall, color = C.Text2)
            Spacer(Modifier.height(10.dp))
            Row {
                Chip("500 (EU)", tds == 500, { Store.setTds(500) })
                Spacer(Modifier.width(8.dp))
                Chip("700 (US)", tds == 700, { Store.setTds(700) })
            }
            Spacer(Modifier.height(10.dp))
            Text("600 ppm = ${"%.2f".format(600.0 / tds)} mS/cm", style = MaterialTheme.typography.bodyLarge, color = C.Lime)
        }
        Spacer(Modifier.height(12.dp))
        Header("Пороги дельты")
        BoxCard {
            Row { Tag("до 250 — норма", C.Green); Spacer(Modifier.width(6.dp)); Tag("250–300 — внимание", C.Yellow) }
            Spacer(Modifier.height(8.dp))
            Row { Tag("больше 300 — засол", C.Red); Spacer(Modifier.width(6.dp)); Tag("выход 800+ — вода", C.Red) }
        }
        Spacer(Modifier.height(12.dp))
        Header("Данные")
        BoxCard {
            val e by Store.events.collectAsState()
            Text("Записей на устройстве: ${e.size}", style = MaterialTheme.typography.bodyLarge, color = C.Text)
            Text("Хранение: внутренний файл приложения, без облака.", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AboutScreen() {
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("О ПРИЛОЖЕНИИ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Spacer(Modifier.height(12.dp))
        BoxCard {
            Text("GrowTracker 0.2.0", style = MaterialTheme.typography.headlineMedium, color = C.Lime)
            Text("Algorythm Studio", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
            Spacer(Modifier.height(10.dp))
            Text("Офлайн-трекер поливов, питания и стадий роста. Все данные хранятся только на телефоне. Интернет приложению не нужен.", style = MaterialTheme.typography.bodyMedium, color = C.Text)
            Spacer(Modifier.height(10.dp))
            Text("Заголовки: Space Grotesk (замена Cy Grotesk Grand). Текст: Montserrat. Лицензии SIL OFL.", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ChartsScreen() {
    val evs by Store.events.collectAsState()
    var pid by remember { mutableStateOf("big") }
    val data = evs.filter { it.plant == pid && it.inP != null && it.outP != null }.sortedBy { it.date }.takeLast(14)
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("ГРАФИКИ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Spacer(Modifier.height(10.dp))
        Row {
            PLANTS.forEach { p ->
                Chip(p.name, pid == p.id, { pid = p.id }, p.color)
                Spacer(Modifier.width(8.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        BoxCard {
            Row { Tag("вход", C.Lime); Spacer(Modifier.width(6.dp)); Tag("выход", C.Purple); Spacer(Modifier.width(6.dp)); Tag("дельта", C.Yellow) }
            Spacer(Modifier.height(12.dp))
            if (data.size < 2) {
                Text("Недостаточно замеров для графика", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
            } else {
                Canvas(Modifier.fillMaxWidth().height(160.dp)) {
                    val maxV = 1000f
                    val step = size.width / (data.size - 1)
                    fun y(v: Double) = size.height - (v.toFloat() / maxV * size.height).coerceIn(0f, size.height)
                    for (g in 0..4) {
                        val gy = size.height * g / 4
                        drawLine(C.Stroke, Offset(0f, gy), Offset(size.width, gy), 1f)
                    }
                    for (i in 0 until data.size - 1) {
                        val x1 = step * i; val x2 = step * (i + 1)
                        drawLine(C.Lime, Offset(x1, y(data[i].inP!!)), Offset(x2, y(data[i + 1].inP!!)), 3f)
                        drawLine(C.Purple, Offset(x1, y(data[i].outP!!)), Offset(x2, y(data[i + 1].outP!!)), 3f)
                    }
                    data.forEachIndexed { i, e ->
                        val x = step * i
                        drawCircle(C.Lime, 4f, Offset(x, y(e.inP!!)))
                        drawCircle(C.Purple, 4f, Offset(x, y(e.outP!!)))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Canvas(Modifier.fillMaxWidth().height(70.dp)) {
                    val bw = size.width / data.size
                    data.forEachIndexed { i, e ->
                        val d = (e.delta ?: 0.0).toFloat().coerceAtLeast(0f)
                        val h = (d / 500f * size.height).coerceIn(2f, size.height)
                        drawRect(deltaColor(e) ?: C.Green, Offset(bw * i + 2f, size.height - h), Size(bw - 4f, h))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("${fmtDate(data.first().date)} — ${fmtDate(data.last().date)} · ${data.size} замеров", style = MaterialTheme.typography.bodySmall, color = C.Text2)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CalendarScreen(nav: androidx.navigation.NavHostController) {
    val evs by Store.events.collectAsState()
    val today = LocalDate.now()
    var ym by remember { mutableStateOf(YearMonth.from(today)) }
    var sel by remember { mutableStateOf(today.toString()) }
    val first = ym.atDay(1)
    val shift = first.dayOfWeek.value - 1
    val days = ym.lengthOfMonth()
    val dayList = evs.filter { it.date == sel }.sortedBy { it.type }
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("КАЛЕНДАРЬ", style = MaterialTheme.typography.headlineLarge, color = C
