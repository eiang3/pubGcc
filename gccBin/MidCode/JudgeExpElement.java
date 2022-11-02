package gccBin.MidCode;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.Element.ElementVar;
import SymbolTableBin.TableSymbol;

public class JudgeExpElement {
    /**
     * 判断一个name是不是var节点
     * 需要讨论的var声明的局部变量
     * 需要排除的：
     * 数组，ok
     * 形参，ok
     * 临时变量，ok
     * 全局变量，
     * @return
     */
    public static boolean isVar(String name, TableSymbol tableSymbol) { //isVarNode
        if (name == null) return false;
        if (tableSymbol.getFather() != null && !(name.charAt(0) == '$')) {
            //不是根节点/不是全局变量  + 不是临时变量
            ElementTable elementTable = APIIRSymTable.getInstance().
                    findElementRecur(tableSymbol, name);
            return elementTable instanceof ElementVar;
        }
        return false;
    }

    public static boolean isNumber(String s) {
        if (s == null || s.length() == 0) {
            return false;
        } else {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                if(s.charAt(i) < '0' || s.charAt(i) > '9'){
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isTemp(String s) {
        if (s == null || s.length() == 0) {
            return false;
        } else {
            return !s.equals("$RET") && s.charAt(0) == '$';
        }
    }

    public static boolean isRET(String s){
        return s.equals("$RET");
    }

    public static boolean isAddr(ElementTable table, String s) {
        if (s == null || s.length() == 0) {
            return false;
        } else {
            return !s.contains("[") && table.getDimension()!=0;
        }
    }

    public static boolean isPlus(String op){
        return op.equals("+");
    }

    public static boolean isMinus(String op){
        return op.equals("-");
    }

    public static boolean isMul(String op){
        return op.equals("*");
    }

    public static boolean isDiv(String op){
        return op.equals("/");
    }

    public static boolean isMod(String op){
        return op.equals("%");
    }

    public static boolean isSll(String op){
        return op.equals("<<");
    }

    public static boolean isSrl(String op){
        return op.equals(">>");
    }

    public static boolean isExp(String s){
        return isTemp(s) || isNumber(s);
    }
}
