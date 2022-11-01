package gccBin.MIPS;

import java.io.*;

public class a {
    private static a instance;

    private boolean end;

    private BufferedReader br;


    private a() {
        this.end = false;
    }

    public static a get() {
        if (instance == null) {
            instance = new a();
        }
        return instance;
    }

}
