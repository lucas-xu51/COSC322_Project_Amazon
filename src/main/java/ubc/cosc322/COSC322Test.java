
package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.BoardGameModel;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

import ygraph.ai.smartfox.games.Spectator;
import ygraph.ai.smartfox.games.amazons.HumanPlayer;

import ubc.cosc322.core.Board;
import ubc.cosc322.core.Color;
import ubc.cosc322.core.RandomPlayer;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer{

	protected Board board;

    public GameClient gameClient = null; 
    protected BaseGameGUI gamegui = null;
	
    private String userName = null;
    private String passwd = null;

	//先黑后白

    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {
		//HumanPlayer player = new HumanPlayer();
		//Spectator player = new Spectator();
		RandomPlayer player = new RandomPlayer("player","pwd");
		//COSC322Test player = new COSC322Test("player","pwd");
        System.out.println(player.getGameGUI());

		if (player.getGameGUI() == null) {
			player.Go();
		} else {
			BaseGameGUI.sys_setup();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					player.Go();
				}
			});
		}
	}
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	super.postSetup();
		this.userName = userName;
		this.passwd = passwd;
		this.board = new Board(gameClient);

		//To make a GUI-based player, create an instance of BaseGameGUI
		//and implement the method getGameGUI() accordingly
		this.gamegui = new BaseGameGUI(this);
    }
 

    @Override
    public void onLogin() {
    	System.out.println("Log in is being run now");
		userName = gameClient.getUserName();
		this.board = new Board(gameClient);
		if(gamegui != null) {
            System.out.println("I am here: " + userName);
			gamegui.setRoomInformation(gameClient.getRoomList());
		}
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
	
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
		System.out.println("Message type: " + messageType);
		System.out.println("Message details: " + msgDetails);
		switch (messageType) {
            case GameMessage.GAME_STATE_BOARD:
                handleBoardMessage(msgDetails); // 处理棋盘状态消息
                break;
            case GameMessage.GAME_ACTION_START:
                handleGameStart(msgDetails); // 处理游戏开始消息
                break;
            case GameMessage.GAME_ACTION_MOVE:
                handleGameMove(msgDetails); // 处理移动消息
                break;
        }
        return true;
    }
	
	private void handleBoardMessage(Map<String, Object> msgDetails) {
        // 从服务器更新棋盘状态
		if (gamegui != null) {
			gamegui.setGameState((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
		}
        board.updateFromServer(msgDetails);

        // 显示棋盘
        board.printBoard();
    }
    
	private void handleGameStart(Map<String, Object> msgDetails) {
		// 设置自己的颜色
		if (msgDetails.get(AmazonsGameMessage.PLAYER_WHITE).equals(userName)) {
			board.setMyColor(Color.WHITE);
			System.out.println("I am the white player");
			makeMove(); // 调用 AI 进行移动
            
		} else if (msgDetails.get(AmazonsGameMessage.PLAYER_BLACK).equals(userName)) {
			board.setMyColor(Color.BLACK);
			System.out.println("I am the black player");
		}
	
		System.out.println("I am playing as: " + board.getMyColor());
	}

	private void handleGameMove(Map<String, Object> msgDetails) {
        if (gamegui != null) {
            gamegui.updateGameState(msgDetails);
        }
    
        // 解析对手的移动并更新棋盘
        int fromRow = ((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR)).get(0) - 1; // 转换为 0-based
        int fromCol = ((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR)).get(1) - 1; // 转换为 0-based
        int toRow = ((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT)).get(0) - 1;   // 转换为 0-based
        int toCol = ((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT)).get(1) - 1;   // 转换为 0-based
        int arrowRow = ((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS)).get(0) - 1;     // 转换为 0-based
        int arrowCol = ((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS)).get(1) - 1;     // 转换为 0-based

        System.out.println(fromRow + " " + fromCol + " " + toRow + " " + toCol + " " + arrowRow + " " + arrowCol);
    
        // 更新棋盘
        board.moveQueen(fromRow, fromCol, toRow, toCol);
        board.shootArrow(toRow, toCol, arrowRow, arrowCol);
    
        // 显示棋盘
        board.printBoard();

        // 切换当前玩家颜色
        board.switchPlayer();
    
        // 如果是自己的回合，调用 AI 进行移动
        if (board.getMyColor() == board.getCurrentPlayer()) {
            makeMove();
        }
    }

    // 让 AI 进行移动
    protected void makeMove() {
        int fromRow = 0;
        int fromCol = 3;
        int toRow = 1;
        int toCol = 3;
        int arrowRow = 2;
        int arrowCol = 3;

        // 检查移动是否合法
        if (board.moveQueen(fromRow, fromCol, toRow, toCol)) {
            if (board.shootArrow(toRow, toCol, arrowRow, arrowCol)) {
                // 将移动信息发送到服务器
                board.updateToServer(fromRow, fromCol, toRow, toCol, arrowRow, arrowCol);

                ArrayList<Integer> queenCurrent = new ArrayList<>(Arrays.asList(fromRow + 1, fromCol + 1));
                ArrayList<Integer> queenNew = new ArrayList<>(Arrays.asList(toRow + 1, toCol + 1));
                ArrayList<Integer> arrow = new ArrayList<>(Arrays.asList(arrowRow + 1, arrowCol + 1));

                gamegui.updateGameState(queenCurrent, queenNew, arrow);

                // 显示棋盘
                board.printBoard();
            } else {
                System.out.println("fail to shoot!!!!");
            }
        } else {
            System.out.println("fail to move queen!!!!");
        }

        
        
    }
    
    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return  this.gamegui;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class
