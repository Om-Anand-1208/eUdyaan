package com.example.eudayan.redesign

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eudayan.R
import com.example.eudayan.auth.AuthState
import java.util.Calendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SessionTimes = listOf("10:00 AM", "11:00 AM", "12:00 PM", "02:00 PM")
private fun sessionDate(dayIndex: Int): LocalDate = LocalDate.now().plusDays(dayIndex.coerceIn(0, 2).toLong())

@Composable fun DoctorListScreen(name: String, onDoctor: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Find a counsellor")
        Spacer(Modifier.height(16.dp)); InputBox(query, { query = it }, "Search counsellors", Modifier.fillMaxWidth().height(53.dp))
        Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All", "Stress", "Anxiety").forEach { label -> FilterPill(label, filter == label) { filter = label } } }
        Spacer(Modifier.height(14.dp))
        val matches = Doctors.withIndex().filter { (index, doctor) ->
            (query.isBlank() || doctor.first.contains(query, true) || doctor.third.contains(query, true)) &&
                (filter == "All" || (filter == "Stress" && index != 2) || (filter == "Anxiety" && index != 1))
        }
        if (matches.isEmpty()) IllustratedPanel(R.drawable.onboarding_support, "No counsellors found", "Try a different name or support area.", "Clear search") { query = ""; filter = "All" }
        matches.forEach { (index, doctor) ->
            SoftCard(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(doctor.second), doctor.first, Modifier.size(100.dp).clip(CircleShape))
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) { Text(doctor.first, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink); Text("Counsellor", fontSize = 12.sp, color = BlueGray); Spacer(Modifier.height(10.dp)); GreenButton("View profile", Modifier.fillMaxWidth()) { onDoctor(index) } }
                }
            }
        }
    }
}

@Composable fun DoctorDetailScreen(doctorIndex: Int, dayIndex: Int, timeIndex: Int, onDay: (Int) -> Unit, onTime: (Int) -> Unit, onBack: () -> Unit, onBook: () -> Unit) {
    val doctor = Doctors[doctorIndex.coerceIn(0, Doctors.lastIndex)]
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        BackRow("Counsellor profile", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            SoftCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(doctor.second), doctor.first, Modifier.size(96.dp).clip(CircleShape)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(doctor.first, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Counsellor", color = BlueGray, fontSize = 12.sp) } }; Spacer(Modifier.height(10.dp)); Text(doctor.third, color = Muted, fontSize = 13.sp, lineHeight = 19.sp) }
            Spacer(Modifier.height(16.dp))
            SoftCard(Modifier.fillMaxWidth()) {
                Text("Choose an available time", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { repeat(3) { i -> val label = sessionDate(i).format(DateTimeFormatter.ofPattern("EEE\nd", Locale.getDefault())); Box(Modifier.weight(1f).height(68.dp).background(if (dayIndex == i) ButtonForest else Sage, RoundedCornerShape(12.dp)).clickable { onDay(i) }, contentAlignment = Alignment.Center) { Text(label, color = if (dayIndex == i) OnButton else Ink, fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Center) } } }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { SessionTimes.forEachIndexed { i, label -> Box(Modifier.weight(1f).height(48.dp).background(if (timeIndex == i) ButtonForest else Sage, RoundedCornerShape(10.dp)).clickable { onTime(i) }, contentAlignment = Alignment.Center) { Text(label, color = if (timeIndex == i) OnButton else Ink, fontSize = 10.sp, textAlign = TextAlign.Center) } } }
                Spacer(Modifier.height(14.dp))
            }
            Spacer(Modifier.height(14.dp)); SoftCard(Modifier.fillMaxWidth(), Sage) { Text("What happens next", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(5.dp)); Text("Save your session request here, then add it to your calendar if you like.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp) }
        }
        GreenButton("Save session request", Modifier.fillMaxWidth(), onBook)
        Spacer(Modifier.height(9.dp))
    }
}

@Composable fun BookingScreen(doctorIndex: Int, bookingDateEpoch: Long, timeIndex: Int, navigate: (String) -> Unit) {
    val context = LocalContext.current
    var calendarMessage by remember { mutableStateOf("") }
    val doctor = Doctors[doctorIndex.coerceIn(0, Doctors.lastIndex)]
    val bookingDate = LocalDate.ofEpochDay(bookingDateEpoch)
    val day = bookingDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    val time = SessionTimes[timeIndex.coerceIn(0,3)]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(22.dp)); Icon(Icons.Default.CheckCircle, null, Modifier.size(72.dp), tint = Forest); Spacer(Modifier.height(12.dp)); Heading("Session request saved"); Spacer(Modifier.height(17.dp))
        SoftCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(doctor.second), doctor.first, Modifier.size(90.dp).clip(CircleShape)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(doctor.first, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text("Counsellor", color = BlueGray, fontSize = 11.sp); Spacer(Modifier.height(9.dp)); Text(day, color = Ink, fontSize = 12.sp); Spacer(Modifier.height(5.dp)); Text(time, color = Ink, fontSize = 12.sp) } } }
        Spacer(Modifier.height(12.dp)); SoftCard(Modifier.fillMaxWidth(), Sage) { Text("Next step", color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(Modifier.height(5.dp)); Text("This request is stored on your device. The counsellor has not received it yet.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp) }
        Spacer(Modifier.height(14.dp)); OutlinedButton(onClick = {
            val selectedDate = bookingDate
            val start = Calendar.getInstance().apply { set(selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth, listOf(10,11,12,14)[timeIndex.coerceIn(0,3)], 0); set(Calendar.SECOND, 0) }.timeInMillis
            val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE, "eUdyaan session with ${doctor.first}").putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start).putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 60 * 60 * 1000)
            if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) else calendarMessage = "No calendar app is available on this device."
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest)) { Icon(Icons.Default.CalendarToday, null); Spacer(Modifier.width(8.dp)); Text("Add to calendar") }
        if (calendarMessage.isNotEmpty()) Text(calendarMessage, color = BlueGray, fontSize = 11.sp)
        Spacer(Modifier.height(8.dp)); GreenButton("Done", Modifier.fillMaxWidth()) { navigate("home") }
        Spacer(Modifier.height(10.dp)); Text("Back to Support", Modifier.clickable { navigate("support") }.padding(12.dp), color = BlueGray, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable fun NotificationsScreen(hasBooking: Boolean, latestMood: Int, doctorIndex: Int, bookingDateEpoch: Long, timeIndex: Int, navigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Updates")
        Spacer(Modifier.height(16.dp))
        if (hasBooking) SimpleRow(Icons.Default.CalendarToday, "Session request saved", "${Doctors[doctorIndex.coerceIn(0, Doctors.lastIndex)].first} · ${LocalDate.ofEpochDay(bookingDateEpoch).format(DateTimeFormatter.ofPattern("MMM d"))} · ${SessionTimes[timeIndex.coerceIn(0,3)]}") { navigate("booking") }
        if (latestMood >= 0) SimpleRow(Icons.Default.Spa, "Check-in saved", "Your mood log has today’s reflection.") { navigate("mood_log") }
        if (!hasBooking && latestMood < 0) IllustratedPanel(R.drawable.onboarding_checkin, "Nothing new yet", "Your check-ins and session requests will appear here.", "Start a check-in") { navigate("mood_checkin") }
    }
}

@Composable fun ProfileScreen(name: String, navigate: (String) -> Unit, onBack: () -> Unit, onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(74.dp).background(CardWhite).padding(horizontal = 18.dp).clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ArrowBack, "Back", tint = BlueGray); Spacer(Modifier.width(19.dp)); Text("Profile", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            SoftCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Image(painterResource(R.drawable.avatar_aanya), "Profile photo", Modifier.size(77.dp).clip(CircleShape)); Spacer(Modifier.width(18.dp)); Text(name, Modifier.weight(1f), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold); Box(Modifier.size(48.dp).clickable { navigate("personal") }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Edit, "Edit profile", tint = Forest, modifier = Modifier.size(21.dp)) } } }
            Spacer(Modifier.height(12.dp)); ProfileItem(Icons.Default.Person, "Personal details") { navigate("personal") }; ProfileItem(Icons.Default.Lock, "Login & security") { navigate("security") }; ProfileItem(Icons.Default.Shield, "Privacy & data") { navigate("privacy") }; ProfileItem(Icons.Default.Notifications, "Updates") { navigate("notifications") }; ProfileItem(Icons.Default.Spa, "Mood log") { navigate("mood_log") }; ProfileItem(Icons.Default.Settings, "Settings") { navigate("settings") }
            Spacer(Modifier.height(9.dp)); Row(Modifier.fillMaxWidth().background(DangerSurface, RoundedCornerShape(12.dp)).clickable { onSignOut() }.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Logout, null, tint = Danger); Spacer(Modifier.width(17.dp)); Text("Sign out", Modifier.weight(1f), color = Danger, fontWeight = FontWeight.Bold, fontSize = 15.sp); Icon(Icons.Default.ChevronRight, null, tint = Danger) }
        }
    }
}
@Composable fun ProfileItem(icon: ImageVector, label: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).background(CardWhite, RoundedCornerShape(14.dp)).clickable { onClick() }.padding(11.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(Sage, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Forest, modifier = Modifier.size(22.dp)) }; Spacer(Modifier.width(17.dp)); Text(label, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Icon(Icons.Default.ChevronRight, null, tint = BlueGray) } }

@Composable
fun SettingsScreen(
    kind: String, name: String, darkMode: Boolean, guestMode: Boolean,
    onNameChange: (String) -> Unit, onDarkModeChange: (Boolean) -> Unit,
    onClearCheckins: () -> Unit, onClearBooking: () -> Unit,
    onClearCommunity: () -> Unit, onClearChat: () -> Unit, onSignOut: () -> Unit,
    navigate: (String) -> Unit, onBack: () -> Unit
) {
    val title = when(kind) {
        "personal" -> "Personal details"; "security" -> "Login & security"
        "privacy" -> "Privacy & data"; "appearance" -> "Appearance"
        "data" -> "Local data"; "about" -> "About eUdyaan"; else -> "Settings"
    }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eudyaan_redesign", 0) }
    var draft by remember(name) { mutableStateOf(name) }
    var savedMessage by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(prefs.getBoolean("anonymous", true)) }
    var dataRevision by remember { mutableIntStateOf(0) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        containerColor = CardWhite,
        title = { Text(when (pendingDelete) { "checkins" -> "Delete check-ins?"; "booking" -> "Delete session request?"; "community" -> "Clear Community activity?"; else -> "Clear ManSathi chat?" }) },
        text = { Text("This removes the saved information from this device.") },
        confirmButton = { TextButton(onClick = {
            when (pendingDelete) { "checkins" -> onClearCheckins(); "booking" -> onClearBooking(); "community" -> onClearCommunity(); "chat" -> onClearChat() }
            dataRevision++; pendingDelete = null
        }) { Text("Delete", color = Danger) } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        BackRow(if (kind in listOf("appearance", "data", "about")) "Settings" else "Profile", onBack)
        Heading(title); Spacer(Modifier.height(18.dp))
        when(kind) {
            "settings" -> {
                SettingsToggle(Icons.Default.DarkMode, "Dark mode", darkMode, onDarkModeChange)
                Spacer(Modifier.height(10.dp))
                SimpleRow(Icons.Default.Palette, "Appearance") { navigate("appearance") }
                SimpleRow(Icons.Default.Shield, "Privacy & data") { navigate("privacy") }
                SimpleRow(Icons.Default.Storage, "Local data") { navigate("data") }
                SimpleRow(Icons.Default.Info, "About eUdyaan") { navigate("about") }
            }
            "appearance" -> {
                SettingsToggle(Icons.Default.DarkMode, "Dark mode", darkMode, onDarkModeChange)
                Spacer(Modifier.height(14.dp))
                SoftCard(Modifier.fillMaxWidth(), Sage) {
                    Text("A calm space, day or night", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp)); Text("Your choice is saved for the next time you open eUdyaan.", color = Muted, fontSize = 13.sp)
                }
            }
            "personal" -> {
                SoftCard(Modifier.fillMaxWidth()) {
                    Text("Display name", color = Ink, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp)); InputBox(draft, { draft = it; savedMessage = "" }, "Your name", Modifier.fillMaxWidth().height(48.dp))
                    Spacer(Modifier.height(13.dp)); GreenButton("Save name", Modifier.fillMaxWidth()) {
                        if (draft.isBlank()) savedMessage = "Enter a name to continue."
                        else { onNameChange(draft.trim()); savedMessage = "Name updated." }
                    }
                    if (savedMessage.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(savedMessage, color = if (draft.isBlank()) Danger else Forest, fontSize = 13.sp) }
                }
            }
            "security" -> {
                SoftCard(Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Lock, null, tint = Forest)
                    Spacer(Modifier.height(8.dp)); Text(if (guestMode) "Guest session" else "Account session", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp)); Text(if (guestMode) "Your activity stays on this device. Sign in to use an account when the service is available." else "You are signed in to your eUdyaan account.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(14.dp))
                    if (guestMode) GreenButton("Sign in to an account", Modifier.fillMaxWidth()) { navigate("login") }
                    else OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Sign out") }
                }
            }
            "privacy" -> {
                SettingsToggle(Icons.Default.Shield, "Anonymous posting", anonymous) { anonymous = it; prefs.edit().putBoolean("anonymous", it).apply() }
                Spacer(Modifier.height(10.dp))
                SimpleRow(Icons.Default.Storage, "Manage local data", "Check-ins and session requests") { navigate("data") }
            }
            "data" -> {
                val count = remember(dataRevision) { prefs.all.keys.count { it.startsWith("mood_") && !it.startsWith("mood_note_") } }
                val bookingSaved = remember(dataRevision) { prefs.getBoolean("has_booking", false) }
                SoftCard(Modifier.fillMaxWidth()) {
                    Text("Mood check-ins", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(5.dp)); Text("$count saved on this device", color = Muted, fontSize = 13.sp)
                    if (count > 0) { Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = { pendingDelete = "checkins" }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Delete check-ins", color = Danger) } }
                }
                Spacer(Modifier.height(10.dp))
                SoftCard(Modifier.fillMaxWidth()) {
                    Text("Session request", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(5.dp)); Text(if (bookingSaved) "One request saved on this device" else "No saved request", color = Muted, fontSize = 13.sp)
                    if (bookingSaved) { Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = { pendingDelete = "booking" }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Delete request", color = Danger) } }
                }
                Spacer(Modifier.height(10.dp))
                val communitySaved = remember(dataRevision) { prefs.contains("posts_json") }
                SoftCard(Modifier.fillMaxWidth()) {
                    Text("Community activity", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(5.dp)); Text(if (communitySaved) "Posts, replies, and likes saved locally" else "No local activity saved", color = Muted, fontSize = 13.sp)
                    if (communitySaved) { Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = { pendingDelete = "community" }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Clear Community activity", color = Danger) } }
                }
                Spacer(Modifier.height(10.dp))
                val chatSaved = remember(dataRevision) { prefs.contains("chat_json") }
                SoftCard(Modifier.fillMaxWidth()) {
                    Text("ManSathi chat", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(5.dp)); Text(if (chatSaved) "Conversation saved locally" else "No conversation saved", color = Muted, fontSize = 13.sp)
                    if (chatSaved) { Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = { pendingDelete = "chat" }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Clear conversation", color = Danger) } }
                }
            }
            "about" -> {
                SoftCard(Modifier.fillMaxWidth(), Sage) { BrandLockup(48, 30); Spacer(Modifier.height(8.dp)); Text("A space to reflect, connect, and explore support.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp) }
                Spacer(Modifier.height(10.dp)); SimpleRow(Icons.Default.Spa, "Explore practices") { navigate("support") }
            }
        }
    }
}

@Composable private fun SettingsToggle(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(43.dp).background(Sage, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Forest, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(11.dp)); Text(title, Modifier.weight(1f), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(
                checkedThumbColor = OnButton, checkedTrackColor = ButtonForest,
                uncheckedThumbColor = Muted, uncheckedTrackColor = Sage,
                uncheckedBorderColor = CardOutline
            ))
        }
    }
}

@Composable fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Ivory), contentAlignment = Alignment.Center) {
        Floral(Modifier.align(Alignment.BottomEnd).size(220.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) { BrandLockup(96, 44); Spacer(Modifier.height(9.dp)); Text("A little space to grow", color = Muted, fontSize = 15.sp) }
    }
}

@Composable fun OnboardingScreen(onDone: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val titles = listOf("A gentle space for every feeling", "Find people who understand", "Support at your own pace")
    val subtitles = listOf("Check in with yourself and notice the little things, one day at a time.", "Share in a kind community or talk with ManSathi whenever you need to.", "Explore guided practices and connect with a counsellor when you’re ready.")
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Box(Modifier.size(48.dp).clickable { onDone() }, contentAlignment = Alignment.Center) { Text("Skip", color = Muted, fontSize = 14.sp) } }
        Spacer(Modifier.weight(0.25f))
        Image(painterResource(listOf(R.drawable.onboarding_checkin, R.drawable.onboarding_community, R.drawable.onboarding_support)[page]), null, Modifier.fillMaxWidth().heightIn(max = 285.dp).weight(1f), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(24.dp)); Text(titles[page], color = Ink, fontSize = 27.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(11.dp)); Text(subtitles[page], color = Muted, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(0.3f)); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(3) { i -> Box(Modifier.size(if (i == page) 18.dp else 7.dp, 7.dp).background(if (i == page) Forest else CardOutline, CircleShape)) } }
        Spacer(Modifier.height(20.dp)); GreenButton(if (page == 2) "Get started" else "Continue", Modifier.fillMaxWidth()) { if (page == 2) onDone() else page++ }; Spacer(Modifier.height(12.dp))
    }
}

@Composable fun AuthScreen(signup: Boolean, onSwitch: () -> Unit, onContinue: (String, String, String) -> Unit, onGuest: () -> Unit, authState: AuthState) {
    var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var password by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Spacer(Modifier.height(16.dp)); BrandLockup(42, 28)
        Spacer(Modifier.height(32.dp)); Text(if (signup) "Make room to grow" else "Welcome back", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
        Text(if (signup) "Create your space for wellbeing." else "Sign in to continue your journey.", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(26.dp)); if (signup) { AuthLabel("Your name"); InputBox(name, { name = it }, "What should we call you?", Modifier.fillMaxWidth().height(49.dp)); Spacer(Modifier.height(15.dp)) }
        AuthLabel("Email address"); InputBox(email, { email = it }, "you@example.com", Modifier.fillMaxWidth().height(49.dp)); Spacer(Modifier.height(15.dp))
        AuthLabel("Password"); InputBox(password, { password = it }, "Enter your password", Modifier.fillMaxWidth().height(49.dp), isPassword = true)
        val shownError = if (authState is AuthState.Error) authState.message else error
        if (shownError.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(shownError, color = Danger, fontSize = 12.sp) }
        Spacer(Modifier.height(23.dp)); GreenButton(if (authState is AuthState.Loading) "Please wait..." else if (signup) "Create account" else "Sign in", Modifier.fillMaxWidth()) { if (!email.contains('@') || password.length < 4 || (signup && name.isBlank())) error = "Enter your details and a password with at least 4 characters." else onContinue(name, email, password) }
        Spacer(Modifier.height(18.dp)); Text(if (signup) "Already have an account?  Sign in" else "New here?  Create an account", Modifier.align(Alignment.CenterHorizontally).clickable { onSwitch() }, color = Forest, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(17.dp)); Text("Continue as a guest", Modifier.align(Alignment.CenterHorizontally).clickable { onGuest() }, color = BlueGray, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp)); Image(painterResource(R.drawable.onboarding_checkin), null, Modifier.fillMaxWidth().height(145.dp), contentScale = ContentScale.Fit)
    }
}
@Composable fun AuthLabel(text: String) { Text(text, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)) }

@Composable fun ResourceScreen(kind: String, onBack: () -> Unit) {
    var playing by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(120) }
    LaunchedEffect(playing) { while (playing && seconds > 0) { kotlinx.coroutines.delay(1000); seconds-- }; if (seconds == 0) playing = false }
    val title = when(kind) { "breathing" -> "Guided breathing"; "meditation" -> "Meditation & audio"; else -> "Mindful articles" }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackRow("Support", onBack)
        Heading(title)
        Spacer(Modifier.height(24.dp))
        if (kind == "articles") {
            SoftCard(Modifier.fillMaxWidth(), Sage) { Text("Small steps for a stressful day", color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp); Spacer(Modifier.height(12.dp)); Text("Notice where you are. Take a slow breath. Choose one small thing you can do next. You do not have to solve everything at once.", color = Muted, fontSize = 15.sp, lineHeight = 23.sp) }
            Spacer(Modifier.height(16.dp))
            SoftCard(Modifier.fillMaxWidth()) { Text("Making room for rest", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(9.dp)); Text("Rest is part of caring for yourself. A few quiet minutes can help you return with more ease.", color = Muted, fontSize = 14.sp, lineHeight = 21.sp) }
        } else {
            Box(Modifier.fillMaxWidth().height(200.dp).background(Sage, CardShape), contentAlignment = Alignment.Center) { Image(painterResource(R.drawable.onboarding_checkin), null, Modifier.size(185.dp), contentScale = ContentScale.Fit) }
            Spacer(Modifier.height(22.dp))
            Text(if (kind == "breathing") "Breathe in gently. Breathe out slowly." else "Let your shoulders soften and notice this moment.", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text("${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}", Modifier.align(Alignment.CenterHorizontally), color = Forest, fontSize = 29.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            GreenButton(if (playing) "Pause" else if (seconds == 0) "Restart" else "Start practice", Modifier.fillMaxWidth()) { if (seconds == 0) { seconds = 120; playing = true } else playing = !playing }
        }
    }
}
