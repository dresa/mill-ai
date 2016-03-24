/*
 * @(#)Move.java  5.2.2004
 *
 * Copyright 2004 Esa Junttila
 */


/**
 * Luokan <tt>Move</tt> ilmentym‰t kuvaavat Myllyn mahdollisia
 * siirtoja. Myllyn jokainen siirto toteutetaan t‰m‰n luokan ilmentymien avulla.
 *
 * @version 5.2.2004
 * @author 	Esa Junttila
 */
public class Move{

    public static final byte NOWHERE = -1;

    /** Mit‰ nappulaa siirret‰‰n? */
    public final byte FROM;

    /** Minne asetetaan nappula? */
    public final byte TO;

    /** Poistetaanko jostakin nappula? */
    public final byte REMOVE;

    /** Ei kutsuta koskaan*/
    private Move() { this.FROM=0; this.TO=0; this.REMOVE=0; }

    /**
     * Luo uuden myllypelin siirron. Parametrit ovat kaikki myllylaudan
     * sallittuja ruutuja eli v‰lilt‰ 0--23. Jos siirrosta puuttuu esim.
     * 'from'-osa, parametriksi asetetaan <tt>Move.NOWHERE</tt>. Parametri
     * 'to' ei hyv‰ksy arvokseen <tt>Move.NOWHERE</tt>.
     *
     * @param from    Mist‰ ruudusta nappula siirret‰‰n pois? (<tt>Move.NOWHERE</tt>
     *                = nappula ilmaantuu tyhj‰st‰, kuten pelin alkuvaiheessa)
     * @param to      Mihin ruutuun uusi/siirretty nappula asetetaan? (Jokaisessa
     *                siirrossa jokin nappula asetetaan johonkin ruutuun!)
     * @param remove  Poistetaanko jonkin ruudun nappula kokonaan pelist‰?
     *                (<tt>Move.NOWHERE</tt> = nappuloita ei poisteta)
     * @exception IllegalArgumentException
     *                 Jos parametrien arvot eiv‰t olleet v‰lill‰ 0--23 tai
     *                 'to' -parametriksi yritettiin antaa <tt>Move.NOWHERE</tt>.
     */
    public Move(byte from, byte to, byte remove) throws IllegalArgumentException {
        if (from != NOWHERE && !(0<=from && from<=23) )
            throw new IllegalArgumentException("public Move(from,to,remove): bad argument 'from': " + from);
        if ( !(0<=to && to<=23) )
            throw new IllegalArgumentException("public Move(from,to,remove): bad argument 'to': " + to);
        if (remove != NOWHERE && !(0<=remove && remove<=23) )
            throw new IllegalArgumentException("public Move(from,to,remove): bad argument 'remove': " + remove);

        this.FROM = from;
        this.TO = to;
        this.REMOVE = remove;
    }

    /**
     * Katso <tt>Move(byte, byte, byte)</tt>
     */
    public Move(int from, int to, int remove) throws IllegalArgumentException {
        this((byte)from, (byte)to, (byte)remove);
    }

    /**
     * Luo <tt>Move</tt>-olion merkkijonoesityksen muodossa
     * <tt>(from,to,remove)</tt>, jossa -1 tarkoittaa <tt>NOWHERE</tt>-ruutua.
     *
     * @return  <tt>Move</tt>-olion merkkijonoesitys
     */
    public String toString() {
         return "("+this.FROM+","+this.TO+","+REMOVE+")";
    }

    /** Testiohjelma */
    public static void main(String[] args) {
        Move move_1;
        Move move_2;
        Move move_3;
        try {
            move_1 = new Move(1,2,3);
            System.out.println(move_1+" --> (1,2,3) OK");
            move_2 = new Move(Move.NOWHERE, 2, Move.NOWHERE);
            System.out.println(move_2+" --> (-1,2,-1) OK");
            move_3 = new Move(7, Move.NOWHERE, 9);
        }
        catch (IllegalArgumentException e) {
            System.out.println(e+", OK!");
        }
        //System.out.println(move_3+" OK?");    // K‰‰nt‰j‰ ei p‰‰st‰ l‰pi! Hyv‰.
    }
}
