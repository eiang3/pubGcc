package gccBin.MIPS.tool;

public class Func {
    private static Func func;

    private Func() {

    }

    public static Func get() {
        if (func == null) {
            func = new Func();
        }
        return func;
    }

}
