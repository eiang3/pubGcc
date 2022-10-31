package gccBin.MidCode.firstProcess;

import SymbolTableBin.APIMidCodeSymTable;
import SymbolTableBin.ElementTable;
import SymbolTableBin.ElementVar;
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
    public static boolean isVar(String name, TableSymbol tableSymbol){ //isVarNode
        if(name == null) return false;
        if(tableSymbol.getFather() != null && !name.contains("$")) {
            //不是根节点/不是全局变量  + 不是临时变量
            ElementTable elementTable = APIMidCodeSymTable.getInstance().
                    findElementRecur(tableSymbol,name);
            return elementTable instanceof ElementVar;
        }
        return false;
    }
}
