package de.dreierschach.app.engine;

import de.dreierschach.app.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DreierschachEngineImpl implements DreierschachEngine {
    private static Logger log = LoggerFactory.getLogger(DreierschachEngineImpl.class);

    private Board board;

    public DreierschachEngineImpl() {
        board = new Board();
    }

    @Override
    public DreierschachEngineImpl withBoard(Board board) {
        this.board = board;
        return this;
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public DreierschachEngineImpl startGame() {
        board.clear();
        board.setup();
        return this;
    }

    @Override
    public boolean move(Pos source, Pos destination) {
        return move(new Move(source, board.get(source), destination, board.get(destination)));
    }

    @Override
    public boolean move(Move m) {
        var move = m;
        if (!validateMoveOrAttack(move)) {
            return false;
        }
        var figure = move.f1();
        if (figure != Figure.NONE) {
            // disable Casteling, when King or Rook are moved
            if (figure.getType() == FigureType.KING || figure.getType() == FigureType.ROOK) {
                board.disableCasteling(figure.getColor());
                if (!board.isCastelingForbidden(figure.getColor())) {
                    log.info("Disable casteling for player {}", figure.getColor());
                    move = move.withDisablesCasteling();
                }
            }
            var b = Board.copyOf(board);
            b.doMove(move);
            checkCheck(b);
            if (b.isCheck(b.getPlayer())) {
                return false;
            }
            board.doMove(move);
            board.nextPlayer();
            checkCheck(board);
            board.notifyMoveListeners();
            return true;
        }
        return false;
    }

    @Override
    public boolean validateMoveOrAttack(Move move) {
        if (move.p2().equals(move.p1()) || !move.p1().isValid() || !move.p2().isValid()) {
            return false;
        }
        var f1 = move.f1();
        if (f1 == Figure.NONE || f1.getColor() != board.getPlayer()) {
            log.info("Move {} is not allowed: figure = {}, active player = {} ", move, f1, board.getPlayer());
            return false;
        }
        var f2 = move.f2();
        if (f2 == Figure.NONE) {
            return isValidMove(board, move);
        }
        if (f2.getColor() == f1.getColor()) {
            log.info("player {} is not allowed to kill its own figure", board.getPlayer());
            return false;
        }
        return isValidAttack(board, move);
    }

    List<Pos> getAttacks(Board b, Color player, Pos pos) {
        var result = new ArrayList<Pos>();
        if (!pos.isValid()) {
            return result;
        }
        result.addAll(getAttacksStraight(b, player, pos));
        result.addAll(getAttacksDiagonal(b, player, pos));
        result.addAll(getAttacksJump(b, player, pos));
        return result;
    }

    void checkCheck(Board b) {
        b.setCheck(Arrays.stream(Color.values())
                .filter(color -> getAttacks(b, color, b.getKingsPosition(color)).size() > 0)
                .collect(Collectors.toSet()));
    }

    void checkCheckmate(Board b) {
        var player = b.getPlayer();
        // TODO: implement
        // is player check?
        // is one opponent and may it be killed?
        // is one opponent and may it be blocked?
        // is more than one opponent and my they both be blocked or one killed and the others blocked?
    }

    List<Pos> getAttacksStraight(Board b, Color player, Pos pos) {
        //@formatter:off
        return scan(b, pos, Dir.Type.straight).stream()
                .filter(p -> b.get(p).getColor() != player)
                .filter(p -> List.of(FigureType.ROOK, FigureType.KING,  FigureType.QUEEN).contains(b.get(p).getType()))
                .filter(p -> isValidAttack(b, new Move(p, b.get(p), pos, Figure.NONE)))
                .collect(Collectors.toList());
        //@formatter:on
    }

    List<Pos> getAttacksDiagonal(Board b, Color player, Pos pos) {
        //@formatter:off
        return scan(b, pos, Dir.Type.diagonal).stream()
                .filter(p -> b.get(p).getColor() != player)
                .filter(p -> List.of(FigureType.BISHOP, FigureType.KING,  FigureType.QUEEN, FigureType.PAWN).contains(b.get(p).getType()))
                .filter(p -> isValidAttack(b, new Move(p, b.get(p), pos, Figure.NONE)))
                .collect(Collectors.toList());
        //@formatter:on
    }

    List<Pos> getAttacksJump(Board b, Color player, Pos pos) {
        return scan(b, pos, Dir.Type.jump).stream().filter(p -> b.get(p).getColor() != player)
                .filter(p -> b.get(p).getType() == FigureType.KNIGHT)
                .filter(p -> isValidAttack(b, new Move(p, b.get(p), pos, Figure.NONE))).collect(Collectors.toList());
    }

    /**
     * Prüfe, ob von Pos ausgehend der diagonale Weg in Richgung dir frei ist.
     * Dafür muss eines der benachbarten Felder, durch die hindurchgezogen werden soll, frei sein.
     *
     * @param pos Ausgangsposition
     * @param dir diagonale Richtung
     * @return true, wenn der Weg frei ist.
     */
    boolean isDiagonalFreeToMove(Board b, Pos pos, Dir dir) {
        var straightDir = Dir.get(Dir.Type.straight, dir.getIndex());
        Pos left = Pos.add(pos, straightDir);
        Pos right = Pos.add(pos, Dir.rotateRight(straightDir));
        return b.get(left) == Figure.NONE || b.get(right) == Figure.NONE;
    }

    List<Pos> scan(Board b, Pos pos, Dir.Type type) {
        var result = new ArrayList<Pos>();
        for (int i = 0; i < type.size(); i++) {
            var dir = Dir.get(type, i);
            var p = pos;
            switch (type) {
                case straight:
                    do {
                        p = Pos.add(p, dir);
                    } while (p.isValid() && b.get(p) == Figure.NONE);
                    if (p.isValid()) {
                        result.add(p);
                    }
                    break;
                case diagonal:
                    if (isDiagonalFreeToMove(b, p, dir)) {
                        do {
                            p = Pos.add(p, dir);
                        } while (p.isValid() && b.get(p) == Figure.NONE && isDiagonalFreeToMove(b, p, dir));
                        if (p.isValid() && b.get(p) != Figure.NONE) {
                            result.add(p);
                        }
                    }
                    break;
                case jump:
                    p = Pos.add(p, dir);
                    if (p.isValid() && b.get(p) != Figure.NONE) {
                        result.add(p);
                    }
                default:
            }
        }
        return result;
    }

    /**
     * checks if figure on Pos source may attack field Pos destination
     * there is no check weather the destination field is empty or not
     *
     * @param move the attacking move
     * @return true, when the attack is possible
     */
    boolean isValidAttack(Board b, Move move) {
        switch (move.f1().getType()) {
            case KNIGHT:
                return isValidMoveKnight(b, move);
            case ROOK:
                return isValidMoveRook(b, move);
            case BISHOP:
                return isValidMoveBishop(b, move);
            case QUEEN:
                return isValidMoveQueen(b, move);
            case KING:
                return isValidMoveKing(b, move);
            case PAWN:
                return isValidKillPawn(b, move);
            default:
                return false;
        }
    }

    /**
     * checks if a move or attack is valid
     *
     * @param move the move or attack
     * @return true, if the move or attack is possible
     */
    boolean isValidMove(Board b, Move move) {
        switch (move.f1().getType()) {
            case KNIGHT:
                return isValidMoveKnight(b, move);
            case ROOK:
                return isValidMoveRook(b, move);
            case BISHOP:
                return isValidMoveBishop(b, move);
            case QUEEN:
                return isValidMoveQueen(b, move);
            case KING:
                return isValidRoachadeKing(b, move) || isValidMoveKing(b, move);
            case PAWN:
                return isValidMovePawn(b, move);
            default:
                return false;
        }
    }

    boolean isValidMoveKnight(Board b, Move move) {
        var dirDistance = Dir.find(move.p1(), move.p2()).orElse(null);
        return dirDistance != null && dirDistance.getLeft().getType() == Dir.Type.jump && dirDistance.getRight() == 1;
    }

    boolean isValidMoveRook(Board b, Move move) {
        var dirDistance = Dir.find(move.p1(), move.p2()).orElse(null);
        if (dirDistance == null || dirDistance.getLeft().getType() != Dir.Type.straight) {
            return false;
        }
        return isFree(b, move.p1(), dirDistance.getLeft(), dirDistance.getRight() - 1);
    }

    boolean isValidMoveBishop(Board b, Move move) {
        var dirDistance = Dir.find(move.p1(), move.p2()).orElse(null);
        if (dirDistance == null || dirDistance.getLeft().getType() != Dir.Type.diagonal) {
            return false;
        }
        return isFree(b, move.p1(), dirDistance.getLeft(), dirDistance.getRight() - 1);
    }

    boolean isValidMoveQueen(Board b, Move move) {
        var dirDistance = Dir.find(move.p1(), move.p2()).orElse(null);
        if (dirDistance == null) {
            return false;
        }
        var type = dirDistance.getLeft().getType();
        if (type != Dir.Type.straight && type != Dir.Type.diagonal) {
            return false;
        }
        return isFree(b, move.p1(), dirDistance.getLeft(), dirDistance.getRight() - 1);
    }

    boolean isValidRoachadeKing(Board b, Move move) {
        var f = move.f1();
        if (b.isCastelingForbidden(f.getColor())) {
            return false;
        }
        if (b.isCheck(f.getColor())) {
            return false;
        }
        switch (f.getColor()) {
            case WHITE:
                if (move.p2().equals(Pos.of("a2"))) {
                    return isSafe(b, move.p1(), Dir.get(Dir.Type.straight, 0), 2);
                }
                if (move.p2().equals(Pos.of("a7"))) {
                    return isSafe(b, move.p1(), Dir.get(Dir.Type.straight, 3), 3);
                }
                return false;
            case BROWN:
                if (move.p2().equals(Pos.of("l7"))) {
                    return isSafe(b, move.p1(), Dir.get(Dir.Type.straight, 2), 2);
                }
                if (move.p2().equals(Pos.of("g2"))) {
                    return isSafe(b, move.p1(), Dir.get(Dir.Type.straight, 5), 3);
                }
                return false;
            case BLACK:
                if (move.p2().equals(Pos.of("g13"))) {
                    return isSafe(b, move.p1(), Dir.get(Dir.Type.straight, 4), 2);
                }
                if (move.p2().equals(Pos.of("l13"))) {
                    return isSafe(b, move.p1(), Dir.get(Dir.Type.straight, 1), 3);
                }
                return false;
            default:
                return false;
        }
    }

    boolean isValidMoveKing(Board b, Move move) {
        var dirDistance = Dir.find(move.p1(), move.p2()).orElse(null);
        if (dirDistance == null) {
            return false;
        }
        if (dirDistance.getRight() != 1) {
            return false;
        }
        var type = dirDistance.getLeft().getType();
        if (type == Dir.Type.diagonal) {
            return isDiagonalFreeToMove(b, move.p1(), dirDistance.getLeft());
        }
        return type == Dir.Type.straight;
    }

    boolean isValidMovePawn(Board b, Move move) {
        var dirDistance = Dir.find(move.p1(), move.p2()).orElse(null);
        if (dirDistance == null) {
            return false;
        }

        var dir = dirDistance.getLeft();
        var distance = dirDistance.getRight();

        var f1 = move.f1();
        if (!isForward(dir, f1.getColor())) {
            return false;
        }

        if (dir.getType() != Dir.Type.straight) {
            return false;
        }
        if (distance == 1) {
            return b.get(Pos.add(move.p1(), dir)) == Figure.NONE;
        }
        if (distance > 2 || !isPawnBaseline(move.p1(), f1.getColor())) {
            return false;
        }
        return isFree(b, move.p1(), dir, 2);
    }

    boolean isValidKillPawn(Board b, Move move) {
        var optionalDir = Dir.find(move.p1(), move.p2());
        if (optionalDir.isEmpty()) {
            return false;
        }

        var dir = optionalDir.get().getLeft();
        var distance = optionalDir.get().getRight();
        var f1 = move.f1();

        if (!isForward(dir, f1.getColor())) {
            return false;
        }
        if (dir.getType() != Dir.Type.diagonal) {
            return false;
        }
        if (distance != 1) {
            return false;
        }
        return isDiagonalFreeToMove(b, move.p1(), dir);
    }

    boolean isSafe(Board b, Pos pos, Dir dir, int length) {
        if (length == 0) {
            return true;
        }
        var f = b.get(pos);
        var p = pos;
        for (int i = 0; i < length; i++) {
            if (dir.getType() == Dir.Type.diagonal) {
                if (!isDiagonalFreeToMove(b, p, dir)) {
                    return false;
                }
            }
            p = Dir.add(p, dir);
            if (!p.isValid() || b.get(p) != Figure.NONE) {
                return false;
            }
            if (!getAttacks(b, f.getColor(), p).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    boolean isFree(Board b, Pos pos, Dir dir, int length) {
        if (length == 0) {
            return dir.getType() != Dir.Type.diagonal || isDiagonalFreeToMove(b, pos, dir);
        }
        var p = pos;
        for (int i = 0; i < length; i++) {
            if (dir.getType() == Dir.Type.diagonal) {
                if (!isDiagonalFreeToMove(b, p, dir)) {
                    return false;
                }
            }
            p = Dir.add(p, dir);
            if (!p.isValid() || b.get(p) != Figure.NONE) {
                return false;
            }
        }
        return true;
    }

    boolean isForward(Dir dir, Color color) {
        var i = dir.getIndex();
        switch (dir.getType()) {
            case jump:
                switch (color) {
                    case WHITE:
                        return i >= 0 && i <= 5;
                    case BROWN:
                        return i >= 4 && i <= 9;
                    case BLACK:
                        return i >= 8 || i <= 2;
                }
            case diagonal:
                switch (color) {
                    case WHITE:
                        return i >= 0 && i <= 2;
                    case BROWN:
                        return i >= 2 && i <= 4;
                    case BLACK:
                        return i >= 4 || i == 0;
                }
            case straight:
                switch (color) {
                    case WHITE:
                        return i == 1 || i == 2;
                    case BROWN:
                        return i == 3 || i == 4;
                    case BLACK:
                        return i == 5 || i == 0;
                }
            default:
                return false;
        }
    }

    boolean isPawnBaseline(Pos pos, Color color) {
        switch (color) {
            case WHITE:
                return pos.y() == 1;
            case BROWN:
                return pos.y() - pos.x() == 4;
            case BLACK:
                return pos.x() == 11;
            default:
                return false;
        }
    }
}
