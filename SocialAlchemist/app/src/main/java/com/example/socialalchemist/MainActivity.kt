package com.example.socialalchemist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socialalchemist.ui.theme.SocialAlchemistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Matches your project's native Compose theme wrapper perfectly
            SocialAlchemistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SocialAlchemistApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAlchemistApp() {
    val recipients = listOf("Friend", "Partner", "Boss", "Parent")
    val situations = listOf("Apology", "Congratulations", "Thanks", "Cancel Plans")

    var selectedRecipient by remember { mutableStateOf<String?>(null) }
    var selectedSituation by remember { mutableStateOf<String?>(null) }
    var resultText by remember { mutableStateOf("") }
    var showInfo by remember { mutableStateOf(false) }

    val purple = Color(0xFF4B0082)
    val gold = Color(0xFFFFD700)

    val backgroundBrush = Brush.verticalGradient(listOf(purple, Color.Black))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "The Social Alchemist",
                        color = gold,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = purple)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calculation Output / Display Screen
                Text(
                    text = when {
                        resultText.isNotEmpty() -> resultText
                        selectedRecipient != null && selectedSituation != null -> "Press '=' to transmute..."
                        else -> "Select recipient & situation..."
                    },
                    color = gold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )

                // Grid 1: Recipients
                Text("Recipients", color = gold, fontWeight = FontWeight.Bold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(recipients) { label ->
                        CalculatorButton(
                            label = label,
                            selected = label == selectedRecipient,
                            onClick = {
                                selectedRecipient = label
                                resultText = ""
                            },
                            color = purple,
                            highlight = gold
                        )
                    }
                }

                // Grid 2: Situations
                Text("Situations", color = gold, fontWeight = FontWeight.Bold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(situations) { label ->
                        CalculatorButton(
                            label = label,
                            selected = label == selectedSituation,
                            onClick = {
                                selectedSituation = label
                                resultText = ""
                            },
                            color = purple,
                            highlight = gold
                        )
                    }
                }

                // Action Control Row ("AC" and "=") - UPDATED TO BE BIGGER AND BOLDER
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    // Enlarged AC Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp) // Increased height from 60.dp to 80.dp
                            .background(purple, shape = MaterialTheme.shapes.medium)
                            .clickable {
                                selectedRecipient = null
                                selectedSituation = null
                                resultText = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AC", color = gold, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    // Enlarged and Color-Inverted "=" Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp) // Increased height to match
                            .background(gold, shape = MaterialTheme.shapes.medium) // Gold background to pop out
                            .clickable {
                                // Triggers the processing logic inside SocialFormulas.kt
                                resultText = SocialFormulas.calculate(
                                    selectedRecipient,
                                    selectedSituation
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("=", color = purple, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            if (showInfo) {
                InfoDialog(onDismiss = { showInfo = false })
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color,
    highlight: Color
) {
    val background = if (selected) highlight else color
    val textColor = if (selected) color else highlight

    Box(
        modifier = Modifier
            .padding(8.dp)
            .height(60.dp)
            .background(background, shape = MaterialTheme.shapes.medium)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it!") }
        },
        title = { Text("About The Social Alchemist") },
        text = {
            Text(
                "The Social Alchemist transforms emotional nuance into linguistic gold. " +
                        "Select your recipient, choose a social context, and watch the app conjure " +
                        "the ideal message — part empathy, part art, pure alchemy."
            )
        }
    )
}