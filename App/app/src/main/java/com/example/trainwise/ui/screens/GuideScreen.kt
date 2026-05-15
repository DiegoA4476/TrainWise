package com.example.trainwise.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.data.models.Message
import com.example.trainwise.ui.theme.Orange
import com.example.trainwise.ui.viewmodels.GuideViewModel
import kotlinx.coroutines.launch

@Composable
fun GuideScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: GuideViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val chatMessages = viewModel.chatMessages
    val isLoading = viewModel.isLoading
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            GuideBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onWorkoutsClick = onNavigateToWorkouts,
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ChatHeader()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                items(chatMessages) { message ->
                    ChatBubble(
                        message = message,
                        onImportWorkout = { json ->
                            scope.launch {
                                viewModel.importWorkout(
                                    jsonString = json,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Workout imported successfully!")
                                        }
                                    },
                                    onError = { error ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error: $error")
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
                
                if (isLoading) {
                    item {
                        LoadingBubble()
                    }
                }
            }

            ChatInput(
                value = inputText,
                onValueChange = { inputText = it },
                onSendClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val messageToSend = inputText
                        inputText = ""
                        viewModel.sendMessage(messageToSend)
                    }
                },
                enabled = !isLoading
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
            modifier = Modifier.padding(end = 60.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thinking...", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ChatHeader() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AutoAwesome, 
                "AI", 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Trainer", 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp
                )
                Text(
                    text = "Online to help you", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, onImportWorkout: (String) -> Unit = {}) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    val workoutTag = "---WORKOUT_JSON---"
    val endTag = "---END_JSON---"
    
    val hasWorkout = !message.isFromUser && message.content.contains(workoutTag)
    
    val displayContent = if (hasWorkout) {
        message.content.substringBefore(workoutTag).trim()
    } else {
        message.content
    }

    val jsonContent = if (hasWorkout) {
        message.content.substringAfter(workoutTag).substringBefore(endTag).trim()
    } else null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column {
                Text(
                    text = displayContent,
                    modifier = Modifier.padding(12.dp),
                    color = textColor,
                    fontSize = 15.sp
                )
                
                if (hasWorkout && jsonContent != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Button(
                        onClick = { onImportWorkout(jsonContent) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add, 
                                "Import", 
                                tint = MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ADD TO MY WORKOUTS", 
                                color = MaterialTheme.colorScheme.primary, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInput(
    value: String, 
    onValueChange: (String) -> Unit, 
    onSendClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                placeholder = { 
                    Text(
                        "Ask for a workout...", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    cursorColor = Orange,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = enabled,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Orange,
                    disabledContainerColor = Orange.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, 
                    null, 
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun GuideBottomNavigationBar(onHomeClick: () -> Unit, onWorkoutsClick: () -> Unit, onProfileClick: () -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onWorkoutsClick,
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, null) },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    }
}
