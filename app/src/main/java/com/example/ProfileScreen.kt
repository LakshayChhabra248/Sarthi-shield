package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = "John Doe",
                onValueChange = {},
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "+1 (555) 123-4567",
                onValueChange = {},
                label = { Text("Emergency Contact") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "A+",
                onValueChange = {},
                label = { Text("Blood Group") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
        }
    }
}
