public class MillAI {

    private static final short MAX_VALUE = 9999;
    private static final short MIN_VALUE = -9999;

    private double random = 0;

    private int nodesOpened = 0;
    private int nodesTotal = 0;
    private Node bestPathSoFar = null;
    private int rootNodes = 0;
    private int currentRootNode = -1;

    private boolean timeLimited = false;
    private byte depthLimit = -1;
    private long timeLimit = -1;
    private long searchStarted = 0;

    private byte player = -1;
    private byte opponent = -1;

    public MillAI() { }

    public void stopSearch() {
        this.timeLimited = true;
        this.timeLimit = 0;
    }

    public Move depthSearch(MillGame game, byte depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("depthSearch(MillGame,byte): "+
                                               "depth:"+depth+" must be a positive number.");
        }
        this.nodesOpened = 0;
        this.nodesTotal = 0;
        this.bestPathSoFar = null;

        this.timeLimited = false;
        this.depthLimit = depth;
        this.player = game.getActivePlayer();
        this.opponent = game.getOpponent();

        this.searchStarted = System.currentTimeMillis();

        if (depth == 0) {
            return this.randomMove(game);
        }

        Node best = maxValue(new Node(game, (byte)0, null), MIN_VALUE, MAX_VALUE);
        if (best != null && best.PATH != null) {
            System.out.println("depth:"+depth+
                               " move:"+best.PATH.PREVIOUS_MOVE+
                               " value:"+best.VALUE+
                               " nodes:"+this.nodesOpened+
                               " time:"+(System.currentTimeMillis() - this.searchStarted)+"ms");
        
        }
        this.printPath(best.PATH);
        if (best == null || best.PATH == null) {
            return null; // keskeytysarvo!
        }
        return best.PATH.PREVIOUS_MOVE;
    }


    public Move timeSearch(MillGame game, int timeLimitSeconds) {
        if (timeLimitSeconds < 1 || 100000 <= timeLimitSeconds) {
            throw new IllegalArgumentException("timeLimitSeconds must be between"+
                                               " 1 and 100000 seconds (27 hours).");
        }
        this.nodesTotal = 0;
        this.timeLimited = true;
        this.timeLimit = timeLimitSeconds*1000; // s --> ms
        this.player = game.getActivePlayer();
        this.opponent = game.getOpponent();
        this.searchStarted = System.currentTimeMillis();
        this.bestPathSoFar = null;

        Node best = null;
        Node candidate;
        for (this.depthLimit = 1; !this.timeOut() && this.depthLimit < Byte.MAX_VALUE; this.depthLimit++) {
            this.nodesOpened = 0;
            candidate = maxValue(new Node(game, (byte)0, null), MIN_VALUE, MAX_VALUE);
            if (this.timeOut()) {
                if (candidate != null && best.VALUE < candidate.VALUE) {
                    best = candidate;
                    System.out.print("*"); // merkit‰‰n kesken j‰‰nyt, mutta hyv‰ksytty
                }
                else {
                    System.out.print("/"); // merkit‰‰n kesken ja huomiotta j‰‰nyt
                }
            }
            else {
                best = candidate;
            }
            this.bestPathSoFar = best;
            if (best.VALUE == MAX_VALUE) {
                break;
            }
        }
        System.out.println("depth:"+this.depthLimit+
                           " move:"+best.PATH.PREVIOUS_MOVE+
                           " value:"+best.VALUE+
                           " nodes:"+this.nodesTotal+
                           " time:"+(System.currentTimeMillis() - this.searchStarted)+"ms");
        this.printPath(best.PATH);

        return best.PATH.PREVIOUS_MOVE;
    }

    private boolean timeOut() {
        if ( !this.timeLimited) {
            return false;
        }
        return System.currentTimeMillis() - this.searchStarted >= this.timeLimit;
    }


    private Node maxValue(Node currentNode, short alpha, short beta) {
        this.nodesOpened++;
        this.nodesTotal++;

        if (currentNode.DEPTH >= this.depthLimit) {
            currentNode.setValue(this.evaluate(currentNode.GAME));
            currentNode.setPath(null);
            return currentNode;
        }
        short bestValue = Short.MIN_VALUE; // -32768
        Node bestPath = null;

        Move[] allMoves = LegalMoves.getAllLegalMoves(currentNode.GAME);
        this.sortMoves(allMoves);

        MillGame copy;
        Move currentMove;
        for (int moveIndex = 0; moveIndex < allMoves.length; moveIndex++) {
            copy = (MillGame) currentNode.GAME.clone();
            currentMove = allMoves[moveIndex];

            if (copy.makeMove(currentMove, false)) { // VOITTO ?!
                if (copy.getActivePlayer() == this.player) {
                    currentNode.setValue(MAX_VALUE);
                }
                else {
                    currentNode.setValue(MIN_VALUE);
                }
                // alempi rekursiotaso voi lukea suoritetun siirron
                currentNode.setPath( new Node(copy, (byte)(currentNode.DEPTH +1), currentMove) );
                return currentNode;
            }

            if (currentNode.DEPTH == 0 && !this.timeOut()) {
                this.rootNodes = allMoves.length;
                this.currentRootNode = moveIndex;
            }

            Node nextNode = this.minValue(new Node(copy, (byte)(currentNode.DEPTH +1), currentMove), alpha, beta);
            if (this.timeLimited && this.timeOut()) {     // nopea peruutus
                if (currentNode.DEPTH == 0) {
                    if (moveIndex > 0) {                  // Palautetaan t‰h‰n asti
                        currentNode.setValue(bestValue);  // kokonaan l‰pik‰ydyist‰
                        currentNode.setPath(bestPath);    // juuritason haaroista paras.
                    }
                    else {
                        currentNode.setValue(MIN_VALUE);  // Ainoan avatun haaran todellisesta
                        currentNode.setPath(null);        // arvosta ei saatu viel‰ varmuutta.
                    }
                }
                return currentNode;
            }
            if (nextNode.VALUE == MAX_VALUE) { // VOITTO !!
                currentNode.setValue(MAX_VALUE);
                currentNode.setPath(nextNode);
                return currentNode;
            }
            if (nextNode.VALUE > bestValue) {
                bestValue = nextNode.VALUE;
                bestPath = nextNode;
            }
            if (bestValue >= beta) { // karsinta
                currentNode.setValue(bestValue);
                currentNode.setPath(bestPath);
                return currentNode;
            }
            if (bestValue > alpha) {
                alpha = bestValue;
            }
        }
        currentNode.setValue(bestValue);
        currentNode.setPath(bestPath);
        return currentNode;
    }

    private Node minValue(Node currentNode, short alpha, short beta) {
        this.nodesOpened++;
        this.nodesTotal++;

        if (currentNode.DEPTH >= this.depthLimit) {
            currentNode.setValue(this.evaluate(currentNode.GAME));
            currentNode.setPath(null);
            return currentNode;
        }
        short worstValue = Short.MAX_VALUE; // 32767
        Node worstPath = null;

        Move[] allMoves = LegalMoves.getAllLegalMoves(currentNode.GAME);
        this.sortMoves(allMoves);

        MillGame copy;
        Move currentMove;
        for (int moveIndex = 0; moveIndex < allMoves.length; moveIndex++) {
            copy = (MillGame) currentNode.GAME.clone();
            currentMove = allMoves[moveIndex];

            if (copy.makeMove(currentMove, false)) { // TAPPIO ?!
                if (copy.getActivePlayer() == this.player) {
                    currentNode.setValue(MAX_VALUE);
                }
                else {
                    currentNode.setValue(MIN_VALUE);
                }
                // alempi rekursiotaso voi lukea suoritetun siirron
                currentNode.setPath( new Node(copy, (byte)(currentNode.DEPTH +1), currentMove) );
                return currentNode;
            }

            Node nextNode = this.maxValue(new Node(copy, (byte)(currentNode.DEPTH +1), currentMove), alpha, beta);
            if (this.timeLimited && this.timeOut()) { // nopea peruutus
                return currentNode;
            }
            if (nextNode.VALUE == MIN_VALUE) { // TAPPIO !!
                currentNode.setValue(MIN_VALUE);
                currentNode.setPath(nextNode);
                return currentNode;
            }
            if (nextNode.VALUE < worstValue) {
                worstValue = nextNode.VALUE;
                worstPath = nextNode;
            }
            if (worstValue <= alpha) { // karsinta
                currentNode.setValue(worstValue);
                currentNode.setPath(worstPath);
                return currentNode;
            }
            if (worstValue < beta) {
                beta = worstValue;
            }
        }
        currentNode.setValue(worstValue);
        currentNode.setPath(worstPath);
        return currentNode;
    }


/*
    private int quiescence(short alpha, short beta, byte searchLimit) {
    }
*/

    private void sortMoves(Move[] moves) { // quicksort, persversio
        this.random = Math.random();
        quicksort(moves, 0, moves.length - 1);      // ~ AW:n kirjasta
    }

    private void quicksort(Move[] moves, int start, int end) {
        int left = start;
        int right = end;
        Move divider = moves[(start+end)/2];
        do {
            while (this.compareMoves(moves[left], divider) > 0) { // ## suurimmat alkuun
                left++;
            }
            while (this.compareMoves(divider, moves[right]) > 0) { // ## suurimmat alkuun
                right--;
            }
            if (left <= right) {
                Move tmp = moves[left];
                moves[left] = moves[right];
                moves[right] = tmp;
                left++;
                right--;
            }
        } while (left < right);

        if (start < right) {
            quicksort(moves, start, right);
        }
        if (left < end) {
            quicksort(moves, left, end);
        }
    }


    private int compareMoves(Move move_1, Move move_2) { // palauttaa +1, jos move_1 on parempi!
        if (move_1.REMOVE != Move.NOWHERE && move_2.REMOVE == Move.NOWHERE) {
            return +1;
        }
        if (move_1.REMOVE == Move.NOWHERE && move_2.REMOVE != Move.NOWHERE) {
            return -1;
        }

        boolean tmp_1 = this.isJunctionSquare(move_1.TO);
        boolean tmp_2 = this.isJunctionSquare(move_2.TO);
        if ( tmp_1 && !tmp_2) {
            return +1;
        }
        if ( !tmp_1 && tmp_2) {
            return -1;
        }

        tmp_1 = this.isTJunctionSquare(move_1.TO);
        tmp_2 = this.isTJunctionSquare(move_2.TO);
        if ( tmp_1 && !tmp_2) {
            return +1;
        }
        if ( !tmp_1 && tmp_2) {
            return -1;
        }

        tmp_1 = this.isMiddleCornerSquare(move_1.TO);
        tmp_2 = this.isMiddleCornerSquare(move_2.TO);
        if ( tmp_1 && !tmp_2) {
            return +1;
        }
        if ( !tmp_1 && tmp_2) {
            return -1;
        }

        return 0;
    }

    private boolean isJunctionSquare(byte square) {
        return (square == 4 || square == 10 || square == 13 || square == 19);
    }
    private boolean isTJunctionSquare(byte square) {
        return (square == 1 || square == 7 || square == 9 || square == 11 ||
                square == 12 || square == 14 || square == 16 || square == 22);
    }
    private boolean isCornerSquare(byte square) {
        return (square == 0 || square == 2 || square == 3 || square == 5 ||
                square == 6 || square == 8 || square == 15 || square == 17 ||
                square == 18 || square == 20 || square == 21 || square == 23);
    }
    private boolean isMiddleCornerSquare(byte square) {
        return (square == 3 || square == 5 || square == 18 || square == 20);
    }


    private short evaluate(MillGame game) {
        if (game.getGameState() == MillGame.PHASE_GAME_OVER) {
            System.out.println("Evaluating winning position!");
            if (game.getActivePlayer() == this.player) {
                return MAX_VALUE;
            }
            else {
                return MIN_VALUE;
            }
        }

        MillBoard board = game.getMillBoard();
        int value = 0;

        int playerPieces = board.getColouredSquares(this.player).length;
        int opponentPieces = board.getColouredSquares(this.opponent).length;
        byte playerHandPieces;
        byte opponentHandPieces;

        if (game.getGameState() == MillGame.PHASE_BEGINNING) {
            if (this.player == MillGame.WHITE_PLAYER) {
                playerHandPieces = game.getWhitePiecesInHand();
                opponentHandPieces = game.getBlackPiecesInHand();
            }
            else {
                playerHandPieces = game.getBlackPiecesInHand();
                opponentHandPieces = game.getWhitePiecesInHand();
            }
            value += 100*( (playerPieces+playerHandPieces) - (opponentPieces+opponentHandPieces) );

            if (playerHandPieces < opponentHandPieces) { // hyvityst‰
                value -= 20 * 2; // Oletetaan, ett‰ vastustaja saa "ylim‰‰r‰isell‰"
                                 // nappulallaan kaksi vapaata ruutua.
            }
            else if (playerHandPieces > opponentHandPieces) {
                value += 20 * 2; // Oletetaan, ett‰ pelaaja saa "ylim‰‰r‰isell‰"
                                 // nappulallaan kaksi vapaata ruutua.
            }

        }
        else {
            value += 100*(playerPieces - opponentPieces);
        }

        byte[] squares = board.getColouredSquares(player);
        for (int squareIndex = 0; squareIndex < squares.length; squareIndex++) {
            value += 20 * this.getEmptyNeighbourCount(squares[squareIndex], board);
        }
        squares = board.getColouredSquares(opponent);
        for (int squareIndex = 0; squareIndex < squares.length; squareIndex++) {
            value -= 20 * this.getEmptyNeighbourCount(squares[squareIndex], board);
        }
//        double random = 0.9 + (Math.random()*0.2); // ] 0.9 ; 1.1 [
        return (short) value;
    }

    private int getEmptyNeighbourCount(byte square, MillBoard board) {
        byte[] neighbours = BoardInfo.getNeighbours(square);
        int counter = 0;
        for (int neighbourIndex = 0; neighbourIndex < neighbours.length; neighbourIndex++) {
            if (board.get(neighbours[neighbourIndex]) == BoardInfo.EMPTY) {
                counter++;
            }
        }
        return counter;
    }

    private void printPath(Node node) {
        Node tmp = node;
        while (tmp != null) {
            System.out.print(tmp.PREVIOUS_MOVE);
            tmp = tmp.PATH;
        }
        System.out.println();
    }

    private Move randomMove(MillGame game) {
        Move[] moves = LegalMoves.getAllLegalMoves(game);
        return moves[ (int)(Math.random()*moves.length) ];
    }


    // laskennan tulostietoja juuri nyt.
    public String toString() {
        String tmp = "";
        tmp = tmp + "time used: "+((System.currentTimeMillis() - this.searchStarted)/1000)+"s\n";
        tmp = tmp + "searching root nodes: "+(this.currentRootNode + 1)+"/"+this.rootNodes+"\n";
        tmp = tmp + "nodes: "+this.nodesTotal+"\n";
//        if (this.timeLimited) {
            tmp = tmp + "searching depth "+this.depthLimit+"\n";
            if (this.bestPathSoFar != null) {
                tmp = tmp + "best move: " +bestPathSoFar.PATH.PREVIOUS_MOVE +
                            ", value: "+bestPathSoFar.VALUE;
            }
//        }
//        else {
//        }
        return tmp;

    }

	public static void main(String[] args) {
        MillBoard board = new MillBoard();
/*        board.setPiece((byte)0, BoardInfo.BLACK);
        board.setPiece((byte)3, BoardInfo.BLACK);
        board.setPiece((byte)7, BoardInfo.BLACK);
        board.setPiece((byte)18, BoardInfo.BLACK);
        board.setPiece((byte)19, BoardInfo.BLACK);
        board.setPiece((byte)21, BoardInfo.BLACK);
        board.setPiece((byte)23, BoardInfo.BLACK);

        board.setPiece((byte)1, BoardInfo.WHITE);
        board.setPiece((byte)2, BoardInfo.WHITE);
        board.setPiece((byte)4, BoardInfo.WHITE);
        board.setPiece((byte)5, BoardInfo.WHITE);
        board.setPiece((byte)9, BoardInfo.WHITE);
        board.setPiece((byte)16, BoardInfo.WHITE);
        board.setPiece((byte)22, BoardInfo.WHITE);
*/
        board.setPiece((byte)13, BoardInfo.BLACK);

        board.setPiece((byte)4, BoardInfo.WHITE);
        board.setPiece((byte)10, BoardInfo.WHITE);


        MillGame test = new MillGame();
/*        test.generateMillGame(board,
                              MillGame.WHITE_PLAYER,
                              MillGame.PHASE_MIDGAME,
                              (byte)0,
                              (byte)0);
*/

        test.generateMillGame(board,
                              MillGame.BLACK_PLAYER,
                              MillGame.PHASE_BEGINNING,
                              (byte)7,
                              (byte)8);

        MillAI cpu = new MillAI();
        cpu.depthSearch(test, (byte)10);
	}
}
