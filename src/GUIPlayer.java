import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Choice;
import java.awt.Label;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class GUIPlayer extends Panel {
    public static final int HUMAN = 0;
    public static final int CPU = 1;

    public static final int RANDOM = 0,
                            ONE_SECOND = 1,
                            THREE_SECONDS = 2,
                            FIVE_SECONDS = 3,
                            TEN_SECONDS = 4,
                            THIRTY_SECONDS = 5,
                            DEPTH_1 = 6,
                            DEPTH_2 = 7,
                            DEPTH_4 = 8,
                            DEPTH_6 = 9,
                            DEPTH_8 = 10,
                            DEPTH_10 = 11,
                            DEPTH_12 = 12;

    private static final Font CHOICE_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Color ACTIVE_INFOAREA_COLOR = new Color(192,255,192);
    private static final Color PASSIVE_INFOAREA_COLOR = SystemColor.control; //haaleanv‰rinen tausta

    private Label header;
    private TextArea infoArea;
    private Choice playerChoice;
    private Choice levelChoice;
    private Panel topRow;

    private String hiddenInfoText = "";

    public GUIPlayer(String name,
                     Color nameColor,
                     Color background,
                     int defaultPlayer) throws IllegalArgumentException {
        if (name == null || nameColor == null || background == null ||
            defaultPlayer < HUMAN || defaultPlayer > CPU) {
            throw new IllegalArgumentException(
                "No argument for GUIPlayer can be null."+
                "Defaultplayer:"+defaultPlayer+" must be 0 or 1.");
        }

        // header
        this.header = new Label(name, Label.LEFT);
        this.header.setFont(CHOICE_FONT);
        this.header.setForeground(nameColor);
        this.header.setBackground(background);

        // player choices
        this.playerChoice = new Choice();
        this.playerChoice.setFont(CHOICE_FONT);
        this.playerChoice.add("Human");
        this.playerChoice.add("CPU");
        this.playerChoice.select(defaultPlayer); // oletusarvo

        // level choices
        this.levelChoice = new Choice();
        this.levelChoice.setFont(CHOICE_FONT);
        this.levelChoice.add("random");
        this.levelChoice.add("1 s");
        this.levelChoice.add("3 s");
        this.levelChoice.add("5 s");
        this.levelChoice.add("10 s");
        this.levelChoice.add("30 s");
        this.levelChoice.add("depth 1");
        this.levelChoice.add("depth 2");
        this.levelChoice.add("depth 4");
        this.levelChoice.add("depth 6");
        this.levelChoice.add("depth 8");
        this.levelChoice.add("depth 10");
        this.levelChoice.add("depth 12");
        this.levelChoice.select(THREE_SECONDS); // oletusarvo

        // ylin rivi: nimi ja valinnat
        this.topRow = new Panel(new BorderLayout());
        this.topRow.setBackground(background);
        this.topRow.add(this.header, BorderLayout.WEST);
        this.topRow.add(this.playerChoice, BorderLayout.CENTER);
        this.topRow.add(this.levelChoice, BorderLayout.EAST);

        // info area
        this.infoArea = new TextArea("",6,40, TextArea.SCROLLBARS_NONE);
        this.infoArea.setEditable(false);
        this.infoArea.setForeground(Color.BLUE);

        // kaikki kiinni Paneliin
        super.setLayout(new BorderLayout()); //hgap, vgap
        super.add(this.topRow, BorderLayout.NORTH);
        super.add(this.infoArea, BorderLayout.CENTER);

        // level-valinta ei n‰y ihmispelaajalla
        if (defaultPlayer == CPU) {
            this.levelChoice.setVisible(true);
        }
        else {
            this.levelChoice.setVisible(false);
        }

        // aluksi vuoro ei ole kell‰‰n
        this.setActive(false);

        // tapahtumakuuntelija player-valintaan.
        this.playerChoice.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (playerChoice.getSelectedIndex() == CPU) {
                        levelChoice.setVisible(true);
                    }
                    else {
                        levelChoice.setVisible(false);
                    }
                    topRow.validate();
                }
            }
        );

        super.validate();
    }

    public void setInfoText(String s) {
        this.infoArea.setText(s);
        this.hiddenInfoText = s;
        this.infoArea.validate();                     //    ############# toimiiko ilman?!
    }

    public void showInfoText(boolean value) {
        if (value) {
            this.infoArea.setText(this.hiddenInfoText);
        }
        else {
            this.infoArea.setText("");
        }
        this.infoArea.validate();
    }

    public void setActive(boolean active) {
        if (active) {
            this.infoArea.setBackground(ACTIVE_INFOAREA_COLOR);
        }
        else {
//            this.infoArea.setBackground(PASSIVE_INFOAREA_COLOR);
            this.infoArea.setBackground(GUI.BACKGROUND_COLOR);
        }
//        this.infoArea.invalidate();
    }

    public void setChoicesActive(boolean active) {
        this.playerChoice.setEnabled(active);
        this.levelChoice.setEnabled(active);
        this.playerChoice.validate();
        this.levelChoice.validate();
    }

    public int getPlayerChoice() {
        return this.playerChoice.getSelectedIndex();
    }

    public int getCPULevelChoice() throws IllegalStateException {
        if (this.getPlayerChoice() == HUMAN) {
            throw new IllegalStateException("Level choices are available only for CPU players.");
        }
        return this.levelChoice.getSelectedIndex();
    }
}
