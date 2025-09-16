public class LocalBoard {

    private Mark[][] cells;
    private boolean closed;
    private Mark winner;

    public LocalBoard(){
        cells = new Mark[3][3];
        for (int r = 0; r < 3; r++){
            for (int c = 0; c < 3; c++){
                cells[r][c]=Mark.EMPTY;
            }
        }
        closed = false;
        winner = Mark.EMPTY;
    }

    public boolean isClosed() {
        return closed;
    }

    public Mark getWinner() {
        return winner;
    }

    public Mark getCell(int r, int c) {
        return cells[r][c];
    }

    public void play(int r, int c,Mark mark) {
        if(closed){
            throw new IllegalStateException("Ce plateau local est déjà fermé");
        }
        if(cells[r][c] != Mark.EMPTY){
            throw new IllegalArgumentException("Case déjà occupé");
        }

        cells[r][c] = mark;
        checkWinner();
    }

    private void checkWinner() {

        // Vérifie les lignes
        for (int r = 0; r < 3; r++) {
            if (cells[r][0] != Mark.EMPTY &&
                    cells[r][0] == cells[r][1] &&
                    cells[r][1] == cells[r][2]) {
                winner = cells[r][0];
                closed = true;
                return;
            }
        }

        // Vérifie les colonnes
        for (int c = 0; c < 3; c++) {
            if (cells[0][c] != Mark.EMPTY &&
                    cells[0][c] == cells[1][c] &&
                    cells[1][c] == cells[2][c]) {
                winner = cells[0][c];
                closed = true;
                return;
            }
        }
        // Vérifie les diagonales
        if (cells[0][0] != Mark.EMPTY &&
                cells[0][0] == cells[1][1] &&
                cells[1][1] == cells[2][2]) {
            winner = cells[0][0];
            closed = true;
            return;
        }
        if (cells[0][2] != Mark.EMPTY &&
                cells[0][2] == cells[1][1] &&
                cells[1][1] == cells[2][0]) {
            winner = cells[0][2];
            closed = true;
            return;
        }

        // Vérifie si toutes les cases sont occupées
        boolean full = true;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (cells[r][c] == Mark.EMPTY) {
                    full = false;
                    break;
                }
            }
        }
        if (full) {
            // Plateau local rempli sans gagnant => fermé, winner reste EMPTY
            closed = true;
        }
    }

    public void unplay(int r, int c) {
        // Remet la case à EMPTY
        cells[r][c] = Mark.EMPTY;
        closed = false;
        winner = Mark.EMPTY;
        checkWinner();
    }

    public void setCell(int r, int c, Mark mark) {
        cells[r][c] = mark;
    }
}
