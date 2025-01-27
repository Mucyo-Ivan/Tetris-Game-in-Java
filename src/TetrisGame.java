import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private static final int BOARD_WIDTH = 10; // Adjusted width
    private static final int BOARD_HEIGHT = 20; // Adjusted height
    private static final int TILE_SIZE = 25; // Adjusted tile size
    private static final Color[] COLORS = {
            Color.BLACK, Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED
    };

    private final int[][][] TETRIMINOS = {
            {}, // Empty (index 0)
            {{1, 1, 1, 1}}, // I shape
            {{2, 0, 0}, {2, 2, 2}}, // J shape
            {{0, 0, 3}, {3, 3, 3}}, // L shape
            {{4, 4}, {4, 4}},       // O shape
            {{0, 5, 5}, {5, 5, 0}}, // S shape
            {{0, 6, 0}, {6, 6, 6}}, // T shape
            {{7, 7, 0}, {0, 7, 7}}  // Z shape
    };

    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int currentPiece, nextPiece;
    private int[][] currentShape;
    private int currentX, currentY;
    private boolean isGameOver = false;
    private int score = 0;
    private long startTime; // For bonus scoring based on time
    private int speed = 500; // Initial speed (milliseconds)

    private Timer timer;

    public TetrisGame() {
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE + 150, BOARD_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        startGame();
    }

    private void startGame() {
        timer = new Timer(speed, this);
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        currentPiece = 0;
        nextPiece = new Random().nextInt(TETRIMINOS.length - 1) + 1;
        spawnPiece();
        score = 0;
        timer.start();
    }

    private void spawnPiece() {
        Random random = new Random();
        currentPiece = nextPiece;
        nextPiece = random.nextInt(TETRIMINOS.length - 1) + 1;
        currentShape = TETRIMINOS[currentPiece];
        currentX = BOARD_WIDTH / 2 - currentShape[0].length / 2;
        currentY = 0;
        startTime = System.currentTimeMillis();

        if (!canMove(currentX, currentY, currentShape)) {
            isGameOver = true;
            timer.stop();
        }
    }

    private boolean canMove(int x, int y, int[][] shape) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != 0) {
                    int newX = x + col;
                    int newY = y + row;
                    if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT || board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placePiece() {
        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                if (currentShape[row][col] != 0) {
                    board[currentY + row][currentX + col] = currentPiece;
                }
            }
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            boolean fullLine = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                linesCleared++;
                for (int i = row; i > 0; i--) {
                    board[i] = board[i - 1].clone();
                }
                board[0] = new int[BOARD_WIDTH];
            }
        }

        // Update score based on lines cleared
        if (linesCleared > 0) {
            int timeBonus = (int) (1000 / Math.max(1, (System.currentTimeMillis() - startTime) / 100)); // Bonus for speed
            score += switch (linesCleared) {
                case 1 -> 100 + timeBonus;
                case 2 -> 300 + timeBonus;
                case 3 -> 500 + timeBonus;
                case 4 -> 800 + timeBonus;
                default -> 0;
            };
            // Increase game speed
            if (speed > 100) {
                speed -= 10;
                timer.setDelay(speed);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isGameOver) return;

        if (canMove(currentX, currentY + 1, currentShape)) {
            currentY++;
        } else {
            placePiece();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] != 0) {
                    g.setColor(COLORS[board[row][col]]);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw current piece
        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                if (currentShape[row][col] != 0) {
                    g.setColor(COLORS[currentPiece]);
                    g.fillRect((currentX + col) * TILE_SIZE, (currentY + row) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw next piece
        g.setColor(Color.WHITE);
        g.drawString("Next Piece:", BOARD_WIDTH * TILE_SIZE + 20, 30);
        for (int row = 0; row < TETRIMINOS[nextPiece].length; row++) {
            for (int col = 0; col < TETRIMINOS[nextPiece][row].length; col++) {
                if (TETRIMINOS[nextPiece][row][col] != 0) {
                    g.setColor(COLORS[nextPiece]);
                    g.fillRect(BOARD_WIDTH * TILE_SIZE + 50 + col * TILE_SIZE / 2, 50 + row * TILE_SIZE / 2, TILE_SIZE / 2, TILE_SIZE / 2);
                }
            }
        }

        // Draw score
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, BOARD_WIDTH * TILE_SIZE + 20, 150);

        // Draw game over message
        if (isGameOver) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", BOARD_WIDTH * TILE_SIZE / 2 - 40, BOARD_HEIGHT * TILE_SIZE / 2);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isGameOver) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                if (canMove(currentX - 1, currentY, currentShape)) currentX--;
            }
            case KeyEvent.VK_RIGHT -> {
                if (canMove(currentX + 1, currentY, currentShape)) currentX++;
            }
            case KeyEvent.VK_DOWN -> {
                if (canMove(currentX, currentY + 1, currentShape)) currentY++;
            }
            case KeyEvent.VK_UP -> {
                int[][] rotated = rotatePiece(currentShape);
                if (canMove(currentX, currentY, rotated)) currentShape = rotated;
            }
        }
        repaint();
    }

    private int[][] rotatePiece(int[][] shape) {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                rotated[col][shape.length - 1 - row] = shape[row][col];
            }
        }
        return rotated;
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new TetrisGame());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
