package com.example.chess

import kotlin.random.Random

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
                    if (p != null && p.color != piece.color) moves.add(target)
                }
            }
        }
        PieceType.KNIGHT -> {
            val offsets = listOf(Pair(-2,-1), Pair(-2,1), Pair(-1,-2), Pair(-1,2), Pair(1,-2), Pair(1,2), Pair(2,-1), Pair(2,1))
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
        PieceType.ROOK -> addLinearMoves(pos, piece.color, board, moves, listOf(Pair(-1,0), Pair(1,0), Pair(0,-1), Pair(0,1)))
        PieceType.BISHOP -> addLinearMoves(pos, piece.color, board, moves, listOf(Pair(-1,-1), Pair(-1,1), Pair(1,-1), Pair(1,1)))
        PieceType.QUEEN -> addLinearMoves(pos, piece.color, board, moves, listOf(Pair(-1,0), Pair(1,0), Pair(0,-1), Pair(0,1), Pair(-1,-1), Pair(-1,1), Pair(1,-1), Pair(1,1)))
    }
    return moves
}

fun addLinearMoves(pos: Position, color: PieceColor, board: Board, moves: MutableList<Position>, dirs: List<Pair<Int, Int>>) {
    for (d in dirs) {
        var cr = pos.row + d.first
        var cc = pos.col + d.second
        while (cr in 0..7 && cc in 0..7) {
            val target = Position(cr, cc)
            val p = board[target]
            if (p == null) {
                moves.add(target)
            } else {
                if (p.color != color) moves.add(target)
                break
            }
            cr += d.first
            cc += d.second
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

fun makeBotMove(board: Board, difficulty: BotDifficulty): Board {
    val botColor = PieceColor.BLACK
    val visibleSquares = getVisibleSquares(botColor, board)
    val botPieces = board.filter { it.value.color == botColor }
    val allValidMoves = mutableListOf<Pair<Position, Position>>()

    for ((startPos, piece) in botPieces) {
        val validTargets = getValidMoves(startPos, piece, board)
        for (target in validTargets) {
            allValidMoves.add(Pair(startPos, target))
        }
    }

    if (allValidMoves.isEmpty()) return board

    val selectedMove = if (difficulty == BotDifficulty.MEDIUM) {
        val captureMoves = allValidMoves.filter { (_, target) ->
            val targetPiece = board[target]
            targetPiece != null && targetPiece.color == PieceColor.WHITE && visibleSquares.contains(target)
        }
        if (captureMoves.isNotEmpty()) captureMoves[Random.nextInt(captureMoves.size)] else allValidMoves[Random.nextInt(allValidMoves.size)]
    } else {
        allValidMoves[Random.nextInt(allValidMoves.size)]
    }

    val newBoard = board.toMutableMap()
    val pieceToMove = newBoard[selectedMove.first]
    if (pieceToMove != null) {
        newBoard[selectedMove.second] = pieceToMove
        newBoard.remove(selectedMove.first)
    }
    return newBoard
}
