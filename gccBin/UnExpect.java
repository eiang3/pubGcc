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

    public static void unexpect(String s) {
        System.out.println("unexpect error in " + s);
    }

    public static void varNotInTable(String s) {
        System.out.println("cannot find " + s + " in tableSymbol");
    }

    /**
     * s is not global,local or fParam
     *
     * @param s *
     */
    public static void elementNotAnything(String s) {
        System.out.println(s + " is not global,local or fParam");
    }

    public static void negOff(String s) {
        System.out.println("neg off in " + s);
    }

    public static void notAnExp(String s) {
        System.out.println(s + " is not a number or temp");
    }

    public static void fParamIndexError(String s, int index) {
        System.out.println(s + " is a FParam has a error index of " + index);
    }

    public static void notNum(String s) {
        System.out.println(s + " is not a num");
    }

    public static void NULL(){
        System.out.println("null pointer");
    }
}
