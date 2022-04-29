package robot;

import physical.things.Point3D;
import physical.transforms.Scale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static misc.SettingsConstants.WORLD_HEIGHT;

public class SchematicFiller {
  private Point3D pos;
  private Robot robot;
  private JFrame jFrame;
  private Scale tpScale;
  private TextArea textArea;

  String prevText;

  private static final int TP_Y_OFFSET = 2 * WORLD_HEIGHT;

  public SchematicFiller(int scale) throws AWTException {
    this.robot = new Robot();
    this.pos = new Point3D(0, 0, 0);
    tpScale = new Scale(scale);
    prevText = null;
  }

  private void buildJFrame(WaitTarget waitTarget) {
    closeJFrame();

    KeyListener pingListener = new SimpleKeyListener(waitTarget::ping, KeyEvent.VK_SPACE);
    KeyListener quitListener = new SimpleKeyListener(this::closeJFrame, KeyEvent.VK_Q);

    jFrame = new JFrame();
    jFrame.addKeyListener(pingListener);
    jFrame.addKeyListener(quitListener);
    jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(500, 300));
    jFrame.add(panel);

    textArea = new TextArea();
    textArea.setPreferredSize(new Dimension(400, 200));
    textArea.setEditable(false);

    panel.add(textArea);
    setText("Constructing");

    panel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        System.out.println("Focusing on jFrame");
        jFrame.requestFocus();
      }
    });

    jFrame.addFocusListener(new FocusListener() {
      @Override
      public synchronized void focusGained(FocusEvent e) {
        if (prevText != null) {
          textArea.setText(prevText);
          prevText = null;
        }
      }

      @Override
      public synchronized void focusLost(FocusEvent e) {
        if (prevText == null) {
          prevText = textArea.getText();
          textArea.setText("Click outside the text box to continue");
        }
      }
    });

    jFrame.pack();
    jFrame.setVisible(true);
    jFrame.requestFocus();
  }

  /**
   * Blocks between items on space bar or something
   */
  public void constructSchematic(File dir, String prefix) {
    if (prefix.length() != 0 && !prefix.endsWith("/")) {
      prefix += '/';
    }
    WaitTarget waitTarget = new WaitTarget();

    buildJFrame(waitTarget);

    setText("Attempting to collect mouse target:\nPosition this window and Minecraft so they don't overlap.\nOpen chat in Minecraft.\nThen hover over Minecraft and press 'space'.");
    waitTarget.pause();
    Point mouseTarget = MouseInfo.getPointerInfo().getLocation();
    Typer.click(robot, mouseTarget);
    runCommand("/tellraw @s {\"text\":\"Please align yourself and set yourself to flying mode.\"}");
    pause(waitTarget, mouseTarget);

    File[] children = dir.listFiles();
    if (children == null) {
      throw new IllegalStateException("Directory not found: " + dir);
    }
    Arrays.sort(children, new FileComparator());
    for (File child : children) {
      String loadTarget = prefix + removeSuffix(child.getName());
      attemptTeleport(parsePoint(child.getName()));
      runCommand("//schem load " + loadTarget);
//      pause(waitTarget, mouseTarget);
      runCommand("//paste");
    }

    closeJFrame();
  }

  private void setText(String s) {
    prevText = null;
    textArea.setText(s + "\n\nPress 'q' to quit.");
  }

  private void closeJFrame() {
    System.out.println("Attempting to close jFrame");
    if (jFrame != null) {
      System.out.println("Closing jFrame");
      jFrame.dispatchEvent(new WindowEvent(jFrame, WindowEvent.WINDOW_CLOSING));
    }
  }

  private static Point getComponentCenter(Component component) {
    Dimension dim = component.getSize();
    Point p = component.getLocationOnScreen();
    return new Point(((int) p.getX()) + ((int) dim.getWidth() / 2), ((int) p.getY()) + ((int) dim.getHeight() / 2));
  }

  private Point jFrameClickTarget() {
    int jFrameCenterX = (int) jFrame.getLocationOnScreen().getX() + (jFrame.getWidth() / 2);
    int jFrameBottom = (int) jFrame.getLocationOnScreen().getY() + jFrame.getHeight();
    int textAreaLowBound = (int) textArea.getLocationOnScreen().getY() + textArea.getHeight();

    return new Point(jFrameCenterX, (jFrameBottom + textAreaLowBound) / 2);
  }

  private void pause(WaitTarget waitTarget, Point mouseTarget) {
    setText("Open chat and press 'space' to continue");
    Typer.click(robot, jFrameClickTarget());
    waitTarget.pause();
    setText("Running...");
    Typer.click(robot, mouseTarget);
  }

  private static String removeSuffix(String fileName) {
    int dot = fileName.lastIndexOf('.');
    String suffix = fileName.substring(dot + 1);
    if (!(suffix.equals("schematic") || suffix.equals("schem"))) {
      throw new IllegalArgumentException("Are you sure " + fileName + " is a schematic file?");
    }
    return fileName.substring(0, dot);
  }

  private static Point3D parsePoint(String fileName) {
    String name = removeSuffix(fileName);
    String[] items = name.split(",");
    if (items.length != 3) {
      throw new IllegalArgumentException("Not a parseable Point3D: \"" + fileName + "\"");
    }

    items[0] = removeWhitespace(items[0]);
    items[1] = removeWhitespace(items[1]);
    items[2] = removeWhitespace(items[2]);

    return new Point3D(
        Integer.parseInt(items[0]),
        Integer.parseInt(items[1]),
        Integer.parseInt(items[2])
    );
  }

  private static String expectWrapping(String s, String prefix, String suffix, boolean ignoreWhitespace) {
    if (ignoreWhitespace) {
      s = removeWhitespace(s);
    }
    if (!s.startsWith(prefix)) {
      throw new IllegalArgumentException("\"" + s + "\" does not start with \"" + prefix + "\"");
    }
    if (!s.endsWith(suffix)) {
      throw new IllegalArgumentException("\"" + s + "\" does not end with \"" + suffix + "\"");
    }
    s = s.substring(prefix.length(), s.length() - suffix.length());
    if (ignoreWhitespace) {
      s = removeWhitespace(s);
    }
    return s;
  }

  private static String removeWhitespace(String s) {
    int start;
    for (start = 0; start < s.length(); start++) {
      if (!Character.isWhitespace(s.charAt(start))) {
        break;
      }
    }
    if (start >= s.length()) {
      return "";
    }
    // String has at least one non-whitespace character
    int end;
    for (end = s.length(); end > 0; end--) {
      if (!Character.isWhitespace(s.charAt(end - 1))) {
        break;
      }
    }
    return s.substring(start, end);
  }


  private void attemptTeleport(Point3D absTarget) {
    Point3D diff = absTarget.translate(pos.negate());
    diff = tpScale.apply(diff);
    runCommand("/tp " + toRelativeCoordString(diff.translate(0, TP_Y_OFFSET, 0)));
    Point3D lowerTarget = new Point3D(0, -TP_Y_OFFSET, 0);
    runCommand("/fill " + toRelativeCoordString(lowerTarget) + " " + toRelativeCoordString(lowerTarget.translate(0, 1, 0)) + " air");
    runCommand("/tp " + toRelativeCoordString(lowerTarget));
    pos = absTarget;
  }

  private static String toRelativeCoordString(Point3D p) {
    return "~" + intToStr(p.getX())
        + " ~" + intToStr(p.getY())
        + " ~" + intToStr(p.getZ());
  }

  private static String intToStr(int i) {
    if (i == 0) {
      return "";
    } else {
      return String.valueOf(i);
    }
  }

  private void runCommand(String s) {
    System.out.println("Attempting command: " + s);
    Typer.type(robot, s);
    Typer.type(robot, "\nt");
  }

  private static class FileComparator implements Comparator<File> {
    // Do lower schematics first
    // Also, since schematics are pasted one +z from where you are (and it fills your target teleport with air), do the positive z ones first.
    @Override
    public int compare(File o1, File o2) {
      int yDiff = parsePoint(o1.getName()).getY() - parsePoint(o2.getName()).getY();
      if (yDiff != 0) {
        return yDiff;
      } else {
        // Inverted order since large z should go first
        return parsePoint(o2.getName()).getZ() - parsePoint(o1.getName()).getZ();
      }
    }
  }
}
