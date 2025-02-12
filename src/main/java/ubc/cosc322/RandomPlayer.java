package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ubc.cosc322.core.Board;
import ubc.cosc322.core.Color;

public class RandomPlayer extends COSC322Test {

    public RandomPlayer(String userName, String passwd) {
        super(userName, passwd);
    }

    @Override
    protected void makeMove() {
        // 获取当前玩家的颜色
        Color myColor = board.getMyColor();

        // 找到所有合法的移动
        List<Move> legalMoves = findAllLegalMoves(myColor);

        if (legalMoves.isEmpty()) {
            System.out.println("No legal moves available!");
            return;
        }

        // 随机选择一个合法的移动
        Random random = new Random();
        Move selectedMove = legalMoves.get(random.nextInt(legalMoves.size()));

        // 执行移动
        if (board.moveQueen(selectedMove.fromRow, selectedMove.fromCol, selectedMove.toRow, selectedMove.toCol)) {
            if (board.shootArrow(selectedMove.toRow, selectedMove.toCol, selectedMove.arrowRow, selectedMove.arrowCol)) {
                // 将移动信息发送到服务器
                board.updateToServer(selectedMove.fromRow, selectedMove.fromCol, selectedMove.toRow, selectedMove.toCol, selectedMove.arrowRow, selectedMove.arrowCol);

                // 更新 GUI
                ArrayList<Integer> queenCurrent = new ArrayList<>(Arrays.asList(selectedMove.fromRow + 1, selectedMove.fromCol + 1));
                ArrayList<Integer> queenNew = new ArrayList<>(Arrays.asList(selectedMove.toRow + 1, selectedMove.toCol + 1));
                ArrayList<Integer> arrow = new ArrayList<>(Arrays.asList(selectedMove.arrowRow + 1, selectedMove.arrowCol + 1));

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