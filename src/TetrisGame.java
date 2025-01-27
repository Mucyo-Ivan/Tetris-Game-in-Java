import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class TetrisGame extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int TILE_SIZE = 30;
    private Timer timer;
    private int speed = 500;
    private int level = 1;
    private int linesCleared = 0;
    private int score = 0;

    private final Color[] COLORS = {
            Color.BLACK, Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.PINK, Color.RED
    };

    private final int[][][] TETRIMINOS = {
            {},
            {{1, 1, 1, 1}}, // I shape
            {{2, 0, 0}, {2, 2, 2}}, // J shape
            {{0, 0, 3}, {3, 3, 3}}, // L shape
            {{4, 4}, {4, 4}},       // O shape
            {{0, 5, 5}, {5, 5, 0}}, // S shape
            {{0, 6, 0}, {6, 6, 6}}, // T shape
            {{7, 7, 0}, {0, 7, 7}}  // Z shape
    };

    private int[][] board;
    private int currentX, currentY, currentPiece;
    private int[][] currentShape;
    private int nextPiece;
    private boolean isGameOver;

    public TetrisGame() {
        setFocusable(true);
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE + 200, BOARD_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        addKeyListener(this);

        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        timer = new Timer(speed, this);
        startGame();
    }

    private void startGame() {
        isGameOver = false;
        level = 1;
        speed = 500;
        linesCleared = 0;
        score = 0;
        clearBoard();
        spawnPiece();
        nextPiece = new Random().nextInt(TETRIMINOS.length - 1) + 1;
        timer.setDelay(speed);
        timer.start();
    }

    private void clearBoard() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = 0;
            }
        }
    }

    private void spawnPiece() {
        currentPiece = nextPiece;
        currentShape = TETRIMINOS[currentPiece];
        currentX = BOARD_WIDTH / 2 - currentShape[0].length / 2;
        currentY = 0;

        nextPiece = new Random().nextInt(TETRIMINOS.length - 1) + 1;

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

                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT || board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void lockPiece() {
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
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            boolean fullLine = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == 0) {
                    fullLine = false;
                    break;
                }
            }

            if (fullLine) {
                for (int r = row; r > 0; r--) {
                    board[r] = board[r - 1];
                }
                board[0] = new int[BOARD_WIDTH];
                linesCleared++;
                score += 100;

                if (linesCleared % 10 == 0) {
                    level++;
                    speed = Math.max(50, speed - 50);
                    timer.setDelay(speed);
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPiece(g);
        drawNextPiece(g);
        drawScore(g);

        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over", getWidth() / 4, getHeight() / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER to Restart", getWidth() / 4, getHeight() / 2 + 30);
        }
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                int value = board[row][col];
                if (value != 0) {
                    g.setColor(COLORS[value]);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawPiece(Graphics g) {
        g.setColor(COLORS[currentPiece]);
        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                if (currentShape[row][col] != 0) {
                    g.fillRect((currentX + col) * TILE_SIZE, (currentY + row) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawNextPiece(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Next Piece:", BOARD_WIDTH * TILE_SIZE + 20, 50);

        int[][] nextShape = TETRIMINOS[nextPiece];
        for (int row = 0; row < nextShape.length; row++) {
            for (int col = 0; col < nextShape[row].length; col++) {
                if (nextShape[row][col] != 0) {
                    g.setColor(COLORS[nextPiece]);
                    g.fillRect(BOARD_WIDTH * TILE_SIZE + 50 + col * TILE_SIZE, 80 + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, BOARD_WIDTH * TILE_SIZE + 20, 200);
        g.drawString("Level: " + level, BOARD_WIDTH * TILE_SIZE + 20, 230);
    }

    private void movePiece(int dx, int dy) {
        if (canMove(currentX + dx, currentY + dy, currentShape)) {
            currentX += dx;
            currentY += dy;
        } else if (dy > 0) {
            lockPiece();
        }
        repaint();
    }

    private void rotatePiece() {
        int[][] rotatedShape = new int[currentShape[0].length][currentShape.length];

        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                rotatedShape[col][currentShape.length - row - 1] = currentShape[row][col];
            }
        }

        if (canMove(currentX, currentY, rotatedShape)) {
            currentShape = rotatedShape;
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        movePiece(0, 1);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isGameOver) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    movePiece(-1, 0);
                    break;
                case KeyEvent.VK_RIGHT:
                    movePiece(1, 0);
                    break;
                case KeyEvent.VK_DOWN:
                    movePiece(0, 1);
                    break;
                case KeyEvent.VK_UP:
                    rotatePiece();
                    break;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            startGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tetris");
            TetrisGame game = new TetrisGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(game.BOARD_WIDTH * game.TILE_SIZE + 250, game.BOARD_HEIGHT * game.TILE_SIZE + 40);
            frame.setVisible(true);
        });
    }
}
