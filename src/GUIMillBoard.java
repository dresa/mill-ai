import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observer;
import java.util.Observable;

public class GUIMillBoard extends Canvas {
    private static final Color BOARD_COLOR = Color.white,
                               BORDER_COLOR = Color.blue,
                               LINE_COLOR = Color.black,
                               LEGAL_COLOR = Color.green,
                               SELECTED_COLOR = Color.blue,
                               SELECTED_COLOR_WHITE = new Color(0, 255, 255),
                               SELECTED_COLOR_BLACK = new Color(0, 255, 255),
                               WHITE_OUTER_COLOR = new Color(180,180,180),
                               WHITE_INNER_COLOR = Color.white,
                               BLACK_OUTER_COLOR = Color.black,
                               BLACK_INNER_COLOR = new Color(150,150,150);

    // suhteessa perusmittoihin (100 = width/10 ja height/10)
    private static final int RELATIVE_SQUARE_DIAMETER = 20,
                             RELATIVE_PIECE_DIAMETER = 60,
                             CIRCLE_THICKNESS = 3,
                             X = 0,
                             Y = 1;
    private static final byte NONE = -1;


    private int[][] squareCoordinates;
    private Dimension previousSize;
    private BufferedImage bufferImage;
    private int widthUnit;
    private int heightUnit;
    private MillGame game;
    private byte selectedFromSquare;
    private byte selectedToSquare;
    private boolean highlightLegalSquares;
    private ObservableWrapper moveInformation;


    public GUIMillBoard(MillGame game) {
        if (game == null) {
            throw new IllegalArgumentException("GUIMillBoard(MillGame): Parameter 'MillGame' can¨'t be null.");
        }
        this.squareCoordinates = new int[24][2];
        this.previousSize = new Dimension(0, 0);
        this.bufferImage = null;
        this.game = game;
        this.selectedFromSquare = NONE;
        this.selectedToSquare = NONE;
        this.highlightLegalSquares = false;

        this.moveInformation = new ObservableWrapper();

        // tulkitsee klikkaukset ruuduiksi ja tiedottaa klikatun ruudun kuuntelijalle
        super.addMouseListener( new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int coordX = e.getX();
                    int coordY = e.getY();

                    // Minkä ruudun läheisyydessä hiirtä klikattiin?
                    // Lasketaan ruudun kohdalta nappulan halkaisijaa vastaava
                    // ympyrä ja katsotaan, onko klikattu piste ympyrän sisällä.
                    // = onko _nappulaa_ klikattu?
                    for (int square = 0; square < squareCoordinates.length; square++) {
                        int deltaX = Math.abs(coordX - squareCoordinates[square][X]);
                        int deltaY = Math.abs(coordY - squareCoordinates[square][Y]);

                        if ( Math.sqrt( deltaX*deltaX + deltaY*deltaY) <=
                                RELATIVE_PIECE_DIAMETER * widthUnit/200) {
                            moveInformation.setChanged();
                            moveInformation.notifyObservers(new Byte((byte)square)); // nollaa "changed"-flagin
                            return;
                        }
                    }
                }
            }
        );

    }

    public void addObserver(Observer observer) throws IllegalArgumentException {
        if (observer == null) {
            throw new IllegalArgumentException("GUIMillBoard can't be created without a non-null Observer.");
        }
        this.moveInformation.addObserver(observer);
    }

    public void setHighlightLegalSquares(boolean active) {
        this.highlightLegalSquares = active;
    }

    public void setGame(MillGame game) {
        if (game == null) {
            throw new IllegalArgumentException("GUIMillBoard(MillGame): Parameter 'MillGame' can¨'t be null.");
        }
        this.game = game;
    }

// Kädestä asetettava nappula aiheuttaa sinisen renkaan riippumatta
// asetettavan nappulan väristä. Millaiseksi selectedPiece tulee värjätä,
// voidaa tarkistaa whitePieces ja blackPieces -taulukoista.
    public void setSelectedFromSquare(byte square) throws IllegalArgumentException {
        if ( (square < 0 || 23 < square) && square != NONE) {
            throw new IllegalArgumentException("setSelectedFromSquare(int): illegal square value:"+square);
        }
        this.selectedFromSquare = square;
    }

    public void clearSelectedFromSquare() {
        this.selectedFromSquare = NONE;
    }

    public void setSelectedToSquare(byte square) throws IllegalArgumentException {
        if ( (square < 0 || 23 < square) && square != NONE) {
            throw new IllegalArgumentException("setSelectedToSquare(int): illegal square value:"+square);
        }
        this.selectedToSquare = square;
    }

    public void clearSelectedToSquare() {
        this.selectedToSquare = NONE;
    }


    public void update(Graphics g) {
        this.paint(g);
    }

    public void paint(Graphics g) {
//System.out.println("update/paint(Graphics g)");
        Dimension size = this.getSize();
        if ( !size.equals(this.previousSize) ) {
            this.widthUnit = super.getWidth()/10;
            this.heightUnit = super.getHeight()/10;
            this.calculateSquareCoordinates();
            this.previousSize = size;
        }

        // tarvitaanko uusi puskuri?
        if (bufferImage == null ||
            size.width > this.bufferImage.getWidth() ||
            size.height > this.bufferImage.getHeight() ) {
                this.bufferImage = (BufferedImage)super.createImage(size.width, size.height);
        }

        Graphics gBuffer = this.bufferImage.getGraphics();
        gBuffer.setClip(g.getClip());

        // Piirretään puskuriin:
        gBuffer.setColor(super.getBackground());
        gBuffer.fillRect(0, 0, size.width, size.height);
        this.drawBoardBorder(gBuffer);
        this.drawLinesAndSquares(gBuffer);
        this.drawHandPieces(gBuffer);
        this.drawPieces(gBuffer);
        this.drawLegalMoves(gBuffer);
        this.drawSelectedFrom(gBuffer);
        this.drawSelectedTo(gBuffer);

        // Piirretään puskurin sisältö kerralla ruudulle.
        g.drawImage(bufferImage, 0, 0, this);
    }


    private void drawBoardBorder(Graphics g) {
        int boardCornerX = 1 * this.widthUnit;
        int boardCornerY = 1 * this.heightUnit;
        int boardWidth = 8 * this.widthUnit;
        int boardHeight = 8 * this.heightUnit;

        // reunan ulkoreuna
        g.setColor(Color.black);
        g.drawRect(boardCornerX, boardCornerY, boardWidth, boardHeight);

        //reunaa
        g.setColor(BORDER_COLOR);
        g.fillRect(boardCornerX + 1, boardCornerY + 1, boardWidth - 2, boardHeight - 2);

        // reunan sisäpinta
        g.setColor(Color.black);
        g.drawRect(boardCornerX + 7, boardCornerY + 7, boardWidth - 14, boardHeight - 14);

        // itse lauta
        g.setColor(BOARD_COLOR);
        g.fillRect(boardCornerX + 8, boardCornerY + 8, boardWidth - 16, boardHeight - 16);
    }

    private void drawLinesAndSquares(Graphics g) {
        g.setColor(LINE_COLOR);

        // ulkokehä
        g.drawRect(squareCoordinates[0][X], squareCoordinates[0][Y],
                   squareCoordinates[2][X] - squareCoordinates[0][X],
                   squareCoordinates[21][Y] - squareCoordinates[0][Y] ); //x,y,width, height
        // välikehä
        g.drawRect(squareCoordinates[3][X], squareCoordinates[3][Y],
                   squareCoordinates[5][X] - squareCoordinates[3][X],
                   squareCoordinates[18][Y] - squareCoordinates[3][Y] ); //x,y,width, height

        // sisäkehä
        g.drawRect(squareCoordinates[6][X], squareCoordinates[6][Y],
                   squareCoordinates[8][X] - squareCoordinates[6][X],
                   squareCoordinates[15][Y] - squareCoordinates[6][Y] ); //x,y,width, height

        // viiva 1 -- 7
        g.drawLine(squareCoordinates[1][X], squareCoordinates[1][Y],
                   squareCoordinates[7][X], squareCoordinates[7][Y] ); //x1,y1,x2,y2
        // viiva 14 -- 12
        g.drawLine(squareCoordinates[14][X], squareCoordinates[14][Y],
                   squareCoordinates[12][X], squareCoordinates[12][Y] ); //x1,y1,x2,y2
        // viiva 22 -- 16
        g.drawLine(squareCoordinates[22][X], squareCoordinates[22][Y],
                   squareCoordinates[16][X], squareCoordinates[16][Y] ); //x1,y1,x2,y2
        // viiva 9 -- 11
        g.drawLine(squareCoordinates[9][X], squareCoordinates[9][Y],
                   squareCoordinates[11][X], squareCoordinates[11][Y] ); //x1,y1,x2,y2

        // Ympyrät ruutupaikoille:
        int width = RELATIVE_SQUARE_DIAMETER * this.widthUnit/100;
        int height = RELATIVE_SQUARE_DIAMETER * this.heightUnit/100;
        for (int square = 0; square < squareCoordinates.length; square++) {
             g.fillOval(squareCoordinates[square][X] - width/2,
                        squareCoordinates[square][Y] - height/2,
                        width,
                        height);
        }
    }

    private void drawHandPieces(Graphics g) {
        int whiteHeight = this.heightUnit/2;
        int blackHeight = super.getHeight() - this.heightUnit/2;

        int whitePiecesInHand = this.game.getWhitePiecesInHand();
        int blackPiecesInHand = this.game.getBlackPiecesInHand();

        this.cascadePieces(g, whitePiecesInHand, whiteHeight,
                           WHITE_OUTER_COLOR, WHITE_INNER_COLOR);
        this.cascadePieces(g, blackPiecesInHand, blackHeight,
                           BLACK_OUTER_COLOR, BLACK_INNER_COLOR);
    }
    private void cascadePieces(Graphics g, int pieces, int coordY, Color outer, Color inner) {
        for (int pieceIndex = 1; pieceIndex <= pieces; pieceIndex++) {
            draw3DPiece(g, pieceIndex * this.getWidth()/(pieces + 1), coordY, outer, inner);
        }
    }

    private void drawPieces(Graphics g) {
        MillBoard board = this.game.getMillBoard();

        byte[] whiteSquares = board.getColouredSquares(BoardInfo.WHITE);
        for (int pieceIndex = 0; pieceIndex < whiteSquares.length; pieceIndex++) {
            this.draw3DPiece(g,
                             this.squareCoordinates[whiteSquares[pieceIndex]][X],
                             this.squareCoordinates[whiteSquares[pieceIndex]][Y],
                             WHITE_OUTER_COLOR,
                             WHITE_INNER_COLOR);
        }

        byte[] blackSquares = board.getColouredSquares(BoardInfo.BLACK);
        for (int pieceIndex = 0; pieceIndex < blackSquares.length; pieceIndex++) {
            this.draw3DPiece(g,
                             this.squareCoordinates[blackSquares[pieceIndex]][X],
                             this.squareCoordinates[blackSquares[pieceIndex]][Y],
                             BLACK_OUTER_COLOR,
                             BLACK_INNER_COLOR);
        }
    }

    private void drawLegalMoves(Graphics g) {
        if ( !this.highlightLegalSquares) {
            return;
        }

        byte[] legalSquares;
        if (this.game.getGameState() == MillGame.PHASE_GAME_OVER) {
            return;
        }
        else if (this.game.getGameState() == MillGame.PHASE_BEGINNING) {
            if (this.selectedToSquare == NONE) {
                legalSquares = LegalMoves.getAllLegalTOSquares(this.game,
                                                                   this.selectedFromSquare);
            }
            else {
                legalSquares = LegalMoves.getAllLegalREMOVESquares(this.game,
                                                                   this.selectedFromSquare,
                                                                   this.selectedToSquare);
            }
        }
        else { // MIDGAME JA ENDGAME
            if (this.selectedFromSquare == NONE) {
                legalSquares = LegalMoves.getAllLegalFROMSquares(this.game);
            }
            else if (this.selectedToSquare == NONE) {
                legalSquares = LegalMoves.getAllLegalTOSquares(this.game,
                                                                   this.selectedFromSquare);
            }
            else {
                legalSquares = LegalMoves.getAllLegalREMOVESquares(this.game,
                                                                   this.selectedFromSquare,
                                                                   this.selectedToSquare);
            }
        }

        for (int squareIndex = 0; squareIndex < legalSquares.length; squareIndex++) {
            this.drawSquareCircle(g, legalSquares[squareIndex], LEGAL_COLOR);
        }
    }


    private void drawSelectedFrom(Graphics g) {
        if (this.selectedFromSquare == NONE) {
            return;
        }
        MillBoard board = this.game.getMillBoard();

        // valittu nappula oli valkoinen
        if (board.get( (byte)this.selectedFromSquare) == BoardInfo.WHITE) {
            this.draw3DPiece(g,
                             this.squareCoordinates[this.selectedFromSquare][X],
                             this.squareCoordinates[this.selectedFromSquare][Y],
                             WHITE_OUTER_COLOR,
                             SELECTED_COLOR_WHITE);
        }
        else if (board.get( (byte)this.selectedFromSquare) == BoardInfo.BLACK) {
            this.draw3DPiece(g,
                             this.squareCoordinates[this.selectedFromSquare][X],
                             this.squareCoordinates[this.selectedFromSquare][Y],
                             BLACK_OUTER_COLOR,
                             SELECTED_COLOR_BLACK);
        }
        else {
            throw new IllegalStateException("this.selectedFromSquare:"+this.selectedFromSquare+" contains"+
                                            " no white nor black piece nor has value of "+NONE+".");
        }
    }

    private void drawSelectedTo(Graphics g) {
        if (this.selectedToSquare == NONE) {
            return;
        }
        this.drawSquareCircle(g, this.selectedToSquare, SELECTED_COLOR);
    }


   // Piirretään tosi monta ympyrää pienenevällä säteellä.
   // coordX ja coordY ovat nappulan keskipisteen koordinaatit.
    private void draw3DPiece(Graphics g,
                             int coordX,
                             int coordY,
                             Color outerColor,
                             Color innerColor) {

        int width = RELATIVE_PIECE_DIAMETER * this.widthUnit/100;
        int height = RELATIVE_PIECE_DIAMETER * this.heightUnit/100;

        int widRadius = width/2;
        int heiRadius = height/2;
        int greater = 0;
        if (widRadius >= heiRadius) {
              greater = widRadius;
        }
        else {
            greater = heiRadius;
        }

        int red,
            green,
            blue;
        // näillä muuttuvilla säteillä piirretään useita täytettyjä ympyröitä
        int newWidthRadius;
        int newHeightRadius;
        for (int counter = 0; counter <= greater; counter++) {
             newWidthRadius = widRadius - counter * widRadius/greater; // vähennys on enintään 'counter'
             newHeightRadius = heiRadius - counter * heiRadius/greater;// vähennys on enintään 'counter'

             // värien lineaarinen interpolointi
             red = outerColor.getRed() + counter*(innerColor.getRed()-outerColor.getRed())/greater;
             green = outerColor.getGreen() + counter*(innerColor.getGreen()-outerColor.getGreen())/greater;
             blue = outerColor.getBlue() + counter*(innerColor.getBlue()-outerColor.getBlue())/greater;
             g.setColor(new Color(red,green,blue));

             g.fillOval(coordX - newWidthRadius,
                        coordY - newHeightRadius,
                        2 * newWidthRadius,
                        2 * newHeightRadius);
        }
    }


    public void drawCircle(int coordX, int coordY, Color circleColor) throws IllegalArgumentException {
        if (circleColor == null) {
            throw new IllegalArgumentException("drawCircle(int,int,Color): Illegal Parameter value: Color == null");
        }
        Graphics g = super.getGraphics();
        if (g == null) {
            return;
        }
        this.drawCircle(g, coordX, coordY, circleColor);
    }


    // tällä voidaan animoida nappulan liikkumista.
    // (piirretään vuoron perään renkaita ruutujen välille)
    private void drawCircle(Graphics g, int coordX, int coordY, Color circleColor) {
        int width = RELATIVE_PIECE_DIAMETER * this.widthUnit/100;
        int height = RELATIVE_PIECE_DIAMETER * this.heightUnit/100;

        g.setColor(circleColor);
        for (int circleNumber = 0; circleNumber < CIRCLE_THICKNESS; circleNumber++) {
            g.drawOval(coordX - width/2 - circleNumber,
                       coordY - height/2 - circleNumber,
                       width + 2 * circleNumber,
                       height + 2 * circleNumber);

        }
    }


    public void drawSquareCircle(int squareNumber, Color circleColor) throws IllegalArgumentException,
                                                                             ArrayIndexOutOfBoundsException {
        if (circleColor == null) {
            throw new IllegalArgumentException("drawSquareCircle(int,Color): Illegal Parameter value: Color == null");
        }
        Graphics g = super.getGraphics();
        if (g == null) {
            return;
        }
        this.drawSquareCircle(g, squareNumber, circleColor);
    }


    // Rengas sopii aina täsmälleen nappulan ympärille jos se piirretään ruutuun!
    private void drawSquareCircle(Graphics g, int squareNumber, Color circleColor) {
        this.drawCircle(g,
                        squareCoordinates[squareNumber][X],
                        squareCoordinates[squareNumber][Y],
                        circleColor);
    }

    // Palauttaa renkaiden koordinaatteja parametrina annetun määrän.
    // Renkaat kulkevat annetusta lähtöruudusta annettuun maaliruutuun,
    // joten vasta arvo 3 palauttaa ruutujen välissä olevia renkaita.
    // mahdollistaa renkaiden animoinnin.
    // nullpointerexception
    public int[][] getCircleCoordinatesBetweenSquares(int circles, int fromSquare, int toSquare)
                                                            throws IllegalArgumentException,
                                                                   ArrayIndexOutOfBoundsException {
        if (circles < 2) {
            throw new IllegalArgumentException("Parameter circle:"+circles+" must be at least 2");
        }
        int[][] coordinates = new int[circles][2]; // x ja y
        int startX = this.squareCoordinates[fromSquare][X];
        int startY = this.squareCoordinates[fromSquare][Y];
        int endX = this.squareCoordinates[toSquare][X];
        int endY = this.squareCoordinates[toSquare][Y];

        for (int circleIndex = 0; circleIndex < circles; circleIndex++) {
            coordinates[circleIndex][X] = startX + circleIndex * (endX - startX) / (circles -1);
            coordinates[circleIndex][Y] = startY + circleIndex * (endY - startY) / (circles -1);
        }
        return coordinates;
    }


    private void calculateSquareCoordinates() {
        this.squareCoordinates[0][X] = 2 * this.widthUnit;
        this.squareCoordinates[0][Y] = 2 * this.heightUnit;
        this.squareCoordinates[1][X] = 5 * this.widthUnit;
        this.squareCoordinates[1][Y] = 2 * this.heightUnit;
        this.squareCoordinates[2][X] = 8 * this.widthUnit;
        this.squareCoordinates[2][Y] = 2 * this.heightUnit;
        this.squareCoordinates[3][X] = 3 * this.widthUnit;
        this.squareCoordinates[3][Y] = 3 * this.heightUnit;
        this.squareCoordinates[4][X] = 5 * this.widthUnit;
        this.squareCoordinates[4][Y] = 3 * this.heightUnit;
        this.squareCoordinates[5][X] = 7 * this.widthUnit;
        this.squareCoordinates[5][Y] = 3 * this.heightUnit;
        this.squareCoordinates[6][X] = 4 * this.widthUnit;
        this.squareCoordinates[6][Y] = 4 * this.heightUnit;
        this.squareCoordinates[7][X] = 5 * this.widthUnit;
        this.squareCoordinates[7][Y] = 4 * this.heightUnit;
        this.squareCoordinates[8][X] = 6 * this.widthUnit;
        this.squareCoordinates[8][Y] = 4 * this.heightUnit;
        this.squareCoordinates[9][X] = 2 * this.widthUnit;
        this.squareCoordinates[9][Y] = 5 * this.heightUnit;
        this.squareCoordinates[10][X] = 3 * this.widthUnit;
        this.squareCoordinates[10][Y] = 5 * this.heightUnit;
        this.squareCoordinates[11][X] = 4 * this.widthUnit;
        this.squareCoordinates[11][Y] = 5 * this.heightUnit;
        this.squareCoordinates[12][X] = 6 * this.widthUnit;
        this.squareCoordinates[12][Y] = 5 * this.heightUnit;
        this.squareCoordinates[13][X] = 7 * this.widthUnit;
        this.squareCoordinates[13][Y] = 5 * this.heightUnit;
        this.squareCoordinates[14][X] = 8 * this.widthUnit;
        this.squareCoordinates[14][Y] = 5 * this.heightUnit;
        this.squareCoordinates[15][X] = 4 * this.widthUnit;
        this.squareCoordinates[15][Y] = 6 * this.heightUnit;
        this.squareCoordinates[16][X] = 5 * this.widthUnit;
        this.squareCoordinates[16][Y] = 6 * this.heightUnit;
        this.squareCoordinates[17][X] = 6 * this.widthUnit;
        this.squareCoordinates[17][Y] = 6 * this.heightUnit;
        this.squareCoordinates[18][X] = 3 * this.widthUnit;
        this.squareCoordinates[18][Y] = 7 * this.heightUnit;
        this.squareCoordinates[19][X] = 5 * this.widthUnit;
        this.squareCoordinates[19][Y] = 7 * this.heightUnit;
        this.squareCoordinates[20][X] = 7 * this.widthUnit;
        this.squareCoordinates[20][Y] = 7 * this.heightUnit;
        this.squareCoordinates[21][X] = 2 * this.widthUnit;
        this.squareCoordinates[21][Y] = 8 * this.heightUnit;
        this.squareCoordinates[22][X] = 5 * this.widthUnit;
        this.squareCoordinates[22][Y] = 8 * this.heightUnit;
        this.squareCoordinates[23][X] = 8 * this.widthUnit;
        this.squareCoordinates[23][Y] = 8 * this.heightUnit;
    }

/*
    private boolean containsSquare(byte[] array, int square) {
        if (array == null) {
            return false;
        }
        for (int index = 0; index < array.length; index++) {
            if (array[index] == square) {
                return true;
            }
        }
        return false;
    }
*/




    private class ObservableWrapper extends Observable {
        protected void setChanged() {
            super.setChanged();
        }
    }

}
