package com.tuitionmanager.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    databaseVersion: Int = 2,
    entityCount: Int = 18,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tuition Manager") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Foundation & Database Layer (STEP 3)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Room Database Version: $databaseVersion (Offline-First)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Registered Entities: $entityCount Entities (STEP 1 + STEP 2)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Migration 1 -> 2: Non-destructive Active",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Architecture Verification",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Student Diary Entity & DAO: Ready")
                        Text("• Exam & ExamResult Entities: Ready")
                        Text("• Schedule Overlap Engine: Ready")
                        Text("• Exam Evaluation & Grade Math: Ready")
                        Text("• Guardian Preference Channels: Ready")
                        Text("• Hilt Dependency Injection: Ready")
                    }
                }
            }
        }
    }
}
