package inputs;

import main.GamePanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class kbInput implements KeyListener {

    private final GamePanel panel;

    public kbInput(GamePanel panel) {
        this.panel = panel;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (panel.showTitleScreen) {
            if (code == KeyEvent.VK_ENTER) {
                panel.startGame(); // Dismiss title screen
            }
            return;
        }

        switch (code) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                panel.setXVel(-5);
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                panel.setXVel(5);
                break;

            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                panel.jump();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (!panel.showTitleScreen) {
            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A ||
                    code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
                panel.setXVel(0);
            }
        }
    }
}
