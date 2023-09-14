package de.dreierschach.app.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class Dir extends Pos {
    //@formatter:off
    private static final Dir[] STRAIGHT_DIRS = {
            new Dir(Pos.of(-1, 0), Type.straight, 0),
            new Dir(Pos.of(0, 1), Type.straight, 1),
            new Dir(Pos.of(1, 1), Type.straight, 2),
            new Dir(Pos.of(1, 0), Type.straight, 3),
            new Dir(Pos.of(0, -1), Type.straight, 4),
            new Dir(Pos.of(-1, -1), Type.straight, 5)};
    //@formatter:on

    //@formatter:off
    private static final Dir[] DIAGONAL_DIRS = {
            new Dir(Pos.of(-1, 1), Type.diagonal, 0),
            new Dir(Pos.of(1, 2), Type.diagonal, 1),
            new Dir(Pos.of(2, 1), Type.diagonal, 2),
            new Dir(Pos.of(1, -1), Type.diagonal, 3),
            new Dir(Pos.of(-1, -2), Type.diagonal, 4),
            new Dir(Pos.of(-2, -1), Type.diagonal, 5)};

    //@formatter:on

    //@formatter:off
    private static final Dir[] JUMP_DIRS = {
            new Dir(Pos.of(-2, 1), Type.jump, 0),
            new Dir(Pos.of(-1, 2), Type.jump, 1),
            new Dir(Pos.of(1, 3),Type.jump, 2),
            new Dir(Pos.of(2, 3),Type.jump, 3),
            new Dir(Pos.of(3, 2),Type.jump, 4),
            new Dir(Pos.of(3, 1),Type.jump, 5),
            new Dir(Pos.of(2, -1),Type.jump, 6),
            new Dir(Pos.of(1, -2),Type.jump, 7),
            new Dir(Pos.of(-1, -3),Type.jump, 8),
            new Dir(Pos.of(-2, -3),Type.jump, 9),
            new Dir(Pos.of(-3, -1),Type.jump, 10),
            new Dir(Pos.of(-3, -2),Type.jump, 11)};
    //@formatter:on

    private static final Dir[] ALL_DIRS = Stream.of(STRAIGHT_DIRS, DIAGONAL_DIRS, JUMP_DIRS).flatMap(Arrays::stream)
            .toArray(Dir[]::new);

    private final Type type;
    private final int index;

    public enum Type {
        straight(6), diagonal(6), jump(12);

        Type(int size) {
            this.size = size;
        }

        private final int size;

        public int size() {
            return size;
        }
    }

    public Dir(Pos pos, Type type, int index) {
        super(pos.x(), pos.y());
        this.type = type;
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public static Dir rotateLeft(Dir dir) {
        return get(dir.getType(), dir.getIndex() + dir.getType().size - 1);
    }

    public static Dir rotateRight(Dir dir) {
        return get(dir.getType(), dir.getIndex() + 1);
    }

    public static Optional<Pair<Dir, Integer>> find(Pos source, Pos destination) {
        var diff = Pos.sub(destination, source);
        for (var dir : ALL_DIRS) {
            var length = Pos.divide(diff, dir);
            if (length.isPresent() && length.get() > 0) {
                return Optional.of(Pair.of(dir, length.get()));
            }
        }
        return Optional.empty();
    }

    public static Dir get(Type type, int index) {
        switch (type) {
            case straight:
                return STRAIGHT_DIRS[index % type.size()];
            case diagonal:
                return DIAGONAL_DIRS[index % type.size()];
            case jump:
            default:
                return JUMP_DIRS[index % type.size()];
        }
    }
}
