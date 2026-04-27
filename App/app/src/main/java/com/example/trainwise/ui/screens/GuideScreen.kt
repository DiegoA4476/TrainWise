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
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.GuideViewModel

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

    // Scroll to bottom when messages change or loading state changes
    LaunchedEffect(chatMessages.size, isLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size)
        }
    }

    Scaffold(
        bottomBar = {
            GuideBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onWorkoutsClick = onNavigateToWorkouts,
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = DarkBackground
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
                    ChatBubble(message)
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
            color = CardBackground,
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
                        .background(Orange)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Thinking...", color = GrayText, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ChatHeader() {
    Surface(
        color = CardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.AutoAwesome, "AI", tint = Orange, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("AI Trainer", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Online to help you", color = GrayText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isFromUser) Orange else CardBackground
    val textColor = if (message.isFromUser) DarkBackground else White
    val shape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                fontSize = 15.sp
            )
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
        color = Color.Black,
        modifier = Modifier.fillMaxWidth()
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
                placeholder = { Text("Ask for a workout...", color = GrayText) },
                modifier = Modifier
                    .weight(1f)
                    .background(CardBackground, RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    disabledContainerColor = CardBackground,
                    cursorColor = Orange,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    disabledTextColor = GrayText
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
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = DarkBackground)
            }
        }
    }
}

@Composable
fun GuideBottomNavigationBar(onHomeClick: () -> Unit, onWorkoutsClick: () -> Unit, onProfileClick: () -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onWorkoutsClick,
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White
            )
        )

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, null) },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Orange,
                selectedIconColor = White,
                selectedTextColor = White
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White
            )
        )
    }
}
