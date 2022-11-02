package SymbolTableBin;

import GramTree.Element.FuncFParam;
import SymbolTableBin.Element.ElementFunc;
import SymbolTableBin.Element.ElementTable;

import java.util.ArrayList;

public class APIErrorSymTable {
    private static APIErrorSymTable instance;
    private TableSymbol fatherTable;

    private static final boolean close = false;

    private APIErrorSymTable() {
    }

    public static APIErrorSymTable getInstance() {
        if (instance == null) {
            instance = new APIErrorSymTable();
        }
        return instance;
    }

    public void setFatherTable(TableSymbol fatherTable) {
        if(close) return;
        this.fatherTable = fatherTable;
    }

    public boolean isNameReDefined(String str) {
        if(close) return false;
        TableSymbol nowTable = APIGramSymTable.getInstance().getNowTable();
        return nowTable.contain(str);
    }

    public boolean isNameUnDefined(String str) {
        if(close) return false;
        TableSymbol nowTable = APIGramSymTable.getInstance().getNowTable();
        while (nowTable != null) {
            if (nowTable.contain(str)) {
                return false;
            }
            nowTable = nowTable.getFather();
        }
        return true;
    }

    public ElementFunc getFuncDef(String str) {
        if(close) return null;

        if (fatherTable.contain(str)) {
            ElementTable tableElement = fatherTable.getElement(str);
            if (tableElement instanceof ElementFunc) {
                return (ElementFunc) tableElement;
            }
        }
        return null;
    }

    public ArrayList<FuncFParam> getFuncDefFParams(String str){
        if(close) return null;
        ElementFunc elementFunc = getFuncDef(str);
        if(elementFunc == null) return new ArrayList<>();
        else return elementFunc.getParams();
    }

    public ElementTable getElement(String str) {
        if(close) return null;

        TableSymbol nowTable = APIGramSymTable.getInstance().getNowTable();

        while (nowTable != null) {
            if (nowTable.contain(str)) {
                return nowTable.getElement(str);
            }
            nowTable = nowTable.getFather();
        }

        return null;
    }
}
