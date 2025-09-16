import java.util.ArrayList;
import java.util.List;

public class Board {

    private LocalBoard[][] boards;

    public Board() {
        boards = new LocalBoard[3][3];
        for(int r = 0; r < 3; r++){
            for(int c = 0; c < 3; c++){
                boards[r][c] = new LocalBoard();
            }
        }
    }

    public void play(int gridRow, int gridCol, Mark mark) {
        int br = gridRow / 3;
        int bc = gridCol / 3;
        int lr = gridRow % 3;
        int lc = gridCol % 3;
        boards[br][bc].play(lr, lc, mark);
    }

    public LocalBoard getLocalBoard(int br, int bc) {
        return boards[br][bc];
    }

    public Mark checkGlobalWinner() {
        Mark[][] big = new Mark[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                big[r][c] = boards[r][c].getWinner();
            }
        }
        for (int r = 0; r < 3; r++) {
            if (big[r][0] != Mark.EMPTY && big[r][0] == big[r][1] && big[r][1] == big[r][2]) {
                return big[r][0];
            }
        }
        for (int c = 0; c < 3; c++) {
            if (big[0][c] != Mark.EMPTY && big[0][c] == big[1][c] && big[1][c] == big[2][c]) {
                return big[0][c];
            }
        }
        if (big[0][0] != Mark.EMPTY && big[0][0] == big[1][1] && big[1][1] == big[2][2]) {
            return big[0][0];
        }
        if (big[0][2] != Mark.EMPTY && big[0][2] == big[1][1] && big[1][1] == big[2][0]) {
            return big[0][2];
        }
        return Mark.EMPTY;
    }

    public boolean isGlobalClosed() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (!boards[r][c].isClosed()) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<Move> generateMoves(Move lastMove, Mark currentPlayer) {
        List<Move> moves = new ArrayList<>();
        int br = lastMove.getRow() % 3;
        int bc = lastMove.getCol() % 3;

        if (boards[br][bc].isClosed()) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (!boards[r][c].isClosed()) {
                        for (int rr = 0; rr < 3; rr++) {
                            for (int cc = 0; cc < 3; cc++) {
                                if (boards[r][c].getCell(rr, cc) == Mark.EMPTY) {
                                    moves.add(new Move(r * 3 + rr, c * 3 + cc));
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int rr = 0; rr < 3; rr++) {
                for (int cc = 0; cc < 3; cc++) {
                    if (boards[br][bc].getCell(rr, cc) == Mark.EMPTY) {
                        moves.add(new Move(br * 3 + rr, bc * 3 + cc));
                    }
                }
            }
        }
        return moves;
    }

    public void unplay(int gr, int gc) {
        int br = gr / 3;
        int bc = gc / 3;
        int lr = gr % 3;
        int lc = gc % 3;
        boards[br][bc].unplay(lr, lc);
    }

    public void setCellGlobal(int gr, int gc, Mark mark) {
        int br = gr / 3;
        int bc = gc / 3;
        int lr = gr % 3;
        int lc = gc % 3;
        boards[br][bc].setCell(lr, lc, mark);
    }
}
