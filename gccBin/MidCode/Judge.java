package gccBin.MidCode;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementFParam;
import SymbolTableBin.Element.ElementFunc;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.Element.ElementVar;
import SymbolTableBin.TableSymbol;

public class Judge {

    /**
     * 判断一个name是不是var节点
     * 需要讨论的var声明的局部变量
     * 需要排除的：
     * 数组，ok
     * 形参，ok
     * 临时变量，ok
     * 全局变量，
     *
     * @return *
     */
    public static boolean is_LocalVar(String name, TableSymbol tableSymbol) { //isVarNode
        if (name == null || name.length() == 0) return false;
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);
        if (elementTable != null && !elementTable.isGlobal() && !(name.charAt(0) == '$')) {
            //不是根节点/不是全局变量  + 不是临时变量
            return elementTable instanceof ElementVar;
        }
        return false;
    }

    /**
     * 判断一个elementTable是不是local的
     * 非全局，非形参
     *
     * @param elementTable *
     * @return *
     */
    public static boolean isLocal(ElementTable elementTable) { //isVarNode
        return !elementTable.isGlobal() && !(elementTable instanceof ElementFParam);
    }

    /**
     * elementTable is FParam
     *
     * @param elementTable *
     * @return *
     */
    public static boolean isFParam(ElementTable elementTable) {
        return elementTable instanceof ElementFParam;
    }

    /**
     * elementTable is Global
     * ans
     * not funcDef
     *
     * @param elementTable *
     * @return *
     */
    public static boolean isGlobal(ElementTable elementTable) {
        return elementTable.isGlobal() && !(elementTable instanceof ElementFunc);
    }

    public static boolean isNumber(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        if (s.length() == 1 && s.charAt(0) == '-') {
            return false;
        }
        if (s.length() != 1 && s.charAt(0) == '-') {
            s = s.substring(1);
        }

        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;

    }

    public static boolean isTemp(String s) {
        if (s == null || s.length() == 0) {
            return false;
        } else {
            return !s.equals("$RET") && s.charAt(0) == '$';
        }
    }

    public static boolean isVar(String a) {
        return !isExp(a) && !isArrayValue(a);
    }

    public static boolean isRET(String s) {
        return s.equals("$RET");
    }

    public static boolean isArrayValue(String s) {
        return s.contains("[");
    }

    public static boolean isAddress(ElementTable elementTable, String s) {
        if (elementTable == null || s == null || s.length() == 0) {
            return false;
        } else {
            return !s.contains("[") && elementTable.getDimension() != 0;
        }
    }

    /**
     * 如果n是2的幂次（最小为1），则返回一个数字，代表幂次
     * 否则返回-1
     * 注意，负数会先转化为整数
     *
     * @param n *
     * @return *
     */
    public static int isTimesTwo(int n) {
        if (n < 0) n = n * -1;
        if (!((n & (n - 1)) == 0)) {
            return -1;
        }
        if (n == 0) return -1;
        return Integer.toBinaryString(n).length() - 1;
    }

    public static boolean isDivOptimize(String op) {
        return op.equals("**");
    }

    public static boolean isPlus(String op) {
        return op.equals("+");
    }

    public static boolean isMinus(String op) {
        return op.equals("-");
    }

    public static boolean isMul(String op) {
        return op.equals("*");
    }

    public static boolean isDiv(String op) {
        return op.equals("/");
    }

    public static boolean isMod(String op) {
        return op.equals("%");
    }

    public static boolean isSll(String op) {
        return op.equals("<<");
    }

    public static boolean isSrl(String op) {
        return op.equals(">>");
    }

    public static boolean isSra(String op) {
        return op.equals(">>>");
    }

    public static boolean isExp(String s) {
        return isTemp(s) || isNumber(s);
    }

}
