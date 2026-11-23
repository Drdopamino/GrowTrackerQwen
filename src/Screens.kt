package com.algorythmstudio.growtracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun HomeScreen(nav: NavHostController) {
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
            val day = runCatching { ChronoUnit.DAYS.between(LocalDate.parse(p.flip), LocalDate.now()).toInt() }.getOrElse { 0 }
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
                        if (last != null && d != null) Tag("Δ ${d.toInt()} ppm", deltaColor(last) ?: C.Green) else Tag("нет замеров", C.Text2)
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
fun PlantsScreen(nav: NavHostController) {
    val evs by Store.events.collectAsState()
    Column(scrollMod()) {
        BackBar("РАСТЕНИЯ") { nav.popBackStack() }
        PLANTS.forEach { p ->
            val list = evs.filter { it.plant == p.id }
            val day = runCatching { ChronoUnit.DAYS.between(LocalDate.parse(p.flip), LocalDate.now()).toInt() }.getOrElse { 0 }
            val ha = runCatching { LocalDate.parse(p.flip).plusDays(p.min.toLong()).toString() }.getOrElse { "" }
            val hb = runCatching { LocalDate.parse(p.flip).plusDays(p.max.toLong()).toString() }.getOrElse { "" }
            BoxCard(Modifier.padding(bottom = 12.dp)) {
                Text(p.name, style = MaterialTheme.typography.headlineMedium, color = p.color)
                Text("${p.gen} · горшок ${p.pot} л", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                Spacer(Modifier.height(10.dp))
                Row {
                    Ring(if (day > 0) day.toFloat() / p.max else 0f, p.color, if (day > 0) "$day" else "0")
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("12/12: ${fmtDate(p.flip)}", style = MaterialTheme.typography.bodyMedium, color = C.Text)
                        Text("Харвест: ${fmtDate(ha)} — ${fmtDate(hb)}", style = MaterialTheme.typography.bodyMedium, color = C.Text)
                        Text("Интервал полива: ${p.interval} дня", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                        Text("Записей: ${list.size}", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CatalogScreen(nav: NavHostController) {
    Column(scrollMod()) {
        BackBar("КАТАЛОГ") { nav.popBackStack() }
        Text("Дозы по умолчанию и вклад в PPM", style = MaterialTheme.typography.bodySmall, color = C.Text2)
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
fun DiaryScreen(nav: NavHostController) {
    val evs by Store.events.collectAsState()
    val list = evs.filter { it.err || it.type == "note" }.sortedByDescending { it.date }
    Column(scrollMod()) {
        BackBar("ДНЕВНИК") { nav.popBackStack() }
        Text("Ошибки и заметки: ${list.size}", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        Spacer(Modifier.height(12.dp))
        if (list.isEmpty()) BoxCard { Text("Пока нет записей с пометкой ОШИБКА", style = MaterialTheme.typography.bodyMedium, color = C.Text2) }
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
fun SettingsScreen(nav: NavHostController) {
    val tds by Store.tds.collectAsState()
    val evs by Store.events.collectAsState()
    Column(scrollMod()) {
        BackBar("НАСТРОЙКИ") { nav.popBackStack() }
        Header("Шкала TDS")
        BoxCard {
            Text("EC = PPM / шкала. Влияет на все пересчёты.", style = MaterialTheme.typography.bodySmall, color = C.Text2)
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
            Text("Записей на устройстве: ${evs.size}", style = MaterialTheme.typography.bodyLarge, color = C.Text)
            Text("Хранение: внутренний файл приложения, без облака.", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AboutScreen(nav: NavHostController) {
    Column(scrollMod()) {
        BackBar("О ПРИЛОЖЕНИИ") { nav.popBackStack() }
        BoxCard {
            Text("GrowTracker 0.2.0", style = MaterialTheme.typography.headlineMedium, color = C.Lime)
            Text("Algorythm Studio", style = MaterialTheme.typography.bodyMedium, color = C.Text2)
            Spacer(Modifier.height(10.dp))
            Text("Офлайн-трекер поливов, питания и стадий роста. Данные хранятся только на телефоне, интернет не нужен.", style = MaterialTheme.typography.bodyMedium, color = C.Text)
            Spacer(Modifier.height(10.dp))
            Text("Заголовки: Space Grotesk (замена Cy Grotesk Grand). Текст: Montserrat. Лицензии SIL OFL.", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun MenuScreen(nav: NavHostController) {
    val items = listOf(
        Triple(R.drawable.ic_leaf, "Растения", "plants"),
        Triple(R.drawable.ic_flask, "Каталог удобрений", "catalog"),
        Triple(R.drawable.ic_pencil, "Дневник и ошибки", "diary"),
        Triple(R.drawable.ic_chart, "Графики", "charts"),
        Triple(R.drawable.ic_gear, "Настройки", "settings"),
        Triple(R.drawable.ic_note, "О приложении", "about")
    )
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("МЕНЮ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Spacer(Modifier.height(12.dp))
        items.forEach { i ->
            BoxCard(Modifier.padding(bottom = 10.dp).clickable { nav.navigate(i.third) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(i.first), null, tint = C.Lime, modifier = Modifier.size(23.dp))
                    Spacer(Modifier.width(14.dp))
                    Text(i.second, style = MaterialTheme.typography.bodyLarge, color = C.Text, modifier = Modifier.weight(1f))
                    Icon(painterResource(R.drawable.ic_arrow_right), null, tint = C.Text2, modifier = Modifier.size(18.dp))
                }
            }
        }
        Text("GrowTracker 0.2.0 · Algorythm Studio", style = MaterialTheme.typography.bodySmall, color = C.Text2, modifier = Modifier.padding(vertical = 12.dp))
        Spacer(Modifier.height(20.dp))
    }
}
