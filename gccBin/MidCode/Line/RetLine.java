package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * ret t1
 * ret
 * ret main //main函数退出的时候
 */
public class RetLine extends Line {
    private String exp;

    private String funcName;

    private boolean gotoExit; //

    public RetLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        gotoExit = false;
        if (ele.length == 2) {
            if(ele[1].equals("main")){
                gotoExit = true;
            }
            exp = ele[1];
        }
    }


    public boolean isGotoExit() {
        return gotoExit;
    }

    public String getExp() {
        return exp;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }
}
