import java.util.List;

public class AIPlayer {
    private Mark cpuMark;
    private long timeLimitMs;

    public AIPlayer(Mark cpuMark, long timeLimitMs) {
        this.cpuMark = cpuMark;
        this.timeLimitMs = timeLimitMs;
    }

    /**
     * Retourne le meilleur coup à jouer,
     * en utilisant Alpha-Beta (ou Minimax).
     *
     * @param board     L'état actuel du plateau géant
     * @param lastMove  Le dernier coup joué (pour générer les coups)
     * @return          Le coup sélectionné par l'IA
     */
    public Move getBestMove(Board board, Move lastMove) {
        long startTime = System.currentTimeMillis();

        java.util.List<Move> moves = board.generateMoves(lastMove, cpuMark);

        if (moves.isEmpty()) {
            return null;
        }

        Move bestMove = moves.get(0);
        int bestValue = Integer.MIN_VALUE;

        int alpha = Integer.MIN_VALUE;
        int beta  = Integer.MAX_VALUE;

        for (Move move : moves) {

            board.play(move.getRow(), move.getCol(), cpuMark);

            int value = alphaBeta(board,
                    move,                  // le dernier coup devient celui qu'on vient de jouer
                    1,                     // profondeur initiale = 1
                    startTime,
                    alpha,
                    beta,
                    false,                 // isMaximizing = false (l'adversaire joue)
                    getOpponent(cpuMark));

            // Annule le coup (backtracking)
            board.unplay(move.getRow(), move.getCol());

            // On met à jour la meilleure valeur
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestValue);
            if (alpha >= beta) {
                // Coupure beta
                break;
            }
        }

        return bestMove;
    }

    /**
     * La fonction alphaBeta (ou Minimax) qui évalue récursivement.
     *
     * @param board       Le plateau
     * @param lastMove    Le dernier coup joué
     * @param depth       La profondeur actuelle
     * @param startTime   L'heure de début (pour la limite de temps)
     * @param alpha       Borne alpha (pour Max)
     * @param beta        Borne beta (pour Min)
     * @param isMaximizing Indique si c'est au tour du joueur MAX (cpu) ou MIN (adversaire)
     * @param currentMark La pièce du joueur courant (X ou O)
     * @return            Un score représentant la valeur de la position
     */
    private static final int MAX_DEPTH = 4; // ou 5

    private int alphaBeta(Board board,
                          Move lastMove,
                          int depth,
                          long startTime,
                          int alpha,
                          int beta,
                          boolean isMaximizing,
                          Mark currentMark) {

        // Vérification du temps
        if (System.currentTimeMillis() - startTime >= timeLimitMs) {
            return evaluate(board);
        }

        // Vérification de la profondeur
        if (depth >= MAX_DEPTH) {
            return evaluate(board);
        }

        // Vérification de la fin de partie
        Mark winner = board.checkGlobalWinner();
        if (winner == cpuMark) {
            return 1000 - depth;
        } else if (winner == getOpponent(cpuMark)) {
            return -1000 + depth;
        } else if (board.isGlobalClosed()) {
            return 0;
        }

        // Génération des coups
        List<Move> moves = board.generateMoves(lastMove, currentMark);
        if (moves.isEmpty()) {
            return evaluate(board);
        }

        if (isMaximizing) {
            int bestValue = Integer.MIN_VALUE;
            for (Move move : moves) {
                board.play(move.getRow(), move.getCol(), currentMark);
                int value = alphaBeta(board, move, depth + 1, startTime,
                        alpha, beta, false, getOpponent(currentMark));
                board.unplay(move.getRow(), move.getCol());
                bestValue = Math.max(bestValue, value);
                alpha = Math.max(alpha, bestValue);
                if (alpha >= beta) {
                    break;
                }
            }
            return bestValue;
        } else {
            int bestValue = Integer.MAX_VALUE;
            for (Move move : moves) {
                board.play(move.getRow(), move.getCol(), currentMark);
                int value = alphaBeta(board, move, depth + 1, startTime,
                        alpha, beta, true, getOpponent(currentMark));
                board.unplay(move.getRow(), move.getCol());
                bestValue = Math.min(bestValue, value);
                beta = Math.min(beta, bestValue);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestValue;
        }
    }


    /**
     * Évalue la position du point de vue de 'cpuMark'
     * quand ce n'est pas un état terminal clair (pas de vainqueur global).
     *
     * --> À peaufiner selon ce qu'on veut.
     */
    private int evaluateLocalBoard(LocalBoard lb, Mark perspective) {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += evaluateLine(lb.getCell(i,0), lb.getCell(i,1), lb.getCell(i,2), perspective);
            sum += evaluateLine(lb.getCell(0,i), lb.getCell(1,i), lb.getCell(2,i), perspective);
        }
        sum += 1.5 * evaluateLine(lb.getCell(0,0), lb.getCell(1,1), lb.getCell(2,2), perspective);
        sum += 1.5 * evaluateLine(lb.getCell(0,2), lb.getCell(1,1), lb.getCell(2,0), perspective);

        return sum;
    }

    private int evaluateLine(Mark m1, Mark m2, Mark m3, Mark perspective) {
        Mark opp = getOpponent(perspective);
        int countP = 0, countOpp = 0;
        Mark[] cells = {m1, m2, m3};
        for (Mark m : cells) {
            if (m == perspective) countP++;
            else if (m == opp)    countOpp++;
        }
        
        if (countP > 0 && countOpp > 0) return 0;
        if (countP == 0 && countOpp == 0) return 0;
        if (countP > 0) {
            
            return (int) Math.pow(10, countP - 1);
        } else {
            return -(int) Math.pow(10, countOpp - 1);
        }
    }

    private int evaluate(Board board) {
        int score = 0;
        for (int lr = 0; lr < 3; lr++) {
            for (int lc = 0; lc < 3; lc++) {
                LocalBoard lb = board.getLocalBoard(lr, lc);
                Mark w = lb.getWinner();
                if (w == cpuMark) {
                    score += 100;  
                } else if (w == getOpponent(cpuMark)) {
                    score -= 100;
                } else {
                    score += evaluateLocalBoard(lb, cpuMark);
                }
            }
        }
        return score;
    }


    private Mark getOpponent(Mark m) {
        return (m == Mark.X) ? Mark.O : Mark.X;
    }
}
