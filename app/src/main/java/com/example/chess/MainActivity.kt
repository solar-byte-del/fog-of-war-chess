package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- МОДЕЛЬ ДАННЫХ ---
enum class PieceColor { WHITE, BLACK }
enum class PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }

data class Position(val row: Int, val col: Int)

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor
) {
    fun getSymbol(): String {
        return when (color) {
            PieceColor.WHITE -> when (type) {
                PieceType.PAWN -> "♙"
                PieceType.ROOK -> "♖"
                PieceType.KNIGHT -> "♘"
                PieceType.BISHOP -> "♗"
                PieceType.QUEEN -> "♕"
                PieceType.KING -> "♔"
            }
            PieceColor.BLACK -> when (type) {
                PieceType.PAWN -> "♟"
                PieceType.ROOK -> "♜"
                PieceType.KNIGHT -> "♞"
                PieceType.BISHOP -> "♝"
                PieceType.QUEEN -> "♛"
                PieceType.KING -> "♚"
            }
        }
    }
}

typealias Board = Map<Position, ChessPiece>

// --- ГЛАВНАЯ АКТИВНОСТЬ ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    GameScreen()
                }
            }
        }
    }
}

// --- ЛОГИКА ИГРЫ ---
fun createInitialBoard(): Board {
    val b = mutableMapOf<Position, ChessPiece>()
    
    // Черные фигуры
    b[Position(0, 0)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
    b[Position(0, 1)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
    b[Position(0, 2)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
    b[Position(0, 3)] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
    b[Position(0, 4)] = ChessPiece(PieceType.KING, PieceColor.BLACK)
    b[Position(0, 5)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
    b[Position(0, 6)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
    b[Position(0, 7)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
    for (i in 0..7) b[Position(1, i)] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)

    // Белые фигуры
    for (i in 0..7) b[Position(6, i)] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
    b[Position(7, 0)] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
    b[Position(7, 1)] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
    b[Position(7, 2)] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
    b[Position(7, 3)] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
    b[Position(7, 4)] = ChessPiece(PieceType.KING, PieceColor.WHITE)
    b[Position(7, 5)] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
    b[Position(7, 6)] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
    b[Position(7, 7)] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)

    return b
}

fun getValidMoves(pos: Position, piece: ChessPiece, board: Board): List<Position> {
    val moves = mutableListOf<Position>()
    val row = pos.row
    val col = pos.col
    val direction = if (piece.color == PieceColor.WHITE) -1 else 1

    when (piece.type) {
        PieceType.PAWN -> {
            val f1 = Position(row + direction, col)
            if (f1.row in 0..7 && board[f1] == null) {
                moves.add(f1)
                val f2 = Position(row + 2 * direction, col)
                if (((piece.color == PieceColor.WHITE && row == 6) || (piece.color == PieceColor.BLACK && row == 1)) && board[f2] == null) {
                    moves.add(f2)
                }
            }
            for (dc in listOf(-1, 1)) {
                val target = Position(row + direction, col + dc)
                if (target.row in 0..7 && target.col in 0..7) {
                    val p = board[target]
                    if (p != null && p.color != piece.color) {
                        moves.add(target)
                    }
                }
            }
        }
        PieceType.KNIGHT -> {
            val offsets = listOf(
                Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
            )
            for (o in offsets) {
                val target = Position(row + o.first, col + o.second)
                if (target.row in 0..7 && target.col in 0..7) {
                    val p = board[target]
                    if (p == null || p.color != piece.color) moves.add(target)
                }
            }
        }
        PieceType.KING -> {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val target = Position(row + dr, col + dc)
                    if (target.row in 0..7 && target.col in 0..7) {
                        val p = board[target]
                        if (p == null || p.color != piece.color) moves.add(target)
                    }
                }
            }
        }
        PieceType.ROOK -> addLinearMoves(pos, piece.color, board, moves, listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)))
        PieceType.BISHOP -> addLinearMoves(pos, piece.color, board, moves, listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
        PieceType.QUEEN -> addLinearMoves(pos, piece.color, board, moves, listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1), Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
    }
    return moves
}

fun addLinearMoves(pos: Position, color: PieceColor, board: Board, moves: MutableList<Position>, dirs: List<Pair<Int, Int>>) {
    for (d in dirs) {
        var currRow = pos.row + d.first
        var currCol = pos.col + d.second
        while (currRow in 0..7 && currCol in 0..7) {
            val target = Position(currRow, currCol)
            val p = board[target]
            if (p == null) {
                moves.add(target)
            } else {
                if (p.color != color) moves.add(target)
                break
            }
            currRow += d.first
            currCol += d.second
        }
    }
}

fun getVisibleSquares(color: PieceColor, board: Board): Set<Position> {
    val visible = mutableSetOf<Position>()
    for ((pos, piece) in board) {
        if (piece.color == color) {
            visible.add(pos)
            visible.addAll(getValidMoves(pos, piece, board))
        }
    }
    return visible
}

// --- ИНТЕРФЕЙС ЭКРАНА ---
@Composable
fun GameScreen() {
    var board by remember { mutableStateOf(createInitialBoard()) }
    var turn by remember { mutableStateOf(PieceColor.WHITE) }
    var selectedPosition by remember { mutableStateOf<Position?>(null) }

    val visibleSquares = getVisibleSquares(turn, board)
    val activePiece = selectedPosition?.let { board[it] }
    val possibleMoves = if (activePiece != null && activePiece.color == turn) {
        getValidMoves(selectedPosition!!, activePiece, board)
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (turn == PieceColor.WHITE) "Ход Белых" else "Ход Черных",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.Black)
        ) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0..7) {
                        val currentPos = Position(row, col)
                        val isVisible = visibleSquares.contains(currentPos)
                        val piece = if (isVisible) board[currentPos] else null

                        val isSelected = selectedPosition == currentPos
                        val isPossibleTarget = possibleMoves.contains(currentPos)

                        val baseColor = if (!isVisible) {
                            Color(0xFF222222)
                        } else if ((row + col) % 2 == 0) {
                            Color(0xFFEEEEEE)
                        } else {
                            Color(0xFF769656)
                        }

                        val cellColor = when {
                            isSelected -> Color(0xFFF7EC74)
                            isPossibleTarget && piece != null -> Color(0xFFE25B5B)
                            isPossibleTarget -> Color(0xFFBAC94A)
                            else -> baseColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(cellColor)
                                .clickable {
                                    if (isPossibleTarget && selectedPosition != null) {
                                        val newBoard = board.toMutableMap()
                                        newBoard[currentPos] = board[selectedPosition!!]!!
                                        newBoard.remove(selectedPosition!!)
                                        board = newBoard
                                        selectedPosition = null
                                        turn = if (turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                                    } else if (piece != null && piece.color == turn) {
                                        selectedPosition = currentPos
                                    } else {
                                        selectedPosition = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (piece != null) {
                                Text(
                                    text = piece.getSymbol(),
                                    fontSize = 32.sp,
                                    color = if (piece.color == PieceColor.WHITE) Color.White else Color.Black
                                )
                            } else if (isPossibleTarget && isVisible) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0x44000000), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                board = createInitialBoard()
                turn = PieceColor.WHITE
                selectedPosition = null
            },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Начать заново")
        }
    }
}
