public class Performancetest{
	public static void main(String[] args) {



         MillBoard board = new MillBoard();
         MillGame testgame = new MillGame();


//////////////////////////// LOPPUPELI: //////////////////////////
         board.setPiece((byte)13, BoardInfo.BLACK);
         board.setPiece((byte)18, BoardInfo.BLACK);
         board.setPiece((byte)19, BoardInfo.BLACK);
         board.setPiece((byte)1, BoardInfo.WHITE);
         board.setPiece((byte)4, BoardInfo.WHITE);
         board.setPiece((byte)9, BoardInfo.WHITE);
         board.setPiece((byte)14, BoardInfo.WHITE);
         board.setPiece((byte)17, BoardInfo.WHITE);
         board.setPiece((byte)21, BoardInfo.WHITE);

         byte activePlayer = MillGame.BLACK_PLAYER;
         byte opponent = MillGame.WHITE_PLAYER;
         testgame.generateMillGame(board, activePlayer, MillGame.PHASE_ENDGAME, (byte)0, (byte)0);


         int KIERROKSIA = 1000;
         long alkuhetki = System.currentTimeMillis();
         Move[] moves = null;
         for (int laskuri=0; laskuri < KIERROKSIA; laskuri++) {
              moves = LegalMoves.getAllLegalMoves2(testgame);
         }
         long loppuhetki = System.currentTimeMillis();
         System.out.println("isLegal-metodi: Aikaa kului "+(loppuhetki-alkuhetki)+"ms.  Kierroksia: "+KIERROKSIA+"  Sallittuja siirtoja:"+moves.length);


         KIERROKSIA = 10000;
         alkuhetki = System.currentTimeMillis();
         moves = null;
         for (int laskuri=0; laskuri < KIERROKSIA; laskuri++) {
              moves = LegalMoves.getAllLegalMoves(testgame);
         }
         loppuhetki = System.currentTimeMillis();
         System.out.println("parannettu metodi: Aikaa kului "+(loppuhetki-alkuhetki)+"ms.  Kierroksia: "+KIERROKSIA+"  Sallittuja siirtoja:"+moves.length);


    }
}
