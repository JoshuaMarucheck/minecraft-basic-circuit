package physical;

import physical.things.Point3D;
import physical.transforms.Rotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class CanonicalPaths {
  public static final Collection<PathDrawer> connections = makeConnections();

  private static Collection<PathDrawer> makeConnections() {
    ArrayList<PathDrawer> r = new ArrayList<>();
    {
      PathDrawer drawer = new PathDrawer();
      drawer.addConnection(new Point3D(0, 0, 0), new Point3D(1, 0, 0));

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(drawer.transform(func));
      }
    }
    {
      PathDrawer drawer = new PathDrawer();
      drawer.addConnection(new Point3D(0, 0, 0), new Point3D(1, 1, 0));
      drawer.addPoint(new Point3D(0, 1, 0));

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(drawer.transform(func));
      }
    }
    {
      PathDrawer drawer = new PathDrawer();
      drawer.addConnection(new Point3D(0, 0, 0), new Point3D(1, -1, 0));
      drawer.addPoint(new Point3D(1, 0, 0));

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(drawer.transform(func));
      }
    }
    return r;
  }


}
