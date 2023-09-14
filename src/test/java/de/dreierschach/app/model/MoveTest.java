package de.dreierschach.app.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

public class MoveTest {

    private static Stream<Arguments> provideIsCastelingTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(Pos.of("a4"), Figure.WHITE_KING, Pos.of("a2"), Move.Casteling.KING_SIDE_CASTELING),
                Arguments.of(Pos.of("a4"), Figure.WHITE_KING, Pos.of("a7"), Move.Casteling.QUEEN_SIDE_CASTELING),
                Arguments.of(Pos.of("a4"), Figure.BLACK_KING, Pos.of("a2"), Move.Casteling.NONE),
                Arguments.of(Pos.of("a4"), Figure.WHITE_KING, Pos.of("a5"), Move.Casteling.NONE),
                Arguments.of(Pos.of("a3"), Figure.WHITE_KING, Pos.of("a2"), Move.Casteling.NONE),

                Arguments.of(Pos.of("j5"), Figure.BROWN_KING, Pos.of("g2"), Move.Casteling.QUEEN_SIDE_CASTELING),
                Arguments.of(Pos.of("j5"), Figure.BROWN_KING, Pos.of("l7"), Move.Casteling.KING_SIDE_CASTELING),
                Arguments.of(Pos.of("j5"), Figure.BROWN_QUEEN, Pos.of("l7"), Move.Casteling.NONE),
                Arguments.of(Pos.of("j5"), Figure.BROWN_KING, Pos.of("m8"), Move.Casteling.NONE),
                Arguments.of(Pos.of("i4"), Figure.BROWN_KING, Pos.of("l7"), Move.Casteling.NONE),

                Arguments.of(Pos.of("i13"), Figure.BLACK_KING, Pos.of("g13"), Move.Casteling.KING_SIDE_CASTELING),
                Arguments.of(Pos.of("i13"), Figure.BLACK_KING, Pos.of("l13"), Move.Casteling.QUEEN_SIDE_CASTELING),
                Arguments.of(Pos.of("i13"), Figure.BLACK_KNIGHT, Pos.of("l13"), Move.Casteling.NONE),
                Arguments.of(Pos.of("j13"), Figure.BLACK_KING, Pos.of("l13"), Move.Casteling.NONE),
                Arguments.of(Pos.of("i13"), Figure.BLACK_KING, Pos.of("k13"), Move.Casteling.NONE)
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideIsCastelingTestdata")
    void castelingTest(Pos p, Figure sourceFigure, Pos q, Move.Casteling casteling) {
        Move result = new Move(p, sourceFigure, q, null);
        assertEquals(casteling, result.casteling());
    }
}
