package de.dreierschach.app.model;

public class Move {
    public enum Check {NONE, CHECK, CHECKMATE}

    public enum Casteling {NONE, KING_SIDE_CASTELING, QUEEN_SIDE_CASTELING}

    private final Pos p1;
    private final Figure f1;
    private final Pos p2;
    private final Figure f2;
    private final Check check;
    private final Casteling casteling;
    private final boolean disablesCasteling;

    public Move(Pos p1, Figure f1, Pos p2, Figure f2) {
        this.p1 = p1;
        this.f1 = f1;
        this.p2 = p2;
        this.f2 = f2;
        this.check = Check.NONE;
        this.casteling = checkCasteling();
        this.disablesCasteling = false;
    }

    public Move(Pos p1, Figure f1, Pos p2, Figure f2, Check check, Casteling casteling, boolean disablesCasteling) {
        this.p1 = p1;
        this.f1 = f1;
        this.p2 = p2;
        this.f2 = f2;
        this.check = check;
        this.casteling = casteling;
        this.disablesCasteling = disablesCasteling;
    }

    public Pos p1() {
        return p1;
    }

    public Figure f1() {
        return f1;
    }

    public Pos p2() {
        return p2;
    }

    public Figure f2() {
        return f2;
    }

    public Check check() {
        return check;
    }

    public Casteling casteling() {
        return casteling;
    }

    public Move withCheck(Check check) {
        return new Move(p1, f1, p2, f2, check, casteling, disablesCasteling);
    }

    public boolean isDisablesCasteling() {
        return disablesCasteling;
    }

    public Move withDisablesCasteling() {
        return new Move(p1, f1, p2, f2, check, casteling, true);
    }

    private Casteling checkCasteling() {
        if (f1.getType() != FigureType.KING) {
            return Casteling.NONE;
        }
        switch (f1.getColor()) {
            case WHITE:
                if (!p1.equals(Pos.of("a4"))) {
                    return Casteling.NONE;
                }
                if (p2.equals(Pos.of("a2"))) {
                    return Casteling.KING_SIDE_CASTELING;
                }
                if (p2.equals(Pos.of("a7"))) {
                    return Casteling.QUEEN_SIDE_CASTELING;
                }
                break;
            case BROWN:
                if (!p1.equals(Pos.of("j5"))) {
                    return Casteling.NONE;
                }
                if (p2.equals(Pos.of("l7"))) {
                    return Casteling.KING_SIDE_CASTELING;
                }
                if (p2.equals(Pos.of("g2"))) {
                    return Casteling.QUEEN_SIDE_CASTELING;
                }
                break;
            case BLACK:
                if (!p1.equals(Pos.of("i13"))) {
                    return Casteling.NONE;
                }
                if (p2.equals(Pos.of("g13"))) {
                    return Casteling.KING_SIDE_CASTELING;
                }
                if (p2.equals(Pos.of("l13"))) {
                    return Casteling.QUEEN_SIDE_CASTELING;
                }
        }
        return Casteling.NONE;
    }
}
