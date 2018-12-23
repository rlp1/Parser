package test.rlp.parser;

import rlp.parser.Parser;

import java.io.File;
import java.io.IOException;

public class TestParser {

    private static final File TEST_FILE = new File("test.txt");

    public static void main(String[] args) {
        createFile();

        final Parser parser = new Parser(TEST_FILE);
        parser.parse();

        // System.out.println("Buffer: " + parser.getBuffer());
        System.out.println("Variables: " + parser.getVariables());

        // parser.get("kv").mapValue().forEach((k, v) -> System.out.println("k: " + k + ", v: " + v));
        // parser.get("arr").listValue().forEach(System.out::println);
    }

    private static void createFile() {
        if (!TEST_FILE.exists()) {
            try {
                TEST_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
