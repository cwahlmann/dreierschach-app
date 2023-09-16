package de.dreierschach.app.views.dreierschach;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.dom.DisabledUpdateMode;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import de.dreierschach.app.BoardExporter;
import de.dreierschach.app.engine.DreierschachEngine;
import de.dreierschach.app.model.*;
import de.dreierschach.app.views.MainLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.CanvasRenderingContext2D;

@PageTitle("Dreierschach")
@Route(value = "main", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class DreierschachView extends HorizontalLayout {

    private static final String[] COLORS_BG = {"#614f32", "#f0f0e0", "#b99f77"};

    private final BoardExporter boardExporter;
    private Canvas canvas;

    private final HorizontalLayout content;
    private final TextArea protocol;
    private final NativeLabel player;
    private final HorizontalLayout check;
    private final Button zoomInButton;
    private final Button zoomOutButton;
    private final Button exportButton;

    private double width = 1024;
    private int height;
    private double tileWidth;
    private double tileHeight;

    private final Board board;

    private final DreierschachEngine engine;

    private Pos from = null;

    public DreierschachView(DreierschachEngine engine, BoardExporter boardExporter) {
        this.engine = engine;
        this.boardExporter = boardExporter;
        board = this.engine.board();

        setMargin(true);

        content = new HorizontalLayout();
        updateContent(1000);

        board.addFieldChangeListener(e -> drawFigure(e.getNewValue(), e.getPos().x(), e.getPos().y(), false));
        board.addMoveListener(this::addProtocol);
        board.addCheckListener(this::onCheckChanged);
        board.addPlayerChangeListener(this::onPlayerChanged);

        protocol = new TextArea();
        protocol.setReadOnly(true);
        protocol.setWidth(30, Unit.EM);
        protocol.setHeight(100, Unit.PERCENTAGE);

        player = new NativeLabel();
        check = new HorizontalLayout();

        var state = new HorizontalLayout(new VerticalLayout(player), check);

        zoomInButton = new Button(VaadinIcon.PLUS.create());
        zoomInButton.addClickShortcut(Key.KEY_Q);
        zoomInButton.getElement().setProperty("title", "[q]");
        zoomInButton.getStyle().set("--lumo-button-size", "var(--lumo-size-xs)");
        zoomOutButton = new Button(VaadinIcon.MINUS.create());
        zoomOutButton.addClickShortcut(Key.KEY_A);
        zoomOutButton.getElement().setProperty("title", "[a]");
        zoomOutButton.getStyle().set("--lumo-button-size", "var(--lumo-size-xs)");
        exportButton = new Button("Export");
        exportButton.addClickListener(event -> System.out.println(boardExporter.toJson(board)));
        var zoomPanel = new VerticalLayout(zoomInButton, zoomOutButton, exportButton);
        zoomPanel.setSizeUndefined();
        zoomInButton.addClickListener(event -> {
            if (this.width <= 2500) {
                updateContent(this.width * 1.2);
            }
        });
        zoomOutButton.addClickListener(event -> {
            if (this.width >= 480) {
                updateContent(this.width / 1.2);
            }
        });

        var right = new VerticalLayout();
        right.setHeight(100, Unit.PERCENTAGE);
        right.add(state);
        right.addAndExpand(protocol);
        setWidth(100, Unit.PERCENTAGE);
        add(zoomPanel);
        add(content);
        add(right);
        engine.startGame();
    }

    private void updateContent(double width) {
        content.removeAll();

        this.width = width;
        var tileWidth = width / 13.0;
        var tileHeight = tileWidth / Math.sqrt(3) * 2;
        var height = tileHeight * 10;

        this.tileWidth = (int) tileWidth;
        this.tileHeight = (int) tileHeight;
        this.height = (int) height;

        canvas = new Canvas((int) width, this.height);
        canvas.getElement().addEventListener("click", this::onDomEvent).addEventData("event.offsetX")
                .addEventData("event.offsetY").setDisabledUpdateMode(DisabledUpdateMode.ALWAYS);
        canvas.setSizeUndefined();
        content.add(canvas);
        board.refresh();
        if (from != null) {
            drawFigure(board.get(from), from.x(), from.y(), true);
        }

    }

    private void onDomEvent(DomEvent event) {
        var offsetX = (int) event.getEventData().getNumber("event.offsetX");
        var offsetY = (int) event.getEventData().getNumber("event.offsetY");
        int y = 12 - (int) ((offsetY - tileHeight * 0.125) / (tileHeight * 0.75));
        int x = (int) ((offsetX + (y - 5) * tileWidth / 2) / tileWidth);
        var pos = Pos.of(x, y);
        if (pos.isValid()) {
            if (from == null) {
                if (board.get(pos) != Figure.NONE && board.get(pos).getColor() == board.getPlayer()) {
                    drawFigure(board.get(pos), pos.x(), pos.y(), true);
                    from = pos;
                }
                return;
            }
            if (!engine.move(from, pos)) {
                drawFigure(board.get(from), from.x(), from.y(), false);
            }
            from = null;
        }
    }

    private void drawTile(int x, int y, boolean marked) {
        CanvasRenderingContext2D ctx = canvas.getContext();

        int bg = (x + y) % 3;

        var pos = position(x, y);
        var px = pos.getLeft();
        var py = pos.getRight();

        ctx.setFillStyle(marked ? "green" : COLORS_BG[bg]);
        ctx.setStrokeStyle(marked ? "green" : COLORS_BG[bg]);
        ctx.setLineWidth(0);
        ctx.beginPath();
        ctx.moveTo(px + tileWidth / 2, py);
        ctx.lineTo(px + tileWidth, py + tileHeight * .25);
        ctx.lineTo(px + tileWidth, py + tileHeight * .75);
        ctx.lineTo(px + tileWidth / 2, py + tileHeight);
        ctx.lineTo(px, py + tileHeight * .75);
        ctx.lineTo(px, py + tileHeight * .25);
        ctx.lineTo(px + tileWidth / 2, py);
        ctx.fill();
        ctx.closePath();
        ctx.stroke();
    }

    private void drawFigure(Figure figure, int x, int y, boolean marked) {
        drawTile(x, y, marked);
        if (figure == Figure.NONE) {
            return;
        }
        var pos = position(x, y);
        var px = pos.getLeft() + tileWidth * 0.15;
        var py = pos.getRight() + tileHeight * 0.15;
        String img = getImage(figure);
        CanvasRenderingContext2D ctx = canvas.getContext();
        ctx.drawImage(img, px, py, tileWidth * 0.7, tileHeight * 0.7);
    }

    public String getImage(Figure figure) {
        if (figure == Figure.NONE) {
            return "";
        }
        return getImage(figure.getColor(), figure.getType());
    }

    public String getImage(Color color, FigureType type) {
        return "images/" + color.name().toLowerCase() + "_" + type.name().toLowerCase() + ".svg";
    }

    public String getImageCheck(Color color) {
        return "images/" + color.name().toLowerCase() + "_" + FigureType.KING.name().toLowerCase() + "_check.svg";
    }

    private Pair<Double, Double> position(int x, int y) {
        return Pair.of(x * tileWidth + (5 - y) * tileWidth / 2, (12 - y) * tileHeight * 0.75);
    }

    private void addProtocol(Board.MoveEvent event) {
        String value = protocol.getValue();
        var f1 = event.getMove().f1();
        var c = f1.getColor();
        if (c == Color.WHITE) {
            if (StringUtils.isNotEmpty(value)) {
                value += "\n";
            }
            value += event.getMoveCount() + ": ";
        } else {
            value += ", ";
        }
        value += board.getLastMoveAsString();
        protocol.setValue(value);
    }

    private void onCheckChanged(Board.CheckEvent event) {
        check.removeAll();
        event.getNewValue().forEach(player -> {
            check.add(new Image(getImageCheck(player), player.name() + "+"));
        });
    }

    private void onPlayerChanged(Board.PlayerChangeEvent event) {
        player.setText("It's " + event.getNewValue().name() + "s move.");
        player.setClassName(event.getNewValue().name().toLowerCase());
    }
}
