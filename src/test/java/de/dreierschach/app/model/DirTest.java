package de.dreierschach.app.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

public class DirTest {


    private static Stream<Arguments> provideFindTestdata() {
        //@formatter:off
        return Stream.of(
                Arguments.of(Pos.of("e6"), Pos.of("e3"), Pos.of(-1,0), Dir.Type.straight, 3),
                Arguments.of(Pos.of("e6"), Pos.of("g10"), Pos.of(2,1), Dir.Type.diagonal, 2),
                Arguments.of(Pos.of("e6"), Pos.of("h8"), Pos.of(2,3), Dir.Type.jump, 1)
        );
        //@formatter:on
    }

    @ParameterizedTest
    @MethodSource("provideFindTestdata")
    void testFind(Pos source, Pos destination, Pos expectedPos, Dir.Type expectedType, int expectedDistance) {
        var result = Dir.find(source, destination);
        if (expectedPos == null) {
            assertFalse(result.isPresent());
            return;
        }
        assertTrue(result.isPresent());
        assertEquals(expectedPos, result.get().getLeft());
        assertEquals(expectedType, result.get().getLeft().getType());
        assertEquals(expectedDistance, result.get().getRight());
    }
}
