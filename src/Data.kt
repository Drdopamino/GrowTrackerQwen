package com.algorythmstudio.growtracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.util.UUID

data class Plant(val id: String, val name: String, val gen: String, val pot: Double, val flip: String, val min: Int, val max: Int, val interval: Int, val color: Color)
data class Fert(val key: String, val name: String, val cat: String, val dose: Double, val ppm: Double)
data class Ev(
    val id: String, val plant: String, val type: String, val date: String,
    val vol: Double?, val inP: Double?, val outP: Double?, val inPh: Double?, val outPh: Double?,
    val note: String, val ferts: List<String>, val err: Boolean
)

val Ev.delta: Double? get() = if (inP != null && outP != null) outP - inP else null

fun deltaColor(e: Ev): Color? {
    val d = e.delta ?: return null
    return if (d > 300 || (e.outP ?: 0.0) >= 800) C.Red else if (d >= 250) C.Yellow else C.Green
}

fun deltaAdvice(e: Ev): String? {
    val d = e.delta ?: return null
    return if (d > 300 || (e.outP ?: 0.0) >= 800) "Засол. Следующий полив — чистая вода." else if (d >= 250) "Внимание: соли накапливаются." else null
}

val SEED_YEAR: Int = if (LocalDate.now().isBefore(LocalDate.of(2026, 10, 1))) 2025 else 2026

val PLANTS = listOf(
    Plant("big", "Большой", "Сатива", 15.0, "$SEED_YEAR-10-04", 77, 84, 4, C.Lime),
    Plant("small", "Малый", "Индика (Афганка)", 10.0, "$SEED_YEAR-10-06", 56, 63, 4, C.Purple)
)

fun plant(id: String): Plant = PLANTS.firstOrNull { it.id == id } ?: PLANTS[0]

val TYPES = listOf("water" to "Полив", "feed" to "Компот", "flush" to "Промывка", "spray" to "Опрыскивание", "flip" to "12/12", "defol" to "Обрезка", "note" to "Заметка")
fun typeName(t: String): String = TYPES.firstOrNull { it.first == t }?.second ?: t

val FERTS = listOf(
    Fert("micro", "TriPart Micro", "База", 2.0, 150.0),
    Fert("grow", "TriPart Grow", "База", 2.0, 120.0),
    Fert("bloom", "TriPart Bloom", "База", 2.0, 130.0),
    Fert("atami", "Atami B'cuzz Bloom Stimulator", "Стимуляторы", 1.0, 30.0),
    Fert("delta", "Delta 9 (CannaBioGen)", "Стимуляторы", 1.0, 20.0),
    Fert("mass", "Simplex Mass", "Стимуляторы", 1.0, 25.0),
    Fert("power", "Simplex Power", "Стимуляторы", 1.0, 25.0),
    Fert("taste", "Taste", "Стимуляторы", 1.0, 20.0),
    Fert("biobizz", "Biobizz Bloom", "Стимуляторы", 2.0, 60.0),
    Fert("calmag", "CalMag (Simplex)", "Добавки", 1.0, 100.0),
    Fert("h2o2", "Перекись водорода 3%", "Добавки", 2.0, 0.0),
    Fert("amber", "Янтарная кислота", "Добавки", 0.1, 0.0),
    Fert("phdown", "pH-Down (Ломоносов)", "Корректоры", 0.1, 0.0)
)

fun fertName(k: String): String = FERTS.firstOrNull { it.key == k }?.name ?: k
fun normFert(k: String): String = if (k.trim().equals("atachi", true)) "atami" else k.trim()

private const val SEED = """
09-15|small|water|2||||||2 литра. Начал чахнуть (перелив).|1
09-17|big|water||620|920|||micro,grow,bloom,calmag,taste|TriPart + CalMag + Taste|0
09-22|small|water|1|449|742|||micro,grow,bloom,taste|TriPart + Taste (1 л)|0
09-24|big|spray||340||||delta|Delta 9 по листу|0
09-24|small|spray||340||||delta|Delta 9 по листу|0
09-25|big|water||139|538|||atami|Вода + Atami|0
09-27|small|flush||100|477|||h2o2,amber|Вода, перекись, янтарка. Два этапа: 477 → 311|0
09-30|big|flush||77|391||||Вода. Два этапа: 391 → 271|0
09-30|big|defol|||||||Обрезка нижних побегов|0
09-30|big|feed||453|386|||micro,grow,bloom,calmag,atami,amber|TriPart + CalMag + Atami + янтарка|0
10-02|big|spray||||||mass|Simplex Mass по листу|0
10-02|small|spray||||||mass|Simplex Mass по листу|0
10-03|small|feed||507|715|||calmag,micro,grow,bloom,atami|CalMag + TriPart + Atami|0
10-04|big|flip|||||||Перевод на 12/12. Начало цветения|0
10-05|big|feed||445|640|||calmag,micro,grow,bloom,atami,amber|Вход 445–510. CalMag + TriPart + Atami + янтарка|0
10-06|small|flip|||||||Перевод на 12/12. Начало цветения|0
10-06|big|spray||||||delta|Delta 9 по листу|0
10-06|small|spray||||||delta|Delta 9 по листу|0
10-09|big|water||200|646|||atami|Вода + Atami|0
10-11|small|water||||||atami|Вода + Atami|0
10-14|big|water||||||atami|Вода + Atami|0
10-15|small|feed||518|645|||micro,grow,bloom,calmag,delta|TriPart + CalMag + Delta 9 (корень)|0
10-15|big|spray||||||delta|Delta 9 по листу|0
10-15|small|spray||||||delta|Delta 9 по листу|0
10-17|big|feed||504|546|6.0||micro,grow,bloom|Только TriPart, вода 240 ppm|0
10-19|small|water||230|425|6.0||amber|Вода + янтарка, pH 6.0–6.1|0
10-21|big|feed||570|830|6.2|6.9|micro,grow,bloom,atami,delta|Вход 570–660. pH 6.2–6.8 → 6.9|0
10-23|small|feed||511|716|6.1|6.5|micro,grow,bloom,atami|pH ~6.1 → ~6.5|0
10-26|big|water||340|630|||taste|Вода + Taste|0
10-28|small|water||230|429|||atami|Вода + Atami|0
10-30|big|water||240|560||||Чистая вода|0
10-30|big|feed||440|462|||micro,grow,bloom,atami,delta|Через 10 минут после воды. ОШИБКА: мало PPM|1
11-01|small|feed|1.4|640|750|||micro,grow,bloom,atami,delta|Delta 9: 1.5 мл на 1.4 л|0
11-02|big|feed|1.4|750||||micro,grow,bloom,atami,delta|Полив 1|0
11-02|big|water|1|520|749|||biobizz|Полив 2: вода + остатки Biobizz Bloom|0
11-06|small|water|1.4|240|426|||atami|Вода + Atami|0
11-07|big|water|2|240|740|||atami|Вода + Atami|0
11-10|small|feed||666|749|6.0||micro,grow,bloom,atami,delta|pH 6.0–6.3|0
11-11|big|water|2|240|880|||atami,delta|Вода 0.6 л + вода/Atami/Delta 9 1.4 л|0
11-14|small|water||240|700|||atami|Вода + Atami, замеры приблизительные|0
11-15|big|feed|2|460|920|||micro,grow,bloom,atami|1.4 л + 0.6 л|0
11-18|small|water||180|340||||Чистая вода|0
11-19|big|flush|2|170|830||||Промывка 1|0
11-19|big|flush|1.4|180|460||||Промывка 2|0
11-22|small|feed||676|749|6.5||micro,grow,bloom,delta|TriPart + Delta 9|0
"""

private fun seed(): List<Ev> = SEED.trim().lines().mapNotNull { line ->
    val p = line.split("|")
    if (p.size < 10) return@mapNotNull null
    Ev(
        id = UUID.nameUUIDFromBytes(line.toByteArray()).toString(),
        plant = p[1], type = p[2], date = "$SEED_YEAR-${p[0]}",
        vol = p[3].toDoubleOrNull(), inP = p[4].toDoubleOrNull(), outP = p[5].toDoubleOrNull(),
        inPh = p[6].toDoubleOrNull(), outPh = p[7].toDoubleOrNull(),
        note = p[9].trim(),
        ferts = p[8].split(",").map { normFert(it) }.filter { it.isNotEmpty() },
        err = p.getOrNull(10)?.trim() == "1"
    )
}

object Store {
    val events = MutableStateFlow<List<Ev>>(emptyList())
    val tds = MutableStateFlow(500)
    private var file: File? = null

    fun init(f: File) {
        file = f
        if (f.exists()) {
            val ok = runCatching {
                val o = JSONObject(f.readText())
                tds.value = o.optInt("tds", 500)
                val a = o.getJSONArray("ev")
                val l = ArrayList<Ev>()
                for (i in 0 until a.length()) {
                    val j = a.getJSONObject(i)
                    val fa = j.optJSONArray("f") ?: JSONArray()
                    val fl = ArrayList<String>()
                    for (k in 0 until fa.length()) fl.add(fa.getString(k))
                    l.add(
                        Ev(
                            j.getString("id"), j.getString("p"), j.getString("t"), j.getString("d"),
                            j.optString("v").toDoubleOrNull(), j.optString("i").toDoubleOrNull(), j.optString("o").toDoubleOrNull(),
                            j.optString("ip").toDoubleOrNull(), j.optString("op").toDoubleOrNull(),
                            j.optString("n"), fl, j.optBoolean("e", false)
                        )
                    )
                }
                events.value = l
            }.isSuccess
            if (!ok) { events.value = seed(); persist() }
        } else { events.value = seed(); persist() }
    }

    private fun persist() {
        val f = file ?: return
        runCatching {
            val a = JSONArray()
            events.value.forEach { e ->
                a.put(JSONObject().apply {
                    put("id", e.id); put("p", e.plant); put("t", e.type); put("d", e.date)
                    put("v", e.vol?.toString() ?: ""); put("i", e.inP?.toString() ?: ""); put("o", e.outP?.toString() ?: "")
                    put("ip", e.inPh?.toString() ?: ""); put("op", e.outPh?.toString() ?: "")
                    put("n", e.note); put("f", JSONArray(e.ferts)); put("e", e.err)
                })
            }
            f.writeText(JSONObject().put("tds", tds.value).put("ev", a).toString())
        }
    }

    fun upsert(e: Ev) {
        events.value = if (events.value.any { it.id == e.id }) events.value.map { if (it.id == e.id) e else it } else events.value + e
        persist()
    }

    fun delete(id: String): Ev? {
        val e = events.value.firstOrNull { it.id == id }
        events.value = events.value.filterNot { it.id == id }
        persist()
        return e
    }

    fun restore(e: Ev) { events.value = events.value + e; persist() }
    fun setTds(v: Int) { tds.value = v; persist() }
    fun newId(): String = UUID.randomUUID().toString()
}

@Composable
fun NumField(label: String, value: String, onChange: (String) -> Unit, mod: Modifier = Modifier, decimal: Boolean = true) {
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = mod,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = C.Lime, unfocusedBorderColor = C.Stroke,
            focusedTextColor = C.Text, unfocusedTextColor = C.Text,
            cursorColor = C.Lime, focusedLabelColor = C.Lime, unfocusedLabelColor = C.Text2,
            focusedContainerColor = C.Surface2, unfocusedContainerColor = C.Surface2
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun JournalScreen(nav: androidx.navigation.NavHostController) {
    val evs by Store.events.collectAsState()
    val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf("") }
    var fp by remember { mutableStateOf("all") }
    var ft by remember { mutableStateOf("all") }
    val filtered = evs.filter { e ->
        (fp == "all" || e.plant == fp) && (ft == "all" || e.type == ft) &&
            (q.isBlank() || e.note.contains(q, true) || typeName(e.type).contains(q, true) ||
                e.ferts.any { fertName(it).contains(q, true) } || plant(e.plant).name.contains(q, true))
    }.sortedWith(compareByDescending<Ev> { it.date }.thenBy { it.type })
    val groups = filtered.groupBy { it.date }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("ЖУРНАЛ", style = MaterialTheme.typography.headlineLarge, color = C.Text)
        Text("${filtered.size} записей из ${evs.size}", style = MaterialTheme.typography.bodySmall, color = C.Text2)
        Spacer(Modifier.height(10.dp))
        NumField("Поиск: заметка, удобрение, растение", q, { q = it }, Modifier.fillMaxWidth(), false)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Chip("Все", fp == "all", { fp = "all" })
            PLANTS.forEach { p ->
                Spacer(Modifier.width(6.dp))
                Chip(p.name, fp == p.id, { fp = p.id }, p.color)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            Chip("Все типы", ft == "all", { ft = "all" })
            TYPES.forEach { t ->
                Spacer(Modifier.width(6.dp))
                Chip(t.second, ft == t.first, { ft = t.first }, C.Purple)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (filtered.isEmpty()) {
            BoxCard { Text("Ничего не найдено. Измени фильтры или добавь событие.", style = MaterialTheme.typography.bodyMedium, color = C.Text2) }
        }
        LazyColumn(Modifier.weight(1f)) {
            groups.forEach { (date, list) ->
                item {
                    Text(fmtDate(date).uppercase(), style = MaterialTheme.typography.headlineSmall, color = C.Lime,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
                }
                items(list.size) { idx ->
                    val e = list[idx]
                    val dc = deltaColor(e)
                    BoxCard(Modifier.padding(bottom = 8.dp).clickable { nav.navigate("form/${e.type}/${e.id}") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(plant(e.plant).color))
                            Spacer(Modifier.width(8.dp))
                            Text(plant(e.plant).name, style = MaterialTheme.typography.titleMedium, color = C.Text)
                            Spacer(Modifier.width(8.dp))
                            Text(typeName(e.type), style = MaterialTheme.typography.bodyMedium, color = C.Text2, modifier = Modifier.weight(1f))
                            if (e.err) { Tag("ОШИБКА", C.Red); Spacer(Modifier.width(6.dp)) }
                            Icon(painterResource(R.drawable.ic_trash), "Удалить", tint = C.Text2,
                                modifier = Modifier.size(20.dp).clip(CircleShape).clickable {
                                    val removed = Store.delete(e.id)
                                    scope.launch {
                                        val r = Snack.showSnackbar("Запись удалена", actionLabel = "Вернуть")
                                        if (r == SnackbarResult.ActionPerformed && removed != null) Store.restore(removed)
                                    }
                                })
                        }
                        if (e.inP != null || e.outP != null || e.vol != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${e.inP?.toInt() ?: "—"} → ${e.outP?.toInt() ?: "—"}", style = Metric, color = C.Text)
                                Spacer(Modifier.width(6.dp))
                                Text("ppm", style = MaterialTheme.typography.bodySmall, color = C.Text2, modifier = Modifier.weight(1f))
                                if (dc != null) Tag("Δ ${e.delta!!.toInt()}", dc)
                            }
                        }
                        val extra = ArrayList<String>()
                        if (e.vol != null) extra.add("${e.vol} л")
                        if (e.inPh != null) extra.add("pH вход ${e.inPh}")
                        if (e.outPh != null) extra.add("pH выход ${e.outPh}")
                        if (extra.isNotEmpty()) {
                            Spacer(Modifier.height(5.dp))
                            Text(extra.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = C.Text2)
                        }
                        if (e.ferts.isNotEmpty()) {
                            Spacer(Modifier.height(7.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState())) {
                                e.ferts.forEach { k ->
                                    Tag(fertName(k).substringBefore(" (").take(22), C.Purple)
                                    Spacer(Modifier.width(5.dp))
                                }
                            }
                        }
                        if (e.note.isNotBlank()) {
                            Spacer(Modifier.height(7.dp))
                            Text(e.note, style = MaterialTheme.typography.bodyMedium, color = C.Text2)
                        }
                        val adv = deltaAdvice(e)
                        if (adv != null) {
                            Spacer(Modifier.height(7.dp))
                            Text(adv, style = MaterialTheme.typography.bodySmall, color = dc ?: C.Yellow)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
fun EventForm(nav: androidx.navigation.NavHostController, type0: String, id: String) {
    val tds by Store.tds.collectAsState()
    val all by Store.events.collectAsState()
    val orig = all.firstOrNull { it.id == id }
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf(orig?.type ?: type0) }
    var pid by remember { mutableStateOf(orig?.plant ?: "big") }
    var date by remember { mutableStateOf(orig?.date ?: LocalDate.now().toString()) }
    var vol by remember { mutableStateOf(orig?.vol?.toString() ?: "") }
    var inp by remember { mutableStateOf(orig?.inP?.toString() ?: "") }
    var outp by remember { mutableStateOf(orig?.outP?.toString() ?: "") }
    var inph by remember { mutableStateOf(orig?.inPh?.toString() ?: "") }
    var outph by remember { mutableStateOf(orig?.outPh?.toString() ?: "") }
    var note by remember { mutableStateOf(orig?.note ?: "") }
    var err by remember { mutableStateOf(orig?.err ?: false) }
    var ferts by remember { mutableStateOf(orig?.ferts ?: emptyList()) }

    val i = inp.toDoubleOrNull()
    val o = outp.toDoubleOrNull()
    val d = if (i != null && o != null) o - i else null
    val probe = Ev("", pid, type, date, vol.toDoubleOrNull(), i, o, inph.toDoubleOrNull(), outph.toDoubleOrNull(), note, ferts, err)
    val dc = deltaColor(probe)
    val dateOk = runCatching { LocalDate.parse(date) }.isSuccess
    val phWarn = inph.toDoubleOrNull()?.let { it < 5.9 || it > 6.9 } ?: false

    Column(scrollMod()) {
        BackBar(if (orig == null) "НОВОЕ СОБЫТИЕ" else "ПРАВКА") { nav.popBackStack() }
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            TYPES.forEach { t ->
                Chip(t.second, type == t.first, { type = t.first })
                Spacer(Modifier.width(6.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row {
            PLANTS.forEach { p ->
                Chip(p.name, pid == p.id, { pid = p.id }, p.color)
                Spacer(Modifier.width(8.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        NumField("Дата (ГГГГ-ММ-ДД)", date, { date = it }, Modifier.fillMaxWidth(), false)
        if (!dateOk) Text("Дата должна быть в формате 2026-11-23", style = MaterialTheme.typography.bodySmall, color = C.Red)
        Spacer(Modifier.height(8.dp))
        NumField("Объём, л", vol, { vol = it }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Header("Замеры")
        Row {
            NumField("Вход PPM", inp, { inp = it }, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            NumField("Выход PPM", outp, { outp = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row {
            Tag("EC вход ${if (i != null) "%.2f".format(i / tds) else "—"}", C.Lime)
            Spacer(Modifier.width(6.dp))
            Tag("EC выход ${if (o != null) "%.2f".format(o / tds) else "—"}", C.Purple)
            Spacer(Modifier.width(6.dp))
            Tag("шкала $tds", C.Text2)
        }
        Spacer(Modifier.height(8.dp))
        Row {
            NumField("pH вход", inph, { inph = it }, Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            NumField("pH выход", outph, { outph = it }, Modifier.weight(1f))
        }
        if (phWarn) {
            Spacer(Modifier.height(6.dp))
            Text("Цель pH 6.3–6.5: отклонение больше 0.4", style = MaterialTheme.typography.bodySmall, color = C.Yellow)
        }
        if (d != null) {
            Spacer(Modifier.height(12.dp))
            AccentCard(dc ?: C.Green) {
                Text("ДЕЛЬТА ${d.toInt()} ppm", style = MaterialTheme.typography.headlineMedium, color = C.OnLime)
                Text(deltaAdvice(probe) ?: "Соли в норме, растение пьёт ровно.", style = MaterialTheme.typography.bodyMedium, color = C.OnLime)
            }
        }
        Spacer(Modifier.height(12.dp))
        Header("Удобрения")
        BoxCard {
            FERTS.forEach { f ->
                val on = ferts.contains(f.key)
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable {
                    ferts = if (on) ferts - f.key else ferts + f.key
                }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = on, onCheckedChange = { ferts = if (on) ferts - f.key else ferts + f.key },
                        colors = CheckboxDefaults.colors(checkedColor = C.Lime, checkmarkColor = C.OnLime, uncheckedColor = C.Stroke))
                    Column(Modifier.weight(1f)) {
                        Text(f.name, style = MaterialTheme.typography.bodyMedium, color = if (on) C.Text else C.Text2)
                        val total = vol.toDoubleOrNull()?.let { it * f.dose }
                        Text("${f.dose} мл/л" + if (total != null) " · ${"%.2f".format(total)} мл на объём" else "",
                            style = MaterialTheme.typography.bodySmall, color = C.Text2)
                    }
                }
            }
            if (ferts.isNotEmpty() && vol.toDoubleOrNull() == null) {
                Text("Укажи объём — посчитаю миллилитры", style = MaterialTheme.typography.bodySmall, color = C.Yellow)
            }
        }
        Spacer(Modifier.height(12.dp))
        Header("Заметка")
        NumField("Что заметил", note, { note = it }, Modifier.fillMaxWidth(), false)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { err = !err }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = err, onCheckedChange = { err = it },
                colors = CheckboxDefaults.colors(checkedColor = C.Red, checkmarkColor = C.OnLime, uncheckedColor = C.Stroke))
            Text("Пометить как ОШИБКУ", style = MaterialTheme.typography.bodyMedium, color = if (err) C.Red else C.Text2)
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(if (orig == null) "Сохранить" else "Обновить", {
                if (!dateOk) { scope.launch { Snack.showSnackbar("Проверь дату") }; return@Pill }
                Store.upsert(Ev(orig?.id ?: Store.newId(), pid, type, date, vol.toDoubleOrNull(), i, o,
                    inph.toDoubleOrNull(), outph.toDoubleOrNull(), note.trim(), ferts, err))
                scope.launch { Snack.showSnackbar(if (orig == null) "Событие добавлено" else "Событие обновлено") }
                nav.popBackStack()
            })
            if (orig != null) {
                Spacer(Modifier.width(12.dp))
                Icon(painterResource(R.drawable.ic_trash), "Удалить", tint = C.Red,
                    modifier = Modifier.size(46.dp).clip(CircleShape).border(1.5.dp, C.Red, CircleShape).padding(11.dp).clickable {
                        val removed = Store.delete(orig.id)
                        nav.popBackStack()
                        scope.launch {
                            val r = Snack.showSnackbar("Запись удалена", actionLabel = "Вернуть")
                            if (r == SnackbarResult.ActionPerformed && removed != null) Store.restore(removed)
                        }
                    })
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}
