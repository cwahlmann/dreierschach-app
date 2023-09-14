package de.dreierschach.app.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PosTest {
    @Test
    void testCreate1() {
        var pos = Pos.of(3, 4);
        assertEquals(3, pos.x());
        assertEquals(4, pos.y());
    }

    @Test
    void testCreate2() {
        // good

        var pos = Pos.of("f7");
        assertEquals(6, pos.x());
        assertEquals(5, pos.y());

        pos = Pos.of("k12");
        assertEquals(11, pos.x());
        assertEquals(10, pos.y());

        // bad

        pos = Pos.of("");
        assertEquals(-1, pos.x());
        assertEquals(-1, pos.y());

        pos = Pos.of((String) null);
        assertEquals(-1, pos.x());
        assertEquals(-1, pos.y());

        pos = Pos.of("x");
        assertEquals(-1, pos.x());
        assertEquals(-1, pos.y());

        pos = Pos.of("a000");
        assertEquals(-1, pos.x());
        assertEquals(-1, pos.y());

        pos = Pos.of("ab");
        assertEquals(-1, pos.x());
        assertEquals(-1, pos.y());
    }

    @Test
    void testCreate3() {
        var p1 = Pos.of(3, 4);
        var p2 = Pos.of(p1);
        assertEquals(3, p2.x());
        assertEquals(4, p2.y());

        var p = Pos.of((Pos) null);
        assertEquals(-1, p.x());
        assertEquals(-1, p.y());
    }

    @Test
    void testAddSub() {
        var p1 = Pos.of(1, 2);
        var p2 = Pos.of(-1, 3);

        var p3 = Pos.add(p1, p2);
        assertEquals(0, p3.x());
        assertEquals(5, p3.y());

        var p4 = Pos.sub(p1, p2);
        assertEquals(2, p4.x());
        assertEquals(-1, p4.y());
    }

    @Test
    void testMult() {
        var p1 = Pos.of(1, 2);
        var p2 = Pos.mult(p1, 5);
        assertEquals(5, p2.x());
        assertEquals(10, p2.y());
    }

    private static Stream<Arguments> provideDivideTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(Pos.of(2, 4), Pos.of(1, 2), Optional.of(2)),
                Arguments.of(Pos.of(2, 5), Pos.of(1, 2), Optional.empty()),
                Arguments.of(Pos.of(4,0), Pos.of(2, 0), Optional.of(2)),
                Arguments.of(Pos.of(0, 3), Pos.of(0, 1), Optional.of(3)),
                Arguments.of(Pos.of(0, 0), Pos.of(0, 0), Optional.of(1)),
                Arguments.of(Pos.of(1, 2), Pos.of(0, 2), Optional.empty()),
                Arguments.of(Pos.of(1, 0), Pos.of(0, 2), Optional.empty()),
                Arguments.of(Pos.of(1, 2), Pos.of(1, 0), Optional.empty()),
                Arguments.of(Pos.of(0, 2), Pos.of(1, 0), Optional.empty())
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideDivideTestdata")
    void testDivide(Pos p1, Pos p2, Optional<Integer> expected) {
        var d = Pos.divide(p1, p2);
        assertEquals(expected, d);
    }

    private static Stream<Arguments> provideIsValidTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(Pos.of("a1"), true),
                Arguments.of(Pos.of("a6"), true),
                Arguments.of(Pos.of("a7"), true),
                Arguments.of(Pos.of("f1"), true),
                Arguments.of(Pos.of("m8"), true),
                Arguments.of(Pos.of("m13"), true),
                Arguments.of(Pos.of("f13"), true),
                Arguments.of(Pos.of("a8"), true),

                Arguments.of(Pos.of(0,-1), false),
                Arguments.of(Pos.of(-1,0), false),
                Arguments.of(Pos.of(0,6), false),
                Arguments.of(Pos.of(6,12), false),
                Arguments.of(Pos.of(7,13), false),
                Arguments.of(Pos.of(13,12), false),
                Arguments.of(Pos.of(12,13), false),
                Arguments.of(Pos.of(4,12), false),
                Arguments.of(Pos.of(8,0), false)
                );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideIsValidTestdata")
    void testIsValid(Pos p, boolean expected) {
        assertEquals(expected, p.isValid());
    }

    private static Stream<Arguments> provideIsBaselineTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(Pos.of("a1"), true),
                Arguments.of(Pos.of("a6"), true),
                Arguments.of(Pos.of("c2"), false),

                Arguments.of(Pos.of("g2"), true),
                Arguments.of(Pos.of("m8"), true),
                Arguments.of(Pos.of("j6"), false),

                Arguments.of(Pos.of("m13"), true),
                Arguments.of(Pos.of("f13"), true),
                Arguments.of(Pos.of("i12"), false),

                Arguments.of(Pos.of("g6"), false)
                );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideIsBaselineTestdata")
    void testIsBaseline(Pos p, boolean expected) {
        assertEquals(expected, p.isBaseline(p));
    }

}