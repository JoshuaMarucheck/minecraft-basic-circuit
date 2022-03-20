import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.tags.collection.CompoundTag;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PackageTest {
  public static final Nbt NBT = new Nbt();
  public static void main(String[] args) throws IOException {
    File nbtFile = new File("/Users/joshua/Library/Application Support/minecraft/schematics/Picture1.schematic");
    CompoundTag tag = NBT.fromFile(nbtFile);
    String s = tag.toString();
    String prev = null;
    int count = 1;
    for (String item : s.split(",")) {
      if (item.equals(prev)) {
        count++;
      } else {
        if (count != 1) {
          System.out.print(" x");
          System.out.print(count);
        }
        System.out.println();
        System.out.print(item);
        prev = item;
        count = 1;
      }
    }
    if (count != 1) {
      System.out.print(" x");
      System.out.print(count);
    }
    System.out.println();

//    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(nbtFile));
//    try {
//
//    } finally {
//      inputStream.close();
//    }
  }
}
