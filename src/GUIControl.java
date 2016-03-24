import java.util.Observer;
import java.util.Observable;

public class GUIControl implements Observer {
    private MillGame game;
    private GUIMillBoard guiBoard;
    private GUI gui;
    private GUIPlayer whitePlayer,
                      blackPlayer;

    private MoveParser moveParser;
    private MillAI ai;

    private boolean highlightLegalSquares;
    private boolean gameRunning;
    private boolean thinking;
    private Thread infoThread;
    private Thread cpuThread;
    private Thread cpuMatchThread;

    public GUIControl(GUIMillBoard guiBoard, GUI gui) {
        if (guiBoard == null) {
            throw new IllegalArgumentException("GUIControl(GUIMillBoard,GUI): Parameter 'GUIMillBoard' can't be null.");
        }
        if (gui == null) {
            throw new IllegalArgumentException("GUIControl(GUIMillBoard,GUI): Parameter 'GUI' can't be null.");
        }
        this.game = null;
        this.guiBoard = guiBoard;
        this.gui = gui;
        this.whitePlayer = null;
        this.blackPlayer = null;

        this.moveParser = new MoveParser(this.guiBoard);
        this.moveParser.addObserver(this);
        this.ai = new MillAI();

        this.highlightLegalSquares = true;
        this.gameRunning = false;
        this.thinking = false;
        this.infoThread = null;
        this.cpuThread = null;
        this.cpuMatchThread = null;
    }

    public void setHighlightLegalSquares(boolean value) {
        if (this.highlightLegalSquares != value) {
            this.highlightLegalSquares = value;

            if (this.gameRunning && !this.CPUIsActive()) {
                this.guiBoard.setHighlightLegalSquares(value);
                this.guiBoard.repaint();    
            }
        }
    }

    public void newGame(MillGame game, GUIPlayer whitePlayer, GUIPlayer blackPlayer) {
        this.stopGame();
        this.game = game;
        this.continueGame(whitePlayer, blackPlayer);
    }

    public void continueGame(GUIPlayer whitePlayer, GUIPlayer blackPlayer) {

        // Continue-Stopia r‰mpytt‰m‰ll‰ saadaan useita kutsuja
        // vaikka laskenta on k‰ynniss‰. Silloin olimuutujiin
        // ehdit‰‰n asettaa uudet s‰ikeet ja interrupt()-operaatio
        // kohdistuukin uusiin eik‰ vanhoihin s‰ikeisiin! Boolean-arvo
        // thinking est‰‰ t‰m‰n! Jee.
        if (this.thinking) {
            System.out.println("Estetty!");
            return;
        }
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;

        if (this.game.getGameState() == MillGame.PHASE_GAME_OVER) {
            System.out.println("Can't continue: Game is over");
            return;
        }

        this.gameRunning = true;
        this.moveParser.setGame(this.game);
        this.guiBoard.setGame(this.game);
        this.setPlayerChoicesActive(false);
        this.guiBoard.setHighlightLegalSquares(this.highlightLegalSquares);
        this.setActivePlayer();

// ########
        // kone vs. kone
        if (whitePlayer.getPlayerChoice() == GUIPlayer.CPU &&
            blackPlayer.getPlayerChoice() == GUIPlayer.CPU) {
                this.moveParser.setClickable(false);
                this.cpuMatchThread = new GUIControl.CPUMatchThread();
                this.cpuMatchThread.start();
        }
        else {
            // (ihminen vs. ihminen) TAI (kone vs. ihminen) TAI (ihminen vs. kone)
            if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) { // valkoinen aloittaa
                if (this.whitePlayer.getPlayerChoice() == GUIPlayer.CPU) { // tietokone aloittaa
                    this.computerMoves();
                    this.moveParser.setPlayer(this.blackPlayer);
                }
                else {
                    this.moveParser.setPlayer(this.whitePlayer);
                    this.moveParser.setClickable(true);
                }
            }
            else { // musta aloittaa
                if (this.blackPlayer.getPlayerChoice() == GUIPlayer.CPU) { // tietokone aloittaa
                    this.computerMoves();
                    this.moveParser.setPlayer(this.whitePlayer);
                }
                else {
                    this.moveParser.setPlayer(this.blackPlayer);
                    this.moveParser.setClickable(true);
                }
            }
        }
        this.guiBoard.repaint();
        this.gui.refreshButtons();
    }

    public void stopGame() {
        this.gameRunning = false;
        this.moveParser.setClickable(false);
        this.guiBoard.setHighlightLegalSquares(false);
        this.clearActivePlayer();
        this.setPlayerChoicesActive(true);
        
        this.ai.stopSearch();
        if (this.infoThread != null)
            this.infoThread.interrupt();
        if (this.cpuThread != null)
            this.cpuThread.interrupt();
        if (this.cpuMatchThread != null)
            this.cpuMatchThread.interrupt();

        this.gui.refreshButtons();
        this.guiBoard.repaint();
    }

    public void undo() {
        this.stopGame();
        this.moveParser.clearSelections();
        this.game.undo();
        this.guiBoard.repaint();
        this.gui.refreshButtons();
    }

    public void redo() {
        this.stopGame();
        this.moveParser.clearSelections();
        this.game.redo();
        this.guiBoard.repaint();
        this.gui.refreshButtons();
    }

    public boolean gameIsRunning() {
        return this.gameRunning;
    }


    public void update(Observable o, Object move) {
        byte activePlayer = this.game.getActivePlayer();
        if ( this.CPUIsActive() ) {
            return; // koneen vuoro
        }

        boolean victory = this.game.makeMove( (Move)move );
        GUIControl.this.gui.refreshButtons(); // redo ei ainakaan ole en‰‰ voimassa
        if (victory) { // voitto?
            this.victory();
        }
        else {
            if ( this.CPUIsActive()) { // siirt‰‰kˆ kone seuraavaksi?
                this.computerMoves();
            }
            else { // ihmisten v‰linen peli
                this.setActivePlayer();
                if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) {
                    this.moveParser.setPlayer(this.whitePlayer);
                }
                else {
                    this.moveParser.setPlayer(this.blackPlayer);
                }
            }
        }
    }

    private void computerMoves() {
        this.moveParser.setClickable(false);
        this.cpuThread = new GUIControl.CPUThread();
        this.thinking = true;
        this.cpuThread.start();
    }

    private class CPUThread extends Thread {
        public void run() {
            GUIControl.this.setActivePlayer();
            GUIControl.this.guiBoard.setHighlightLegalSquares(false);

            int level;
            if (game.getActivePlayer() == MillGame.WHITE_PLAYER) {
                whitePlayer.setInfoText("");
                infoThread = new GUIControl.InfoThread(whitePlayer);
                level = whitePlayer.getCPULevelChoice();
            }
            else {
                blackPlayer.setInfoText("");
                infoThread = new GUIControl.InfoThread(blackPlayer);
                level = blackPlayer.getCPULevelChoice();
            }

            try {
            	Thread.sleep(1000);
            }
            catch (InterruptedException e) {
            }

            if ( !GUIControl.this.gameRunning) {
                GUIControl.this.thinking = false;
                GUIControl.this.clearActivePlayer();
                return;
            }

            GUIControl.this.infoThread.start();

            Move move;
            if (level == GUIPlayer.DEPTH_1)
                move = ai.depthSearch(GUIControl.this.game, (byte)1);
            else if (level == GUIPlayer.DEPTH_2)
                move = ai.depthSearch(GUIControl.this.game, (byte)2);
            else if (level == GUIPlayer.DEPTH_4)
                move = ai.depthSearch(GUIControl.this.game, (byte)4);
            else if (level == GUIPlayer.DEPTH_6)
                move = ai.depthSearch(GUIControl.this.game, (byte)6);
            else if (level == GUIPlayer.DEPTH_8)
                move = ai.depthSearch(GUIControl.this.game, (byte)8);
            else if (level == GUIPlayer.DEPTH_10)
                move = ai.depthSearch(GUIControl.this.game, (byte)10);
            else if (level == GUIPlayer.DEPTH_12)
                move = ai.depthSearch(GUIControl.this.game, (byte)12);
            else if (level == GUIPlayer.ONE_SECOND)
                move = ai.timeSearch(GUIControl.this.game, 1);
            else if (level == GUIPlayer.THREE_SECONDS)
                move = ai.timeSearch(GUIControl.this.game, 3);
            else if (level == GUIPlayer.FIVE_SECONDS)
                move = ai.timeSearch(GUIControl.this.game, 5);
            else if (level == GUIPlayer.TEN_SECONDS)
                move = ai.timeSearch(GUIControl.this.game, 10);
            else if (level == GUIPlayer.THIRTY_SECONDS)
                move = ai.timeSearch(GUIControl.this.game, 30);
            else // RANDOM
                move = ai.depthSearch(GUIControl.this.game, (byte)0);
            GUIControl.this.infoThread.interrupt();
            if (GUIControl.this.gameRunning) {
                // ########### ANIMOINTI ############                   ################### !!!
                if (GUIControl.this.game.makeMove(move)) {
                    GUIControl.this.victory();
                }
                else {
                    GUIControl.this.moveParser.setClickable( !CPUIsActive() );
                    GUIControl.this.setActivePlayer();
                }
            }
            GUIControl.this.thinking = false;
            GUIControl.this.guiBoard.repaint();
            GUIControl.this.gui.refreshButtons(); // redo ei ole ainakaan en‰‰ voimassa
            System.out.println("CPUThread kuoli...");
        }
    }

    private class InfoThread extends Thread {
        private GUIPlayer targetPlayer;
        public InfoThread(GUIPlayer player) {
            if (player == null) {
                throw new IllegalArgumentException("InfoThread(GUIPlayer): "+
                                         "Parameter GUIPlayer can't be null");
            }
            this.targetPlayer = player;
        }
 
        public void run() {
            try {
                while ( !this.isInterrupted()) {
                    this.targetPlayer.setInfoText(GUIControl.this.ai.toString());
                    Thread.sleep(200);
                }
            }
            catch (InterruptedException e) {
            }
            
            this.targetPlayer.setInfoText(GUIControl.this.ai.toString()); // viime hetken p‰ivitys
System.out.println("InfoThread kuoli...");
        }
    }


    private class CPUMatchThread extends Thread {
        public void run() {
            Thread computerMove = new GUIControl.CPUThread();
            while ( !this.isInterrupted() && gameRunning) {
                GUIControl.this.thinking = true;
                computerMove.run(); // koodi suoritetaan _nykyisess‰_ s‰ikeess‰!
            }
System.out.println("CPUMatchThread kuoli...");
        }
    }

    private boolean CPUIsActive() {
        byte activePlayer = this.game.getActivePlayer();
        if (activePlayer == MillGame.WHITE_PLAYER &&
            this.whitePlayer.getPlayerChoice() == GUIPlayer.CPU) {
                return true;
        }
        else if (activePlayer == MillGame.BLACK_PLAYER &&
            this.blackPlayer.getPlayerChoice() == GUIPlayer.CPU) {
                return true;
        }
        return false;
    }


    private void setActivePlayer() {
        if (this.whitePlayer == null || this.blackPlayer == null) {
            return;
        }

        boolean highlightPossible = false;
        if (this.game.getActivePlayer() == MillGame.WHITE_PLAYER) {
            this.whitePlayer.setActive(true);
            this.whitePlayer.showInfoText(true);
            this.blackPlayer.setActive(false);
            if (this.whitePlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
                highlightPossible = true;
            }
            if (this.blackPlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
                this.blackPlayer.showInfoText(false);
            }
        }
        else {
            this.whitePlayer.setActive(false);
            this.blackPlayer.setActive(true);
            this.blackPlayer.showInfoText(true);
            if (this.blackPlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
                highlightPossible = true;
            }
            if (this.whitePlayer.getPlayerChoice() == GUIPlayer.HUMAN) {
                this.whitePlayer.showInfoText(false);
            }
        }
        if (highlightPossible) {
            this.guiBoard.setHighlightLegalSquares(this.highlightLegalSquares);
        }
        else {
            this.guiBoard.setHighlightLegalSquares(false);
        }

    }

    private void clearActivePlayer() {
        if (this.whitePlayer != null) {
            whitePlayer.setActive(false);        
        }
        if (this.blackPlayer != null) {
            blackPlayer.setActive(false);        
        }
        this.guiBoard.setHighlightLegalSquares(false);
    }

    private void setPlayerChoicesActive(boolean value) {
        if (this.whitePlayer != null) {
            whitePlayer.setChoicesActive(value);
        }
        if (this.blackPlayer != null) {
            blackPlayer.setChoicesActive(value);
        }
    }

    private void victory() {
        this.gameRunning = false;
        System.out.println("WIN!"); // guiBoard piirt‰‰ voittokuviot?
    }
}