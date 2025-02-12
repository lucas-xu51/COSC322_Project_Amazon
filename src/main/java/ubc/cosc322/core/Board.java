package ubc.cosc322.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;
import ygraph.ai.smartfox.games.BaseGameGUI;

public class Board {

    // 棋盘大小
    public static final int SIZE = 10;

    // 棋盘状态
    protected int[][] board;

    // 当前玩家颜色
    private Color myColor;

    // 当前回合的玩家颜色
    private Color currentPlayer;

    // 游戏客户端，用于与服务器通信
    private GameClient gameClient;

    // 构造函数
    public Board(GameClient gameClient) {
        this.board = new int[SIZE][SIZE];
        this.gameClient = gameClient;
        this.currentPlayer = Color.WHITE; // 白方先手
    }

    // 从服务器更新棋盘状态
    public void updateFromServer(Map<String, Object> msgDetails) {
        // 从 msgDetails 中提取棋盘状态
        ArrayList<Integer> boardState = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);

        // 去掉第一行和第一列，转换为 10x10 的棋盘
        for (int row = 1; row <= SIZE; row++) {
            for (int col = 1; col <= SIZE; col++) {
                // 计算 11x11 棋盘中的索引
                int index = row * 11 + col;
                // 将值赋给 10x10 棋盘
                int serverX = col - 1; // 转换为 0-based 索引
                int serverY = row - 1; // 转换为 0-based 索引
                int localRow =  serverY; // 转换为本地坐标
                int localCol = serverX; // 转换为本地坐标
                board[localRow][localCol] = boardState.get(index);
            }
        }
    }

    // 判断自己是黑方还是白方
    public Color getMyColor() {
        return myColor;
    }

    // 设置自己的颜色
    public void setMyColor(Color myColor) {
        this.myColor = myColor;
    }

    // 获取当前回合的玩家颜色
    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    // 切换当前回合的玩家
    public void switchPlayer() {
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    // 移动皇后
    public boolean moveQueen(int fromRow, int fromCol, int toRow, int toCol) {
        // 检查起点是否是当前玩家的皇后
        if (board[fromRow][fromCol] != myColor.getCode()) {
            System.out.println("The starting point is not the current player's queen!");
            return false;
        }
    
        // 检查终点是否在棋盘范围内且为空
        if (!isValidPosition(toRow, toCol) || board[toRow][toCol] != 0) {
            System.out.println("The target position is invalid!");
            return false;
        }
    
        // 检查移动路径是否畅通
        if (!isPathClear(fromRow, fromCol, toRow, toCol)) {
            System.out.println("The path is blocked!");
            return false;
        }
    
        // 移动皇后
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = 0;
        return true;
    }

    // 射箭
    public boolean shootArrow(int fromRow, int fromCol, int toRow, int toCol) {
        // 检查终点是否在棋盘范围内且为空
        if (!isValidPosition(toRow, toCol) || board[toRow][toCol] != 0) {
            System.out.println("arrow not legal!!!!!");
            return false;
        }

        // 检查射箭路径是否畅通
        if (!isPathClear(fromRow, fromCol, toRow, toCol)) {
            System.out.println("Archery path blocked!");
            return false;
        }

        // 放置箭
        board[toRow][toCol] = 3;
        return true;
    }

    // 检查路径是否畅通
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.signum(toRow - fromRow);
        int colStep = Integer.signum(toCol - fromCol);

        int row = fromRow + rowStep;
        int col = fromCol + colStep;

        while (row != toRow || col != toCol) {
            if (board[row][col] != 0) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    // 检查位置是否合法
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    // 显示棋盘
    public void printBoard() {
        System.out.println("now board state: ");
        for (int row = SIZE - 1 ; row >= 0; row--) {
            for (int col = 0; col < SIZE; col++) {
                switch (board[row][col]) {
                    case 0:
                        System.out.print(" . "); // 空格子
                        break;
                    case 1:
                        System.out.print(" B "); // 黑皇后
                        break;
                    case 2:
                        System.out.print(" W "); // 白皇后
                        break;
                    case 3:
                        System.out.print(" X "); // 箭
                        break;
                    default:
                        System.out.print(" ? "); // 未知值
                        break;
                }
            }
            System.out.println(); // 换行
        }
    }

    // 将棋盘状态更新到服务器
    public void updateToServer(int fromRow, int fromCol, int toRow, int toCol, int arrowRow, int arrowCol) {
        // 将本地坐标转换为服务器坐标
        int serverFromX = fromRow;
        int serverFromY = fromCol;
        int serverToX = toRow;
        int serverToY = toCol;
        int serverArrowX = arrowRow;
        int serverArrowY = arrowCol;

        // 创建移动信息的 Map
        Map<String, Object> moveDetails = new HashMap<>();

        // 皇后当前位置
        ArrayList<Integer> queenPosCurr = new ArrayList<>();
        queenPosCurr.add(serverFromX + 1); // 转换为 1-based 索引
        queenPosCurr.add(serverFromY + 1);

        // 皇后目标位置
        ArrayList<Integer> queenPosNext = new ArrayList<>();
        queenPosNext.add(serverToX + 1); // 转换为 1-based 索引
        queenPosNext.add(serverToY + 1);

        // 箭的目标位置
        ArrayList<Integer> arrowPos = new ArrayList<>();
        arrowPos.add(serverArrowX + 1); // 转换为 1-based 索引
        arrowPos.add(serverArrowY + 1);

        System.out.println("from: " + queenPosCurr);
        System.out.println("to: " + queenPosNext);
        System.out.println("arrow: " + arrowPos);

        // 将移动信息添加到 Map
        moveDetails.put(AmazonsGameMessage.QUEEN_POS_CURR, queenPosCurr);
        moveDetails.put(AmazonsGameMessage.QUEEN_POS_NEXT, queenPosNext);
        moveDetails.put(AmazonsGameMessage.ARROW_POS, arrowPos);

        // 发送移动信息到服务器
        gameClient.sendMoveMessage(moveDetails);


        // 切换当前回合的玩家
        switchPlayer();
    }

    public int[][] getBoard() {
        return board;
    }
}