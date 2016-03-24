public class Node {
    public final MillGame GAME;
    public final byte DEPTH;
    public final Move PREVIOUS_MOVE;
    public Node PATH;
    public short VALUE;

    public Node(MillGame game, byte depth, Move previousMove) {
        this.GAME = game;
        this.DEPTH = depth;
        this.PREVIOUS_MOVE = previousMove;
        this.PATH = null;
    }

    public void setPath(Node next) {
        this.PATH = next;
    }

    public void setValue(short value) {
        this.VALUE = value;
    }

    public String toString() {
        return " ||--> depth:"+DEPTH+" ,prevMove:"+PREVIOUS_MOVE+" , value:"+VALUE+", path:\n"+PATH;
    }
}