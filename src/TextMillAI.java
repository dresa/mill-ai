public class TextMillAI{
	public static void main(String[] args) {
        MillGame game = new MillGame();
        MillAI ai = new MillAI();
        boolean victory = false;
        while ( !victory) {
            System.out.println(game);

            if (game.getActivePlayer() == MillGame.BLACK_PLAYER) {
                System.out.println("AI thinking...");
                victory = game.makeMove( ai.timeSearch(game,4) );
            }
            else {

                String fromStr = Lue.rivi();
                String toStr = Lue.rivi();
                String removeStr = Lue.rivi();

                if (fromStr.equals("undo")) {
                    game.undo();
                    game.undo();
                }
                else if (fromStr.equals("redo")) {
                    game.redo();
                    game.redo();
                }
                else {
                    try {
                        byte from = Byte.parseByte(fromStr);
                        byte to = Byte.parseByte(toStr);
                        byte remove = Byte.parseByte(removeStr);
                        victory = game.makeMove( new Move(from, to, remove) );
                    }
                    catch (NumberFormatException e) {
                        System.out.println("######## FROM, TO tai REMOVE ovat lukuja väliltä -1 -- 23!");
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("#################################### Laiton siirto!");
                    }
                }
            }
            if ( !victory ) {
                System.out.println("SIIRTO OLI OK!");
            }
            else {
                System.out.println("********* Peli loppui ! ********** Voittaja:"+
                                   game.getActivePlayer());
            }
        }
    }
}
