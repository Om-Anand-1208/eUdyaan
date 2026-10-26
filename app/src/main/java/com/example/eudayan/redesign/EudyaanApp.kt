package com.example.eudayan.redesign

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.view.WindowCompat
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eudayan.R
import com.example.eudayan.auth.AuthState
import com.example.eudayan.auth.AuthViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val CardShape = RoundedCornerShape(20.dp)

data class FeedPost(val text: String, val age: String, val icon: Int, val replies: androidx.compose.runtime.snapshots.SnapshotStateList<String> = mutableStateListOf(), var liked: Boolean = false, val author: String = "Anonymous")
val Doctors = listOf(
    Triple("Dr. Aafreen Khan", R.drawable.doctor_aafreen, "Compassionate support with a focus on CBT and mindfulness."),
    Triple("Dr. Sameer Wani", R.drawable.doctor_sameer, "Warm, practical guidance for stress and life transitions."),
    Triple("Dr. Nida Shah", R.drawable.doctor_nida, "Supportive care for emotional wellbeing and resilience.")
)
val MoodNames = listOf("Anxious", "Tired", "Okay", "Good", "Joyful", "Calm", "Happy", "Okay", "Anxious", "Low", "Tired")
val MoodIcons = listOf(R.drawable.mood_anxious, R.drawable.mood_tired, R.drawable.mood_okay, R.drawable.mood_good, R.drawable.mood_joyful, R.drawable.checkin_calm, R.drawable.checkin_happy, R.drawable.checkin_neutral, R.drawable.checkin_anxious, R.drawable.checkin_low, R.drawable.checkin_sleepy)

@Composable
fun EudyaanApp(previewScreen: String? = null) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eudyaan_redesign", 0) }
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    var screen by remember { mutableStateOf(previewScreen ?: "splash") }
    var previous by remember { mutableStateOf("home") }
    var selectedMood by remember { mutableIntStateOf(-1) }
    var latestMood by remember { mutableIntStateOf(prefs.getInt("mood_${LocalDate.now().toEpochDay()}", -1)) }
    var moodRevision by remember { mutableIntStateOf(0) }
    var latestMoodNote by remember { mutableStateOf(prefs.getString("mood_note_${LocalDate.now().toEpochDay()}", "") ?: "") }
    var selectedDoctor by remember { mutableIntStateOf(prefs.getInt("selected_doctor", 0)) }
    var selectedDay by remember { mutableIntStateOf(prefs.getInt("selected_day", 0)) }
    var selectedTime by remember { mutableIntStateOf(prefs.getInt("selected_time", 0)) }
    var bookingDateEpoch by remember { mutableStateOf(prefs.getLong("booking_date", LocalDate.now().toEpochDay())) }
    var hasBooking by remember { mutableStateOf(prefs.getBoolean("has_booking", false)) }
    var name by remember { mutableStateOf(prefs.getString("name", "Aanya") ?: "Aanya") }
    var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    var guestMode by remember { mutableStateOf(prefs.getBoolean("guest_mode", true)) }
    val activity = context as? Activity
    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkMode
                isAppearanceLightNavigationBars = !darkMode
            }
        }
    }
    val posts = remember { loadPosts(prefs) }
    fun persistPosts() { savePosts(prefs, posts) }
    fun go(next: String) { previous = screen; screen = next }
    fun saveMood(note: String = "") { if (selectedMood < 0) return; latestMood = selectedMood; latestMoodNote = note; val day = LocalDate.now().toEpochDay(); prefs.edit().putInt("mood_$day", selectedMood).putString("mood_note_$day", note).apply(); moodRevision++; go("mood_saved") }
    fun back() { screen = when(screen) { "doctor_detail" -> "doctors"; "booking" -> "doctor_detail"; "mood_saved" -> "home"; "post_detail" -> "community"; "mood_checkin" -> "home"; "doctors" -> "support"; "profile" -> "home"; else -> previous } }
    BackHandler(screen !in listOf("splash", "onboarding", "home", "login", "signup")) { back() }
    LaunchedEffect(screen) {
        if (screen == "splash") { delay(2300); screen = if (!prefs.getBoolean("seen_onboarding", false)) "onboarding" else if (prefs.getBoolean("session_active", false)) "home" else "login" }
    }
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            name = (authState as AuthState.Success).data.user.name
            prefs.edit().putString("name", name).putBoolean("session_active", true).apply()
            guestMode = false
            prefs.edit().putBoolean("guest_mode", false).apply()
            screen = "home"
            authViewModel.resetAuthState()
        }
    }
    val showChrome = screen !in listOf("splash", "onboarding", "login", "signup")
    val navSelected = when(screen) { "community", "post_detail", "write_post" -> "community"; "chat" -> "chat"; "support", "doctors", "doctor_detail", "booking" -> "support"; else -> "home" }
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
    EudyaanTheme(darkMode) {
    Column(Modifier.fillMaxSize().background(Ivory).statusBarsPadding().navigationBarsPadding()) {
        if (showChrome && screen != "profile") EudyaanHeader(onNotifications = { go("notifications") }, onProfile = { go("profile") })
        Box(Modifier.weight(1f)) {
            when(screen) {
                "splash" -> SplashScreen()
                "onboarding" -> OnboardingScreen(onDone = { prefs.edit().putBoolean("seen_onboarding", true).apply(); go("login") })
                "login" -> AuthScreen(false, onSwitch = { authViewModel.resetAuthState(); go("signup") }, onContinue = { _, email, password -> authViewModel.login(email, password, "user") }, onGuest = { guestMode = true; prefs.edit().putBoolean("session_active", true).putBoolean("guest_mode", true).apply(); go("home") }, authState = authState)
                "signup" -> AuthScreen(true, onSwitch = { authViewModel.resetAuthState(); go("login") }, onContinue = { entered, email, password -> authViewModel.signup(entered, email, password) }, onGuest = { guestMode = true; prefs.edit().putBoolean("session_active", true).putBoolean("guest_mode", true).apply(); go("home") }, authState = authState)
                "home" -> HomeScreen(name, selectedMood, onMood = { selectedMood = it }, onSave = { if (selectedMood >= 0) saveMood() else go("mood_checkin") }, navigate = ::go)
                "community" -> CommunityScreen(name, posts, onPostsChanged = ::persistPosts, navigate = ::go)
                "post_detail" -> PostDetailScreen(posts, onPostsChanged = ::persistPosts, navigate = ::go, onBack = ::back)
                "write_post" -> WritePostScreen(anonymous = prefs.getBoolean("anonymous", true), onBack = ::back, onPost = { text -> posts.add(0, FeedPost(text, LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d")), R.drawable.flower_avatar, author = if (prefs.getBoolean("anonymous", true)) "Anonymous" else name)); persistPosts(); go("community") })
                "chat" -> ChatScreen(name)
                "support" -> SupportScreen(name, navigate = ::go)
                "mood_log" -> MoodLogScreen(moodRevision, navigate = ::go)
                "mood_checkin" -> MoodCheckinScreen(name, selectedMood, onMood = { selectedMood = it }, onSave = { saveMood(it) })
                "mood_saved" -> MoodSavedScreen(selectedMood, navigate = ::go)
                "doctors" -> DoctorListScreen(name, onDoctor = { selectedDoctor = it; go("doctor_detail") })
                "doctor_detail" -> DoctorDetailScreen(selectedDoctor, selectedDay, selectedTime, onDay = { selectedDay = it }, onTime = { selectedTime = it }, onBack = ::back, onBook = { hasBooking = true; bookingDateEpoch = LocalDate.now().plusDays(selectedDay.toLong()).toEpochDay(); prefs.edit().putBoolean("has_booking", true).putLong("booking_date", bookingDateEpoch).putInt("selected_doctor", selectedDoctor).putInt("selected_day", selectedDay).putInt("selected_time", selectedTime).apply(); go("booking") })
                "booking" -> BookingScreen(selectedDoctor, bookingDateEpoch, selectedTime, navigate = ::go)
                "notifications" -> NotificationsScreen(hasBooking, latestMood, selectedDoctor, bookingDateEpoch, selectedTime, navigate = ::go)
                "profile" -> ProfileScreen(name, navigate = ::go, onBack = ::back, onSignOut = { prefs.edit().putBoolean("session_active", false).apply(); go("login") })
                "settings", "personal", "security", "privacy", "appearance", "data", "about" -> SettingsScreen(
                    kind = screen, name = name, darkMode = darkMode, guestMode = guestMode,
                    onNameChange = { name = it; prefs.edit().putString("name", it).apply() },
                    onDarkModeChange = { darkMode = it; prefs.edit().putBoolean("dark_mode", it).apply() },
                    onClearCheckins = { val edit = prefs.edit(); prefs.all.keys.filter { it.startsWith("mood_") }.forEach { key -> edit.remove(key) }; edit.apply(); latestMood = -1; latestMoodNote = ""; moodRevision++ },
                    onClearBooking = { prefs.edit().remove("has_booking").remove("booking_date").apply(); hasBooking = false },
                    onClearCommunity = { prefs.edit().remove("posts_json").apply(); posts.clear(); posts.addAll(samplePosts()) },
                    onClearChat = { prefs.edit().remove("chat_json").apply() },
                    onSignOut = { prefs.edit().putBoolean("session_active", false).apply(); go("login") },
                    navigate = ::go, onBack = ::back
                )
                "breathing", "meditation", "articles" -> ResourceScreen(screen, onBack = { go("support") })
                else -> HomeScreen(name, selectedMood, onMood = { selectedMood = it }, onSave = { saveMood() }, navigate = ::go)
            }
        }
        if (showChrome && !keyboardVisible) BottomNav(navSelected, onNavigate = ::go)
    }
    }
}

@Composable
fun EudyaanHeader(onNotifications: () -> Unit, onProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Sage).clickable { onNotifications() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Notifications, "Notifications", tint = Forest, modifier = Modifier.size(23.dp))
        }
        BrandLockup(40, 24)
        Box(Modifier.size(48.dp).clickable { onProfile() }, contentAlignment = Alignment.Center) { Image(painterResource(R.drawable.avatar_aanya), "Profile", Modifier.size(42.dp).clip(CircleShape)) }
    }
}

@Composable fun BrandLockup(iconSize: Int, titleSize: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(iconSize.dp).background(if (LocalEudyaanPalette.current.isDark) Color(0xFFF0F5E9) else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.brand_wreath_refined), null, Modifier.size((iconSize - 2).dp))
        }
        Spacer(Modifier.width(2.dp))
        Text("eUdyaan", fontFamily = AwesomeTitleFamily, fontWeight = FontWeight.Normal, fontSize = titleSize.sp, color = Forest)
    }
}

@Composable
fun BottomNav(selected: String, onNavigate: (String) -> Unit) {
    val items = listOf(Triple("home", "Home", Icons.Outlined.Home), Triple("community", "Community", Icons.Outlined.People), Triple("chat", "ManSathi", Icons.Outlined.ChatBubbleOutline), Triple("support", "Support", Icons.Outlined.FavoriteBorder))
    Row(Modifier.fillMaxWidth().height(66.dp).shadow(2.dp).background(CardWhite), verticalAlignment = Alignment.CenterVertically) {
        items.forEach { (route, label, icon) ->
            val active = selected == route
            Column(Modifier.weight(1f).fillMaxHeight().clickable { onNavigate(route) }.padding(top = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, label, tint = if (active) Forest else BlueGray, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(2.dp))
                Text(label, color = if (active) Forest else BlueGray, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                if (active) Box(Modifier.padding(top = 4.dp).width(30.dp).height(3.dp).background(Forest, CircleShape))
            }
        }
    }
}

@Composable fun Heading(title: String, subtitle: String? = null) {
    Text(title, fontSize = 25.sp, lineHeight = 29.sp, color = Ink, fontWeight = FontWeight.Bold)
    if (subtitle != null) { Spacer(Modifier.height(3.dp)); Text(subtitle, fontSize = 14.sp, lineHeight = 19.sp, color = Muted) }
}
@Composable fun SectionTitle(text: String) { Text(text, fontSize = 20.sp, color = Ink, fontWeight = FontWeight.Bold) }
@Composable fun GreenButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(13.dp)).background(Brush.horizontalGradient(listOf(ButtonForest, ForestLight))).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = OnButton, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}
@Composable fun SoftCard(modifier: Modifier = Modifier, color: Color = CardWhite, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.shadow(1.dp, CardShape).clip(CardShape).background(color).border(1.dp, CardOutline, CardShape).padding(14.dp), content = content)
}
@Composable fun Floral(modifier: Modifier = Modifier) { Image(painterResource(R.drawable.floral_corner), null, modifier) }
@Composable fun MoodFace(index: Int, size: Int = 52) { Image(painterResource(MoodIcons[index.coerceIn(0,MoodIcons.lastIndex)]), MoodNames[index.coerceIn(0,MoodNames.lastIndex)], Modifier.size(size.dp)) }
@Composable fun BackRow(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp).clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ArrowBack, "Back", tint = Ink, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(title, color = BlueGray, fontSize = 14.sp)
    }
}
@Composable fun SimpleRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).shadow(2.dp, RoundedCornerShape(14.dp)).background(CardWhite, RoundedCornerShape(14.dp)).clickable { onClick() }.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(43.dp).background(Sage, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Forest, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink); if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = Muted, lineHeight = 14.sp) }
        Icon(Icons.Default.ChevronRight, null, tint = BlueGray, modifier = Modifier.size(19.dp))
    }
}

@Composable
fun IllustratedPanel(illustration: Int, title: String, detail: String? = null, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth().clip(CardShape).background(Sage).border(1.dp, CardOutline, CardShape).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(illustration), null, Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.height(8.dp))
        Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (detail != null) { Spacer(Modifier.height(5.dp)); Text(detail, color = Muted, fontSize = 13.sp, lineHeight = 18.sp, textAlign = TextAlign.Center) }
        if (action != null) { Spacer(Modifier.height(14.dp)); GreenButton(action, Modifier.fillMaxWidth(), onAction) }
    }
}

@Composable fun HomeScreen(name: String, selectedMood: Int, onMood: (Int) -> Unit, onSave: () -> Unit, navigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Hello, $name")
        Spacer(Modifier.height(23.dp))
        SoftCard(Modifier.fillMaxWidth(), Peach) {
            Text("How are you feeling today?", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("All feelings are welcome here.", color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(20.dp))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val faceSize = if (maxWidth < 310.dp) 42 else 58
                Row(Modifier.fillMaxWidth()) {
                    MoodNames.take(5).forEachIndexed { index, mood ->
                        Column(Modifier.weight(1f).clickable { onMood(index) }, horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(faceSize.dp).border(if (selectedMood == index) 2.dp else 1.dp, if (selectedMood == index) Forest else CardWhite, CircleShape).padding(3.dp), contentAlignment = Alignment.Center) { MoodFace(index, faceSize - 10) }
                            Text(mood, color = Ink, fontSize = if (faceSize == 42) 10.sp else 11.sp, maxLines = 1)
                        }
                    }
                }
            }
            Spacer(Modifier.height(23.dp)); GreenButton(if (selectedMood < 0) "Choose how you feel" else "Save check-in", Modifier.fillMaxWidth(), onSave); Spacer(Modifier.height(7.dp))
        }
        Spacer(Modifier.height(17.dp)); SectionTitle("A little support")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupportTile("ManSathi", Icons.Default.ChatBubbleOutline, Modifier.weight(1f)) { navigate("chat") }
            SupportTile("Community", Icons.Default.People, Modifier.weight(1f)) { navigate("community") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SupportTile("Counsellors", Icons.Default.CalendarToday, Modifier.weight(1f)) { navigate("doctors") }
            SupportTile("Resources", Icons.Default.Book, Modifier.weight(1f)) { navigate("support") }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(145.dp).clip(CardShape).background(Sage)) {
            Floral(Modifier.align(Alignment.BottomEnd).size(132.dp))
            Column(Modifier.padding(16.dp)) { Text("DAILY PRACTICE", color = Muted, fontSize = 10.sp); Spacer(Modifier.height(5.dp)); Text("Grounding Breath", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("A two-minute reset", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(7.dp)); GreenButton("Start practice", Modifier.width(145.dp)) { navigate("breathing") } }
        }
        Spacer(Modifier.height(15.dp))
    }
}

@Composable fun SupportTile(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.height(78.dp).shadow(1.dp, CardShape).background(CardWhite, CardShape).border(1.dp, CardOutline, CardShape).clickable { onClick() }.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).background(Sage, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Forest, modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(8.dp)); Text(title, color = Ink, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable fun CommunityScreen(name: String, posts: List<FeedPost>, onPostsChanged: () -> Unit, navigate: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eudyaan_redesign", 0) }
    val anonymous = prefs.getBoolean("anonymous", true)
    var tab by remember { mutableStateOf("For you") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Community")
        Spacer(Modifier.height(9.dp)); Row(Modifier.background(Sage, CircleShape).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Shield, null, tint = Forest, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(if (anonymous) "Posting anonymously" else "Posting with your name", color = Muted, fontSize = 12.sp) }
        Spacer(Modifier.height(12.dp))
        SoftCard(Modifier.fillMaxWidth(), Peach) {
            Text("What’s on your mind?", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("Share a thought or ask a question.", color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(10.dp)); GreenButton("Write a post", Modifier.fillMaxWidth()) { navigate("write_post") }
        }
        Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("For you", "Recent").forEach { label -> FilterPill(label, tab == label) { tab = label } } }
        Spacer(Modifier.height(9.dp))
        val visiblePosts = posts.withIndex().let { indexed -> if (tab == "For you") indexed.sortedWith(compareByDescending<IndexedValue<FeedPost>> { it.value.age != "Community example" }.thenByDescending { it.value.replies.size }) else indexed }
        visiblePosts.forEach { (index, post) -> PostCard(post, onReply = { PostSelection.index = index; navigate("post_detail") }, onLikeChanged = onPostsChanged); Spacer(Modifier.height(9.dp)) }
        Box(Modifier.fillMaxWidth().clip(CardShape).background(Sage).padding(14.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Shield, null, tint = Forest); Spacer(Modifier.width(10.dp)); Column { Text("A kind, respectful space", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("Listen, be considerate, and protect one another’s privacy.", color = Muted, fontSize = 11.sp, lineHeight = 15.sp) } } }
        Spacer(Modifier.height(14.dp))
    }
}
object PostSelection { var index = 0 }
@Composable fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) { Box(Modifier.heightIn(min = 48.dp).clip(CircleShape).background(if (selected) ButtonForest else Sage).clickable { onClick() }.padding(horizontal = 22.dp, vertical = 10.dp), contentAlignment = Alignment.Center) { Text(label, color = if (selected) OnButton else Ink, fontSize = 12.sp) } }
@Composable fun PostCard(post: FeedPost, onReply: () -> Unit, showReplyAction: Boolean = true, onLikeChanged: () -> Unit = {}) {
    val context = LocalContext.current
    var liked by remember(post) { mutableStateOf(post.liked) }
    var menuExpanded by remember(post) { mutableStateOf(false) }
    SoftCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(post.icon), null, Modifier.size(40.dp).clip(CircleShape)); Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) { Text(post.author, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(post.age, color = Muted, fontSize = 11.sp) }
            Box {
                Box(Modifier.size(48.dp).clickable { menuExpanded = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.MoreVert, "Post options", tint = Muted, modifier = Modifier.size(22.dp)) }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, containerColor = CardWhite) {
                    DropdownMenuItem(text = { Text("Copy post") }, onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("eUdyaan post", post.text)); menuExpanded = false
                    })
                    DropdownMenuItem(text = { Text("Share post") }, onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, post.text) }
                        context.startActivity(Intent.createChooser(intent, "Share post")); menuExpanded = false
                    })
                }
            }
        }
        Spacer(Modifier.height(12.dp)); Text(post.text, color = Ink, fontSize = 15.sp, lineHeight = 21.sp)
        Spacer(Modifier.height(11.dp)); HorizontalDivider(color = CardOutline); Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (showReplyAction) Row(Modifier.weight(1f).heightIn(min = 48.dp).clickable { onReply() }, verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ChatBubbleOutline, null, tint = Muted, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Reply", color = Muted, fontSize = 13.sp) }
            else Spacer(Modifier.weight(1f))
            Box(Modifier.size(48.dp).clickable { liked = !liked; post.liked = liked; onLikeChanged() }, contentAlignment = Alignment.Center) { Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Like", tint = Coral, modifier = Modifier.size(23.dp)) }
        }
    }
}

@Composable fun PostDetailScreen(posts: List<FeedPost>, onPostsChanged: () -> Unit, navigate: (String) -> Unit, onBack: () -> Unit) {
    val post = posts.getOrNull(PostSelection.index) ?: return
    var reply by remember { mutableStateOf("") }
    var update by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        BackRow("Community", onBack); Heading("Post and replies")
        Spacer(Modifier.height(12.dp)); PostCard(post, onReply = {}, showReplyAction = false, onLikeChanged = onPostsChanged)
        Spacer(Modifier.height(16.dp)); SectionTitle("Replies  ${post.replies.size}")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            post.replies.forEach { SoftCard(Modifier.fillMaxWidth().padding(top = 9.dp)) { Text("Anonymous", fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp); Spacer(Modifier.height(5.dp)); Text(it, color = Ink, fontSize = 14.sp) } }
        }
        Row(verticalAlignment = Alignment.CenterVertically) { InputBox(reply, { reply = it }, "Write a reply...", Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); Box(Modifier.size(48.dp).clip(CircleShape).background(ButtonForest).clickable { if (reply.isNotBlank()) { post.replies.add(reply.trim()); reply = ""; update++; onPostsChanged() } }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Send, "Send reply", tint = OnButton, modifier = Modifier.size(20.dp)) } }
        Spacer(Modifier.height(9.dp))
    }
}
@Composable fun WritePostScreen(anonymous: Boolean, onBack: () -> Unit, onPost: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackRow("Community", onBack)
        Heading("Write a post")
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Shield, null, tint = Forest, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp)); Text(if (anonymous) "Posting anonymously" else "Posting with your name", color = Forest, fontSize = 13.sp) }
        Spacer(Modifier.height(12.dp))
        InputBox(text, { text = it; error = "" }, "What would you like to share?", Modifier.fillMaxWidth().height(180.dp))
        Spacer(Modifier.height(16.dp))
        GreenButton("Share post", Modifier.fillMaxWidth()) { if (text.isNotBlank()) onPost(text.trim()) else error = "Write a message before sharing." }
        if (error.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(error, color = Danger, fontSize = 13.sp) }
    }
}

@Composable fun InputBox(value: String, onValueChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, isPassword: Boolean = false) {
    Box(modifier.border(1.dp, CardOutline, RoundedCornerShape(13.dp)).background(CardWhite, RoundedCornerShape(13.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
        if (value.isEmpty()) Text(hint, color = BlueGray, fontSize = 13.sp)
        BasicTextField(value, onValueChange, modifier = Modifier.fillMaxWidth(), visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Ink, fontFamily = RubikFamily))
    }
}

@Composable fun ChatScreen(name: String) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eudyaan_redesign", 0) }
    var message by remember { mutableStateOf("") }
    val messages = remember { loadChat(prefs, name) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("ManSathi")
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ChatBubbleOutline, null, tint = Forest, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Your supportive companion", color = Muted, fontSize = 13.sp) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(18.dp))
            messages.forEach { (mine, text) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    Text(text, Modifier.widthIn(max = 295.dp).padding(bottom = 8.dp).border(1.dp, CardOutline, RoundedCornerShape(16.dp)).background(if (mine) Sage else CardWhite, RoundedCornerShape(16.dp)).padding(13.dp), color = Ink, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
            if (messages.size == 1) {
                Spacer(Modifier.height(15.dp))
                IllustratedPanel(R.drawable.onboarding_community, "Start wherever you are", "Choose a prompt or write your own message.")
                Spacer(Modifier.height(15.dp))
                listOf("I’m feeling anxious", "I need to talk", "Help me slow down").forEach { prompt ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(14.dp)).background(CardWhite).border(1.dp, CardOutline, RoundedCornerShape(14.dp)).clickable { message = prompt }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(prompt, Modifier.weight(1f), color = Ink, fontSize = 13.sp)
                        Icon(Icons.Default.ChevronRight, null, tint = Forest, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Text("ManSathi is not a therapist.", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { InputBox(message, { message = it }, "Write a message...", Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); Box(Modifier.size(48.dp).background(ButtonForest, CircleShape).clickable { if (message.isNotBlank()) { messages.add(Pair(true, message.trim())); messages.add(Pair(false, "Thank you for sharing. What would feel helpful right now?")); saveChat(prefs, messages); message = "" } }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Send, "Send", tint = OnButton, modifier = Modifier.size(20.dp)) } }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable fun SupportScreen(name: String, navigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Find support")
        Spacer(Modifier.height(15.dp))
        Box(Modifier.fillMaxWidth().height(156.dp).clip(CardShape).background(Sage)) {
            Image(painterResource(R.drawable.onboarding_support), null, Modifier.align(Alignment.BottomEnd).size(150.dp), contentScale = ContentScale.Fit)
            Column(Modifier.padding(16.dp)) { Text("Talk to a counsellor", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(5.dp)); Text("Choose a time that works for you.", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(14.dp)); GreenButton("Find a counsellor", Modifier.width(170.dp)) { navigate("doctors") } }
        }
        Spacer(Modifier.height(22.dp)); SectionTitle("Explore at your pace")
        Spacer(Modifier.height(8.dp)); SimpleRow(Icons.Default.Spa, "Guided breathing", "2 min") { navigate("breathing") }
        SimpleRow(Icons.Default.PlayArrow, "Meditation", "2 min") { navigate("meditation") }
        SimpleRow(Icons.Default.Book, "Mindful articles") { navigate("articles") }
        Spacer(Modifier.height(20.dp))
        SoftCard(Modifier.fillMaxWidth(), Peach) {
            Text("Need to talk it through?", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text("ManSathi is here whenever you want to put your thoughts into words.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(14.dp))
            GreenButton("Open ManSathi", Modifier.fillMaxWidth()) { navigate("chat") }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable fun MoodLogScreen(moodRevision: Int, navigate: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eudyaan_redesign", 0) }
    val today = remember { LocalDate.now() }
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    val firstOffset = month.atDay(1).dayOfWeek.value % 7
    val rows = (firstOffset + month.lengthOfMonth() + 6) / 7
    val entries = remember(month, moodRevision) { (1..month.lengthOfMonth()).mapNotNull { day -> val date = month.atDay(day); val mood = prefs.getInt("mood_${date.toEpochDay()}", -1); if (mood >= 0) date to (mood to (prefs.getString("mood_note_${date.toEpochDay()}", "") ?: "")) else null } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Mood log")
        Spacer(Modifier.height(16.dp))
        SoftCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clickable { month = month.minusMonths(1) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.ChevronLeft, "Previous month", tint = Forest) }
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())), Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                Box(Modifier.size(48.dp).clickable { month = month.plusMonths(1) }, contentAlignment = Alignment.Center) { Icon(Icons.Default.ChevronRight, "Next month", tint = Forest) }
            }
            Row(Modifier.fillMaxWidth()) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, Modifier.weight(1f), color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center) } }
            Spacer(Modifier.height(8.dp))
            repeat(rows) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { column ->
                        val date = row * 7 + column - firstOffset + 1
                        val validDate = date in 1..month.lengthOfMonth()
                        val isToday = validDate && month == YearMonth.from(today) && date == today.dayOfMonth
                        val hasEntry = validDate && entries.any { it.first.dayOfMonth == date }
                        Box(Modifier.weight(1f).height(40.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(34.dp).background(if (isToday) ButtonForest else if (hasEntry) Sage else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                                if (validDate) Text("$date", color = if (isToday) OnButton else Ink, fontSize = 12.sp, fontWeight = if (hasEntry) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (entries.isNotEmpty()) entries.asReversed().forEach { (date, entry) ->
            SoftCard(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoodFace(entry.first, 42); Spacer(Modifier.width(10.dp))
                    Column { Text(if (date == today) "Today" else date.format(DateTimeFormatter.ofPattern("MMM d")), color = Muted, fontSize = 12.sp); Text(MoodNames[entry.first.coerceIn(0, MoodNames.lastIndex)], color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp); if (entry.second.isNotBlank()) Text(entry.second, color = Muted, fontSize = 12.sp) }
                }
            }
        } else IllustratedPanel(R.drawable.onboarding_checkin, "No check-ins this month", "Your reflections will appear here.")
        Spacer(Modifier.height(10.dp)); GreenButton("${if (prefs.getInt("mood_${today.toEpochDay()}", -1) >= 0) "Update" else "Add"} today’s check-in", Modifier.fillMaxWidth()) { navigate("mood_checkin") }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable fun MoodCheckinScreen(name: String, selectedMood: Int, onMood: (Int) -> Unit, onSave: (String) -> Unit) {
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(17.dp)); Heading("Daily check-in"); Spacer(Modifier.height(15.dp))
        SoftCard(Modifier.fillMaxWidth()) {
            Text("How are you feeling today?", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp)); val labels = listOf("Calm", "Happy", "Okay", "Anxious", "Low", "Tired"); val icons = listOf(5,6,7,8,9,10)
            repeat(2) { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(3) { col -> val i = row*3+col; Column(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if (selectedMood == icons[i]) Peach else Color.Transparent).clickable { onMood(icons[i]) }.padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { MoodFace(icons[i], 62); Text(labels[i], color = Ink, fontSize = 12.sp) } } }; Spacer(Modifier.height(12.dp)) }
            Spacer(Modifier.height(8.dp)); Text("Add a note (optional)", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp); Spacer(Modifier.height(6.dp)); InputBox(note, { note = it }, "What’s behind this feeling?", Modifier.fillMaxWidth().height(94.dp)); Spacer(Modifier.height(17.dp))
            if (selectedMood >= 0) GreenButton("Save check-in", Modifier.fillMaxWidth()) { onSave(note.trim()) }
            else Box(Modifier.fillMaxWidth().height(48.dp).background(Sage, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Text("Choose a feeling to continue", color = Muted, fontSize = 14.sp) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable fun MoodSavedScreen(selectedMood: Int, navigate: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(20.dp)); Icon(Icons.Default.CheckCircle, null, Modifier.size(75.dp), tint = Forest); Spacer(Modifier.height(13.dp)); Heading("Check-in saved")
        Spacer(Modifier.height(17.dp)); SoftCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { MoodFace(selectedMood, 60); Spacer(Modifier.width(18.dp)); Column { Text("Today", color = BlueGray, fontSize = 11.sp); Text(MoodNames[selectedMood.coerceIn(0, MoodNames.lastIndex)], color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp) } } }
        Spacer(Modifier.height(19.dp)); Text("What would feel helpful right now?", Modifier.fillMaxWidth(), color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp); Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SoftCard(Modifier.weight(1f).clickable { navigate("breathing") }, Sage) { Icon(Icons.Default.Spa, null, tint = Forest); Spacer(Modifier.height(8.dp)); Text("Try breathing", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) }; SoftCard(Modifier.weight(1f).clickable { navigate("chat") }, Peach) { Icon(Icons.Default.ChatBubbleOutline, null, tint = Forest); Spacer(Modifier.height(8.dp)); Text("Talk to ManSathi", color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) } }
        Spacer(Modifier.height(17.dp)); GreenButton("View mood log", Modifier.fillMaxWidth()) { navigate("mood_log") }; Spacer(Modifier.height(8.dp)); Text("Back to home", Modifier.clickable { navigate("home") }.padding(12.dp), color = Forest, fontSize = 13.sp)
    }
}
