/*
 * @(#)MillGame.java  9.2.2004
 *
 * Copyright 2004 Esa Junttila
 */

import java.util.Stack;

/**
 * Luokan <tt>MillGame</tt>-ilmentym‰t kuvaavat yht‰ myllypeli‰.
 * Oliot pit‰v‰t itse huolta siit‰ ett‰ pelin kulku menee oikein.
 * Luokan perustalle voi rakentaa l‰hes millaisen k‰yttˆliittym‰n tahansa.
 * Myllynpeli jakaantuu nelj‰‰n pelivaiheeseen:<br>
 * <ul><li><tt>PHASE_BEGINNING</tt> aloittaa pelin ja se sis‰lt‰‰
 *         kaikki siirrot jolloin pelaajilla on viel‰ nappuloita
 *         k‰siss‰‰n. T‰llˆin nappulat asetetaan k‰dest‰ laudalle.</li>
 *     <li><tt>PHASE_MIDGAME</tt>: k‰sinappulat on jo asetettu ja
 *         peli jatkuu normaalisti nappuloita viereisiin ruutuihin siirrellen.</li>
 *     <li><tt>PHASE_ENDGAME</tt>: Kun jommalla kummalla pelaajista on
 *         en‰‰ kolme nappulaa j‰ljell‰, h‰n saa siirt‰‰ nappuloitaan
 *         mihin tahansa vapaisiin ruutuihin.</li>
 *     <li><tt>PHASE_GAME_OVER</tt>: peli on ohi eik‰ uusia siirtoja oteta vastaan.</li></ul>
 */
public class MillGame implements Cloneable {

    /** Pelivaihe*/
    public static final byte PHASE_BEGINNING  = 1,
                             PHASE_MIDGAME = 2,
                             PHASE_ENDGAME = 3,
                             PHASE_GAME_OVER  = 4;

    /** Monellako k‰sinappulalla aloitetaan*/
    private static final byte PIECES_IN_HAND = 9;

    /** Pelaajien arvot*/
    public static final byte BLACK_PLAYER = BoardInfo.BLACK,
                             WHITE_PLAYER = BoardInfo.WHITE;

    /** Pelin sis‰inen tila*/
    private byte gameState;

    /** Vuorossa oleva pelaaja (pelin p‰‰ttyess‰ sama kuin voittaja)*/
    private byte activePlayer;

    /** Valkoisella nappuloita k‰dess‰‰n*/
    private byte whitePiecesInHand;

    /** Mustalla nappuloista k‰dess‰‰n*/
    private byte blackPiecesInHand;

    /** Pelin k‰ytt‰m‰ pelilauta*/
    private MillBoard board;

    /** Pelihistoria peruutuksia (undo) varten*/
    private Stack history;

    /** Pelitulevaisuus peruutuksen peruutuksia (redo) varten*/
    private Stack future;

   /**
    * Luo uuden myllypelin, jossa mustalla on ensimm‰inen siirtovuoro
    */
    public MillGame() {
        this.newGame();
    }

   /**
    * Asettaa nykyisen pelin takaisin l‰htˆasemiin.
    */
    public void newGame() {
        this.gameState = PHASE_BEGINNING;
        this.activePlayer = WHITE_PLAYER;
        this.whitePiecesInHand = PIECES_IN_HAND;
        this.blackPiecesInHand = PIECES_IN_HAND;
        this.board = new MillBoard();
        this.history = new Stack();
        this.future = new Stack();
    }


    /**
     * Palauttaa vuoronumeron. Pelin aloitustilanne on vuoro 0.
     * @return Vuoron numero
     */
    public int getTurnNumber() {
        return this.history.size() +1;
    }


   /**
    * Luo <tt>MillGame</tt>-oliosta itsen‰isen kopion
    * omilla olioilla (eik‰ pelkill‰ viitteill‰). Kopio
    * ei sis‰ll‰ aiempia historiatietoja, mutta niit‰
    * voidaan lis‰t‰ siihen.
    * return Olion kopio ilman aiempia historiatietoja.
    */
    public Object clone() {
        MillGame copy = new MillGame();
        copy.gameState = this.gameState;
        copy.activePlayer = this.activePlayer;
        copy.whitePiecesInHand = this.whitePiecesInHand;
        copy.blackPiecesInHand = this.blackPiecesInHand;
        copy.board = (MillBoard)this.board.clone();
        return copy;
    }


   /**
    * Palauttaa peliin saman tilanteen kuin
    * ennen edellist‰ siirtoa.
    *
    * @exception IllegalStateException
    *                   Jos vuoro on pelin ensimm‰inen, undoa ei voi suorittaa.
    */
    public void undo() throws IllegalStateException {
        if (this.history.empty()) {
            throw new IllegalStateException("Undo cannot be done during first turn!");
        }
        this.future.push(this.clone());
        this.restoreGame( (MillGame)(this.history.pop()) );
    }

   /**
    * Palauttaa peliin saman tilanteen kuin
    * ennen edellist‰ <tt>undo</tt>-toimitoa. Jos edellisen <tt>undo</tt>-
    * toiminnon j‰lkeen on suoritettu <tt>makeMove</tt>,
    * <tt>redo</tt>-toiminnot eiv‰t ole mahdollisia ennen uusia
    * <tt>undo</tt>-kutsuja.
    * @exception IllegalStateException
    *                   Jos <tt>undo</tt>-toimintoa ei ole suoritettu tai sen j‰lkeen on
    *                   kutsuttu <tt>makeMove</tt>, redoa ei voi suorittaa.
    */
    public void redo() throws IllegalStateException {
        if (this.future.empty()) {
            throw new IllegalStateException("Redo cannot be done unless undo has been used first!");
        }
        this.history.push(this.clone());
        this.restoreGame( (MillGame)(this.future.pop()) );
    }

    /** Palauttaa t‰h‰n peliolioon toisen MillGame-olion pelitilanteen.*/
    private void restoreGame(MillGame restored) {
        this.gameState = restored.gameState;
        this.activePlayer = restored.activePlayer;
        this.whitePiecesInHand = restored.whitePiecesInHand;
        this.blackPiecesInHand = restored.blackPiecesInHand;
        this.board = restored.board;
        // HUOM! Historiatiedot ovat olemassa vain t‰ss‰ oliossa.
        // Historian ja Tulevaisuuden pelit eiv‰t itse sis‰ll‰ mit‰‰n historiatietoja.
    }

    /**
     * Onko <tt>undo()</tt>-toiminto mahdollinen.
     * Undo() on mahdollinen jos
     *
     *
     */
    public boolean undoIsPossible() {
        return this.history.size() > 0;
    }

    /**
     *
     *
     *
     *
     */
    public boolean redoIsPossible() {
        return this.future.size() > 0;
    }


   /**
    * Palauttaa kopion pelin k‰ytt‰m‰st‰ laudasta
    * @return Kopio <tt>MillBoard</tt>-oliosta.
    */
    public MillBoard getMillBoard() {
        return (MillBoard)this.board.clone();
    }

    /** Montako nappulaa mustalla pelaajalla on viel‰ k‰dess‰‰n*/
    public byte getBlackPiecesInHand() { return this.blackPiecesInHand; }

    /** Montako nappulaa vakloisella pelaajalla on viel‰ k‰dess‰‰n*/
    public byte getWhitePiecesInHand() { return this.whitePiecesInHand; }

   /**
    * Palauttaa vuorossa olevan pelaajan tai pelin p‰‰tytty‰ voittajan.
    * @return <ul><li><tt>MillGame.BLACK_PLAYER</tt>, jos vuoro on
    *             mustalla pelaajalla.</li>
    *             <li><tt>MillGame.WHITE_PLAYER</tt>, jos vuoro on
    *             valkoisella pelaajalla.</li></ul>
    */
    public byte getActivePlayer() { return this.activePlayer; }

   /**
    * Palauttaa pelin sisl‰isen tilan.
    * return <ul><li><tt>MillGame.PHASE_BEGINNING</tt>, jos pelaajilla on
    *                viel‰ k‰sinappuloita j‰ljell‰.</li>
    *            <li><tt>MillGame.PHASE_MIDGAME</tt>, jos k‰sinappulat ovat
    *                loppuneet ja molemmilla on ainakin nelj‰ nappulaa laudalla.</li>
    *            <li><tt>MillGame.PHASE_ENDGAME</tt>, jos ainakin toisella
    *                pelaajalla on tasan kolme nappulaa laudalla.</li>
    *            <li><tt>MillGame.PHASE_GAME_OVER</tt>, jos peli on p‰‰ttynyt.</li></ul>
    *
    */
    public byte getGameState() { return this.gameState; }





   /**
    * Suorittaa annetun siirron t‰ss‰ peliss‰ nykyisen pelaajan vuorolla.
    * Jos peli p‰‰ttyy siirron vaikutuksesta, palautetaan <tt>true</tt>.
    * Voittajan saa selville silloin metodilla <tt>getActivePlayer()</tt>.
    * Peli tallentaa samalla pelin historiatietoja mahdollisia peruutuksia
    * (<tt>undo(), redo()</tt>) varten.
    * @param move Siirto joka halutaan tehd‰
    * @return <ul><li><tt>True</tt>, jos peli p‰‰ttyi. Pelin voittajan saa
    *              selville kutsumalla metodia getActivePlayer.</li>
    *             <li><tt>False</tt>, jos peli jatkuu siirron j‰lkeenkin.</li></ul>
    * @exception IllegalArgumentException Jos <tt>move</tt> on arvoltaan <tt>null</tt>
    *            TAI jos se on t‰ss‰ pelin tilanteessa virheellinen siirto.
    * @exception IllegalStateException Jos peli on jo p‰‰ttynyt (TAI tapahtui
    *            sis‰inen virhe.)
    */
    public boolean makeMove(Move move) throws IllegalArgumentException,
                                              IllegalStateException {
        return this.move(move, true);
    }

   /**
    * Suorittaa annetun siirron t‰ss‰ peliss‰ nykyisen pelaajan vuorolla.
    * Jos peli p‰‰ttyy siirron vaikutuksesta, palautetaan <tt>true</tt>.
    * Voittajan saa selville silloin metodilla <tt>getActivePlayer()</tt>.
    * <p>
    * T‰m‰ metodi on tarkoitettu vaihtoehtoiseksi kutsuksi kun rakennetaan
    * teko‰ly‰. Pelipuussa liikkuminen <tt>undo()</tt>-ja <tt>redo()</tt>-
    * toiminnoilla on hieman hitaampaa kuin normaali pelin kopioiminen
    * <tt>clone()</tt>-metodilla. Jos peruutustoimintoja ei tarvita, niiden
    * yll‰pit‰minen voidaan j‰tt‰‰ pois parametrin arvolla <tt>false</tt>.
    *
    * @param move Siirto joka halutaan tehd‰
    * @param undoRedoEnabled  Tahdotaanko pelin rekisterˆiv‰n pelihistorian.
    * @return <ul><li><tt>True</tt>, jos peli p‰‰ttyi. Pelin voittajan saa
    *              selville kutsumalla metodia getActivePlayer.</li>
    *             <li><tt>False</tt>, jos peli jatkuu siirron j‰lkeenkin.</li></ul>
    * @exception IllegalArgumentException Jos <tt>move</tt> on arvoltaan <tt>null</tt>
    *            TAI jos se on t‰ss‰ pelin tilanteessa virheellinen siirto.
    * @exception IllegalStateException Jos peli on jo p‰‰ttynyt (TAI tapahtui
    *            sis‰inen virhe.)
    */
    public boolean makeMove(Move move, boolean undoRedoEnabled) throws IllegalArgumentException,
                                                                       IllegalStateException {
        return this.move(move, undoRedoEnabled);
    }


    /** Siirron tarkistukset ja itse toteutus*/
    private boolean move(Move move, boolean undoRedoEnabled) throws IllegalArgumentException,
                                                                    IllegalStateException {
        if (move == null) {
            throw new IllegalArgumentException("makeMove(Move): Parameter 'Move' cannot be null. ");
        }

        if ( !LegalMoves.isLegalMove(move, this) ) {
            throw new IllegalArgumentException("makeMove(Move): Parameter 'Move' is an illegal move. ");
        }

        if (undoRedoEnabled) {
            // tallennetaan nykyinen tilanne historiatietoihin
            this.history.push(this.clone());
        }
        else {
            this.history = new Stack(); // historian nollaus
        }
        // move avaa pelimaailmassa uuden haaran eiv‰tk‰
        //aiemmat redo-toiminnot ole en‰‰ j‰rkevi‰.
        this.future = new Stack();

        if (this.gameState == PHASE_BEGINNING) {
            if (this.activePlayer == WHITE_PLAYER) {
                this.whitePiecesInHand--;
                this.board.setPiece(move.TO, BoardInfo.WHITE);
            }
            else if (this.activePlayer == BLACK_PLAYER) {
                this.blackPiecesInHand--;
                this.board.setPiece(move.TO, BoardInfo.BLACK);
            }
            else {
                throw new IllegalStateException("makeMove(Move): Internal error --> "+
                                                "Illegal activePlayer:"+this.activePlayer);
            }

            if (move.REMOVE != Move.NOWHERE) {
                this.board.removePiece(move.REMOVE);
            }

            if (this.whitePiecesInHand == 0 && this.blackPiecesInHand == 0) {
                this.gameState = PHASE_MIDGAME;

                   // Voi olla, ett‰ asetettuaan kaikki nappulansa alkupeliss‰
                   // jompikumpi pelaajista j‰‰ jumiin keskipeliss‰.
                   // Tuo nappuloiden m‰‰r‰n putoaminen alle kolmen
                   // ei taida olla kuitenkaan edes mahdollinen.
                if ( this.board.allPiecesJammed(this.activePlayer)  ||
                    (this.board.getColouredSquares(this.activePlayer)).length < 3) {
                        this.gameState = PHASE_GAME_OVER;
                        this.activePlayer = this.getOpponent(); // Vastustaja voitti pelin!
                        return true;
                }
                if ( this.board.allPiecesJammed(this.getOpponent())  ||
                    (this.board.getColouredSquares(this.getOpponent())).length < 3) {
                        this.gameState = PHASE_GAME_OVER;
                        return true;
                }
            }
            this.activePlayer = this.getOpponent();
            return false;
        }
        else if (this.gameState == PHASE_MIDGAME || this.gameState == PHASE_ENDGAME) {
            this.board.movePiece(move.FROM, move.TO);

            // myllyn poistama nappula:
            if (move.REMOVE != Move.NOWHERE) {
                this.board.removePiece(move.REMOVE);
            }

            if ( (this.board.getColouredSquares(BoardInfo.WHITE)).length == 3  ||
                 (this.board.getColouredSquares(BoardInfo.BLACK)).length == 3)
                    this.gameState = PHASE_ENDGAME;

            // Jos siirron j‰lkeen vastustajan kaikki nappulat ovat jumissa tai h‰nen
            // nappuloidensa m‰‰r‰ laskee alle kolmen, nyt vuorossa oleva pelaaja on voittanut!
            // Moka voi sattua myˆs pelaajalle itselleen, jolloin vastustaja voittaa.
            if ( (this.gameState == PHASE_MIDGAME && this.board.allPiecesJammed(this.getOpponent()) ) ||
                  (this.board.getColouredSquares(this.getOpponent())).length < 3) {
                        this.gameState = PHASE_GAME_OVER;
                        return true;
            }
            if ( (this.gameState == PHASE_MIDGAME && this.board.allPiecesJammed(this.activePlayer) ) ||
                  (this.board.getColouredSquares(this.activePlayer)).length < 3) {
                        this.gameState = PHASE_GAME_OVER;
                        this.activePlayer = this.getOpponent(); // Vastustaja voitti pelin!
                        return true;
            }

            this.activePlayer = this.getOpponent();
            return false;
        }
        else {
            throw new IllegalStateException("makeMove(Move): Game is over and no more moves can be played.");
        }
    }


   /**
    * Palauttaa nyt vuorossa olevan pelaajan vastustajan.
    * @return Palautuu joko <tt>MillGame.WHITE_PLAYER</tt> tai <tt>MillGame.BLACK_PLAYER</tt>.
    */
    public byte getOpponent() {
        if (this.activePlayer == WHITE_PLAYER)
            return BLACK_PLAYER;
        else
            return WHITE_PLAYER;
    }


   /**
    * Palauttaa myllypelin tekstimuotoisen esityksen tekstipohjaisia
    * k‰yttˆliittymi‰ varten.
    * @return Olion <tt>MillGame</tt> merkkimuotoinen esitys.
    */
    public String toString() {
         String tmp = "";
         tmp = tmp + this.board+"\n";
         if (this.gameState == PHASE_BEGINNING)
              tmp = tmp +"ALKUPELI!";
         else if (this.gameState == PHASE_MIDGAME)
              tmp = tmp +"KESKIPELI!";
         else if (this.gameState == PHASE_ENDGAME)
              tmp = tmp +"LOPPUPELI!";
         else
              tmp = tmp +"PELI OHI!";

         if (this.activePlayer == WHITE_PLAYER)
              tmp = tmp + "  vuoro: VALKOINEN";
         else
              tmp = tmp + "  vuoro: MUSTA";

         tmp = tmp + " Valkoisia k‰sinappuloita:"+this.whitePiecesInHand+" Mustia k‰sinappuloita:"+this.blackPiecesInHand;
         return tmp + "\nVuoro:"+this.getTurnNumber()+" Siirto on muotoa (FROM, TO, REMOVE), jossa luku on 0--23 tai -1.";
    }


    /**
     * TESTITARKOITUKSIIN: Metodi, jolla voi asettaa <tt>MillGame</tt>-olion
     * haluamaansa (testi-)tilaan. HUOM! Metodi ei tarkista onko pelitilanne
     * lainkaan j‰rkev‰!
     */
    public void generateMillGame(MillBoard newBoard,
                                 byte newActivePlayer,
                                 byte newGameState,
                                 byte newWhitePiecesInHand,
                                 byte newBlackPiecesInHand) {
        this.board = newBoard;
        this.activePlayer = newActivePlayer;

        this.gameState = newGameState;
        this.whitePiecesInHand = newWhitePiecesInHand;
        this.blackPiecesInHand = newBlackPiecesInHand;
    }


	public static void main(String[] args) {
        MillBoard board = new MillBoard();
        board.setPiece((byte)0, BoardInfo.BLACK);
        board.setPiece((byte)3, BoardInfo.BLACK);
        board.setPiece((byte)4, BoardInfo.BLACK);
        board.setPiece((byte)9, BoardInfo.BLACK);
        board.setPiece((byte)10, BoardInfo.BLACK);
        board.setPiece((byte)11, BoardInfo.BLACK);
        board.setPiece((byte)18, BoardInfo.BLACK);
        board.setPiece((byte)2, BoardInfo.WHITE);
        board.setPiece((byte)12, BoardInfo.WHITE);
        board.setPiece((byte)14, BoardInfo.WHITE);
        board.setPiece((byte)16, BoardInfo.WHITE);
        board.setPiece((byte)19, BoardInfo.WHITE);
        board.setPiece((byte)22, BoardInfo.WHITE);
        board.setPiece((byte)23, BoardInfo.WHITE);

        MillGame test = new MillGame();
        test.generateMillGame(board, WHITE_PLAYER, PHASE_MIDGAME, (byte)0, (byte)0);

        System.out.println("***miettii hetken***");
        long alkuhetki = System.currentTimeMillis();
        int TOISTOJA = 1000;
        int laskuri = 0;
        for (int i=0; i < TOISTOJA; i++) {
            for (byte eka=0; eka < 24; eka++)
                for (byte toka=0; toka < 24; toka++) {
                        board.areNeighbours(eka, toka);
                        laskuri++;
                   }
         }
         long loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'areNeighbours(byte,byte)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+laskuri);

         TOISTOJA = 10000;
         alkuhetki = System.currentTimeMillis();
         for (int i=0; i < TOISTOJA; i++)
              board.allPiecesJammed(BLACK_PLAYER);
         loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'allPiecesJammed(BLACK)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+TOISTOJA);


         TOISTOJA = 10000;
         alkuhetki = System.currentTimeMillis();
         for (int i=0; i < TOISTOJA; i++)
              board.allPiecesJammed(WHITE_PLAYER);
         loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'allPiecesJammed(WHITE)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+TOISTOJA);

         TOISTOJA = 10000;
         alkuhetki = System.currentTimeMillis();
         for (int i=0; i < TOISTOJA; i++)
              board.createsNewMill(new Move(12,17,-1), WHITE_PLAYER);
         loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'createsNewMill(Move, byte)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+TOISTOJA);


         TOISTOJA = 10000;
         alkuhetki = System.currentTimeMillis();
         for (int i=0; i < TOISTOJA; i++)
              for (byte ruutu=0; ruutu < 24; ruutu++)
                   board.isPartOfMill(ruutu);
         loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'isPartOfMill(byte)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+(TOISTOJA*24));

         TOISTOJA = 10000;
         alkuhetki = System.currentTimeMillis();
         for (int i=0; i < TOISTOJA; i++)
              board.allPiecesInMills(WHITE_PLAYER);
         loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'allPiecesInMills(WHITE_PLAYER)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+TOISTOJA);


         TOISTOJA = 10000;
         alkuhetki = System.currentTimeMillis();
         for (int i=0; i < TOISTOJA; i++)
              board.allPiecesInMills(BLACK_PLAYER);
         loppuhetki = System.currentTimeMillis();
         System.out.println("Metodi 'allPiecesInMills(BLACK_PLAYER)' vei aikaa " +(loppuhetki-alkuhetki)+" ms. Toistoja oli "+TOISTOJA);

    }
}
