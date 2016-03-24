/*
 * @(#)BoardInfo.java  5.2.2004
 *
 * Copyright 2004 Esa Junttila
 */


/**
 * Luokka tarjoaa <tt>MillBoard</tt>-olioon liittyvi‰ vakioarvoja ja
 * laskentaa tehostavia taulukoita.
 *
 * @version 5.2.2004
 * @author 	Esa Junttila
 */
public class BoardInfo{

    /** Pelilaudan vakioita*/
    public static final byte SQUARES_ON_BOARD = 24,
                             EMPTY = 0,
                             BLACK = 1,
                             WHITE = 2;

    /** Jokaisen <tt>MillBoard</tt>-olion ruudun naapuriruudut taulukoituna*/
    private static final byte[][] NEIGHBOUR_SQUARES =
              {    { 1, 9}, { 0, 2, 4}, { 1,14},
                   { 4,10}, { 1, 3, 5, 7}, { 4,13},
                   { 7,11}, { 4, 6, 8}, { 7,12},
 { 0,10,21}, { 3, 9,11,18}, { 6,10,15}, { 8,13,17}, { 5,12,14,20}, { 2,13,23},
                   {11,16}, {15,17,19}, {12,16},
                   {10,19}, {16,18,20,22}, {13,19},
                   { 9,22}, {19,21,23}, {14,22}      };


   /**
    * Palauttaa parametrina annetun ruudun kaikki vierusnaapurit.
    * @param square    Mink‰ ruudun naapureita haetaan?
    * @return byte[]-taulukossa kaikki annetun ruudun vierusnaapurit
    * @exception ArrayIndexOutOfBoundsException
    *                       Jos ei ole <tt>0 <= square <= 23</tt>
    */
    public static byte[] getNeighbours(byte square) {
        byte[] neighbours = new byte[NEIGHBOUR_SQUARES[square].length];
        for (byte neighbour=0; neighbour < neighbours.length; neighbour++) {
            neighbours[neighbour] = NEIGHBOUR_SQUARES[square][neighbour];
        }
        return neighbours;
    }


    /** Jokaisella ruudulla on taulukoituna kaksi myllylinjaa*/
    private static final Object[] MILL_LINES;


    /** Muodostaa RUUTUJEN_MYLLYLINJAT. Tuntuu v‰h‰n tyhm‰lt‰, mutta t‰m‰ on pakko hoitaa n‰in.*/
    private static Object[] createMillLines() {
        byte[][] square_00 = {{1,2},{9,21}},
                 square_01 = {{0,2},{4,7}},
                 square_02 = {{0,1},{14,23}},
                 square_03 = {{4,5},{10,18}},
                 square_04 = {{1,7},{3,5}},
                 square_05 = {{3,4},{13,20}},
                 square_06 = {{7,8},{11,15}},
                 square_07 = {{1,4},{6,8}},
                 square_08 = {{6,7},{12,17}},
                 square_09 = {{0,21},{10,11}},
                 square_10 = {{3,18},{9,11}},
                 square_11 = {{6,15},{9,10}},
                 square_12 = {{8,17},{13,14}},
                 square_13 = {{5,20},{12,14}},
                 square_14 = {{2,23},{12,13}},
                 square_15 = {{6,11},{16,17}},
                 square_16 = {{15,17},{19,22}},
                 square_17 = {{8,12},{15,16}},
                 square_18 = {{3,10},{19,20}},
                 square_19 = {{16,22},{18,20}},
                 square_20 = {{5,13},{18,19}},
                 square_21 = {{0,9},{22,23}},
                 square_22 = {{16,19},{21,23}},
                 square_23 = {{2,14},{21,22}};

        Object[] millLines = new Object[24];
        millLines[ 0] = square_00;
        millLines[ 1] = square_01;
        millLines[ 2] = square_02;
        millLines[ 3] = square_03;
        millLines[ 4] = square_04;
        millLines[ 5] = square_05;
        millLines[ 6] = square_06;
        millLines[ 7] = square_07;
        millLines[ 8] = square_08;
        millLines[ 9] = square_09;
        millLines[10] = square_10;
        millLines[11] = square_11;
        millLines[12] = square_12;
        millLines[13] = square_13;
        millLines[14] = square_14;
        millLines[15] = square_15;
        millLines[16] = square_16;
        millLines[17] = square_17;
        millLines[18] = square_18;
        millLines[19] = square_19;
        millLines[20] = square_20;
        millLines[21] = square_21;
        millLines[22] = square_22;
        millLines[23] = square_23;

        return millLines;
    }

    // Kun luokka ladataan, asetetaan Myllylinjat muuttujaan
    static {
        MILL_LINES = createMillLines();
    }

   /**
    * Palauttaa parametrina annetun ruudun kaksi myllylinjaa
    * ruutuineen. Ensimm‰isen myllylinjan ruudut ovat
    * <p>
    * <tt>square</tt>, <tt>byte[0][0]</tt> ja <tt>byte[0][1]</tt>
    * <p>
    * Toisen myllylinjan ruudut ovat
    * <p>
    * <tt>square</tt>, <tt>byte[1][0]</tt> ja <tt>byte[1][1]</tt>
    *
    * @param square    Mink‰ ruudun myllylinjat halutaan
    * @return    byte[][]-taulukko (koko: 2x2), joka sis‰lt‰‰ myllylinjojen muut ruudut
    * @exception ArrayIndexOutOfBoundsException
    *                       Jos ei ole <tt>0 <= square <= 23</tt>
    */
    public static byte[][] getMillLines(byte square) {
        byte[][] millLines = {
            { ((byte[][])MILL_LINES[square])[0][0], ((byte[][])MILL_LINES[square])[0][1]},
            { ((byte[][])MILL_LINES[square])[1][0], ((byte[][])MILL_LINES[square])[1][1]}
                             };
        return millLines;
    }



    /** Testiohjelma*/
	public static void main(String[] args) {
        byte[] neighbours = getNeighbours((byte)5);
        for (int neighbour=0; neighbour < neighbours.length; neighbour++) {
            System.out.print(neighbours[neighbour]+", ");
        }
        System.out.println( " --> 4, 13 OK");

        byte[][] millLines = getMillLines((byte)5);
        for (int millLine=0; millLine < 2; millLine++) {
            for (int index=0; index < 2; index++) {
                System.out.print(millLines[millLine][index]+",");
            }
            System.out.println();
        }
        System.out.println( " --> (3,4) && (13,20) OK");

    }
}
