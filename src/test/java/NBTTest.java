import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.primitive.StringTag;
import nbt.Constants;
import nbt.SNBTParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static nbt.NBTMaker.NBT;

public class NBTTest {
  private static final File JSON_OUT_SAMPLE = Paths.get(Constants.nbtRoot).resolve("out.json").toFile();
  private static final Path NBT_PATH = Paths.get(Constants.nbtRoot);

  public static void main(String[] args) throws IOException, SNBTParser.SNBTParseException, UnitTestFailException {
    checkFile(NBT_PATH.resolve("test_tag.json").toFile());
    checkFile(NBT_PATH.resolve("base_tag.json").toFile());
    jsonTest();
  }

  private static void checkFile(File file) throws IOException, SNBTParser.SNBTParseException, UnitTestFailException {
    String snbt = NBT.toSnbt((CompoundTag) SNBTParser.fromFile(file));
    if (!snbt.equals(NBT.toSnbt((CompoundTag) SNBTParser.parse(snbt)))) {
      throw new UnitTestFailException("tag did not match after stringifying and reparsing");
    }
  }

  private static void jsonTest() throws IOException {
    CompoundTag root = new CompoundTag("root");

    root.putInt("primitive", 3);
    root.putIntArray("array", new int[]{0, 1, 2, 3});

    List<StringTag> list = new LinkedList<>();
    list.add(new StringTag("duck"));
    list.add(new StringTag("goose"));

    root.putList("list", list);
    root.put("compound", new CompoundTag());

    NBT.toJson(root, JSON_OUT_SAMPLE);
    System.out.println(NBT.fromJson(JSON_OUT_SAMPLE).equals(root));
  }
}
