package de.dreierschach.app.engine;

import de.dreierschach.app.model.Board;
import de.dreierschach.app.model.Move;
import de.dreierschach.app.model.Pos;

public interface DreierschachEngine {
    DreierschachEngine startGame();

    DreierschachEngineImpl withBoard(Board board);

    Board board();

    boolean move(Pos source, Pos destination);

    boolean move(Move move);

    boolean validateMoveOrAttack(Move move);
}
