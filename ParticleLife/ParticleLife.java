import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ParticleLife extends JPanel
        implements ActionListener, MouseWheelListener, MouseListener, MouseMotionListener, KeyListener {

    private static final int INITIAL_WIDTH = 500;
    private static final int INITIAL_HEIGHT = 500;
    private static final int CELL_SIZE = 1;
    private static final Color BACKGROUND_COLOR = new Color(0x21262e);

    private int[][] grid;
    private int[][] nextGrid;
    private Random random;
    private int cellSize = CELL_SIZE;
    private int offsetX = 0;
    private int offsetY = 0;

    private int dragStartX, dragStartY;
    private boolean dragging = false;

    private BufferedImage[] frames;
    private int frameCount = 0;
    private Timer recordingTimer;
    private boolean recording = false;

    public ParticleLife() {
        grid = new int[INITIAL_WIDTH / CELL_SIZE][INITIAL_HEIGHT / CELL_SIZE];
        nextGrid = new int[INITIAL_WIDTH / CELL_SIZE][INITIAL_HEIGHT / CELL_SIZE];
        random = new Random();

        // Initialize grid with random particles
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                grid[x][y] = random.nextInt(2);
            }
        }

        Timer timer = new Timer(5, this); // Update every 100ms
        timer.start();

        frames = new BufferedImage[30 * 30]; // 30 seconds at 30 FPS
        recordingTimer = new Timer(33, new ActionListener() { // 33ms for ~30 FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frameCount < frames.length && recording) {
                    BufferedImage frame = new BufferedImage(grid.length * cellSize + 10, grid[0].length * cellSize + 10,
                            BufferedImage.TYPE_INT_RGB);
                    Graphics g = frame.getGraphics();
                    g.setColor(BACKGROUND_COLOR);
                    g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
                    paintGrid(g);
                    frames[frameCount] = frame;
                    frameCount++;
                } else if (recording) {
                    recordingTimer.stop();
                    recording = false;
                    saveGif();
                }
            }
        });

        addMouseWheelListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setFocusable(true);
        setBackground(BACKGROUND_COLOR);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintGrid(g);
    }

    private void paintGrid(Graphics g) {
        int gridWidth = grid.length * cellSize;
        int gridHeight = grid[0].length * cellSize;

        offsetX = Math.max(0, Math.min(offsetX, getWidth() - gridWidth));
        offsetY = Math.max(0, Math.min(offsetY, getHeight() - gridHeight));

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                if (grid[x][y] == 1) {
                    int neighbors = countNeighbors(x, y);
                    Color color = getColor(neighbors);
                    g.setColor(color);
                    g.fillRect(x * cellSize + offsetX + 5, y * cellSize + offsetY + 5, cellSize, cellSize); // +5 for
                                                                                                            // padding
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update grid based on rules
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                int neighbors = countNeighbors(x, y);

                // Example rules:
                if (grid[x][y] == 1) {
                    // Particle survives if it has 2 or 3 neighbors
                    if (neighbors == 2 || neighbors == 3) {
                        nextGrid[x][y] = 1;
                    } else {
                        nextGrid[x][y] = 0;
                    }
                } else {
                    // Particle is born if it has 3 neighbors
                    if (neighbors == 3) {
                        nextGrid[x][y] = 1;
                    } else {
                        nextGrid[x][y] = 0;
                    }
                }
            }
        }

        // Swap grids
        int[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;

        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            // Zoom in
            cellSize++;
        } else {
            // Zoom out
            cellSize = Math.max(1, cellSize - 1);
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            dragStartX = e.getX();
            dragStartY = e.getY();
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            int dragEndX = e.getX();
            int dragEndY = e.getY();

            int deltaX = dragEndX - dragStartX;
            int deltaY = dragEndY - dragStartY;

            offsetX = Math.max(0, Math.min(offsetX + deltaX, getWidth() - grid.length * cellSize));
            offsetY = Math.max(0, Math.min(offsetY + deltaY, getHeight() - grid[0].length * cellSize));

            dragStartX = dragEndX;
            dragStartY = dragEndY;

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_0) {
            if (!recording) {
                startRecording();
                System.out.println("Recording started. Press '0' again to stop.");
            } else {
                stopRecording();
                System.out.println("Recording stopped.");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void startRecording() {
        frameCount = 0;
        recording = true;
        recordingTimer.start();
    }

    private void stopRecording() {
        recording = false;
        recordingTimer.stop();
        saveGif();
    }

    private void saveGif() {
        try {
            GifSequenceWriter writer = new GifSequenceWriter(
                    ImageIO.createImageOutputStream(new File("particle_life.gif")),
                    BufferedImage.TYPE_INT_RGB, 33, true);
            for (BufferedImage frame : frames) {
                if (frame != null) {
                    writer.writeToSequence(frame);
                }
            }
            writer.close();
            System.out.println("GIF saved to: " + new File("particle_life.gif").getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving GIF: " + e.getMessage());
        }
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0)
                    continue;

                int nx = x + i;
                int ny = y + j;

                if (nx < 0)
                    nx = grid.length - 1;
                if (nx >= grid.length)
                    nx = 0;
                if (ny < 0)
                    ny = grid[0].length - 1;
                if (ny >= grid[0].length)
                    ny = 0;

                if (grid[nx][ny] == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    private Color getColor(int neighbors) {
        switch (neighbors) {
            case 0:
                return Color.BLACK;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.YELLOW;
            case 4:
                return Color.ORANGE;
            case 5:
                return Color.RED;
            default:
                return Color.WHITE;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Particle Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ParticleLife());
        frame.setPreferredSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}