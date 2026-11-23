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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import java.io.File
import java.time.LocalDate
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
            val tl = Offset(w / 2, w / 2)
            val sz = Size(size.width - w, size.height - w)
            drawArc(C.Stroke, 0f, 360f, false, tl, sz, style = Stroke(w))
            drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, tl, sz, style = Stroke(w, cap = StrokeCap.Round))
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
fun BackBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_arrow_left), "Назад", tint = C.Text) }
        Text(title, style = MaterialTheme.typography.headlineMedium, color = C.Text)
    }
}

@Composable
fun BottomBar(current: String?, onTab: (String) -> Unit) {
    val tabs = listOf(
        Triple("home", "Главная", R.drawable.ic_home),
        Triple("journal", "Журнал", R.drawable.ic_journal),
        Triple("calendar", "Календарь", R.drawable.ic_calendar),
        Triple("charts", "Графики", R.drawable.ic_chart),
        Triple("menu", "Меню", R.drawable.ic_menu)
    )
    Column(Modifier.fillMaxWidth().background(C.Surface)) {
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(C.Stroke))
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(62.dp)) {
            tabs.forEach { t ->
                val sel = t.first == current
                Column(Modifier.weight(1f).fillMaxHeight().clickable { onTab(t.first) }.padding(top = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(t.third), t.second, tint = if (sel) C.Lime else C.Text2, modifier = Modifier.size(23.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(t.second, style = MaterialTheme.typography.labelSmall, color = if (sel) C.Lime else C.Text2, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun App() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val tabs = listOf("home", "journal", "calendar", "charts", "menu")
    val isTab = route != null && tabs.contains(route)
    Glow {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = C.Text,
            snackbarHost = {
                SnackbarHost(Snack) { data ->
                    Snackbar(snackbarData = data, containerColor = C.Surface2, contentColor = C.Text, actionColor = C.Lime)
                }
            },
            bottomBar = {
                if (isTab) BottomBar(route) { r ->
                    nav.navigate(r) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            floatingActionButton = {
                if (route == "home" || route == "journal") Pill("+ Событие") { nav.navigate("form/water/new") }
            }
        ) { pad ->
            NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad)) {
                composable("home") { HomeScreen(nav) }
                composable("journal") { JournalScreen(nav) }
                composable("calendar") { CalendarScreen(nav) }
                composable("charts") { ChartsScreen() }
                composable("menu") { MenuScreen(nav) }
                composable("plants") { PlantsScreen(nav) }
                composable("catalog") { CatalogScreen(nav) }
                composable("diary") { DiaryScreen(nav) }
                composable("settings") { SettingsScreen(nav) }
                composable("about") { AboutScreen(nav) }
                composable("form/{type}/{id}") { back ->
                    EventForm(nav, back.arguments?.getString("type") ?: "water", back.arguments?.getString("id") ?: "new")
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Store.init(File(filesDir, "growtracker.json"))
        setContent { AppTheme { App() } }
    }
}
