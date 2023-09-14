package de.dreierschach.app.model;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Board {

    private final List<Consumer<FieldChangeEvent>> fieldChangeListeners;
    private final List<Consumer<PlayerChangeEvent>> playerChangeListeners;
    private final List<Consumer<MoveEvent>> moveListeners;
    private final List<Consumer<CheckEvent>> checkListeners;

    private final Figure[][] figures;
    private final Set<Color> castelingForbidden;
    private final Set<Color> check;
    private Color player;
    private final List<Move> moves;
    private final Map<Color, Pos> kingsPositions;

    public Board() {
        this.figures = new Figure[13][13];
        this.castelingForbidden = new HashSet<>();
        this.check = new HashSet<>();
        this.fieldChangeListeners = new ArrayList<>();
        this.playerChangeListeners = new ArrayList<>();
        this.moveListeners = new ArrayList<>();
        this.checkListeners = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.kingsPositions = new HashMap<>();
        clear();
    }

    /**
     * returns a copy of the given board, butwithout any listeners
     *
     * @param board the board to copy
     * @return the copy
     */
    public static Board copyOf(Board board) {
        var result = new Board();
        for (int i = 0; i < 13; i++) {
            System.arraycopy(board.figures[i], 0, result.figures[i], 0, 13);
        }
        result.check.clear();
        result.check.addAll(board.check);

        result.moves.clear();
        result.moves.addAll(board.moves);

        result.player = board.player;

        result.castelingForbidden.clear();
        result.castelingForbidden.addAll(board.castelingForbidden);

        result.kingsPositions.clear();
        result.kingsPositions.putAll(board.kingsPositions);

        return result;
    }

    public Color getPlayer() {
        return player;
    }

    public Board setPlayer(Color player) {
        var oldValue = this.player;
        this.player = player;
        notifyPlayerChangeListeners(new PlayerChangeEvent(oldValue, player));
        return this;
    }

    public Board nextPlayer() {
        switch (player) {
            case WHITE:
                setPlayer(Color.BROWN);
                break;
            case BROWN:
                setPlayer(Color.BLACK);
                break;
            case BLACK:
                setPlayer(Color.WHITE);
        }
        return this;
    }

    public Board set(Figure figure, Pos pos) {
        if (pos.isValid()) {
            var oldValue = figures[pos.y()][pos.x()];
            figures[pos.y()][pos.x()] = figure;
            if (figure.getType() == FigureType.KING) {
                kingsPositions.put(figure.getColor(), pos);
            }
            notifyFieldChangeListeners(new FieldChangeEvent(pos, oldValue, figure));
        }
        return this;
    }

    public Figure get(Pos pos) {
        return pos.isValid() ? figures[pos.y()][pos.x()] : Figure.NONE;
    }

    public Pos getKingsPosition(Color color) {
        return kingsPositions.get(color);
    }

    public void setCheck(Set<Color> players) {
        var oldValue = new HashSet<>(this.check);
        var newValue = new HashSet<>(players);
        this.check.clear();
        this.check.addAll(players);
        notifyCheckListeners(new CheckEvent(oldValue, newValue));
    }

    public boolean isCheck(Color player) {
        return check.contains(player);
    }

    public Stream<Move> getMoves() {
        return moves.stream();
    }

    public Move getLastMove() {
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(moves.size() - 1);
    }

    public String getLastMoveAsString() {
        Move move = getLastMove();
        if (move == null) {
            return "";
        }

        String result = figureToNotation(move.f1()) + move.p1();
        if (move.f2() != Figure.NONE) {
            result += "x";
        }
        result += move.p2();
        if (!check.isEmpty()) {
            result += "+";
        }
        return result;
    }

    private String figureToNotation(Figure figure) {
        switch (figure.getType()) {
            case KING:
                return "K";
            case BISHOP:
                return "L";
            case KNIGHT:
                return "S";
            case QUEEN:
                return "D";
            case ROOK:
                return "T";
            default:
            case PAWN:
                return "";
        }
    }

    public Move createMove(Pos p1, Pos p2) {
        return new Move(p1, get(p1), p2, get(p2));
    }

    public Board doMove(Move move) {
        set(move.f1(), move.p2());
        set(Figure.NONE, move.p1());
        if (move.casteling() != Move.Casteling.NONE) {
            switch (move.f1().getColor()) {
                case WHITE:
                    switch (move.casteling()) {
                        case KING_SIDE_CASTELING:
                            set(Figure.WHITE_ROOK, Pos.of("a3"));
                            set(Figure.NONE, Pos.of("a1"));
                            break;
                        case QUEEN_SIDE_CASTELING:
                            set(Figure.WHITE_ROOK, Pos.of("a6"));
                            set(Figure.NONE, Pos.of("a8"));
                    }
                    break;
                case BROWN:
                    switch (move.casteling()) {
                        case KING_SIDE_CASTELING:
                            set(Figure.BROWN_ROOK, Pos.of("k6"));
                            set(Figure.NONE, Pos.of("m8"));
                            break;
                        case QUEEN_SIDE_CASTELING:
                            set(Figure.BROWN_ROOK, Pos.of("h3"));
                            set(Figure.NONE, Pos.of("f1"));
                    }
                    break;
                case BLACK:
                    switch (move.casteling()) {
                        case KING_SIDE_CASTELING:
                            set(Figure.BLACK_ROOK, Pos.of("h13"));
                            set(Figure.NONE, Pos.of("f13"));
                            break;
                        case QUEEN_SIDE_CASTELING:
                            set(Figure.BLACK_ROOK, Pos.of("k13"));
                            set(Figure.NONE, Pos.of("m13"));
                    }
                    break;
            }
        }
        moves.add(move);
        return this;
    }

    public void notifyMoveListeners() {
        notifyMoveListeners(new MoveEvent(moves.size(), moves.get(moves.size() - 1), false));
    }

//    public Board undoLastMove() {
//        if (moves.size() > 0) {
//            var move = moves.get(moves.size() - 1);
//            set(move.f1(), move.p1());
//            set(move.f2(), move.p2());
//            if (move.casteling() != Move.Casteling.NONE) {
//                switch (move.f1().getColor()) {
//                    case WHITE:
//                        switch (move.casteling()) {
//                            case KING_SIDE_CASTELING:
//                                set(Figure.NONE, Pos.of("a3"));
//                                set(Figure.WHITE_ROOK, Pos.of("a1"));
//                                break;
//                            case QUEEN_SIDE_CASTELING:
//                                set(Figure.NONE, Pos.of("a6"));
//                                set(Figure.WHITE_ROOK, Pos.of("a8"));
//                        }
//                        break;
//                    case BROWN:
//                        switch (move.casteling()) {
//                            case KING_SIDE_CASTELING:
//                                set(Figure.NONE, Pos.of("k6"));
//                                set(Figure.BROWN_ROOK, Pos.of("m8"));
//                                break;
//                            case QUEEN_SIDE_CASTELING:
//                                set(Figure.NONE, Pos.of("h3"));
//                                set(Figure.BROWN_ROOK, Pos.of("f1"));
//                        }
//                        break;
//                    case BLACK:
//                        switch (move.casteling()) {
//                            case KING_SIDE_CASTELING:
//                                set(Figure.NONE, Pos.of("h13"));
//                                set(Figure.BLACK_ROOK, Pos.of("f13"));
//                                break;
//                            case QUEEN_SIDE_CASTELING:
//                                set(Figure.NONE, Pos.of("k13"));
//                                set(Figure.BLACK_ROOK, Pos.of("m13"));
//                        }
//                        break;
//                }
//            }
//            if (move.isDisablesCasteling()) {
//                enableCasteling(move.f1().getColor());
//            }
//            moves.remove(move);
//            notifyMoveListeners(new MoveEvent(moves.size(), move, true));
//        }
//        return this;
//    }

    public Board disableCasteling(Color color) {
        castelingForbidden.add(color);
        return this;
    }

    public Board enableCasteling(Color color) {
        castelingForbidden.remove(color);
        return this;
    }

    public boolean isCastelingForbidden(Color color) {
        return castelingForbidden.contains(color);
    }

    public Board clear() {
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 8 + j; i++) {
                set(Figure.NONE, Pos.of(i, j));
            }
        }
        for (int j = 6; j < 13; j++) {
            for (int i = j - 5; i < 13; i++) {
                set(Figure.NONE, Pos.of(i, j));
            }
        }
        Arrays.stream(Color.values()).forEach(color -> kingsPositions.put(color, Pos.of(-1, -1)));
        return this;
    }

    public Board refresh() {
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 8 + j; i++) {
                notifyFieldChangeListeners(new FieldChangeEvent(Pos.of(i, j), Figure.NONE, get(Pos.of(i, j))));
            }
        }
        for (int j = 6; j < 13; j++) {
            for (int i = j - 5; i < 13; i++) {
                notifyFieldChangeListeners(new FieldChangeEvent(Pos.of(i, j), Figure.NONE, get(Pos.of(i, j))));
            }
        }
        return this;
    }

    public void setup() {
        set(Figure.WHITE_ROOK, Pos.of("a1"));
        set(Figure.WHITE_BISHOP, Pos.of("a2"));
        set(Figure.WHITE_KNIGHT, Pos.of("a3"));
        set(Figure.WHITE_KING, Pos.of("a4"));
        set(Figure.WHITE_QUEEN, Pos.of("a5"));
        set(Figure.WHITE_BISHOP, Pos.of("a6"));
        set(Figure.WHITE_KNIGHT, Pos.of("a7"));
        set(Figure.WHITE_ROOK, Pos.of("a8"));
        for (int i = 0; i < 9; i++) {
            set(Figure.WHITE_PAWN, Pos.of(i, 1));
        }

        set(Figure.BROWN_ROOK, Pos.of("m8"));
        set(Figure.BROWN_BISHOP, Pos.of("l7"));
        set(Figure.BROWN_KNIGHT, Pos.of("k6"));
        set(Figure.BROWN_KING, Pos.of("j5"));
        set(Figure.BROWN_QUEEN, Pos.of("i4"));
        set(Figure.BROWN_BISHOP, Pos.of("h3"));
        set(Figure.BROWN_KNIGHT, Pos.of("g2"));
        set(Figure.BROWN_ROOK, Pos.of("f1"));
        for (int i = 0; i < 9; i++) {
            set(Figure.BROWN_PAWN, Pos.of(i, 4 + i));
        }

        set(Figure.BLACK_ROOK, Pos.of("f13"));
        set(Figure.BLACK_BISHOP, Pos.of("g13"));
        set(Figure.BLACK_KNIGHT, Pos.of("h13"));
        set(Figure.BLACK_KING, Pos.of("i13"));
        set(Figure.BLACK_QUEEN, Pos.of("j13"));
        set(Figure.BLACK_BISHOP, Pos.of("k13"));
        set(Figure.BLACK_KNIGHT, Pos.of("l13"));
        set(Figure.BLACK_ROOK, Pos.of("m13"));
        for (int i = 0; i < 9; i++) {
            set(Figure.BLACK_PAWN, Pos.of(11, 4 + i));
        }

        moves.clear();
        this.castelingForbidden.clear();
        setCheck(Collections.emptySet());
        setPlayer(Color.WHITE);
    }

    public Board addFieldChangeListener(Consumer<FieldChangeEvent> listener) {
        this.fieldChangeListeners.add(listener);
        return this;
    }

    private void notifyFieldChangeListeners(FieldChangeEvent event) {
        fieldChangeListeners.forEach(c -> c.accept(event));
    }

    public Board addPlayerChangeListener(Consumer<PlayerChangeEvent> listener) {
        this.playerChangeListeners.add(listener);
        return this;
    }

    private void notifyPlayerChangeListeners(PlayerChangeEvent event) {
        playerChangeListeners.forEach(c -> c.accept(event));
    }

    public Board addMoveListener(Consumer<MoveEvent> listener) {
        this.moveListeners.add(listener);
        return this;
    }

    private void notifyMoveListeners(MoveEvent event) {
        moveListeners.forEach(c -> c.accept(event));
    }

    public Board addCheckListener(Consumer<CheckEvent> listener) {
        this.checkListeners.add(listener);
        return this;
    }

    private void notifyCheckListeners(CheckEvent event) {
        checkListeners.forEach(c -> c.accept(event));
    }

    public static class FieldChangeEvent {
        private final Pos pos;
        private final Figure oldValue;
        private final Figure newValue;

        public FieldChangeEvent(Pos pos, Figure oldValue, Figure newValue) {
            this.pos = pos;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public Pos getPos() {
            return pos;
        }

        public Figure getOldValue() {
            return oldValue;
        }

        public Figure getNewValue() {
            return newValue;
        }
    }

    public static class MoveEvent {
        private final int moveCount;
        private final Move move;
        private final boolean undoLastMove;

        public MoveEvent(int moveCount, Move move, boolean undoLastMove) {
            this.moveCount = moveCount;
            this.move = move;
            this.undoLastMove = undoLastMove;
        }

        public int getMoveCount() {
            return moveCount;
        }

        public Move getMove() {
            return move;
        }

        public boolean isUndoLastMove() {
            return undoLastMove;
        }
    }

    public static class PlayerChangeEvent {
        private final Color oldValue;
        private final Color newValue;

        public PlayerChangeEvent(Color oldValue, Color newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public Color getNewValue() {
            return newValue;
        }

        public Color getOldValue() {
            return oldValue;
        }
    }

    public static class CheckEvent {
        private final Set<Color> oldValue;
        private final Set<Color> newValue;

        public CheckEvent(Set<Color> oldValue, Set<Color> newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public Set<Color> getOldValue() {
            return oldValue;
        }

        public Set<Color> getNewValue() {
            return newValue;
        }
    }
}
