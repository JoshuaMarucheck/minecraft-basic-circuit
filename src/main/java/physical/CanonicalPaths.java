package physical;

import graph.Edge;
import physical.things.BlockConstant;
import physical.things.Point3D;
import physical.things.TorchState;
import physical.transforms.Rotation;

import java.util.*;
import java.util.function.Function;

/**
 * The thing to note is that if a path is being
 * followed out of a redstone torch or into one,
 * the downward path cannot be taken.
 */
public class CanonicalPaths {
  private static void writeRedstone(Map<Point3D, BlockConstant> blocks, Point3D p) {
    blocks.put(p, BlockConstant.REDSTONE);
    blocks.put(p.translate(0, -1, 0), BlockConstant.REDSTONE_BASE);
  }

  public static Collection<AnnotatedEdge> horizConnections() {
    ArrayList<AnnotatedEdge> r = new ArrayList<>();
    Edge<Point3D> connection = new Edge<>(new Point3D(0, 0, 0), new Point3D(1, 0, 0));
    HashSet<Point3D> emptyPoints = new HashSet<>();
    {
      TorchState first = TorchState.NONE;
      TorchState second = TorchState.NONE;
      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();

      writeRedstone(blocks, new Point3D(0, 0, 0));
      writeRedstone(blocks, new Point3D(1, 0, 0));
      writeRedstone(blocks, new Point3D(2, 0, 0));

      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, emptyPoints, blocks);

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(annotatedEdge.rotate(func));
      }
    }
    {
      TorchState first = TorchState.INPUT;
      TorchState second = TorchState.NONE;
      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();

      blocks.put(new Point3D(0, 0, 0), BlockConstant.REDSTONE_TORCH_BASE);
      blocks.put(new Point3D(1, 0, 0), BlockConstant.REDSTONE_TORCH);
      writeRedstone(blocks, new Point3D(2, 0, 0));

      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, emptyPoints, blocks);

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(annotatedEdge.rotate(func));
        r.add(annotatedEdge.rotate(func).swapPerspective());
      }
    }
    {
      TorchState first = TorchState.OUTPUT;
      TorchState second = TorchState.NONE;
      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();

      blocks.put(new Point3D(0, 0, 0), BlockConstant.REDSTONE_TORCH_BASE);
      writeRedstone(blocks, new Point3D(1, 0, 0));
      writeRedstone(blocks, new Point3D(2, 0, 0));

      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, emptyPoints, blocks);

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(annotatedEdge.rotate(func));
        r.add(annotatedEdge.rotate(func).swapPerspective());
      }
    }
    return r;
  }

  public static Collection<AnnotatedEdge> diagConnections() {
    ArrayList<AnnotatedEdge> r = new ArrayList<>();
    Edge<Point3D> connection = new Edge<>(new Point3D(0, 0, 0), new Point3D(1, 1, 0));
    HashSet<Point3D> emptyPoints = new HashSet<>();
    emptyPoints.add(new Point3D(0, 1, 0));
    {
      TorchState first = TorchState.NONE;
      TorchState second = TorchState.NONE;
      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();

      writeRedstone(blocks, new Point3D(0, 0, 0));
      writeRedstone(blocks, new Point3D(1, 1, 0));
      writeRedstone(blocks, new Point3D(2, 2, 0));
      blocks.put(new Point3D(0, 1, 0), BlockConstant.AIR);
      blocks.put(new Point3D(1, 2, 0), BlockConstant.AIR);

      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, emptyPoints, blocks);

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(annotatedEdge.rotate(func));
        r.add(annotatedEdge.rotate(func).swapPerspective());
      }
    }
    {
      TorchState first = TorchState.INPUT;
      TorchState second = TorchState.NONE;
      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();
      HashSet<Point3D> localEmptyPoints = new HashSet<>();
      localEmptyPoints.add(new Point3D(0, 1, 0));
      localEmptyPoints.add(new Point3D(1, 0, 0));

      blocks.put(new Point3D(0, 0, 0), BlockConstant.REDSTONE_TORCH_BASE);
      blocks.put(new Point3D(0, 1, 0), BlockConstant.REDSTONE_TORCH);
      writeRedstone(blocks, new Point3D(1, 1, 0));
      writeRedstone(blocks, new Point3D(2, 2, 0));
      blocks.put(new Point3D(1, 2, 0), BlockConstant.AIR);

      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, localEmptyPoints, blocks);

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(annotatedEdge.rotate(func));
        r.add(annotatedEdge.rotate(func).swapPerspective());
      }
    }
    {
      TorchState first = TorchState.NONE;
      TorchState second = TorchState.INPUT;
      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();

      writeRedstone(blocks, new Point3D(0, 0, 0));
      writeRedstone(blocks, new Point3D(1, 1, 0));
      blocks.put(new Point3D(0, 1, 0), BlockConstant.AIR);
      blocks.put(new Point3D(1, 2, 0), BlockConstant.REDSTONE_TORCH);
      blocks.put(new Point3D(2, 2, 0), BlockConstant.REDSTONE_TORCH_BASE);

      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, emptyPoints, blocks);

      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
        r.add(annotatedEdge.rotate(func));
        r.add(annotatedEdge.rotate(func).swapPerspective());
      }
    }
//    {
//      TorchState first = TorchState.INPUT;
//      TorchState second = TorchState.NONE;
//      HashMap<Point3D, BlockConstant> blocks = new HashMap<>();
//      HashSet<Point3D> localEmptyPoints = new HashSet<>();
//      // 0,1,0 and 1,0,0 can optionally be redstone each
//      localEmptyPoints.add(new Point3D(1, 0, 0));
//
//      blocks.put(new Point3D(0, 0, 0), BlockConstant.REDSTONE_TORCH_BASE);
//      blocks.put(new Point3D(1, 0, 0), BlockConstant.REDSTONE_TORCH);
//      writeRedstone(blocks, new Point3D(0, 2, 0));
//      writeRedstone(blocks, new Point3D(1, 2, 0));
//      writeRedstone(blocks, new Point3D(2, 2, 0));
//      blocks.put(new Point3D(1, 2, 0), BlockConstant.AIR);
//
//      AnnotatedEdge annotatedEdge = new AnnotatedEdge(first, second, connection, localEmptyPoints, blocks);
//
//      for (Function<Point3D, Point3D> func : Rotation.horizontalRotations()) {
//        r.add(annotatedEdge.rotate(func));
//        r.add(annotatedEdge.rotate(func).swapPerspective());
//      }
//    }
    // TODO figure out outputs?
    return r;
  }

  public static Collection<AnnotatedEdge> allConnections() {
    ArrayList<AnnotatedEdge> r = new ArrayList<>();
    r.addAll(horizConnections());
    r.addAll(diagConnections());
    return r;
  }
}
