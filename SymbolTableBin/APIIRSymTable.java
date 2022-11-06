package SymbolTableBin;

import SymbolTableBin.Element.*;

import javax.xml.bind.Element;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class APIIRSymTable {
    private static APIIRSymTable instance;

    private TableSymbol rootTable;

    private final HashMap<String,TableSymbol> funs = new HashMap<>();


    //在第一遍更新完符号表中重定义的变量名字后，需要把相应的表项更新。
    private final HashMap<ElementTable,TableSymbol> redefineElement = new HashMap<>();

    private APIIRSymTable(){}

    public static APIIRSymTable getInstance(){
        if(instance == null){
            instance = new APIIRSymTable();
        }
        return instance;
    }

    public void addToRedefineElement(ElementTable elementTable,TableSymbol tableSymbol){
        this.redefineElement.put(elementTable,tableSymbol);
    }

    /**
     * 刷新符号表项，把重定义的变量重命名
     */
    public void refreshTable() throws IOException {
        for(ElementTable elementTable:redefineElement.keySet()){
            elementTable.refreshName(redefineElement.get(elementTable));
        }
    }

    public void setRootTable(TableSymbol rootTable) {
        this.rootTable = rootTable;
    }

    public void addFunTable(String str,TableSymbol tableSymbol){
        this.funs.put(str,tableSymbol);
    }

    /**
    得到符号表中函数类型表项，默认一定是函数
     */
    public ElementFunc getFuncElement(String funcName){
        return (ElementFunc)
                this.rootTable.getElement(funcName);
    }

    public TableSymbol getFuncSymTable(String funcName){
        return funs.get(funcName);
    }

    /**
    从此符号表向上递归查找表项
     */
    public ElementTable findElementRecur(TableSymbol table, String str){
        if(str == null || str.length() == 0) return null;
        while(table != null){
            if(table.contain(str)){
                return table.getElement(str);
            } else {
                table = table.getFather();
            }
        }
        return null;
    }

    /**
     * 如果传入的符号经查找是全局变量，返回查找到的表项，否则，返回null
     */
    public ElementTable findGlobalElement(String str){
        if(!rootTable.contain(str)) return  null;
        return rootTable.getElement(str);
    }

    /*public int findAddress(TableSymbol tableSymbol,String name){
        ElementTable elementTable = findTableElementRecur(tableSymbol,name);
        if(elementTable == null) return 0;
        return elementTable.getPosition().getNum();
    }*/

    /**
     * 判断变量是否是0维常量
     */
    public boolean isConstNum(TableSymbol tableSymbol,String name){
        ElementTable elementTable = findElementRecur(tableSymbol,name);
        return elementTable instanceof ElementConst;
    }

    /**
     *在isConstNum的前提下得到值
     */
    public int getConstNum(TableSymbol tableSymbol,String name){
        ElementTable elementTable = findElementRecur(tableSymbol,name);
        if(elementTable instanceof ElementConst){
            return ((ElementConst)elementTable).getValue().getNum();
        }
        return 0;
    }

    /***
     *找到二维数组第2位的长度
     */
    public int findTwoDimArrayLen(TableSymbol tableSymbol,String name){
        ElementTable elementTable = findElementRecur(tableSymbol,name);
        if(elementTable instanceof ElementConstArray){
            return ((ElementConstArray) elementTable).getLen();
        } else if (elementTable instanceof ElementVarArray) {
            return ((ElementVarArray) elementTable).getLen();
        } else {
            return -1;
        }
    }

    /**
     * 通过符号表和名称寻找0维常量值
     * @param table
     * @param str
     * @return
     */
    public int findValue(TableSymbol table,String str){
        ElementTable elementTable = findElementRecur(table,str);
        if(elementTable instanceof ElementConst)
            return ((ElementConst)elementTable).getValue().getNum();
        return -1;
    }

    /**
     * 通过符号表和名称寻找1维常量值
     * @param table
     * @param str
     * @param oneDim
     * @return
     */
    public int findValue(TableSymbol table,String str,int oneDim){
        ElementConstArray elementArray = (ElementConstArray) findElementRecur(table,str);
        if(elementArray != null) return elementArray.getConstArrValue(oneDim);
        else return -1;
    }

    /**
     * 通过符号表和名称寻找2维常量值
     * @param table
     * @param str
     * @param oneDim
     * @param twoDim
     * @return
     */
    public int findValue(TableSymbol table,String str,int oneDim,int twoDim){
        ElementConstArray elementArray = (ElementConstArray) findElementRecur(table,str);
        if(elementArray != null) return elementArray.getConstArrValue(oneDim,twoDim);
        else return -1;
    }


    public TableSymbol getRootTable() {
        return rootTable;
    }
}
