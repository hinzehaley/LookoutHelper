import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	    Board board = new Board();

        AI ai = new AI();

        Scanner scanner = new Scanner(System.in);

        while(!board.isFull()){
            System.out.println("Select a row");

            int row = scanner.nextInt();
            int column = scanner.nextInt();

            board.placeToken(row, column, "X");
            board.printStdOut();
            if(board.checkWin("X")){
                break;
            }
            System.out.println(" ");
            ai.makeMove(board);
            board.printStdOut();
            if(board.checkWin("0")){
                break;
            }
        }
    }
}
