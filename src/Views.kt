package com.algorythmstudio.growtracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                    val step = size.width / (data.size - 1)
                    for (g in 0..4) {
                        val gy = size.height * g / 4
                        drawLine(C.Stroke, Offset(0f, gy), Offset(size.width, gy), 1f)
                    }
                    for (i in 0 until data.size - 1) {
                        val x1 = step * i
                        val x2 = step * (i + 1)
                        val a1 = size.height - (data[i].inP!!.toFloat() / 1000f * size.height).coerceIn(0f, size.height)
                        val a2 = size.height - (data[i + 1].inP!!.toFloat() / 1000f * size.height).coerceIn(0f, size.height)
                        val b1 = size.height - (data[i].outP!!.toFloat() / 1000f * size.height).coerceIn(0f, size.height)
                        val b2 = size.height - (data[i + 1].outP!!.toFloat() / 1000f * size.height).coerceIn(0f, size.height)
                        drawLine(C.Lime, Offset(x1, a1), Offset(x2, a2), 3f)
                        drawLine(C.Purple, Offset(x1, b1), Offset(x2, b2), 3f)
                    }
                    data.forEachIndexed { i, e ->
                        val x = step * i
                        drawCircle(C.Lime, 4f, Offset(x, size.height - (e.inP!!.toFloat() / 1000f * size.height).coerceIn(0f, size.height)))
                        drawCircle(C.Purple, 4f, Offset(x, size.height - (e.outP!!.toFloat() / 1000f * size.height).coerceIn(0f, size.height)))
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
fun CalendarScreen(nav: NavHostController) {
    val evs by Store.events.collectAsState()
    val today = LocalDate.now()
    var ym by remember { mutableStateOf(YearMonth.from(today)) }
    var sel by remember { mutableStateOf(today.toString()) }
    val shift = ym.atDay(1).dayOfWeek.value - 1
    val days = ym.lengthOfMonth()
    val dayList = evs.filter { it.date == sel }
    Column(scrollMod()) {
        Spacer(Modifier.height(12.dp))
        Text("КАЛЕНДАРЬ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { ym = ym.minusMonths(1) }) { Icon(painterResource(R.drawable.ic_arrow_left), "Назад", tint = C.Text) }
            Text(ym.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium, color = C.Lime, modifier = Modifier.weight(1f))
            IconButton(onClick = { ym = ym.plusMonths(1) }) { Icon(painterResource(R.drawable.ic_arrow_right), "Вперёд", tint = C.Text) }
        }
        BoxCard {
            Row(Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = C.Text2, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(6.dp))
            var cell = 0
            for (row in 0..5) {
                if (cell >= shift + days) break
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val num = cell - shift + 1
                        cell++
                        if (num in 1..days) {
                            val d = ym.atDay(num).toString()
                            val marks = evs.filter { it.date == d }
                            val isToday = d == today.toString()
                            Column(
                                Modifier.weight(1f).padding(2.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (d == sel) C.Surface2 else Color.Transparent)
                                    .border(if (isToday) 1.dp else 0.dp, if (isToday) C.Lime else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { sel = d }.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$num", style = MaterialTheme.typography.bodyMedium, color = if (marks.isEmpty()) C.Text2 else C.Text)
                                Spacer(Modifier.height(3.dp))
                                Row {
                                    val ps = marks.map { it.plant }.distinct().take(2)
                                    if (ps.isEmpty()) Spacer(Modifier.height(5.dp))
                                    ps.forEach { p ->
                                        Box(Modifier.size(5.dp).clip(CircleShape).background(plant(p).color))
                                        Spacer(Modifier.width(2.dp))
                                    }
                                }
                            }
                        } else Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Header(fmtDate(sel), "Добавить") { nav.navigate("form/water/new") }
        if (dayList.isEmpty()) {
            BoxCard { Text("В этот день записей нет", style = MaterialTheme.typography.bodyMedium, color = C.Text2) }
        }
        dayList.forEach { e ->
            BoxCard(Modifier.padding(bottom = 8.dp).clickable { nav.navigate("form/${e.type}/${e.id}") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(plant(e.plant).color))
                    Spacer(Modifier.width(8.dp))
                    Text("${plant(e.plant).name} · ${typeName(e.type)}", style = MaterialTheme.typography.bodyLarge, color = C.Text, modifier = Modifier.weight(1f))
                    val d = e.delta
                    if (d != null) Tag("Δ ${d.toInt()}", deltaColor(e) ?: C.Green)
                }
                if (e.note.isNotBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(e.note, style = MaterialTheme.typography.bodySmall, color = C.Text2)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
