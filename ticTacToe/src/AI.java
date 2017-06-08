/**
 * Created by haleyhinze on 4/6/17.
 */
public class AI {

    public void makeMove(Board board){
        for(int i = 0; i<board.board.length; i++) {
            for (int j = 0; j < board.board.length; j++) {
                if(board.board[i][j].equals("-")){
                    board.board[i][j] = "O";
                    return;
                }
            }
        }
    }
}
