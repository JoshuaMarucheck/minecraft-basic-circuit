package robot;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import java.io.IOException;

import static java.awt.event.KeyEvent.*;

public class Typer {
  private static final WaitTarget waitTarget = new WaitTarget();
  private static final WaitTarget copyWaitTarget = new WaitTarget();
  private static final int copyKey = VK_META;//System.getProperty("os.name").toLowerCase().contains("win") ? VK_CONTROL : VK_META;
  private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  private static final Transferable wipeThing;

  static {
    DataFlavor wipeFlavor = new DataFlavor("minecraft/no-type", "No data");
    wipeThing = new Transferable() {
      @Override
      public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{wipeFlavor};
      }

      @Override
      public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == wipeFlavor;
      }

      @Override
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return new Object();
      }
    };

    clipboard.addFlavorListener(e -> {
//      System.out.println("Clipboard ping");
      copyWaitTarget.ping();
    });
  }

  public static void click(Robot robot, Point target) {
    robot.mouseMove(target.x, target.y);
    waitShort();
    robot.mousePress(InputEvent.BUTTON1_MASK);
    waitShort();
    robot.mouseRelease(InputEvent.BUTTON1_MASK);
    waitShort();
  }

  public static void typeFast(Robot robot, String s) {

    StringSelection selection = new StringSelection(s);

    copyWaitTarget.longPauseSetup();
    clipboard.setContents(wipeThing, null);
    copyWaitTarget.longPauseGo();

    copyWaitTarget.longPauseSetup();
    clipboard.setContents(selection, selection);
    copyWaitTarget.longPauseGo();

    try {
      Thread.sleep(200);
    } catch (InterruptedException ignored) {
    }

    robot.keyPress(copyKey);
    waitShort();
    pressKey(robot, VK_V);
    robot.keyRelease(copyKey);
    waitShort();
  }

  public static void type(Robot robot, String s) {
    for (char c : s.toCharArray()) {
      typeChar(robot, c);
    }
  }

  public static void pressKey(Robot robot, int keyCode) {
    robot.keyPress(keyCode);
    waitShort();
    robot.keyRelease(keyCode);
    waitShort();
  }

  private static void typeChar(Robot robot, char c) {
    boolean isUpper = isUpperCase(c);
    if (isUpper) {
      robot.keyPress(VK_SHIFT);
      waitShort();
    }

    int keyCode = getKeyCode(c);
    pressKey(robot, keyCode);

    if (isUpper) {
      robot.keyRelease(VK_SHIFT);
      waitShort();
    }
  }

  private static void waitShort() {
    waitTarget.waitShort();
  }

  private static boolean isUpperCase(char c) {
    switch (c) {
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
      case 'G':
      case 'H':
      case 'I':
      case 'J':
      case 'K':
      case 'L':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'S':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      case 'Z':
      case '~':
      case '@':
      case '"':
      case '{':
      case '}':
      case '(':
      case ')':
      case ':':
      case '_':
        return true;
      default:
        return false;
    }
  }

  private static int getKeyCode(char c) {
    switch (c) {
      case 'a':
      case 'A':
        return VK_A;
      case 'b':
      case 'B':
        return VK_B;
      case 'c':
      case 'C':
        return VK_C;
      case 'd':
      case 'D':
        return VK_D;
      case 'e':
      case 'E':
        return VK_E;
      case 'f':
      case 'F':
        return VK_F;
      case 'g':
      case 'G':
        return VK_G;
      case 'h':
      case 'H':
        return VK_H;
      case 'i':
      case 'I':
        return VK_I;
      case 'j':
      case 'J':
        return VK_J;
      case 'k':
      case 'K':
        return VK_K;
      case 'l':
      case 'L':
        return VK_L;
      case 'm':
      case 'M':
        return VK_M;
      case 'n':
      case 'N':
        return VK_N;
      case 'o':
      case 'O':
        return VK_O;
      case 'p':
      case 'P':
        return VK_P;
      case 'q':
      case 'Q':
        return VK_Q;
      case 'r':
      case 'R':
        return VK_R;
      case 's':
      case 'S':
        return VK_S;
      case 't':
      case 'T':
        return VK_T;
      case 'u':
      case 'U':
        return VK_U;
      case 'v':
      case 'V':
        return VK_V;
      case 'w':
      case 'W':
        return VK_W;
      case 'x':
      case 'X':
        return VK_X;
      case 'y':
      case 'Y':
        return VK_Y;
      case 'z':
      case 'Z':
        return VK_Z;
      case '0':
      case ')':
        return VK_0;
      case '1':
        return VK_1;
      case '2':
      case '@':
        return VK_2;
      case '3':
        return VK_3;
      case '4':
        return VK_4;
      case '5':
        return VK_5;
      case '6':
        return VK_6;
      case '7':
        return VK_7;
      case '8':
        return VK_8;
      case '9':
      case '(':
        return VK_9;
      case '\n':
        return VK_ENTER;
      case '/':
        return VK_SLASH;
      case '[':
      case '{':
        return VK_OPEN_BRACKET;
      case ']':
      case '}':
        return VK_CLOSE_BRACKET;
      case '~':
        return VK_BACK_QUOTE;
      case '"':
        return VK_QUOTE;
      case ' ':
        return VK_SPACE;
      case '-':
      case '_':
        return VK_MINUS;
      case ':':
        return VK_SEMICOLON;
      case '.':
        return VK_PERIOD;
      case ',':
        return VK_COMMA;
      default:
        throw new IllegalArgumentException("Unrecognized character " + c);
    }
  }


}
