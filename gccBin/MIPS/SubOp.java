package gccBin.MIPS;

import gccBin.UnExpect;

public class SubOp {
    public static int compute(int a, String op, int b) {
        if (op.equals("+")) {
            return a + b;
        } else if (op.equals("-")) {
            return a - b;
        } else if (op.equals("*")) {
            return a * b;
        } else if (op.equals("/")) {
            return a / b;
        } else if (op.equals("%")) {
            return a % b;
        } else if (op.equals("<<")) {
            return a << b;
        } else if (op.equals(">>")) {
            return a >> b;
        } else {
            UnExpect.printf("unexpect operate");
            return 0;
        }
    }

    public static String getArrName(String s) {
        if (!s.contains("[")) return s;
        int l = s.indexOf('[');
        int r = s.indexOf(']');
        return s.substring(0, l);
    }

    public static String getArrSubscript(String s) {
        if (!s.contains("[")) return null;
        int l = s.indexOf('[');
        int r = s.indexOf(']');
        return s.substring(l + 1, r);
    }
}
