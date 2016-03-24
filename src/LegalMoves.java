/*
 * @(#)LegalMoves.java  5.2.2004
 *
 * Copyright 2004 Esa Junttila
 */


/**
 * Luokka <tt>LegalMoves</tt> tarjoaa staattisia palveluja
 * koskien mahdollisia siirtoja (Move-oliot) ja niiden laillisuuksia.
 *
 * @version 5.2.2004
 * @author 	Esa Junttila
 */
public final class LegalMoves{

    /** Ei kutsuta koskaan*/
    private LegalMoves() {}

   /**
    * Onko siirto sallittu <tt>MillGame</tt>-olion tilanteessa.
    * @param move    Testattava siirto
    * @param game    Testattava peli
    * @return boolean    Onko siirto sallittu parametrina annetussa peliss‰
    * @exception NullPointerException
    *            Jos kumpikaan parametreista on <tt>null</tt>.
    */
    public static boolean isLegalMove(Move move, MillGame game) {
        return legal(move, game, game.getMillBoard());
    }


    private static boolean legal(Move move, MillGame game, MillBoard board) {
        byte activePlayer = game.getActivePlayer();
        byte opponent = game.getOpponent();
        byte gameState = game.getGameState();

        if (board.get(move.TO) != BoardInfo.EMPTY) {        // Yritet‰‰n siirt‰‰ varattuun ruutuun
            return false;
        }


        if (gameState == MillGame.PHASE_GAME_OVER) {              // Peli on jo p‰‰ttynyt
            return false;
        }
        else if (gameState == MillGame.PHASE_BEGINNING) {
            if (move.FROM != Move.NOWHERE) {                // Alkupeliss‰ ei siirret‰ nappuloita
                return false;
            }
        }
        else if (gameState == MillGame.PHASE_MIDGAME) {
            if (move.FROM == Move.NOWHERE) {     // Keskipeliss‰ on siirrett‰v‰ nappulaa
                return false;
            }
            if (board.get(move.FROM) != activePlayer) {     // Siirrett‰v‰ nappula ei ole oma
                return false;
            }
            if ( !board.areNeighbours(move.FROM, move.TO) ) { // Siirrot vain vierekk‰isiin ruutuihin
                return false;
            }
        }
        else if (gameState == MillGame.PHASE_ENDGAME) {
            if (move.FROM == Move.NOWHERE) {       // Loppupeliss‰ on siirrett‰v‰ nappulaa
                return false;
            }
            if (board.get(move.FROM) != activePlayer) {     // Siirrett‰v‰ nappula ei ole oma
                return false;
            }
        }


        if (move.REMOVE == Move.NOWHERE) {                  // Ei yritet‰ poistaa nappuloita
            if ( !board.createsNewMill(move, activePlayer) ) {  // Uutta mylly‰ ei muodostu
                return true;
            }
        }
        else if (board.get(move.REMOVE) == opponent) {      // Poistettava on vastustajan nappula
            if (board.createsNewMill(move, activePlayer)) { // Uusi mylly muodostuu
                if ( !board.isPartOfMill(move.REMOVE) ) {   // Poistettava ei kuulu myllyyn
                    return true;
                }
                else if (board.allPiecesInMills(opponent)) {//Kaikki vastustajan nappulat kuuluvat myllyihin
                    return true;
                }
            }
        }

        return false;
    }



    public static Move[] getAllLegalMoves2(MillGame game) {
        Move[] tmpAllMoves = new Move[1000];
        MillBoard board = game.getMillBoard();

        int counter = 0;
        for (byte from=-1; from < 24; from++) {
            for (byte to=0; to < 24; to++) {
                for (byte remove=-1; remove < 24; remove++) {
                    Move move = new Move(from, to, remove);
                    if (LegalMoves.legal(move, game, board)) {
                        tmpAllMoves[counter] = move;
                        counter++;
                    }
                }
            }
        }
        Move[] allMoves = new Move[counter];
        for (int index=0; index < counter; index++) {
            allMoves[index] = tmpAllMoves[index];
        }
        return allMoves;
    }


    public static Move[] getAllLegalMoves(MillGame game) {
        byte gameState = game.getGameState();

        if (gameState == MillGame.PHASE_BEGINNING) {
            return getAllLegalMoves_beginning(game);
        }
        else if (gameState == MillGame.PHASE_MIDGAME) {
            return getAllLegalMoves_middlegame(game);
        }
        else if (gameState == MillGame.PHASE_ENDGAME) {
            return getAllLegalMoves_endgame(game);
        }
        else {
            return new Move[0];
        }
    }


    private static Move[] getAllLegalMoves_beginning(MillGame game) {
        byte activePlayer = game.getActivePlayer();
        byte opponent = game.getOpponent();
        MillBoard board = game.getMillBoard();

        Move[] allMoves = new Move[1000];  // p‰‰st‰ heitetty maksimiarvo
        int counter = 0;
        byte from = Move.NOWHERE;
        for (byte to = 0; to < BoardInfo.SQUARES_ON_BOARD; to++) {
            if (board.get(to) != BoardInfo.EMPTY) {
                continue;
            }
            if (board.createsNewMill( new Move(from, to, (byte)0), activePlayer) ) {
                for (byte remove = 0; remove < BoardInfo.SQUARES_ON_BOARD; remove++) {
                    if (board.get(remove) != opponent) {
                        continue;
                    }
                    // Kuuluuko nappula myllyyn? Jos kuuluu, niin tarkistetaan
                    // kuuluvatko kaikki vastustajan nappulat myllyyn.
                    if (board.isPartOfMill(remove)) {
                        if ( board.allPiecesInMills(opponent) ) {
                            allMoves[counter] = new Move(from, to, remove);
                            counter++;
                        }
                    }
                    else {
                        allMoves[counter] = new Move(from, to, remove);
                        counter++;
                    }
                }
            }
            else {
                allMoves[counter] = new Move(from, to, Move.NOWHERE);
                counter++;
            }
        }
        return compressArray(allMoves, counter);
    }



    private static Move[] getAllLegalMoves_middlegame(MillGame game) {
        byte activePlayer = game.getActivePlayer();
        byte opponent = game.getOpponent();
        MillBoard board = game.getMillBoard();

        Move[] allMoves = new Move[1000];  // p‰‰st‰ heitetty maksimiarvo
        int counter = 0;
        for (byte from = 0; from < BoardInfo.SQUARES_ON_BOARD; from++) {
            if (board.get(from) != activePlayer) {
                continue;
            }
            byte[] neighbours = BoardInfo.getNeighbours(from);
            for (byte neighbourIndex = 0; neighbourIndex < neighbours.length; neighbourIndex++) {
                byte to = neighbours[neighbourIndex];
                if (board.get(to) != BoardInfo.EMPTY) {
                    continue;
                }
                if (board.createsNewMill( new Move (from, to, (byte)0), activePlayer)) {
                    for (byte remove = 0; remove < BoardInfo.SQUARES_ON_BOARD; remove++) {
                        if (board.get(remove) != opponent) {
                            continue;
                        }
                        //Kuuluuko nappula myllyyn? Jos kuuluu, niin tarkistetaan
                        // kuuluvatko kaikki vastustajan nappulat myllyyn.
                        if (board.isPartOfMill(remove)) {
                            if ( board.allPiecesInMills(opponent) ) {
                                allMoves[counter] = new Move(from, to, remove);
                                counter++;
                            }

                        }
                        else {
                            allMoves[counter] = new Move(from, to, remove);
                            counter++;
                        }
                    }
                }
                else {
                    allMoves[counter] = new Move(from, to, Move.NOWHERE);
                    counter++;
                }
            }
        }
        return compressArray(allMoves, counter);
    }



    private static Move[] getAllLegalMoves_endgame(MillGame game) {
        byte activePlayer = game.getActivePlayer();
        byte opponent = game.getOpponent();
        MillBoard board = game.getMillBoard();

        if ( (board.getColouredSquares(activePlayer)).length > 3) {
            return getAllLegalMoves_middlegame(game);
        }

        Move[] allMoves = new Move[1000];  // p‰‰st‰ heitetty maksimiarvo
        int counter = 0;
        for (byte from = 0; from < BoardInfo.SQUARES_ON_BOARD; from++) {
            if (board.get(from) != activePlayer) {
                continue;
            }
            for (byte to = 0; to < BoardInfo.SQUARES_ON_BOARD; to++) {
                if (board.get(to) != BoardInfo.EMPTY) {
                    continue;
                }
                if ( board.createsNewMill( new Move(from, to, (byte)0), activePlayer) ) {
                    for (byte remove = 0; remove < BoardInfo.SQUARES_ON_BOARD; remove++) {
                        if (board.get(remove) != opponent) {
                            continue;
                        }
                        // Kuuluuko nappula myllyyn? Jos kuuluu, niin tarkistetaan
                        // kuuluvatko kaikki vastustajan nappulat myllyyn.
                        if (board.isPartOfMill(remove)) {
                            if ( board.allPiecesInMills(opponent) ) {
                                allMoves[counter] = new Move(from, to, remove);
                                counter++;
                            }
                        }
                        else {
                            allMoves[counter] = new Move(from, to, remove);
                            counter++;
                        }
                    }
                }
                else {
                    allMoves[counter] = new Move(from, to, Move.NOWHERE);
                    counter++;
                }
            }
        }
        return compressArray(allMoves, counter);
    }


    // Jos palautettavan taulun koko on 0, vain 'from == Move.NOWHERE' on sallittu
    public static byte[] getAllLegalFROMSquares(MillGame game) {
        Move[] allMoves = getAllLegalMoves(game);
        byte[] tmpSquares = new byte[allMoves.length]; // t‰m‰ on maksimikoko
        for (int i = 0; i < tmpSquares.length; i++) {
            tmpSquares[i] = -2;
        }
        int counter = 0;
        for (int index = 0; index < allMoves.length; index++) {
            byte from = allMoves[index].FROM;
            if ( from != Move.NOWHERE) {
                if ( !containsValue(tmpSquares, from) ) {
                    tmpSquares[counter] = from;
                    counter++;
                }
            }
        }
        return compressArray(tmpSquares, counter);
    }


    public static byte[] getAllLegalTOSquares(MillGame game, byte fromSquare) {
        Move[] allMoves = getAllLegalMoves(game);
        byte[] tmpSquares = new byte[allMoves.length]; // t‰m‰ on maksimikoko
        for (int i = 0; i < tmpSquares.length; i++) {
            tmpSquares[i] = -2;
        }
        int counter = 0;
        for (int index = 0; index < allMoves.length; index++) {
            byte from = allMoves[index].FROM;
            if (from != fromSquare) { // l‰htˆruudun pit‰‰ t‰sm‰t‰
                continue;
            }
            byte to = allMoves[index].TO;
            if ( to != Move.NOWHERE) {
                if ( !containsValue(tmpSquares, to) ) {
                    tmpSquares[counter] = to;
                    counter++;
                }
            }
        }
        return compressArray(tmpSquares, counter);
    }

    // Jos palautettavan taulun koko on 0, vain 'remove == Move.NOWHERE' on sallittu
    public static byte[] getAllLegalREMOVESquares(MillGame game, byte fromSquare, byte toSquare) {
        Move[] allMoves = getAllLegalMoves(game);
        byte[] tmpSquares = new byte[allMoves.length]; // t‰m‰ on maksimikoko
        for (int i = 0; i < tmpSquares.length; i++) {
            tmpSquares[i] = -2;
        }
        int counter = 0;
        for (int index = 0; index < allMoves.length; index++) {
            byte from = allMoves[index].FROM;
            byte to = allMoves[index].TO;
            if (from != fromSquare || to != toSquare) {
                continue;
            }
            byte remove = allMoves[index].REMOVE;
            if ( remove != Move.NOWHERE) {
                if ( !containsValue(tmpSquares, remove) ) {
                    tmpSquares[counter] = remove;
                    counter++;
                }
            }
        }
        return compressArray(tmpSquares, counter);
    }


    private static boolean containsValue(byte[] array, byte value) {
        for (int index = 0; index < array.length; index++) {
            if (array[index] == value) {
                return true;
            }
        }
        return false;
    }

    private static Move[] compressArray(Move[] array, int count) {
         Move[] compressed = new Move[count];
         for (int moveIndex = 0; moveIndex < count; moveIndex++)
              compressed[moveIndex] = array[moveIndex];
         return compressed;
    }

    private static byte[] compressArray(byte[] array, int count) {
         byte[] compressed = new byte[count];
         for (int moveIndex = 0; moveIndex < count; moveIndex++)
              compressed[moveIndex] = array[moveIndex];
         return compressed;
    }

	public static void main(String[] args) {
        MillBoard board = new MillBoard();
        board.setPiece((byte)0, BoardInfo.BLACK);
//        board.setPiece((byte)3, BoardInfo.BLACK);
        board.setPiece((byte)4, BoardInfo.BLACK);
//        board.setPiece((byte)9, BoardInfo.BLACK);
        board.setPiece((byte)10, BoardInfo.BLACK);
//        board.setPiece((byte)11, BoardInfo.BLACK);
//        board.setPiece((byte)18, BoardInfo.BLACK);

        board.setPiece((byte)1, BoardInfo.WHITE);
        board.setPiece((byte)15, BoardInfo.WHITE);
//        board.setPiece((byte)14, BoardInfo.WHITE);
        board.setPiece((byte)16, BoardInfo.WHITE);
//        board.setPiece((byte)19, BoardInfo.WHITE);
//        board.setPiece((byte)22, BoardInfo.WHITE);
//        board.setPiece((byte)23, BoardInfo.WHITE);

        MillGame test = new MillGame();
        test.generateMillGame(board, MillGame.BLACK_PLAYER, MillGame.PHASE_ENDGAME, (byte)0, (byte)0);

        Move[] moves = getAllLegalMoves(test);
        for (int index = 0; index < moves.length; index++) {
            System.out.println(moves[index]);
        }
    }
}
