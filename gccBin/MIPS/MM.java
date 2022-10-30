package gccBin.MIPS;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

//APIMidCodeMIPS
public class MM {
    private static MM instance;


    private boolean end;
    private BufferedReader br;


    private MM() {
        this.end = false;
    }

    public static MM get() {
        if (instance == null) {
            instance = new MM();
        }
        return instance;
    }

    public void open() throws IOException {
        File inputFile = new File("midCode.txt");
        br = new BufferedReader(
                new InputStreamReader(
                        Files.newInputStream(inputFile.toPath()), "UTF-8"));
    }

    public void close() throws IOException {
        this.br.close();
    }

    public String readLine() throws IOException {
        return this.br.readLine();
    }

    private final ArrayList<String> store = new ArrayList<>();
    private String line;

    public void read() throws IOException {
        if (store.size() == 0) {
            this.line = readLine();
        } else {
            int len = store.size();
            this.line = store.remove(len - 1);
        }
    }

    public void unread() {
        store.add(line);
    }

    private MidCodeType midCType;
    private final Operand t1 = new Operand();
    private final Operand t2 = new Operand();
    private final Operate op = new Operate();
    private final Operand ans = new Operand();

    public void nextMidCode() throws IOException {
        read();
        if (line == null) {
           // midCType = MidCodeType.End;
            return;
        }
        String[] arr = line.split(" ");

    }

    public boolean myEqual(String[] arr, int pos, String str) {
        if (arr.length - 1 < pos) {
            return false;
        }
        return arr[pos].equals(str);
    }

    public boolean myEqual(String[] arr, int pos, String... str) {
        if (arr.length - 1 < pos) {
            return false;
        }
        for (String s : str) {
            if (arr[pos].equals(s)) {
                return true;
            }
        }
        return false;
    }

}
