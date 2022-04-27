import physical2.tiny.BentPath;
import physical2.two.Point2D;

import java.util.Iterator;

public class BentPathTester {
  public static void main(String[] args) {
    exhaust(new BentPath(new Point2D(0, 0), new Point2D(0, 0)).iterator());
    exhaust(new BentPath(new Point2D(0, 0), new Point2D(1, 0)).iterator());
    exhaust(new BentPath(new Point2D(0, 0), new Point2D(2, 0)).iterator());


    exhaust(new BentPath(new Point2D(4, 46), new Point2D(8, 45)).iterator());

    System.out.println("\n\n\n\n");
    exhaust(new BentPath(new Point2D(17, 3), new Point2D(2, 51)).iterator());
  }

  private static void exhaust(Iterator<?> iter) {
    while (iter.hasNext()) {
      iter.next();
    }
  }
}
