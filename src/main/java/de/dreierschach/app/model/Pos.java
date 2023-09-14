package de.dreierschach.app.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

public class Pos {
    private final int x;
    private final int y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Pos of(int x, int y) {
        return new Pos(x, y);
    }

    public static Pos of(String s) {
        if (s == null || s.length() < 2 || s.length() > 3) {
            return Pos.of(-1, -1);
        }
        var x = s.substring(1);
        if (!StringUtils.isNumeric(x)) {
            return Pos.of(-1, -1);
        }
        return Pos.of(Integer.parseInt(x) - 1, s.charAt(0) - (int) 'a');
    }

    public static Pos of(Pos p) {
        if (p == null) {
            return Pos.of(-1, -1);
        }
        return new Pos(p.x(), p.y());
    }

    public static Pos add(Pos p1, Pos p2) {
        return new Pos(p1.x() + p2.x(), p1.y() + p2.y());
    }

    public static Pos sub(Pos p1, Pos p2) {
        return new Pos(p1.x() - p2.x(), p1.y() - p2.y());
    }

    public static Optional<Integer> divide(Pos p1, Pos p2) {
        if (p2.x() == 0) {
            if (p1.x() != 0) {
                return Optional.empty();
            }
            if (p2.y() == 0) {
                if (p1.y() == 0) {
                    return Optional.of(1);
                }
                return Optional.empty();
            }
            return Optional.of(p1.y() / p2.y());
        }

        if (p2.y() == 0) {
            if (p1.y() == 0) {
                return Optional.of(p1.x() / p2.x());
            }
            return Optional.empty();
        }

        var x = (double) p1.x() / (double) p2.x();
        var y = (double) p1.y() / (double) p2.y();

        if (x != y) {
            return Optional.empty();
        }
        return Optional.of((int) x);
    }

    public static Pos mult(Pos p, int n) {
        return new Pos(p.x() * n, p.y() * n);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public boolean isValid() {
        if (x() < 0 || y() < 0) {
            return false;
        }
        if (y() <= 5) {
            return x() < 8 + y();
        }
        return y() < 13 && x() < 13 && x() >= y() - 5;
    }

    public boolean isBaseline(Pos pos) {
        return pos.y() == 0 || pos.y() - pos.x() == 5 || pos.x() == 12;
    }

    public String getXAsString() {
        return String.valueOf(x() + 1);
    }

    public String getYAsString() {
        return String.valueOf((char) (y() + 'a'));
    }

    @Override
    public String toString() {
        return getYAsString() + getXAsString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pos)) {
            return false;
        }
        Pos pos = (Pos) o;
        return x == pos.x && y == pos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
