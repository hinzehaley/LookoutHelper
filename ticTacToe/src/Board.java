/**
 * Created by haleyhinze on 4/6/17.
 */
public class Board {

    String[][] board;

    public Board(){
        board = new String[3][3];
        for(int i = 0; i<board.length; i++){
            for(int j = 0; j<board.length; j++){
                board[i][j] = "-";
            }
        }
    }

    public void placeToken(int row, int column, String val){
        board[row][column] = val;
    }

    public void printStdOut(){
        for(int i = 0; i<board.length; i++){
            for(int j = 0; j<board.length; j++){
                System.out.print(board[i][j]);
                if(j != board.length - 1){
                    System.out.print("|");
                }

            }
            System.out.println("");
        }
    }

    public boolean isFull(){
        for(int i = 0; i<board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if(board[i][j].equals("-")){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkWin(String player){
        int numRow = 0;
        int numColumn = 0;
        int numDiagonalDown = 0;
        int numDiagonalUp = 0;
        for(int i = 0; i<board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if(board[i][j].equals(player)) {
                    numRow++;
                    numColumn++;


                    if (i == j) {
                        {
                            numDiagonalDown++;
                        }
                        if (i == 1) {
                            numDiagonalUp++;
                        }
                    }

                    if (i == 0 && j == 2) {
                        numDiagonalUp++;
                    }
                    if (i == 2 && j == 0) {
                        numDiagonalUp++;
                    }
                }

            }
            if(numRow == 3 || numColumn == 3 || numDiagonalDown == 3 || numDiagonalUp == 3){
                return true;
            }

            numRow = 0;
            numColumn = 0;
        }

        return false;
    }

}
