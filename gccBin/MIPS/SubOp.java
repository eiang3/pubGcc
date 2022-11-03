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
}
