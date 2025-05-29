package main;

import inputs.kbInput;
import inputs.mInputs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements Runnable {

    private mInputs mouseInputs;
    private int xDelta = 640, yDelta = 360;
    private int frame = 0;
    private long lastCheck = 0;

    private List<Platform> platforms = new ArrayList<>();

    // Game Assets
    private BufferedImage[] frames = new BufferedImage[5];
    private int currentFrame = 0;
    private int animationTick = 0;
    private final int animationSpeed = 10;
    private boolean facingRight = true;

    // Velocity
    private int xVel = 0;
    private double yVel = 0;

    // Physics
    private final double gravity = 1;
    private final int jumpStrength = -20;
    private boolean onGround = true;

    // FPS
    private Thread gameThread;
    private final int FPS = 60;
    private int frameCount = 0;
    private long fpsTimer = System.currentTimeMillis();
    private int currentFPS = 0;

    // Title Screen
    public boolean showTitleScreen = true;
    private BufferedImage[] titleFrames;
    private int currentTitleFrame = 0;
    private int titleAnimTick = 0;
    private final int titleAnimSpeed = 10;

    // Respawn
    private void respawnPlayer() {
        xDelta = 100;
        yDelta = 0;
        yVel = 0;
        onGround = false;
    }

    public void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public GamePanel() {
        mouseInputs = new mInputs(this);
        addKeyListener(new kbInput(this));
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);
        setFocusable(true);
        requestFocusInWindow();

        loadFrames();
        loadTitleFrames();
        startGameLoop();

        setBackground(Color.LIGHT_GRAY);
        platforms.add(new Platform(200, 300, 300, 20));
        platforms.add(new Platform(600, 400, 200, 20));
    }

    private void update() {
        if (showTitleScreen) {
            titleAnimTick++;
            if (titleAnimTick >= titleAnimSpeed) {
                titleAnimTick = 0;
                currentTitleFrame = (currentTitleFrame + 1) % titleFrames.length;
            }
            return;
        }

        if (!onGround) {
            yVel += gravity;
        }

        xDelta += xVel;
        yDelta += yVel;

        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }

        boolean grounded = false;

        if (frames[0] != null) {
            int scaledWidth = frames[0].getWidth() * 4;
            int scaledHeight = frames[0].getHeight() * 4;
            Rectangle playerBounds = new Rectangle(xDelta + 5, yDelta, scaledWidth - 10, scaledHeight);

            for (Platform platform : platforms) {
                Rectangle platBounds = platform.getBounds();
                boolean falling = yVel >= 0;
                boolean wasAbove = (yDelta + scaledHeight - yVel) <= platBounds.y;
                boolean intersects = playerBounds.intersects(platBounds);

                if (falling && wasAbove && intersects) {
                    yDelta = platBounds.y - scaledHeight;
                    yVel = 0;
                    grounded = true;
                    break;
                }
            }

            onGround = grounded;

            if (yDelta > getHeight()) {
                respawnPlayer();
            }
        }
    }

    private void loadFrames() {
        try {
            for (int i = 0; i < frames.length; i++) {
                frames[i] = ImageIO.read(getClass().getResource("/playerAssets/playerfinal" + (i + 1) + ".png"));
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void loadTitleFrames() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResource("/backgrounds/titleScreen.png"));
            int frameCount = spriteSheet.getWidth() / 400;
            titleFrames = new BufferedImage[frameCount];

            for (int i = 0; i < frameCount; i++) {
                titleFrames[i] = spriteSheet.getSubimage(i * 400, 0, 400, 224);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        showTitleScreen = false;
    }

    public void setXVel(int dx) {
        this.xVel = dx;
        if (dx > 0) facingRight = true;
        else if (dx < 0) facingRight = false;
    }

    public void jump() {
        if (onGround) {
            yVel = jumpStrength;
            onGround = false;
        }
    }

    public void changeXDelta(int value) {
        this.xDelta += value;
        repaint();
    }

    public void changeYDelta(int value) {
        this.yDelta += value;
        repaint();
    }

    public void setOvalPos(int x, int y) {
        this.xDelta = x;
        this.yDelta = y;
        repaint();
    }

    public void setDeltas(int dx, int dy) {
        xDelta += dx;
        yDelta += dy;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showTitleScreen) {
            if (titleFrames != null && titleFrames.length > 0) {
                BufferedImage frame = titleFrames[currentTitleFrame];
                // Scale to fit the panel
                g.drawImage(frame, 0, 0, getWidth(), getHeight(), null);
            }
            return;
        }

        int scale = 4;

        if (frames[currentFrame] != null) {
            int width = frames[currentFrame].getWidth() * scale;
            int height = frames[currentFrame].getHeight() * scale;

            if (facingRight) {
                g.drawImage(frames[currentFrame], xDelta, yDelta, width, height, null);
            } else {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(frames[currentFrame], xDelta + width, yDelta, -width, height, null);
            }
        }

        for (Platform p : platforms) {
            p.draw(g);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("FPS: " + currentFPS, 10, 20);
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;

                frameCount++;
                if (System.currentTimeMillis() - fpsTimer >= 1000) {
                    currentFPS = frameCount;
                    frameCount = 0;
                    fpsTimer = System.currentTimeMillis();
                }
            }
        }
    }
}
