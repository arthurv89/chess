package nl.arthurvlug.chess.gui.components.board;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Optional;
import javax.swing.JPanel;
import nl.arthurvlug.chess.gui.board.AbstractChessFont;
import nl.arthurvlug.chess.gui.game.Game;
import nl.arthurvlug.chess.gui.game.player.HumanPlayer;
import nl.arthurvlug.chess.utils.board.Board;
import nl.arthurvlug.chess.utils.board.Coordinates;
import nl.arthurvlug.chess.utils.board.pieces.ColoredPiece;
import nl.arthurvlug.chess.utils.game.Move;
import rx.Subscriber;

@SuppressWarnings("serial")
public class BoardPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final String FRITZ_FONT_FILENAME = "DiaTTFri.ttf";

	private static final int BOARD_OFFSET = 80;
	
	private final AbstractChessFont chessFont;
	private final Game game;
	private Board board;
	private Optional<Drag> drag = Optional.empty();
	private Optional<Move> lastMove = Optional.empty();
	int nodesEvaluated;

	public BoardPanel(Game game) throws FontFormatException, IOException {
		this.game = game;
		this.board = game.getBoard();
		this.chessFont = new TrueTypeFont(FRITZ_FONT_FILENAME);

		setLocation(0,  0);
		setLayout(null);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
		if(game == null) return;

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 1000, 1000);

		g2.setColor(Color.BLACK);
		drawPlayerNames(g2);
		drawBoard(board, g2);

		drag.ifPresent(d -> {
			drawStringOnLocation(d.getCurrentMouseLocation(), d.getPieceString(), g2);
		});
	}

	private void drawPlayerNames(final Graphics2D g2) {
		g2.drawString(game.getBlackPlayer().getName() + " " + nodesEvaluated, 0, 20);
		g2.drawString(game.getWhitePlayer().getName(), 0, 520);
	}

	private void drawBoard(final Board board, final Graphics2D g2) {
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				drawSquare(x, y, g2);
				drawPiece(x, y, board.getPiece(x, y), g2);
			}
		}
	}

	private void drawPiece(int xField, int yField, final Optional<ColoredPiece> coloredPieceOption, final Graphics2D g2) {
		if(coloredPieceOption.isPresent()) {
			final int fieldCenterX = (int) (xField * fieldSize() + 0.9 * fieldSize());
			final int fieldCenterY = (int) ((7 - yField) * fieldSize() + 0.75 * fieldSize());
			final String pieceString = chessFont.pieceString(coloredPieceOption.get());
			final Font font = calculateFont();
			final Coordinates coordinates = calculatePosition(pieceString, font, fieldCenterX, fieldCenterY, g2);

			drawStringOnLocation(coordinates, pieceString, g2);
		}
	}

	private Coordinates calculatePosition(final String pieceString, final Font font,
										  final int fieldCenterX, final int fieldCenterY,
										  final Graphics2D g2) {
		// Determine font and position of the string
		int stringWidth = g2.getFontMetrics(font).stringWidth(pieceString);

		int xPos = (int) Math.round(fieldCenterX - 0.5 * stringWidth);
		int yPos = Math.round(fieldCenterY + BOARD_OFFSET);
		return new Coordinates(xPos, yPos);
	}

	private Font calculateFont() {
		return chessFont.deriveFont((float) fontSize());
	}

	private void drawSquare(int x, int y, final Graphics2D g2) {
		lastMove.ifPresent(move -> {
			final Point2D from = fieldCenterPoint(move.getFrom());
			final Point2D to = fieldCenterPoint(move.getTo());
			drawArrow(g2,
					(int) from.getX(),
					(int) from.getY(),
					(int) to.getX(),
					(int) to.getY());
		});
		g2.setStroke(new BasicStroke(1.0f));
		drag.ifPresent(d -> {
			if(d.getCurrentField().getX() == x && d.getCurrentField().getY() == y) {
				g2.setColor(Color.LIGHT_GRAY);
				g2.fillRect(x * fieldSize()+1, BOARD_OFFSET + (7-y) * fieldSize()+1, fieldSize()-2, fieldSize()-2);
			}
		});
		g2.setColor(Color.BLACK);
		g2.drawRect(x * fieldSize(), BOARD_OFFSET + (7-y) * fieldSize(), fieldSize(), fieldSize());
	}

	private Point2D fieldCenterPoint(final Coordinates coordinates) {
		final int fieldCenterX = (int) (coordinates.getX() * fieldSize() + 0.5 * fieldSize());
		final int fieldCenterY = (int) ((7 - coordinates.getY()) * fieldSize() + 2*fieldSize());
		return new Point2D.Float(fieldCenterX, fieldCenterY);
	}

	private int fieldSize() {
		return 50;
	}
	
	private int fontSize() {
		return (int) Math.round(0.8 * fieldSize());
	}

	private void drawStringOnLocation(final Coordinates coordinates,
									  final String pieceString,
									  final Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.setFont(calculateFont());
		g2.drawString(pieceString,
				(int) (coordinates.getX() + (0.6-1) * fieldSize()),
				(int) (coordinates.getY() + (.95-1) * fontSize()));
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		calculateCurrentMouseField(e).ifPresent(mf -> {
			game.getBoard().getField(mf.getX(), mf.getY()).getPiece().ifPresent(selectedPiece -> {
				board = Board.cloneWithoutPiece(game.getBoard(), mf);
				drag = Optional.of(new Drag(currentMouseLocation(e), selectedPiece.getCharacterString(), mf, mf));
				repaint();
			});
		});
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		final Optional<Coordinates> mouseField = calculateCurrentMouseField(e);
		mouseField.ifPresent(mf -> {
			drag = Optional.of(new Drag(currentMouseLocation(e), drag.get().getPieceString(), drag.get().getBeginField(), mf));
			repaint();
		});
	}

	private Optional<Coordinates> calculateCurrentMouseField(final MouseEvent e) {
		final Coordinates clickedLocationRelativeToBoard = clickedLocationRelativeToBoard(e);
		final int x = clickedLocationRelativeToBoard.getX() / fieldSize();
		final int y = 7 - (clickedLocationRelativeToBoard.getY() / fieldSize());
		if(clickedLocationRelativeToBoard.getX() < 0 ||
				clickedLocationRelativeToBoard.getY() < 0 ||
				x > 7 ||
				y > 7) {
			// Out of bounds
			return Optional.empty();
		}
		return Optional.of(new Coordinates(x, y));
	}

	private Coordinates clickedLocationRelativeToBoard(final MouseEvent e) {
		int xPosRelativeToBoard = e.getX() - 1;
		int yPosRelativeToBoard = e.getY() - BOARD_OFFSET - 3;
		return new Coordinates(xPosRelativeToBoard, yPosRelativeToBoard);
	}

	private Coordinates currentMouseLocation(final MouseEvent e) {
		return new Coordinates(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(final MouseEvent e) { }

	@Override
	public void mouseClicked(final MouseEvent e) { }

	@Override
	public void mouseReleased(final MouseEvent e) {
		final Coordinates beginField = drag.get().getBeginField();
		calculateCurrentMouseField(e).ifPresent(endField -> {
			move(new Move(beginField, endField, Optional.empty()));
			drag = Optional.empty();
			repaint();
		});
	}

	@Override
	public void mouseEntered(final MouseEvent e) { }

	@Override
	public void mouseExited(final MouseEvent e) { }


	private void move(final Move move) {
		HumanPlayer blackPlayer = getHumanPlayer();
		for (Subscriber<? super Move> moveSubscriber : blackPlayer.getMoveSubscribers()) {
			moveSubscriber.onNext(move);
		}
	}

	private HumanPlayer getHumanPlayer() {
		try {
			return (HumanPlayer) game.getWhitePlayer();
		} catch(Exception e) {
			return (HumanPlayer) game.getBlackPlayer();
		}
	}

	public void setBoard(final Board board, final Move move) {
		this.board = board;
		this.lastMove = Optional.of(move);
	}

	private void drawArrow(Graphics2D g2, int node1X, int node1Y, int node2X, int node2Y) {
		double arrowAngle = Math.toRadians(45.0);
		double arrowLength = 10.0;
		double dx = node1X - node2X;
		double dy = node1Y - node2Y;
		double angle = Math.atan2(dy, dx);
		int x1 = (int) (Math.cos(angle + arrowAngle) * arrowLength + node2X);
		int y1 = (int) (Math.sin(angle + arrowAngle) * arrowLength + node2Y);

		int x2 = (int) (Math.cos(angle - arrowAngle) * arrowLength + node2X);
		int y2 = (int) (Math.sin(angle - arrowAngle) * arrowLength + node2Y);
		g2.setStroke(new BasicStroke(5.0f));
		g2.setColor(Color.RED);
		g2.drawLine(node1X, node1Y, node2X, node2Y);
		g2.drawLine(node2X, node2Y, x1, y1);
		g2.drawLine(node2X, node2Y, x2, y2);
	}

}
