import java.awt.Frame;
import java.awt.Panel;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI extends Frame {
    public static final Color BACKGROUND_COLOR = new Color(255,220,180); // vaalean ruskea
    private GUIPlayer whitePlayer;
    private GUIPlayer blackPlayer;

    private Panel buttonsAndCheckboxes;
    private Panel buttons;
    private Button newGameButton;
    private Button continueStopButton;
    private Button undoButton;
    private Button redoButton;
    private Checkbox highlightValidMovesCheckbox;

    private Panel leftSide;

    private GUIMillBoard guiBoard;
    private GUIControl control;
    private MillGame game;

    public GUI() {
        super.setTitle("Mill");

        // players
        this.whitePlayer = new GUIPlayer("White Player", Color.black, Color.white, GUIPlayer.HUMAN);
        this.blackPlayer = new GUIPlayer("Black Player", Color.white, Color.black, GUIPlayer.CPU);

        // buttons
        this.newGameButton = new Button("New Game");
        this.continueStopButton = new Button("Continue Game");
        this.undoButton = new Button("Undo move");
        this.redoButton = new Button("Redo move");

        // checkbox
        this.highlightValidMovesCheckbox = new Checkbox("highlight valid moves", true);

        // button panel
        this.buttons = new Panel(new GridLayout(2,2,20,10)); // height, width , hgap, vgap
        this.buttons.add(this.newGameButton);
        this.buttons.add(this.continueStopButton);
        this.buttons.add(this.undoButton);
        this.buttons.add(this.redoButton);

        // all buttons and checkboxes
        this.buttonsAndCheckboxes = new Panel(new BorderLayout());
        this.buttonsAndCheckboxes.add(this.highlightValidMovesCheckbox, BorderLayout.NORTH);
        this.buttonsAndCheckboxes.add(this.buttons, BorderLayout.CENTER);

        // kiinnitet‰‰n kaikki s‰‰tˆelementit lopulta vasemmanpuoleiseen Paneliin
        this.leftSide = new Panel(new BorderLayout(10, 20));
        this.leftSide.add(this.whitePlayer, BorderLayout.NORTH);
        this.leftSide.add(this.buttonsAndCheckboxes, BorderLayout.CENTER);
        this.leftSide.add(this.blackPlayer, BorderLayout.SOUTH);

        this.game = new MillGame();
        this.guiBoard = new GUIMillBoard(this.game);
        this.guiBoard.setVisible(true);
        this.control = new GUIControl(this.guiBoard, this);
        this.newGameButton.setEnabled(true);
        this.continueStopButton.setEnabled(false);
        this.undoButton.setEnabled(false);
        this.redoButton.setEnabled(false);

        // Kaikki kiinni 'this'-Frameen
        super.setBackground(BACKGROUND_COLOR);
        super.setLayout(new BorderLayout(10,20)); //hgap, vgap
        super.add(this.leftSide, BorderLayout.WEST);
        super.add(this.guiBoard, BorderLayout.CENTER);

        this.validate();



        // tapahtumakuuntelija ohjelman sulkemiseen
        super.addWindowListener( new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            }
        );

        // tapahtumakuuntelija "highlight valid moves"-valintaan.
        this.highlightValidMovesCheckbox.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        control.setHighlightLegalSquares(true);
                    }
                    else { //deselected
                        control.setHighlightLegalSquares(false);
                    }
                }
            }
        );

        // tapahtumakuuntelija 'new game' -nappulalle
        this.newGameButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    game = new MillGame();
                    guiBoard.setGame(game);
                    control.newGame(game, whitePlayer, blackPlayer);
                }
            }
        );

        // tapahtumakuuntelija 'undo' -nappulalle
        this.undoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (game.undoIsPossible()) {
                        control.undo();
                    }
                }
            }
        );

        // tapahtumakuuntelija 'redo' -nappulalle
        this.redoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (game.redoIsPossible()) {
                        control.redo();
                    }
                }
            }
        );

        // tapahtumakuuntelija 'continue'- ja 'stop' -nappulalle
        this.continueStopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (control.gameIsRunning()) {
                        control.stopGame();
                    }
                    else {
                        control.continueGame(whitePlayer, blackPlayer);
                    }
                }
            }
        );
    }

    // p‰ivitt‰‰ nappulat (nimet, onko painettavissa)
    public void refreshButtons() {
        String undoLabel = "Undo move "+this.game.getTurnNumber() - 1;
        if ( !this.undoButton.getLabel().equals(undoLabel)) {
            this.undoButton.setLabel(undoLabel);
        }
        this.undoButton.setEnabled(this.game.undoIsPossible());

        String redoLabel = "Redo move "+this.game.getTurnNumber();
        if ( !this.redoButton.getLabel().equals(redoLabel)) {
            this.redoButton.setLabel(redoLabel);
        }
        this.redoButton.setEnabled(this.game.redoIsPossible());

        if (this.control.gameIsRunning()) {
            if ( !this.continueStopButton.getLabel().equals("Stop game") ) {
                this.continueStopButton.setLabel("Stop game");
            }
        }
        else {
            if ( !this.continueStopButton.getLabel().equals("Continue game") ) {
                this.continueStopButton.setLabel("Continue game");
            }
        }
        this.continueStopButton.setEnabled(true);
        super.validate();
    }


	public static void main(String[] args) {
        GUI gui = new GUI();
        gui.setSize(800,600);
        gui.setVisible(true);
        gui.validate();
	}
}
