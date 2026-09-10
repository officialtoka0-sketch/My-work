package com.tokich.mywork

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import android.graphics.Color as AColor
import android.graphics.Paint as APaint
import android.graphics.pdf.PdfDocument

// ---------- COLORS ----------
val BgColor = Color(0xFF0A0A0B)
val CardColor = Color(0xFF17171A)
val CardColor2 = Color(0xFF202024)
val AccentGreen = Color(0xFF34C759)
val DebtRed = Color(0xFFFF453A)
val TextWhite = Color(0xFFF2F2F2)
val TextMuted = Color(0xFF8A8A8E)

// ---------- DATA ----------
data class Debt(val id: Long, val date: String, val amount: Double, val note: String)

data class Strings(
    val selectLanguage: String,
    val selectCurrency: String,
    val calendarTab: String,
    val debtsTab: String,
    val hourlyRate: String,
    val income: String,
    val debtsLabel: String,
    val net: String,
    val addDebt: String,
    val date: String,
    val amount: String,
    val note: String,
    val save: String,
    val cancel: String,
    val delete: String,
    val export: String,
    val help: String,
    val enterHours: String,
    val hoursLabel: String,
    val months: List<String>,
    val weekdays: List<String>,
    val noDebts: String,
    val today: String
)

val RU = Strings(
    selectLanguage = "Выберите язык",
    selectCurrency = "Выберите валюту",
    calendarTab = "Календарь",
    debtsTab = "Долги",
    hourlyRate = "Ставка в час",
    income = "Доход за месяц",
    debtsLabel = "Долги",
    net = "Итого",
    addDebt = "Добавить долг",
    date = "Дата",
    amount = "Сумма",
    note = "Примечание",
    save = "Сохранить",
    cancel = "Отмена",
    delete = "Удалить",
    export = "Экспорт PDF",
    help = "Помощь",
    enterHours = "Часы за день",
    hoursLabel = "ч",
    months = listOf("Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"),
    weekdays = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс"),
    noDebts = "Долгов нет",
    today = "Сегодня"
)

val TR = Strings(
    selectLanguage = "Dil seçin",
    selectCurrency = "Para birimi seçin",
    calendarTab = "Takvim",
    debtsTab = "Borçlar",
    hourlyRate = "Saatlik ücret",
    income = "Bu ayın geliri",
    debtsLabel = "Borçlar",
    net = "Toplam",
    addDebt = "Borç ekle",
    date = "Tarih",
    amount = "Tutar",
    note = "Not",
    save = "Kaydet",
    cancel = "Vazgeç",
    delete = "Sil",
    export = "PDF olarak dışa aktar",
    help = "Yardım",
    enterHours = "Günlük saat",
    hoursLabel = "sa",
    months = listOf("Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"),
    weekdays = listOf("Pt","Sa","Ça","Pe","Cu","Ct","Pa"),
    noDebts = "Borç yok",
    today = "Bugün"
)

val TK = Strings(
    selectLanguage = "Dili saýlaň",
    selectCurrency = "Puluň görnüşini saýlaň",
    calendarTab = "Senenama",
    debtsTab = "Bergiler",
    hourlyRate = "Sagatlyk hak",
    income = "Bu aýyň girdejisi",
    debtsLabel = "Bergiler",
    net = "Jemi",
    addDebt = "Bergi goş",
    date = "Sene",
    amount = "Mukdar",
    note = "Bellik",
    save = "Ýatda sakla",
    cancel = "Ýatyr",
    delete = "Poz",
    export = "PDF hökmünde ýükle",
    help = "Kömek",
    enterHours = "Günlük sagat",
    hoursLabel = "sag",
    months = listOf("Ýanwar","Fewral","Mart","Aprel","Maý","Iýun","Iýul","Awgust","Sentýabr","Oktýabr","Noýabr","Dekabr"),
    weekdays = listOf("Du","Si","Ça","Pe","An","Şe","Ýe"),
    noDebts = "Bergi ýok",
    today = "Şu gün"
)

val translations = mapOf("ru" to RU, "tr" to TR, "tk" to TK)

fun currencySymbol(code: String): String = when (code) {
    "try" -> "₺"
    "rub" -> "₽"
    "tmt" -> "TMT"
    else -> ""
}

fun currencyName(code: String): String = when (code) {
    "try" -> "Türk Lirası"
    "rub" -> "Российский рубль"
    "tmt" -> "Türkmen manaty"
    else -> ""
}

// ---------- STORAGE ----------
class Storage(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("my_work_prefs", Context.MODE_PRIVATE)

    fun isSetupDone() = prefs.getBoolean("setup_done", false)
    fun setSetupDone(v: Boolean) = prefs.edit().putBoolean("setup_done", v).apply()

    fun getLanguage() = prefs.getString("language", "ru") ?: "ru"
    fun setLanguage(v: String) = prefs.edit().putString("language", v).apply()

    fun getCurrency() = prefs.getString("currency", "try") ?: "try"
    fun setCurrency(v: String) = prefs.edit().putString("currency", v).apply()

    fun getRate(): Double = prefs.getString("rate", "0")?.toDoubleOrNull() ?: 0.0
    fun setRate(v: Double) = prefs.edit().putString("rate", v.toString()).apply()

    fun getHours(): Map<String, Double> {
        val raw = prefs.getString("hours", "{}") ?: "{}"
        val obj = JSONObject(raw)
        val map = mutableMapOf<String, Double>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k] = obj.getDouble(k)
        }
        return map
    }

    fun saveHours(map: Map<String, Double>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString("hours", obj.toString()).apply()
    }

    fun getDebts(): List<Debt> {
        val raw = prefs.getString("debts", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Debt>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(Debt(o.getLong("id"), o.getString("date"), o.getDouble("amount"), o.getString("note")))
        }
        return list
    }

    fun saveDebts(list: List<Debt>) {
        val arr = JSONArray()
        list.forEach { d ->
            val o = JSONObject()
            o.put("id", d.id)
            o.put("date", d.date)
            o.put("amount", d.amount)
            o.put("note", d.note)
            arr.put(o)
        }
        prefs.edit().putString("debts", arr.toString()).apply()
    }
}

// ---------- PDF EXPORT ----------
fun exportMonthPdf(
    ctx: Context,
    ym: YearMonth,
    hours: Map<String, Double>,
    debts: List<Debt>,
    rate: Double,
    currency: String,
    s: Strings
): File {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = doc.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = APaint().apply { color = AColor.BLACK; textSize = 20f; isFakeBoldText = true }
    val paint = APaint().apply { color = AColor.BLACK; textSize = 13f }
    val boldPaint = APaint().apply { color = AColor.BLACK; textSize = 13f; isFakeBoldText = true }

    var y = 50f
    val monthLabel = "${s.months[ym.monthValue - 1]} ${ym.year}"
    canvas.drawText("my work — $monthLabel", 40f, y, titlePaint)
    y += 35f

    var totalHours = 0.0
    var totalIncome = 0.0

    canvas.drawText(s.date, 40f, y, boldPaint)
    canvas.drawText(s.hoursLabel, 250f, y, boldPaint)
    canvas.drawText(s.amount, 350f, y, boldPaint)
    y += 20f

    val daysInMonth = ym.lengthOfMonth()
    for (d in 1..daysInMonth) {
        val date = ym.atDay(d)
        val h = hours[date.toString()] ?: 0.0
        if (h > 0.0) {
            val income = h * rate
            totalHours += h
            totalIncome += income
            canvas.drawText(date.toString(), 40f, y, paint)
            canvas.drawText(h.toString(), 250f, y, paint)
            canvas.drawText("${income} ${currencySymbol(currency)}", 350f, y, paint)
            y += 18f
            if (y > 760f) {
                doc.finishPage(page)
                y = 50f
            }
        }
    }

    y += 15f
    canvas.drawText("${s.income}: $totalIncome ${currencySymbol(currency)}", 40f, y, boldPaint)
    y += 25f

    val monthDebts = debts.filter { it.date.startsWith(ym.toString()) }
    val debtsTotal = monthDebts.sumOf { it.amount }
    canvas.drawText("${s.debtsLabel}:", 40f, y, boldPaint)
    y += 20f
    monthDebts.forEach { debt ->
        canvas.drawText("${debt.date}  ${debt.note}  -${debt.amount} ${currencySymbol(currency)}", 40f, y, paint)
        y += 18f
    }
    if (monthDebts.isEmpty()) {
        canvas.drawText(s.noDebts, 40f, y, paint)
        y += 18f
    }

    y += 15f
    val net = totalIncome - debtsTotal
    canvas.drawText("${s.net}: $net ${currencySymbol(currency)}", 40f, y, titlePaint)

    doc.finishPage(page)

    val file = File(ctx.getExternalFilesDir(null), "my_work_${ym}.pdf")
    doc.writeTo(FileOutputStream(file))
    doc.close()
    return file
}

fun sharePdf(ctx: Context, file: File) {
    val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(Intent.createChooser(intent, "PDF"))
}

// ---------- ACTIVITY ----------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = BgColor, surface = BgColor)) {
                MyWorkApp()
            }
        }
    }
}

@Composable
fun MyWorkApp() {
    val ctx = LocalContext.current
    val storage = remember { Storage(ctx) }
    var screen by remember { mutableStateOf("splash") }

    LaunchedEffect(Unit) {
        delay(1100)
        screen = if (storage.isSetupDone()) "main" else "language"
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        when (screen) {
            "splash" -> SplashScreen()
            "language" -> LanguageScreen { lang ->
                storage.setLanguage(lang)
                screen = "currency"
            }
            "currency" -> CurrencyScreen(translations[storage.getLanguage()] ?: RU) { cur ->
                storage.setCurrency(cur)
                storage.setSetupDone(true)
                screen = "main"
            }
            "main" -> MainScreen(storage)
        }
    }
}

@Composable
fun SplashScreen() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    Box(modifier = Modifier.fillMaxSize().background(BgColor), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(800))) {
            Text("my work", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LanguageScreen(onSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Выберите язык / Dil seçin / Dili saýlaň", color = TextWhite, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        SelectRow("🇷🇺", "Русский") { onSelected("ru") }
        Spacer(Modifier.height(14.dp))
        SelectRow("🇹🇷", "Türkçe") { onSelected("tr") }
        Spacer(Modifier.height(14.dp))
        SelectRow("🇹🇲", "Türkmençe") { onSelected("tk") }
    }
}

@Composable
fun CurrencyScreen(s: Strings, onSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(s.selectCurrency, color = TextWhite, fontSize = 16.sp)
        Spacer(Modifier.height(32.dp))
        SelectRow("🇹🇷", "₺  Türk Lirası") { onSelected("try") }
        Spacer(Modifier.height(14.dp))
        SelectRow("🇷🇺", "₽  Российский рубль") { onSelected("rub") }
        Spacer(Modifier.height(14.dp))
        SelectRow("🇹🇲", "TMT  Türkmen manaty") { onSelected("tmt") }
    }
}

@Composable
fun SelectRow(flag: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Text(label, color = TextWhite, fontSize = 16.sp)
    }
}

@Composable
fun MainScreen(storage: Storage) {
    val ctx = LocalContext.current
    val s = translations[storage.getLanguage()] ?: RU
    val currency = storage.getCurrency()

    var tab by remember { mutableStateOf(0) }
    var rate by remember { mutableStateOf(storage.getRate()) }
    var hours by remember { mutableStateOf(storage.getHours()) }
    var debts by remember { mutableStateOf(storage.getDebts()) }
    var ym by remember { mutableStateOf(YearMonth.now()) }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("my work", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            IconTextButton("PDF") {
                val file = exportMonthPdf(ctx, ym, hours, debts, rate, currency, s)
                sharePdf(ctx, file)
            }
            Spacer(Modifier.width(10.dp))
            IconTextButton("?") {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:officialtoka0@gmail.com")
                }
                ctx.startActivity(intent)
            }
        }

        // Tabs
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            TabChip(s.calendarTab, tab == 0) { tab = 0 }
            Spacer(Modifier.width(10.dp))
            TabChip(s.debtsTab, tab == 1) { tab = 1 }
        }

        Spacer(Modifier.height(12.dp))

        if (tab == 0) {
            CalendarTab(
                s = s,
                currency = currency,
                rate = rate,
                onRateChange = { r -> rate = r; storage.setRate(r) },
                hours = hours,
                onHoursChange = { date, h ->
                    val m = hours.toMutableMap()
                    if (h <= 0.0) m.remove(date) else m[date] = h
                    hours = m
                    storage.saveHours(m)
                },
                debts = debts,
                ym = ym,
                onYmChange = { ym = it }
            )
        } else {
            DebtsTab(
                s = s,
                currency = currency,
                debts = debts,
                onAdd = { d ->
                    val list = debts + d
                    debts = list
                    storage.saveDebts(list)
                },
                onDelete = { id ->
                    val list = debts.filter { it.id != id }
                    debts = list
                    storage.saveDebts(list)
                }
            )
        }
    }
}

@Composable
fun IconTextButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = TextWhite, fontSize = 13.sp)
    }
}

@Composable
fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AccentGreen else CardColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (selected) Color.Black else TextWhite, fontSize = 14.sp)
    }
}

@Composable
fun CalendarTab(
    s: Strings,
    currency: String,
    rate: Double,
    onRateChange: (Double) -> Unit,
    hours: Map<String, Double>,
    onHoursChange: (String, Double) -> Unit,
    debts: List<Debt>,
    ym: YearMonth,
    onYmChange: (YearMonth) -> Unit
) {
    val minYm = YearMonth.of(2026, 1)
    val maxYm = YearMonth.of(2060, 12)

    val monthHours = remember(hours, ym) {
        (1..ym.lengthOfMonth()).sumOf { hours[ym.atDay(it).toString()] ?: 0.0 }
    }
    val monthIncome = monthHours * rate
    val monthDebts = remember(debts, ym) { debts.filter { it.date.startsWith(ym.toString()) }.sumOf { it.amount } }
    val net = monthIncome - monthDebts

    var rateText by remember(rate) { mutableStateOf(if (rate == 0.0) "" else rate.toString()) }
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Income summary card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardColor)
                .padding(16.dp)
        ) {
            Text(s.income, color = TextMuted, fontSize = 13.sp)
            Text("${"%.2f".format(monthIncome)} ${currencySymbol(currency)}", color = AccentGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("${s.debtsLabel}: -${"%.2f".format(monthDebts)} ${currencySymbol(currency)}", color = DebtRed, fontSize = 13.sp)
            Text("${s.net}: ${"%.2f".format(net)} ${currencySymbol(currency)}", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = rateText,
            onValueChange = {
                rateText = it
                onRateChange(it.toDoubleOrNull() ?: 0.0)
            },
            label = { Text(s.hourlyRate, color = TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                focusedBorderColor = AccentGreen, unfocusedBorderColor = TextMuted
            )
        )

        Spacer(Modifier.height(14.dp))

        // Month navigation
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconTextButton("<") { if (ym > minYm) onYmChange(ym.minusMonths(1)) }
            Spacer(Modifier.weight(1f))
            Text("${s.months[ym.monthValue - 1]} ${ym.year}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            IconTextButton(">") { if (ym < maxYm) onYmChange(ym.plusMonths(1)) }
        }

        Spacer(Modifier.height(10.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            s.weekdays.forEach { w ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(w, color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        val first = ym.atDay(1)
        val offset = first.dayOfWeek.value - 1
        val days = mutableListOf<LocalDate?>()
        repeat(offset) { days.add(null) }
        for (d in 1..ym.lengthOfMonth()) days.add(ym.atDay(d))

        val today = LocalDate.now()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f)
        ) {
            items(days) { date ->
                if (date == null) {
                    Box(modifier = Modifier.padding(4.dp).size(40.dp))
                } else {
                    val isPast = date.isBefore(today)
                    val isToday = date.isEqual(today)
                    val h = hours[date.toString()] ?: 0.0
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    h > 0.0 -> AccentGreen
                                    isToday -> CardColor2
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { dialogDate = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                date.dayOfMonth.toString(),
                                color = if (h > 0.0) Color.Black else TextWhite,
                                fontSize = 13.sp
                            )
                            if (isPast && h == 0.0) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (dialogDate != null) {
        val date = dialogDate!!
        var input by remember(date) { mutableStateOf((hours[date.toString()] ?: 0.0).let { if (it == 0.0) "" else it.toString() }) }
        AlertDialog(
            onDismissRequest = { dialogDate = null },
            title = { Text("${s.enterHours} — $date", color = TextWhite) },
            containerColor = CardColor,
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(s.hoursLabel, color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentGreen, unfocusedBorderColor = TextMuted
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onHoursChange(date.toString(), input.toDoubleOrNull() ?: 0.0)
                    dialogDate = null
                }) { Text(s.save, color = AccentGreen) }
            },
            dismissButton = {
                TextButton(onClick = { dialogDate = null }) { Text(s.cancel, color = TextMuted) }
            }
        )
    }
}

@Composable
fun DebtsTab(
    s: Strings,
    currency: String,
    debts: List<Debt>,
    onAdd: (Debt) -> Unit,
    onDelete: (Long) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AccentGreen)
                .clickable { showDialog = true }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(s.addDebt, color = Color.Black, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(14.dp))

        if (debts.isEmpty()) {
            Text(s.noDebts, color = TextMuted, fontSize = 14.sp)
        } else {
            LazyColumn {
                items(debts.sortedByDescending { it.date }) { debt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardColor)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(debt.date, color = TextMuted, fontSize = 12.sp)
                            Text(debt.note, color = TextWhite, fontSize = 14.sp)
                        }
                        Text("-${"%.2f".format(debt.amount)} ${currencySymbol(currency)}", color = DebtRed, fontSize = 14.sp)
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onDelete(debt.id) }
                                .padding(6.dp)
                        ) {
                            Text("✕", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var date by remember { mutableStateOf(LocalDate.now().toString()) }
        var amount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(s.addDebt, color = TextWhite) },
            containerColor = CardColor,
            text = {
                Column {
                    OutlinedTextField(
                        value = date, onValueChange = { date = it },
                        label = { Text(s.date, color = TextMuted) }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            focusedBorderColor = AccentGreen, unfocusedBorderColor = TextMuted
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text(s.amount, color = TextMuted) }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            focusedBorderColor = AccentGreen, unfocusedBorderColor = TextMuted
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note, onValueChange = { note = it },
                        label = { Text(s.note, color = TextMuted) }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            focusedBorderColor = AccentGreen, unfocusedBorderColor = TextMuted
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onAdd(Debt(System.currentTimeMillis(), date, amt, note))
                    }
                    showDialog = false
                }) { Text(s.save, color = AccentGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(s.cancel, color = TextMuted) }
            }
        )
    }
}
