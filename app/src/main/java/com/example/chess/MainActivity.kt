package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    MainNavigation()
                }
            }
        }
    }
}

@Composable
fun MainNavigation() {
    var currentMode by remember { mutableStateOf(GameMode.MENU) }
    var difficulty by remember { mutableStateOf(BotDifficulty.EASY) }

    when (currentMode) {
        GameMode.MENU -> MenuScreen(onModeSelect = { mode, diff -> difficulty = diff; currentMode = mode })
        GameMode.PVP -> GameScreen(isBotMode = false, difficulty = difficulty, onBackToMenu = { currentMode = GameMode.MENU })
        GameMode.BOT -> GameScreen(isBotMode = true, difficulty = difficulty, onBackToMenu = { currentMode = GameMode.MENU })
    }
}

@Composable
fun MenuScreen(onModeSelect: (GameMode, BotDifficulty) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Шахматы: Туман Войны", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 48.dp))
        Button(onClick = { onModeSelect(GameMode.PVP, BotDifficulty.EASY) }, modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp)) { Text("Играть вдвоем (PvP)") }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Режим против Бота:", color = Color.Gray, fontSize = 16.sp)
        Button(onClick = { onModeSelect(GameMode.BOT, BotDifficulty.EASY) }, modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp)) { Text("Бот: Легкий уровень") }
        Button(onClick = { onModeSelect(GameMode.BOT, BotDifficulty.MEDIUM) }, modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp)) { Text("Бот: Средний уровень") }
    }
}

@Composable
fun GameScreen(isBotMode: Boolean, difficulty: BotDifficulty, onBackToMenu: () -> Unit) {
    var board by remember { mutableStateOf(createInitialBoard()) }
    var turn by remember { mutableStateOf(PieceColor.WHITE) }
    var selectedPosition by remember { mutableStateOf<Position?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val visibleSquares = getVisibleSquares(PieceColor.WHITE, board)
    val activePiece = selectedPosition?.let { board[it] }
    val possibleMoves = if (activePiece != null && activePiece.color == turn) getValidMoves(selectedPosition!!, activePiece, board) else emptyList()

    if (isBotMode && turn == PieceColor.BLACK) {
        LaunchedEffect(turn) {
            coroutineScope.launch {
                delay(600)
                board = makeBotMove(board, difficulty)
                turn = PieceColor.WHITE
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBackToMenu) { Text("Меню") }
            Text(text = if (turn == PieceColor.WHITE) "Ваш ход" else if (isBotMode) "Думает бот..." else "Ход Черных", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Black)) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0..7) {
                        val currentPos = Position(row, col)
                        val isVisible = if (isBotMode) visibleSquares.contains(currentPos) else getVisibleSquares(turn, board).contains(currentPos)
                        val piece = if (isVisible) board[currentPos] else null
                        val isSelected = selectedPosition == currentPos
                        val isPossibleTarget = possibleMoves.contains(currentPos)

                        val baseColor = if (!isVisible) Color(0xFF222222) else if ((row + col) % 2 == 0) Color(0xFFEEEEEE) else Color(0xFF769656)
                        val cellColor = when {
                            isSelected -> Color(0xFFF7EC74)
                            isPossibleTarget && piece != null -> Color(0xFFE25B5B)
                            isPossibleTarget -> Color(0xFFBAC94A)
                            else -> baseColor
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(cellColor).clickable {
                            if (turn == PieceColor.WHITE || !isBotMode) {
                                if (isPossibleTarget && selectedPosition != null) {
                                    val newBoard = board.toMutableMap()
                                    newBoard[currentPos] = board[selectedPosition!!]!!
                                    newBoard.remove(selectedPosition!!)
                                    board = newBoard
                                    selectedPosition = null
                                    turn = if (turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                                } else if (piece != null && piece.color == turn) {
                                    selectedPosition = currentPos
                                } else { selectedPosition = null }
                            }
                        }, contentAlignment = Alignment.Center) {
                            if (piece != null) {
                                Text(text = piece.getSymbol(), fontSize = 32.sp, color = if (piece.color == PieceColor.WHITE) Color.White else Color.Black)
                            } else if (isPossibleTarget && isVisible) {
                                Box(modifier = Modifier.size(12.dp).background(Color(0x44000000), CircleShape))
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
