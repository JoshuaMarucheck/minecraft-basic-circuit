package robot;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SimpleKeyListener extends KeyAdapter {
  private Runnable callback;
  private int key;

  public SimpleKeyListener(Runnable callback, int key) {
    this.callback = callback;
    this.key = key;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == key) {
      callback.run();
    }
  }
}
