package robot;

import static robot.Typer.KEY_PRESS_PAUSE;

public class WaitTarget {
  private volatile boolean waiting;

  public synchronized void ping() {
    this.notifyAll();
    waiting = false;
  }

  public synchronized void pause() {
    try {
      this.wait();
    } catch (InterruptedException ignored) {
    }
  }

  public synchronized void waitShort() {
    try {
      this.wait(KEY_PRESS_PAUSE, 0);
    } catch (InterruptedException e) {
      System.err.println("Warning: waitShort was interrupted");
    }
  }

  public synchronized void longPauseSetup() {
    waiting = true;
  }

  public synchronized void longPauseGo() {
    if (waiting) {
      try {
        this.wait();
      } catch (InterruptedException ignored) {
      }
    }
  }
}
