package de.dreierschach.app.model;

import java.util.Optional;

public enum Figure {
    NONE(null, null),

    //@formatter:off
    WHITE_KING(Color.WHITE, FigureType.KING),
    WHITE_QUEEN(Color.WHITE, FigureType.QUEEN),
    WHITE_BISHOP(Color.WHITE, FigureType.BISHOP),
    WHITE_KNIGHT(Color.WHITE, FigureType.KNIGHT),
    WHITE_ROOK(Color.WHITE, FigureType.ROOK),
    WHITE_PAWN(Color.WHITE, FigureType.PAWN),

    BROWN_KING(Color.BROWN, FigureType.KING),
    BROWN_QUEEN(Color.BROWN, FigureType.QUEEN),
    BROWN_BISHOP(Color.BROWN, FigureType.BISHOP),
    BROWN_KNIGHT(Color.BROWN, FigureType.KNIGHT),
    BROWN_ROOK(Color.BROWN, FigureType.ROOK),
    BROWN_PAWN(Color.BROWN, FigureType.PAWN),

    BLACK_KING(Color.BLACK, FigureType.KING),
    BLACK_QUEEN(Color.BLACK, FigureType.QUEEN),
    BLACK_BISHOP(Color.BLACK, FigureType.BISHOP),
    BLACK_KNIGHT(Color.BLACK, FigureType.KNIGHT),
    BLACK_ROOK(Color.BLACK, FigureType.ROOK),
    BLACK_PAWN(Color.BLACK, FigureType.PAWN);
    //@formatter:on

    private final Color color;
    private final FigureType type;

    Figure(Color color, FigureType type) {
        this.color = color;
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public FigureType getType() {
        return type;
    }
}
