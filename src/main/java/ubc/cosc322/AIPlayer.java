package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ubc.cosc322.core.Board;
import ubc.cosc322.core.Color;

public class AIPlayer extends COSC322Test {

    public AIPlayer(String userName, String passwd) {
        super(userName, passwd);
    }

    @Override
    protected void makeMove() {
        // 获取所有合法的移动
        List<Move> legalMoves = findAllLegalMoves(board.getMyColor());

        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves available!");
            return;
        }

        // 使用 Minimax 算法选择最佳移动
        Move bestMove = findBestMove(board, legalMoves, 1); // 搜索深度为 3

        // 执行最佳移动
        if (board.moveQueen(bestMove.fromRow, bestMove.fromCol, bestMove.toRow, bestMove.toCol)) {
            if (board.shootArrow(bestMove.toRow, bestMove.toCol, bestMove.arrowRow, bestMove.arrowCol)) {
                // 将移动信息发送到服务器
                board.updateToServer(bestMove.fromRow, bestMove.fromCol, bestMove.toRow, bestMove.toCol, bestMove.arrowRow, bestMove.arrowCol);

                // 更新 GUI
                ArrayList<Integer> queenCurrent = new ArrayList<>(Arrays.asList(bestMove.fromRow + 1, bestMove.fromCol + 1));
                ArrayList<Integer> queenNew = new ArrayList<>(Arrays.asList(bestMove.toRow + 1, bestMove.toCol + 1));
                ArrayList<Integer> arrow = new ArrayList<>(Arrays.asList(bestMove.arrowRow + 1, bestMove.arrowCol + 1));

                gamegui.updateGameState(queenCurrent, queenNew, arrow);

                // 显示棋盘
                board.printBoard();
            } else {
                System.out.println("Failed to shoot arrow!");
            }
        } else {
            System.out.println("Failed to move queen!");
        }
    }

    // 找到所有合法的移动
    private List<Move> findAllLegalMoves(Color myColor) {
        List<Move> legalMoves = new ArrayList<>();

        // 遍历棋盘，找到所有当前玩家的皇后
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.getBoard()[row][col] == myColor.getCode()) {
                    // 找到该皇后所有合法的移动
                    List<Move> queenMoves = findLegalMovesForQueen(row, col);
                    legalMoves.addAll(queenMoves);
                }
            }
        }

        System.out.println(legalMoves.size() + " legal moves found!");

        return legalMoves;
    }

    // 找到某个皇后所有合法的移动 
    private List<Move> findLegalMovesForQueen(int fromRow, int fromCol) {
        List<Move> legalMoves = new ArrayList<>();

        // 检查所有可能的方向
        int[] rowDirections = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colDirections = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < rowDirections.length; i++) {
            int toRow = fromRow + rowDirections[i];
            int toCol = fromCol + colDirections[i];

            // 沿着该方向移动，直到遇到障碍物或棋盘边界
            while (board.isValidPosition(toRow, toCol) && board.getBoard()[toRow][toCol] == 0) {
                // 找到所有合法的箭的位置
                List<int[]> arrowPositions = findLegalArrowPositions(toRow, toCol);

                for (int[] arrowPos : arrowPositions) {
                    legalMoves.add(new Move(fromRow, fromCol, toRow, toCol, arrowPos[0], arrowPos[1]));
                }

                // 继续沿着该方向移动
                toRow += rowDirections[i];
                toCol += colDirections[i];
            }
        }

        return legalMoves;
    }

    // 找到所有合法的箭的位置
    private List<int[]> findLegalArrowPositions(int fromRow, int fromCol) {
        List<int[]> legalArrowPositions = new ArrayList<>();

        // 检查所有可能的方向
        int[] rowDirections = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colDirections = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < rowDirections.length; i++) {
            int arrowRow = fromRow + rowDirections[i];
            int arrowCol = fromCol + colDirections[i];

            // 沿着该方向移动，直到遇到障碍物或棋盘边界
            while (board.isValidPosition(arrowRow, arrowCol) && board.getBoard()[arrowRow][arrowCol] == 0) {
                legalArrowPositions.add(new int[]{arrowRow, arrowCol});

                // 继续沿着该方向移动
                arrowRow += rowDirections[i];
                arrowCol += colDirections[i];
            }
        }

        return legalArrowPositions;
    }

    // 使用 Minimax 算法选择最佳移动
    private Move findBestMove(Board board, List<Move> legalMoves, int depth) {
        Move bestMove = null;
        int bestValue = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            // 模拟移动
            Board newBoard = cloneBoard(board);
            newBoard.moveQueen(move.fromRow, move.fromCol, move.toRow, move.toCol);
            newBoard.shootArrow(move.toRow, move.toCol, move.arrowRow, move.arrowCol);

            // 计算移动的价值
            int moveValue = minimax(newBoard, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            // 更新最佳移动
            if (moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }

        return bestMove;
    }

    // Minimax 算法
    private int minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0) {
            System.out.println("leaf node score: " + evaluateBoard(board));
            return evaluateBoard(board);
        }

        int i =0;
        if (!maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : findAllLegalMoves(board.getMyColor())) {
                System.out.println(i++);
                // 模拟移动
                Board newBoard = cloneBoard(board);
                newBoard.moveQueen(move.fromRow, move.fromCol, move.toRow, move.toCol);
                newBoard.shootArrow(move.toRow, move.toCol, move.arrowRow, move.arrowCol);

                int eval = minimax(newBoard, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                System.out.println("Maximizing score: " + eval);

                if (beta <= alpha) {
                    break; // Alpha-Beta 剪枝
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : findAllLegalMoves(board.getCurrentPlayer())) {
                System.out.println(i++);
                // 模拟移动
                Board newBoard = cloneBoard(board);
                newBoard.moveQueen(move.fromRow, move.fromCol, move.toRow, move.toCol);
                newBoard.shootArrow(move.toRow, move.toCol, move.arrowRow, move.arrowCol);

                int eval = minimax(newBoard, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                System.out.println("Minimizing score: " + eval);

                if (beta <= alpha) {
                    break; // Alpha-Beta 剪枝
                }
            }
            return minEval;
        }
    }

    // 评估函数
    private int evaluateBoard(Board board) {
        int score = 0;

        // 计算当前玩家的移动自由度
        score += calculateMobility(board, board.getMyColor());

        // 计算对手的移动自由度
        score -= calculateMobility(board, board.getCurrentPlayer());

        return score;
    }

    // 计算移动自由度
    private int calculateMobility(Board board, Color color) {
        int mobility = 0;

        // 遍历棋盘，找到所有当前玩家的皇后
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                if (board.getBoard()[row][col] == color.getCode()) {
                    // 计算该皇后的移动自由度
                    mobility += findLegalMovesForQueen(row, col).size();
                }
            }
        }

        System.out.println("Mobility: " + mobility);
        return mobility;
    }

    // 克隆棋盘
    private Board cloneBoard(Board board) {
        Board newBoard = new Board(this.gameClient); // 使用 this.gameClient
        int[][] boardState = board.getBoard();
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                newBoard.getBoard()[row][col] = boardState[row][col];
            }
        }
        newBoard.setMyColor(board.getMyColor());
        newBoard.setCurrentPlayer(board.getCurrentPlayer());
        return newBoard;
    }

    // 内部类，表示一个移动
    private static class Move {
        int fromRow, fromCol; // 皇后的起始位置
        int toRow, toCol;     // 皇后的目标位置
        int arrowRow, arrowCol; // 箭的目标位置

        public Move(int fromRow, int fromCol, int toRow, int toCol, int arrowRow, int arrowCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.arrowRow = arrowRow;
            this.arrowCol = arrowCol;
        }
    }
}