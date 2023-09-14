package de.dreierschach.app.engine;

import de.dreierschach.app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DreierschachEngineTest {
    private DreierschachEngineImpl engine;
    private Board board;

    @BeforeEach
    void init() {
        engine = new DreierschachEngineImpl();
        board = engine.board();
        engine.board().clear();
    }

    @Test
    void isValidAttackTest() {
        var p1 = Pos.of("f4");
        var p2 = Pos.of("d5");
        board.set(Figure.WHITE_KNIGHT, p1);
        var valid = engine.isValidAttack(board, new Move(p1, board.get(p1), p2, null));
        assertTrue(valid);
    }

    private static Stream<Arguments> provideGetAttacksJumpTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(
                        List.of(Pos.of("e3"), Pos.of("f4"), Pos.of("g6"), Pos.of("g7"), Pos.of("f8"), Pos.of("e8"),
                        Pos.of("c7"), Pos.of("b6"), Pos.of("a4"), Pos.of("a3"), Pos.of("b2"), Pos.of("c2")),
                Figure.WHITE_KNIGHT, Pos.of("d5"),
                List.of(Pos.of("e3"), Pos.of("f4"), Pos.of("g6"), Pos.of("g7"), Pos.of("f8"), Pos.of("e8"),
                        Pos.of("c7"), Pos.of("b6"), Pos.of("a4"), Pos.of("a3"), Pos.of("b2"), Pos.of("c2"))),
                Arguments.of(
                        List.of(Pos.of("e2")),
                        Figure.WHITE_KNIGHT, Pos.of("d5"),
                        List.of())
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideGetAttacksJumpTestdata")
    void getAttacksJumpTest(List<Pos> attackers, Figure figure, Pos dest, List<Pos> expected) {
        attackers.forEach(p -> board.set(figure, p));
        var attacks = engine.getAttacksJump(board, Color.BLACK, dest);
        assertEquals(new HashSet<>(expected), new HashSet<>(attacks));
    }

    private static Stream<Arguments> provideGetAttacksStraightTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(
                        List.of(Pos.of("f5")),
                        Figure.WHITE_ROOK, Pos.of("d5"),
                        List.of(Pos.of("f5"))
                )
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideGetAttacksStraightTestdata")
    void getAttacksStraightTest(List<Pos> attackers, Figure figure, Pos dest, List<Pos> expected) {
        attackers.forEach(p -> board.set(figure, p));
        var attacks = engine.getAttacksStraight(board, Color.BLACK, dest);
        assertEquals(new HashSet<>(expected), new HashSet<>(attacks));
    }
}
