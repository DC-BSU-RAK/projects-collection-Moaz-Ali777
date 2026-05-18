// ============================================================
//  IRON RITUAL — Weightlifting Session Tracker
//  Module: Smartphone Apps | Assignment 2
//
//  Architecture Overview:
//  - Single Activity (MainActivity) hosting a NavHost
//  - Bottom Navigation Bar with 3 routes
//  - SharedPreferences wrapped in a singleton PrefsManager
//  - App-level state hoisted into MainActivity via mutableStateOf
//    so unit changes propagate to every screen instantly
// ============================================================

package com.example.ironritual

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// ============================================================
//  SECTION 1: THEME — Brutalist Dark Palette
//  All colours defined as top-level constants so they can be
//  referenced from any composable without passing them down.
// ============================================================

val OnyxBlack   = Color(0xFF0D0D0D)   // Primary background
val SurfaceGrey = Color(0xFF1A1A1A)   // Card / surface layer
val BorderGrey  = Color(0xFF2A2A2A)   // Subtle dividers
val NeonGreen   = Color(0xFF39FF14)   // Primary accent (neon green)
val NeonCyan    = Color(0xFF00E5FF)   // Secondary accent (cyan)
val DeadWhite   = Color(0xFFE8E8E8)   // Body text
val MutedGrey   = Color(0xFF888888)   // Placeholder / secondary text

// ============================================================
//  SECTION 2: SHARED PREFERENCES MANAGER
//
//  A lightweight singleton that wraps all read/write operations
//  for the app's three persistent preferences:
//    • nickname   (String)
//    • weightUnit (String — "KG" or "LBS")
//    • restTimer  (Boolean)
//
//  Usage anywhere in the app:
//    val prefs = PrefsManager(context)
//    prefs.nickname = "IRON_MIKE"
//    val unit = prefs.weightUnit
// ============================================================

class PrefsManager(context: Context) {

    // Obtain (or create) the SharedPreferences file named "iron_ritual_prefs"
    // MODE_PRIVATE means only this app can read or write this file
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("iron_ritual_prefs", Context.MODE_PRIVATE)

    // --- Keys: store as constants to avoid typos across the codebase ---
    companion object {
        const val KEY_NICKNAME    = "user_nickname"
        const val KEY_WEIGHT_UNIT = "weight_unit"
        const val KEY_REST_TIMER  = "rest_timer_sound"
    }

    // --- String preference: User Nickname ---
    // Kotlin property syntax: getter reads from prefs, setter writes to prefs
    var nickname: String
        get() = sharedPrefs.getString(KEY_NICKNAME, "LIFTER") ?: "LIFTER"
        set(value) = sharedPrefs.edit().putString(KEY_NICKNAME, value).apply()

    // --- String preference: Weight Unit ("KG" or "LBS") ---
    var weightUnit: String
        get() = sharedPrefs.getString(KEY_WEIGHT_UNIT, "KG") ?: "KG"
        set(value) = sharedPrefs.edit().putString(KEY_WEIGHT_UNIT, value).apply()

    // --- Boolean preference: Rest Timer Sound Toggle ---
    var restTimerSound: Boolean
        get() = sharedPrefs.getBoolean(KEY_REST_TIMER, true)
        set(value) = sharedPrefs.edit().putBoolean(KEY_REST_TIMER, value).apply()
}

// ============================================================
//  SECTION 3: NAVIGATION ROUTES
//
//  Sealed class acts as a type-safe enum for route strings.
//  Each object holds its route path, icon, and display label
//  — everything the BottomNavBar needs in one place.
// ============================================================

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object LiftDeck    : Screen("lift_deck",    "THE LIFT",   Icons.Default.FitnessCenter)
    object RecordsVault: Screen("records_vault","RECORDS",    Icons.Default.Star)
    object IronPrefs   : Screen("iron_prefs",   "SETTINGS",   Icons.Default.Settings)
}

// ============================================================
//  SECTION 4: MAIN ACTIVITY
//
//  Responsibilities:
//    1. Bootstrap Compose with setContent
//    2. Read initial preferences from SharedPreferences
//    3. Hold app-level mutableStateOf variables (hoisted state)
//       so the NavHost can pass them down to child screens
//    4. Host the Scaffold with BottomNavBar + NavHost
// ============================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IronRitualApp()
        }
    }
}

// ============================================================
//  SECTION 5: ROOT COMPOSABLE — IronRitualApp
//
//  This is the single source of truth for shared state.
//  All mutableStateOf variables are declared here and passed
//  down as parameters (state hoisting pattern).
// ============================================================

@Composable
fun IronRitualApp() {
    val context = LocalContext.current

    // Create one PrefsManager instance, tied to the composition lifetime
    val prefs = remember { PrefsManager(context) }

    // ── Hoisted State ─────────────────────────────────────────
    // These three variables mirror what is stored in SharedPreferences.
    // When the user edits them in IronPrefsScreen and saves, we:
    //   1. Write to prefs (persistence)
    //   2. Update these state vars (immediate UI recomposition)

    var nickname    by remember { mutableStateOf(prefs.nickname) }
    var weightUnit  by remember { mutableStateOf(prefs.weightUnit) }
    var restTimer   by remember { mutableStateOf(prefs.restTimerSound) }

    // ── Navigation Controller ──────────────────────────────────
    val navController = rememberNavController()

    // Dark status bar / overall dark surface
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = OnyxBlack,
            surface    = SurfaceGrey,
            primary    = NeonGreen,
        )
    ) {
        Scaffold(
            containerColor = OnyxBlack,
            bottomBar = {
                IronBottomNavBar(navController = navController)
            }
        ) { innerPadding ->

            // NavHost maps route strings → Composable screens
            NavHost(
                navController    = navController,
                startDestination = Screen.LiftDeck.route,
                modifier         = Modifier.padding(innerPadding)
            ) {
                // Route 1: The Lift Deck
                composable(Screen.LiftDeck.route) {
                    LiftDeckScreen(
                        nickname   = nickname,
                        weightUnit = weightUnit
                    )
                }

                // Route 2: Records Vault
                composable(Screen.RecordsVault.route) {
                    RecordsVaultScreen(weightUnit = weightUnit)
                }

                // Route 3: Iron Preferences
                composable(Screen.IronPrefs.route) {
                    IronPrefsScreen(
                        prefs          = prefs,
                        nickname       = nickname,
                        weightUnit     = weightUnit,
                        restTimer      = restTimer,
                        // Callbacks update hoisted state AND persist to SharedPreferences
                        onNicknameChange    = { newVal -> nickname   = newVal; prefs.nickname        = newVal },
                        onWeightUnitChange  = { newVal -> weightUnit = newVal; prefs.weightUnit      = newVal },
                        onRestTimerChange   = { newVal -> restTimer  = newVal; prefs.restTimerSound  = newVal }
                    )
                }
            }
        }
    }
}

// ============================================================
//  SECTION 6: BOTTOM NAVIGATION BAR
//
//  Iterates over the three Screen objects to build nav items.
//  currentBackStackEntry is observed as State so the selected
//  indicator updates reactively on navigation.
// ============================================================

@Composable
fun IronBottomNavBar(navController: NavController) {
    val screens = listOf(Screen.LiftDeck, Screen.RecordsVault, Screen.IronPrefs)

    // Observe the current back-stack entry; recomposes when route changes
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    NavigationBar(
        containerColor = SurfaceGrey,
        tonalElevation = 0.dp,
        modifier       = Modifier.border(1.dp, BorderGrey)
    ) {
        screens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected  = isSelected,
                icon      = {
                    Icon(
                        imageVector  = screen.icon,
                        contentDescription = screen.label,
                        tint         = if (isSelected) NeonGreen else MutedGrey
                    )
                },
                label     = {
                    Text(
                        text       = screen.label,
                        fontSize   = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isSelected) NeonGreen else MutedGrey
                    )
                },
                colors    = NavigationBarItemDefaults.colors(
                    indicatorColor         = OnyxBlack,   // No pill indicator — brutalist flat style
                    selectedIconColor      = NeonGreen,
                    unselectedIconColor    = MutedGrey,
                ),
                onClick   = {
                    navController.navigate(screen.route) {
                        // Pop everything up to (not including) the start destination
                        // so the back stack stays clean — no duplicates accumulate
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true   // avoid duplicate instances
                        restoreState    = true   // restore scroll / checkbox state
                    }
                }
            )
        }
    }
}

// ============================================================
//  SECTION 7: SCREEN 1 — THE LIFT DECK
//
//  A daily workout logger. Each compound lift has a checkbox
//  backed by a local remember { mutableStateOf(false) }.
//  The nickname from SharedPreferences is displayed as a
//  personalised header.
// ============================================================

@Composable
fun LiftDeckScreen(nickname: String, weightUnit: String) {

    // The four compound lifts — each tracks its own checked state
    data class Lift(val name: String, val muscle: String)
    val lifts = listOf(
        Lift("SQUAT",           "QUADS / GLUTES"),
        Lift("BENCH PRESS",     "CHEST / TRICEPS"),
        Lift("DEADLIFT",        "POSTERIOR CHAIN"),
        Lift("OVERHEAD PRESS",  "SHOULDERS / TRAPS"),
    )

    // One boolean state per lift, stored in a SnapshotStateList
    // so Compose detects changes to individual items
    val checkedStates = remember { mutableStateListOf(false, false, false, false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────────
        Text(
            text       = "IRON RITUAL",
            fontSize   = 11.sp,
            letterSpacing = 6.sp,
            color      = NeonGreen,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        // Nickname is read from hoisted state — updates without restart
        Text(
            text       = "WELCOME BACK, $nickname",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Black,
            color      = DeadWhite,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "TODAY'S RITUAL — MARK YOUR SETS",
            fontSize = 10.sp,
            color = MutedGrey,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(28.dp))

        // ── Lift Cards ──────────────────────────────────────────
        lifts.forEachIndexed { index, lift ->
            LiftCard(
                liftName   = lift.name,
                muscle     = lift.muscle,
                isChecked  = checkedStates[index],
                onChecked  = { checkedStates[index] = it }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(12.dp))

        // ── Session summary pill ────────────────────────────────
        val completedCount = checkedStates.count { it }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceGrey)
                .border(1.dp, if (completedCount == 4) NeonGreen else BorderGrey, RoundedCornerShape(4.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = if (completedCount == 4)
                    "✓  ALL LIFTS COMPLETE — RITUAL DONE"
                else
                    "$completedCount / ${lifts.size} LIFTS COMPLETED",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = if (completedCount == 4) NeonGreen else MutedGrey,
                letterSpacing = 1.sp,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ── Individual Lift Card ─────────────────────────────────────

@Composable
fun LiftCard(liftName: String, muscle: String, isChecked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceGrey)
            .border(
                width = 1.dp,
                color = if (isChecked) NeonGreen else BorderGrey,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onChecked(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text       = liftName,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Black,
                color      = if (isChecked) NeonGreen else DeadWhite,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = muscle,
                fontSize = 10.sp,
                color    = MutedGrey,
                letterSpacing = 1.sp
            )
        }
        Checkbox(
            checked  = isChecked,
            onCheckedChange = onChecked,
            colors   = CheckboxDefaults.colors(
                checkedColor         = NeonGreen,
                uncheckedColor       = MutedGrey,
                checkmarkColor       = OnyxBlack
            )
        )
    }
}

// ============================================================
//  SECTION 8: SCREEN 2 — RECORDS VAULT
//
//  Displays personal record (PR) values. In a production app
//  these would be editable and stored in SharedPreferences or
//  Room DB. Here they are pre-seeded to demonstrate the layout
//  and that the weightUnit preference propagates correctly.
// ============================================================

@Composable
fun RecordsVaultScreen(weightUnit: String) {

    data class Record(val lift: String, val weight: Int, val date: String)

    // Pre-seeded personal records (stored in KG internally)
    val records = listOf(
        Record("SQUAT",           130, "2025-03-14"),
        Record("BENCH PRESS",     80, "2025-02-28"),
        Record("DEADLIFT",        175, "2025-04-01"),
        Record("OVERHEAD PRESS",  80,  "2025-01-19"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text          = "RECORDS VAULT",
            fontSize      = 11.sp,
            letterSpacing = 6.sp,
            color         = NeonCyan,
            fontWeight    = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "PERSONAL BESTS",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Black,
            color      = DeadWhite,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "UNITS: $weightUnit",     // Reflects the saved preference live
            fontSize = 10.sp,
            color = MutedGrey,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(28.dp))

        records.forEach { record ->
            // Convert weight if the user has selected LBS
            val displayWeight = if (weightUnit == "LBS")
                (record.weight * 2.20462).toInt()
            else
                record.weight

            RecordCard(
                liftName      = record.lift,
                weight        = displayWeight,
                unit          = weightUnit,
                dateAchieved  = record.date
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Individual Record Card ───────────────────────────────────

@Composable
fun RecordCard(liftName: String, weight: Int, unit: String, dateAchieved: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceGrey)
            .border(1.dp, BorderGrey, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text          = liftName,
                fontSize      = 14.sp,
                fontWeight    = FontWeight.Black,
                color         = DeadWhite,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = dateAchieved,
                fontSize = 10.sp,
                color    = MutedGrey,
                letterSpacing = 1.sp
            )
        }
        // PR weight badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text       = "$weight",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Black,
                color      = NeonCyan
            )
            Text(
                text     = unit,
                fontSize = 10.sp,
                color    = MutedGrey,
                letterSpacing = 2.sp
            )
        }
    }
}

// ============================================================
//  SECTION 9: SCREEN 3 — IRON PREFERENCES
//
//  Contains three preference controls:
//    1. Nickname TextField  (String preference)
//    2. Unit Toggle         (String preference: "KG" | "LBS")
//    3. Rest Timer Switch   (Boolean preference)
//
//  All values are received as parameters (hoisted state) and
//  changes are pushed back up via lambda callbacks.
//  The PrefsManager write happens inside each callback in
//  IronRitualApp — keeping this screen stateless / pure.
// ============================================================

@Composable
fun IronPrefsScreen(
    prefs             : PrefsManager,    // passed in but writes happen via callbacks
    nickname          : String,
    weightUnit        : String,
    restTimer         : Boolean,
    onNicknameChange  : (String)  -> Unit,
    onWeightUnitChange: (String)  -> Unit,
    onRestTimerChange : (Boolean) -> Unit
) {
    // Local draft state for the TextField so we don't write on every keystroke;
    // we write only when the user taps "SAVE".
    var nicknameDraft by remember(nickname) { mutableStateOf(nickname) }
    var saved         by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────────
        Text(
            text          = "IRON PREFERENCES",
            fontSize      = 11.sp,
            letterSpacing = 6.sp,
            color         = NeonGreen,
            fontWeight    = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "CONFIGURE YOUR RITUAL",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Black,
            color      = DeadWhite,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(32.dp))

        // ──────────────────────────────────────────────────────
        //  PREFERENCE 1: Nickname  (String — SharedPreferences)
        // ──────────────────────────────────────────────────────
        PrefSectionLabel(label = "DISPLAY NAME")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value         = nicknameDraft,
            onValueChange = {
                nicknameDraft = it
                saved = false          // reset saved badge on new input
            },
            placeholder   = { Text("ENTER NICKNAME", color = MutedGrey, fontSize = 13.sp) },
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = NeonGreen,
                unfocusedBorderColor = BorderGrey,
                focusedTextColor     = DeadWhite,
                unfocusedTextColor   = DeadWhite,
                cursorColor          = NeonGreen,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        // Save button — writes to SharedPreferences via callback
        Button(
            onClick = {
                if (nicknameDraft.isNotBlank()) {
                    onNicknameChange(nicknameDraft.uppercase().trim())
                    saved = true
                }
            },
            shape  = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text       = if (saved) "✓  SAVED" else "SAVE NICKNAME",
                color      = OnyxBlack,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = BorderGrey)
        Spacer(Modifier.height(32.dp))

        // ──────────────────────────────────────────────────────
        //  PREFERENCE 2: Weight Unit  (String — "KG" or "LBS")
        // ──────────────────────────────────────────────────────
        PrefSectionLabel(label = "WEIGHT UNITS")
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "Changes reflect across all screens immediately.",
            fontSize = 11.sp,
            color    = MutedGrey
        )
        Spacer(Modifier.height(12.dp))

        // Toggle row: two tappable pills, one highlighted
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("KG", "LBS").forEach { unit ->
                val isActive = weightUnit == unit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) NeonGreen else SurfaceGrey)
                        .border(
                            1.dp,
                            if (isActive) NeonGreen else BorderGrey,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onWeightUnitChange(unit) }   // writes to prefs via callback
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = unit,
                        fontWeight = FontWeight.Black,
                        fontSize   = 16.sp,
                        color      = if (isActive) OnyxBlack else MutedGrey,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = BorderGrey)
        Spacer(Modifier.height(32.dp))

        // ──────────────────────────────────────────────────────
        //  PREFERENCE 3: Rest Timer Sound  (Boolean)
        // ──────────────────────────────────────────────────────
        PrefSectionLabel(label = "REST TIMER SOUND")
        Spacer(Modifier.height(8.dp))

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceGrey)
                .border(1.dp, BorderGrey, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "AUDIBLE REST ALERT",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = DeadWhite,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = if (restTimer) "ENABLED" else "DISABLED",
                    fontSize = 10.sp,
                    color    = if (restTimer) NeonGreen else MutedGrey,
                    letterSpacing = 2.sp
                )
            }
            Switch(
                checked         = restTimer,
                onCheckedChange = { onRestTimerChange(it) },   // writes Boolean to prefs
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = OnyxBlack,
                    checkedTrackColor   = NeonGreen,
                    uncheckedThumbColor = MutedGrey,
                    uncheckedTrackColor = BorderGrey,
                )
            )
        }

        Spacer(Modifier.height(40.dp))

        // ── Prefs summary footer ─────────────────────────────
        Text(
            text      = "All settings are saved automatically to device storage and persist between sessions.",
            fontSize  = 11.sp,
            color     = MutedGrey,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// ── Reusable section label ───────────────────────────────────

@Composable
fun PrefSectionLabel(label: String) {
    Text(
        text          = label,
        fontSize      = 10.sp,
        letterSpacing = 3.sp,
        color         = NeonCyan,
        fontWeight    = FontWeight.Bold
    )
}