package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// --- DATA STRUCTURES ---

enum class PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }
enum class PieceColor { WHITE, BLACK }

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor
)

data class Position(val row: Int, val col: Int)

typealias Board = Map<Position, ChessPiece>

// --- GAME LOGIC ---

fun getInitialBoard(): Board {
    val board = mutableMapOf<Position, ChessPiece>()
    
    val backRow = listOf(
        PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
        PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    )
    
    // Black pieces (Rows 0 and 1)
    for (col in 0..7) {
        board[Position(0, col)] = ChessPiece(backRow[col], PieceColor.BLACK)
        board[Position(1, col)] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
    }
    
    // White pieces (Rows 6 and 7)
    for (col in 0..7) {
        board[Position(6, col)] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
        board[Position(7, col)] = ChessPiece(backRow[col], PieceColor.WHITE)
    }
    
    return board
}

fun getValidMoves(pos: Position, piece: ChessPiece, board: Board): List<Position> {
    val moves = mutableListOf<Position>()
    val deltaRow = if (piece.color == PieceColor.WHITE) -1 else 1
    val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
    
    when (piece.type) {
        PieceType.PAWN -> {
            // One step forward
            val nextPos = Position(pos.row + deltaRow, pos.col)
            if (nextPos.row in 0..7 && board[nextPos] == null) {
                moves.add(nextPos)
                // Two steps from initial position
                val doublePos = Position(pos.row + 2 * deltaRow, pos.col)
                if (pos.row == startRow && board[doublePos] == null) {
                    moves.add(doublePos)
                }
            }
            // Captures
            for (dc in listOf(-1, 1)) {
                val capPos = Position(pos.row + deltaRow, pos.col + dc)
                if (capPos.row in 0..7 && capPos.col in 0..7) {
                    val target = board[capPos]
                    if (target != null && target.color != piece.color) {
                        moves.add(capPos)
                    }
                }
            }
        }
        PieceType.KNIGHT -> {
            val offsets = listOf(
                -2 to -1, -2 to 1, -1 to -2, -1 to 2,
                1 to -2, 1 to 2, 2 to -1, 2 to 1
            )
            for ((dr, dc) in offsets) {
                val target = Position(pos.row + dr, pos.col + dc)
                if (target.row in 0..7 && target.col in 0..7) {
                    if (board[target]?.color != piece.color) {
                        moves.add(target)
                    }
                }
            }
        }
        PieceType.BISHOP -> addSlidingMoves(pos, piece.color, board, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1), moves)
        PieceType.ROOK -> addSlidingMoves(pos, piece.color, board, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1), moves)
        PieceType.QUEEN -> addSlidingMoves(pos, piece.color, board, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0, 0 to -1, 0 to 1), moves)
        PieceType.KING -> {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr == 0 && dc == 0) continue
                    val target = Position(pos.row + dr, pos.col + dc)
                    if (target.row in 0..7 && target.col in 0..7) {
                        if (board[target]?.color != piece.color) {
                            moves.add(target)
                        }
                    }
                }
            }
        }
    }
    return moves
}

private fun addSlidingMoves(
    pos: Position,
    color: PieceColor,
    board: Board,
    directions: List<Pair<Int, Int>>,
    moves: MutableList<Position>
) {
    for ((dr, dc) in directions) {
        var r = pos.row + dr
        var c = pos.col + dc
        while (r in 0..7 && c in 0..7) {
            val target = Position(r, c)
            val piece = board[target]
            if (piece == null) {
                moves.add(target)
            } else {
                if (piece.color != color) {
                    moves.add(target)
                }
                break
            }
            r += dr
            c += dc
        }
    }
}

// Compute all squares visible to a given player based on their pieces' moves and positions
fun getVisibleSquares(color: PieceColor, board: Board): Set<Position> {
    val visible = mutableSetOf<Position>()
    for ((pos, piece) in board) {
        if (piece.color == color) {
            visible.add(pos) // Own pieces are always visible
            visible.addAll(getValidMoves(pos, piece, board)) // Squares they can move to are visible
        }
    }
    return visible
}

// --- UI COMPONENTS ---

class MainActivity : ComponentActivity() {
    override class onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1E1E1E)) {
                    ChessGameScreen()
                }
            }
        }
    }
}

@Composable
fun ChessGameScreen() {
    var board by remember { mutableStateOf(getInitialBoard()) }
    var turn by remember { mutableStateOf(PieceColor.WHITE) }
    var selectedPosition by remember { mutableStateOf<Position?>(null) }
    
    val visibleSquares = remember(board, turn) { getVisibleSquares(turn, board) }
    val activeValidMoves = remember(selectedPosition, board) {
        selectedPosition?.let { pos ->
            board[pos]?.let { piece ->
                getValidMoves(pos, piece, board)
            }
        } ?: emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ход: ${if (turn == PieceColor.WHITE) "Белые" else "Черные"}",
            color = Color.White,
            fontSize = 24,
            modifier = Modifier.padding(16.dp)
        )

        // Chessboard Layout
        Column(
            modifier = Modifier
                .size(360.dp)
                .background(Color.DarkGray)
        ) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..7) {
                        val currentPos = Position(row, col)
                        val isVisible = visibleSquares.contains(currentPos)
                        val piece = if (isVisible) board[currentPos] else null
                        
                        val isSelected = selectedPosition == currentPos
                        val isValidMoveTarget = activeValidMoves.contains(currentPos)
                        
                        // Base cell coloring
                        val isDarkSquare = (row + col) % 2 == 1
                        var baseColor = if (isDarkSquare) Color(0xFF769656) else Color(0xFFEEEEEE)
                        
                        // Apply Fog of War tinting if not visible
                        if (!isVisible) {
                            baseColor = Color(0xFF2B2B2B)
                        } else if (isSelected) {
                            baseColor = Color(0xFFF7EC7D)
                        } else if (isValidMoveTarget) {
                            baseColor = Color(0xFFBACA44)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(baseColor)
                                .clickable {
                                    if (isValidMoveTarget && selectedPosition != null) {
                                        // Execute move
                                        val newBoard = board.toMutableMap()
                                        val movingPiece = newBoard.remove(selectedPosition!!)
                                        if (movingPiece != null) {
                                            newBoard[currentPos] = movingPiece
                                        }
                                        board = newBoard
                                        selectedPosition = null
                                        turn = if (turn == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
                                    } else if (isVisible && piece != null && piece.color == turn) {
                                        // Select piece
                                        selectedPosition = if (isSelected) null else currentPos
                                    } else {
                                        // Clicked empty or fogged cell without valid actions
                                        selectedPosition = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVisible && piece != null) {
                                Text(
                                    text = getPieceSymbol(piece),
                                    fontSize = 28.sp,
                                    color = if (piece.color == PieceColor.WHITE) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getPieceSymbol(piece: ChessPiece): String {
    return when (piece.type) {
        PieceType.PAWN -> "♙"
        PieceType.ROOK -> "♖"
        PieceType.KNIGHT -> "♘"
        PieceType.BISHOP -> "♗"
        PieceType.QUEEN -> "♕"
        PieceType.KING -> "♔"
    }
}
