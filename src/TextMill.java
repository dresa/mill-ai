public class TextMill{
	public static void main(String[] args) {
        MillGame game = new MillGame();
        boolean victory = false;
        while ( !victory) {
            System.out.println(game);
            byte from = (byte) Lue.kluku();
            byte to = (byte) Lue.kluku();
            byte remove = (byte) Lue.kluku();
            try {
                victory = game.makeMove( new Move(from, to, remove) );
                if ( !victory ) {
                    System.out.println("SIIRTO OLI OK!");
                }
                else {
                    System.out.println("********* VOITIT JUURI KOKO PELIN! ********** Voittaja:"+
                                       game.getActivePlayer());
                }

            }
            catch (Exception e) {
                System.out.println("VIRHE: "+e);
            }
        }
    }
}
