package gccBin;

import static java.lang.System.exit;

public class UnExpect {
    public static void error() {
        exit(0);
    }

    public static void printf(String s) {
        System.out.println(s);
    }

    public static void tempNotInMemAndReg(String temp) {
        System.out.println(temp + "is a temp not in mem or reg");
    }

    public static void unexpect(String s){
        System.out.println("unexpect error in "+ s);
    }
}
