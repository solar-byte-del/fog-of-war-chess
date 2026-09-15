package com.example.chess

import kotlin.random.Random
import kotlin.math.max

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

// Проверка: Жив ли еще король конкретного цвета
fun isKingAlive(color: PieceColor, board: Board): Boolean {
    return board.values.any { it.type == PieceType.KING && it.color == color }
}

// Оценка ценности фигуры для сложного ИИ
fun getPieceValue(type: PieceType): Int {
    return when (type) {
        PieceType.PAWN -> 10
        PieceType.KNIGHT -> 30
        PieceType.BISHOP -> 30
        PieceType.ROOK -> 50
        PieceType.QUEEN -> 90
        PieceType.KING -> 9000
    }
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

    val selectedMove = when (difficulty) {
        BotDifficulty.EASY -> allValidMoves[Random.nextInt(allValidMoves.size)]
        
        BotDifficulty.MEDIUM -> {
            val captureMoves = allValidMoves.filter { (_, target) ->
                val targetPiece = board[target]
                targetPiece != null && targetPiece.color == PieceColor.WHITE && visibleSquares.contains(target)
            }
            if (captureMoves.isNotEmpty()) captureMoves[Random.nextInt(captureMoves.size)] else allValidMoves[Random.nextInt(allValidMoves.size)]
        }
        
        BotDifficulty.HARD -> {
            // СЛОЖНЫЙ РЕЖИМ: Оценка позиции на основе видимых данных
            var bestScore = -999999
            val bestMoves = mutableListOf<Pair<Position, Position>>()
            
            // Вычисляем клетки, которые игрок держит под ударом (из тех, что бот видит)
            val enemyVisiblePieces = board.filter { it.value.color == PieceColor.WHITE && visibleSquares.contains(it.key) }
            val squaresUnderAttackByEnemy = mutableSetOf<Position>()
            for ((ePos, ePiece) in enemyVisiblePieces) {
                squaresUnderAttackByEnemy.addAll(getValidMoves(ePos, ePiece, board))
            }

            for (move in allValidMoves) {
                val start = move.first
                val target = move.second
                val movingPiece = board[start] ?: continue
                val targetPiece = board[target]
                
                var score = 0
                
                // 1. Приоритет №1: Если видим короля игрока — немедленно атакуем и побеждаем!
                if (targetPiece != null && targetPiece.type == PieceType.KING) {
                    score += 100000
                }
                
                // 2. Ценность взятия обычной фигуры
                if (targetPiece != null && visibleSquares.contains(target)) {
                    score += getPieceValue(targetPiece.type) * 2
                }
                
                // 3. Защита: Если наша фигура стояла под боем врага, увод её из-под удара дает бонус
                if (squaresUnderAttackByEnemy.contains(start)) {
                    score += getPieceValue(movingPiece.type)
                }
                
                // 4. Опасность: Если мы ходим на клетку под ударом врага, вычитаем ценность нашей фигуры
                if (squaresUnderAttackByEnemy.contains(target)) {
                    score -= getPieceValue(movingPiece.type)
                }
                
                // 5. Движение пешек к превращению
                if (movingPiece.type == PieceType.PAWN && target.row == 7) {
                    score += 80 // Бонус за скорое превращение в Ферзя
                }

                if (score > bestScore) {
                    bestScore = score
                    bestMoves.clear()
                    bestMoves.add(move)
                } else if (score == bestScore) {
                    bestMoves.add(move)
                }
            }
            if (bestMoves.isNotEmpty()) bestMoves[Random.nextInt(bestMoves.size)] else allValidMoves[Random.nextInt(allValidMoves.size)]
        }
    }

    val newBoard = board.toMutableMap()
    val pieceToMove = newBoard[selectedMove.first]
    if (pieceToMove != null) {
        // Логика авто-превращения пешки бота в Ферзя на последней горизонтали
        if (pieceToMove.type == PieceType.PAWN && selectedMove.second.row == 7) {
            newBoard[selectedMove.second] = ChessPiece(PieceType.QUEEN, botColor)
        } else {
            newBoard[selectedMove.second] = pieceToMove
        }
        newBoard.remove(selectedMove.first)
    }
    return newBoard
}
