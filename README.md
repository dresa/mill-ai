# mill-ai
Traditional Mill GUI boardgame with a competent AI opponent.
The game is also known as Nine Men's Morris. See more details from
[Wikipedia](https://en.wikipedia.org/wiki/Nine_Men's_Morris)

Here is a screenshot of the program in-game:
![Mill GUI screenshot](http://www.elisanet.fi/esajakatja/valokuvat/mill-screenshot.png)

The GUI has the following features:
* player/AI vs. player/AI
* AI can be given a certain time limit, or a search depth limit, counted in moves
* displays AI statistics while computing
* undo or redo moves
* able to switch players (or player/AI status) mid-game
* highlights legal moves

This is the first artificial intelligence project I took. I was interested to see
if I could create an AI player so strong that it might beat me once in a while.
I chose Mill as a game, because the rules are simple, and it seemed that not too many competent
AI's had been created yet. In particular, I was very disappointed by the quality
of some commercial Mill AI programs, so I decided to try it myself.
It turned out that I was never able to best my own AI myself. I managed to play a few
draws against it, but that's just because Mill is so prone to end as a draw.
I was both shocked and impressed of myself.

When I wrote this program in 2004, I had about one year's worth of programming experience
under my belt. Java was a natural choice back then.

It took one weekend to write the AI part of the program. In 2004, I played my AI against
the strongest other AI players I could find. My AI still has not lost a game.
Although one of the AI opponents was probably a bit stronger than my AI, the game
ends so easily as a draw that my AI still continues undefeated. Not
too bad for a weekend's job.

The methodology is based on a standard min-max tree-search and alpha-beta pruning.
Ordering of moves turned out to be an important factor during the search, narrowing
the searchable branches quite a lot. I decided not to implement other heuristics,
as the program was already strong enough.
If given a fixed time limit, the search employs iterative deepening. The evaluation
of board situation is based on material advantage and the number of possible moves
-- that's it.
I also added a bit of randomness to make moves less predictable.

As always, tinkering with the GUI took most of the time. Still, the weekend spent
with the AI part seems most fruitful.

Just compile and say ``java GUI``, and have it a go. Let's see if you can beat the AI!
