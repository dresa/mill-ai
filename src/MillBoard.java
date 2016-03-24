/*
 * @(#)MillBoard.java  9.2.2004
 *
 * Copyright 2004 Esa Junttila
 */


/**
 * Luokan <tt>MillBoard</tt> ilmentym‰t kuvaavat Myllypelin
 * pelilautaa. Luokka tarjoaa suuren m‰‰r‰n palveluita
 * esim. nappuloiden eri tilojen, naapurien, jumiutumisen
 * jne. selvitt‰miseen.
 *<p>
 * Pelilauta koostuu 24:st‰ ruudusta (koodit 0--23), jotka on
 * kytketty toisiinsa kuvan osoittamalla tavalla.
 * <tt>toString</tt>-metodia kutsuttaessa pelilauta tulostuu
 * seuraavassa ulkoasussa:
 *
 * <pre>
 * 0 --------- 1 --------- 2
 * |           |           |
 * |   3 ----- 4 ----- 5   |
 * |   |       |       |   |
 * |   |   6 - 7 - 8   |   |
 * |   |   |       |   |   |
 * 9 - 10- 11      12- 13- 14
 * |   |   |       |   |   |
 * |   |   15- 16- 17  |   |
 * |   |       |       |   |
 * |   18----- 19----- 20  |
 * |           |           |
 * 21--------- 22--------- 23
 * </pre>
 *
 */
public class MillBoard implements Cloneable {

    /** Ruutujen arvoja s‰ilytet‰‰n t‰ss‰*/
    private byte[] squares;


   /**
    * Luo uuden <tt>MillBoard</tt>-olion, jossa kaikki ruudut ovat tyhji‰.
    */
    public MillBoard() {
        this.squares = new byte[BoardInfo.SQUARES_ON_BOARD];
    }

   /**
    * Luo tarkan kopion <tt>MillBoard</tt>-oliosta
    * @return    Olion tarkka kopio
    *
    */
    public Object clone() {
        MillBoard copy = new MillBoard();
        for (int index=0; index < this.squares.length; index++) {
            copy.squares[index] = this.squares[index];
        }
        return copy;
    }

   /**
    * Asettaa halutulle laudan ruudulle (koodi 0--23) pelinappulan.
    * Vain tyhj‰lle ruudulle voi asettaa nappulan.
    * Nappulan v‰rin on oltava joko <tt>BoardInfo.WHITE</tt> tai
    * <tt>BoardInfo.BLACK</tt>.
    * @param square    Mille ruudulle nappula asetetaan. (<tt>0 <= square <= 23</tt>)
    * @param color     Mink‰ v‰rinen nappula? <tt>BoardInfo.WHITE</tt> tai
    *                  <tt>BoardInfo.BLACK</tt>
    * @exception ArrayIndexOutOfBoundsException    Jos ei ollut <tt>0 <= square <= 23</tt>.
    * @exception IllegalArgumentException    Jos v‰riarvo ei ollut
    *                             <tt>BoardInfo.WHITE</tt> eik‰ <tt>BoardInfo.WHITE</tt>.
    * @exception IllegalStateException    Jos ruutu oli varattu.
    */
    public void setPiece(byte square, byte color) throws IllegalArgumentException,
                                                         IllegalStateException {
        if ( (color != BoardInfo.BLACK) && (color != BoardInfo.WHITE) ) {
            throw new IllegalArgumentException("setPiece(byte,byte): unknown color value:"+color);
        }
        if (this.squares[square] != BoardInfo.EMPTY) {
            throw new IllegalStateException("setPiece(byte,byte): square "+square+" is occupied.");
        }
        this.squares[square] = color;
    }

   /**
    * Poistaa nappulan annetusta ruudusta.
    * @param square    Mist‰ ruudusta nappula poistetaan? (<tt>0 <= square <= 23</tt>)
    * @exception ArrayIndexOutOfBoundsException  Jos ei ollut <tt>0 <= square <= 23</tt>.
    */
    public void removePiece(byte square) {
        this.squares[square] = BoardInfo.EMPTY;
    }

   /**
    * Siirt‰‰ laudalla olevan nappulan toiseen vapaaseen ruutuun.
    * @param fromSquare    Mist‰ ruudusta nappula siirret‰‰n?
    *                      (<tt>0 <= square <= 23</tt>)
    * @param toSquare      Mihin vapaaseen ruutuun nappula siirret‰‰n?
    *                      (<tt>0 <= square <= 23</tt>)
    * @exception ArrayIndexOutOfBoundsException    Jos ei ollut <tt>0 <= *square <= 23</tt>.
    * @exception IllegalArgumentException    Jos ruutu <tt>fromSquare</tt> oli tyhj‰.
    * @exception IllegalStateException    Jos ruutu <tt>toSquare</tt> oli varattu.
    */
    public void movePiece(byte fromSquare, byte toSquare) throws IllegalArgumentException,
                                                                 IllegalStateException {
        this.setPiece(toSquare, this.squares[fromSquare]);
        this.removePiece(fromSquare);
    }


   /**
    * Antaa myllylaudan v‰riarvon annetusta ruudusta.
    * @param square    Mink‰ ruudun v‰riarvoa kysyt‰‰n?
    *                  (<tt>0 <= square <= 23</tt>)
    * @return <ul><li><tt>BoardInfo.WHITE</tt>, jos ruudulla on valkoinen nappula.</li>
    *             <li><tt>BoardInfo.BLACK</tt>, jos ruudulla on musta nappula.</li>
    *             <li><tt>BoardInfo.EMPTY</tt>, jos ruutu on tyhj‰.</li></ul>
    * @exception ArrayIndexOutOfBoundsException Jos ei ollut <tt>0 <= square <= 23</tt>.
    */
    public byte get(byte square) {
        return this.squares[square];
    }

   /**
    * Palauttaa taulukoituna pelilaudan ruudut, joissa on
    * annettu v‰ri.
    * @param color Mink‰ v‰risi‰ nappuloita halutaan. Joko
    *              <tt>BoardInfo.WHITE</tt>, <tt>BoardInfo.BLACK</tt> tai
    *              <tt>BoardInfo.EMPTY</tt>.
    * return Taulukko ruuduista, joiden nappulan v‰ri oli <tt>color</tt>.
    * @exception IllegalArgumentException  Jos <tt>color</tt> ei ollut
    *              <tt>BoardInfo.WHITE</tt>, <tt>BoardInfo.BLACK</tt> eik‰
    *              <tt>BoardInfo.EMPTY</tt>.
    */
    public byte[] getColouredSquares(byte color) throws IllegalArgumentException {
        if ( (color != BoardInfo.BLACK) && (color != BoardInfo.WHITE) && (color != BoardInfo.EMPTY)) {
            throw new IllegalArgumentException("getSquares(byte): unknown square value:"+color);
        }

        // tmp-taulukko v‰liaikaiseen tallennukseen
        byte[] tmp = new byte[BoardInfo.SQUARES_ON_BOARD]; // 24 indeksi‰ riitt‰‰ varmasti
        int squareCounter = 0;
        for (int index=0; index < this.squares.length; index++) {
            if (this.squares[index] == color) {
                tmp[squareCounter] = (byte) index;
                squareCounter++;
            }
        }

        // Tiivistet‰‰n taulukko
        byte[] array = new byte[squareCounter];
        for (int index=0; index < squareCounter; index++) {
            array[index] = tmp[index];
        }
        return array;
    }



   /**
    * Ovatko annetut ruudut vierekk‰isi‰? Ruutu ei ole naapuri
    * itsens‰ kanssa.
    * @param square_1 Ensimm‰inen vertailtava ruutu (<tt>0 <= square_1 <= 23</tt>)
    * @param square_2 Toinen vertailtava ruutu (<tt>0 <= square_2 <= 23</tt>)
    * @return Ovatko ruudut vierekk‰isi‰?
    * @exception ArrayIndexOutOfBoundsException Jos ei ollut <tt>0 <= square_* <= 23</tt>.
    */
    public boolean areNeighbours(byte square_1, byte square_2) {
        byte[] neighbours = BoardInfo.getNeighbours(square_1);
        for (int index=0; index < neighbours.length; index++) {
            if (neighbours[index] == square_2) {
                return true;
            }
        }
        return false;
    }

   /**
    * Jos annettu siirto suoritettaisiin, syntyisikˆ uusi mylly?
    * Parametri <tt>color</tt> on tarpeellinen, sill‰
    * asetettavan nappulan v‰ri‰ ei voida tiet‰‰ alkupeliss‰.
    *
    * @param move Tarkkailtava siirto.
    * @param color Mink‰ v‰risi‰ myllyj‰ tarkkaillaan
    * @return Syntyikˆ siirron vaikutuksesta uusi mylly?
    * @exception IllegalArgumentException Jos v‰riarvo
    *            <tt>color</tt> ei ollut <tt>BoardInfo.WHITE</tt> eik‰
    *            <tt>BoardInfo.BLACK</tt> TAI jos parametri <tt>color</tt>
    *            ei t‰sm‰nnyt siirrett‰v‰n nappulan v‰riarvon kanssa.
    * @exception NullPointerException Jos <tt>move</tt> on <tt>null</tt>.
    */
    public boolean createsNewMill(Move move, byte color) throws IllegalArgumentException {
        if ( (color != BoardInfo.BLACK) && (color != BoardInfo.WHITE) ) {
            throw new IllegalArgumentException("createsNewMill(Move,byte): unknown color value:"+color);
        }
        if ( (move.FROM != Move.NOWHERE) && (this.squares[move.FROM] != color) ) {
            throw new IllegalArgumentException("createsNewMill(Move,byte): parameter color:"+
                                               color+" doesn't match the color of piece being"+
                                               "moved:"+this.squares[move.FROM]);
        }
        byte[][] millLines = BoardInfo.getMillLines(move.TO);
        byte[] millLine_1 = millLines[0];
        byte[] millLine_2 = millLines[1];

        // Sen lis‰ksi ett‰ myllylinjalla on oltava yhten‰inen v‰ri,
        // viereisist‰ ruuduista kumpikaan ei saa olla se josta nappula siirrettiin
        if ( ((this.squares[millLine_1[0]] == color) && (millLine_1[0] != move.FROM)) &&
             ((this.squares[millLine_1[1]] == color) && (millLine_1[1] != move.FROM)) ) {
                return true;
        }

        if ( ((this.squares[millLine_2[0]] == color) && (millLine_2[0] != move.FROM)) &&
             ((this.squares[millLine_2[1]] == color) && (millLine_2[1] != move.FROM)) ) {
                return true;
        }
        return false;

    }

   /**
    * Kuuluuko nappula johonkin myllyyn? Jos ruudussa ei ole nappulaa,
    * palautuu <tt>false</tt>.
    * @param square Mit‰ ruutua tutkitaan? (<tt>0 <= square <= 23</tt>)
    * @return Kuuluuko nappula johonkin myllyyn?
    * @exception ArrayIndexOutOfBoundsException Jos ei ollut <tt>0 <= square <= 23</tt>
    */
    public boolean isPartOfMill(byte square) {
        if (this.squares[square] == BoardInfo.EMPTY) {
            return false;
        }
        byte[][] millLines = BoardInfo.getMillLines(square);
        byte[] millLine_1 = millLines[0];
        byte[] millLine_2 = millLines[1];
        byte color = this.squares[square];

        return ( ((this.squares[millLine_1[0]] == color) && (this.squares[millLine_1[1]] == color)) ||
                 ((this.squares[millLine_2[0]] == color) && (this.squares[millLine_2[1]] == color))   );
    }

   /**
    * Ovatko kaikki annetun v‰rin nappulat myllyiss‰?
    * @param color Kumpaa v‰ri‰ tutkitaan: <tt>BoardInfo.WHITE</tt> vai
    *              <tt>BoardInfo.BLACK</tt>?
    * @return olivatko kaikki annetun v‰rin nappulat myllyiss‰?
    * @exception IllegalArgumentException Parametri <tt>color</tt> ei ollut
    *            <tt>BoardInfo.WHITE</tt> eik‰ <tt>BoardInfo.BLACK</tt>.
    */
    public boolean allPiecesInMills(byte color) throws IllegalArgumentException {
        if ( (color != BoardInfo.BLACK) && (color != BoardInfo.WHITE) ) {
            throw new IllegalArgumentException("allPiecesInMills(byte): unknown color value:"+color);
        }
        byte[] colouredSquares = this.getColouredSquares(color);
        for (int index=0; index < colouredSquares.length; index++) {
            if ( !this.isPartOfMill(colouredSquares[index]) ) {
                return false;
            }
        }
        return true;
    }

   /**
    * Ovatko kaikki annetun v‰rin nappulat jumissa (ei vapaita naapuriruutuja).
    * @param color Tutkittava v‰ri. Tulee olla joko <tt>BoardInfo.WHITE</tt>
    *              tai <tt>BoardInfo.BLACK</tt>.
    * @return Onko nappula jumissa?
    * @exception IllegalArgumentException Jos parametrin <tt>color</tt> arvo
    *            ei ollut <tt>BoardInfo.WHITE</tt> eik‰ <tt>BoardInfo.BLACK</tt>.
    *
    *
    */
    public boolean allPiecesJammed(byte color) throws IllegalArgumentException {
        if ( (color != BoardInfo.BLACK) && (color != BoardInfo.WHITE) ) {
            throw new IllegalArgumentException("allPiecesInMills(byte): unknown color value:"+color);
        }
        byte[] colouredSquares = this.getColouredSquares(color);
        for (int index=0; index < colouredSquares.length; index++) {
            if ( !this.pieceJammed(colouredSquares[index]) ) {
                return false;
            }
        }
        return true;
    }


   /**
    * Onko nappula jumissa, eli onko sill‰ vapaita naapuriruutuja.
    * @param square Tutkittava ruutu. Tulee olla <tt>0 <= square <= 23</tt>
    * @return Onko nappula jumissa?
    * @exception ArrayIndexOutOfBoundsException Jos ei ollut <tt>0 <= square <= 23</tt>.
    */
    private boolean pieceJammed(byte square) {
        byte[] neighbours = BoardInfo.getNeighbours(square);
        for (int index=0; index < neighbours.length; index++) {
            if (this.squares[neighbours[index]] == BoardInfo.EMPTY) {
                return false;
            }
        }
        return true;
    }


    /**
     * Palauttaa <tt>MillBoard</tt>-olion eli pelilaudan laajan
     * merkkijonoesityksen ruutunumeroineen.
     * @return <tt>MillBoard</tt>-olion laaja merkkijonoesitys
     */
    public String toString() {
        char[] squarecharacters = new char[this.squares.length];
        for (int squareIndex=0; squareIndex < this.squares.length; squareIndex++) {
            if (this.squares[squareIndex] == BoardInfo.EMPTY) {
                squarecharacters[squareIndex] = ' ';
            }
            else if (this.squares[squareIndex] == BoardInfo.WHITE) {
                squarecharacters[squareIndex] = 'w';
            }
            else if (this.squares[squareIndex] == BoardInfo.BLACK) {
                squarecharacters[squareIndex] = 'b';
            }
        }

        char[] s = squarecharacters;
        return "" +
"  "+s[ 0]+" --------- "+s[ 1]+" --------- "+s[ 2]                         + "                      0 --------- 1 --------- 2"+ "\n" +
"  |           |           |"                                              + "                      |           |           |"+ "\n" +
"  |   "+s[ 3]+" ----- "    +s[ 4]+" ----- "    +s[ 5]+"   |"              + "                      |   3 ----- 4 ----- 5   |"+ "\n" +
"  |   |       |       |   |"                                              + "                      |   |       |       |   |"+ "\n" +
"  |   |   "+s[ 6]+" - "+s[ 7]+" - "+s[ 8]+"   |   |"                      + "                      |   |   6 - 7 - 8   |   |"+ "\n" +
"  |   |   |       |   |   |"                                              + "                      |   |   |       |   |   |"+ "\n" +
"  "+s[ 9]+" - "+s[10]+" - "+s[11]+"       "+s[12]+" - "+s[13]+" - "+s[14] + "                      9 - 10- 11      12- 13- 14"+ "\n" +
"  |   |   |       |   |   |"                                              + "                      |   |   |       |   |   |"+ "\n" +
"  |   |   "+s[15]+" - "+s[16]+" - "+s[17]+"   |   |"                      + "                      |   |   15- 16- 17  |   |"+ "\n" +
"  |   |       |       |   |"                                              + "                      |   |       |       |   |"+ "\n" +
"  |   "+s[18]+" ----- "+s[19]+" ----- "+s[20]+"   |"                      + "                      |   18----- 19----- 20  |"+ "\n" +
"  |           |           |"                                              + "                      |           |           |"+ "\n" +
"  "+s[21]+" --------- "+s[22]+" --------- "+s[23]                         + "                      21--------- 22--------- 23"       ;

    }


    /** Testiohjelma*/
	public static void main(String[] args) {

	}
}
