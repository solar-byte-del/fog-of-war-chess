package com.example.chess

enum class PieceColor { WHITE, BLACK }
enum class PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }
enum class GameMode { MENU, PVP, BOT }

// Добавили HARD в список, чтобы ChessLogic.kt видел сложный режим
enum class BotDifficulty { EASY, MEDIUM, HARD }

data class Position(val row: Int, val col: Int)

data class ChessPiece(val type: PieceType, val color: PieceColor) {
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

fun createInitialBoard(): Board {
    val b = mutableMapOf<Position, ChessPiece>()
    b[Position(0, 0)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
    b[Position(0, 1)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
    b[Position(0, 2)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
    b[Position(0, 3)] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
    b[Position(0, 4)] = ChessPiece(PieceType.KING, PieceColor.BLACK)
    b[Position(0, 5)] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
    b[Position(0, 6)] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
    b[Position(0, 7)] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
    for (i in 0..7) b[Position(1, i)] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)

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
