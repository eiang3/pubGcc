package SymbolTableBin;

import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.Element.ElementVar;
import gccBin.ERROR.ErrorHandle;
import gccBin.MidCode.firstProcess.IRFirst;

import javax.xml.bind.Element;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
符号表 ok
 */

public class TableSymbol {
    private final HashMap<String, ElementTable> elements; //符号表项

    private final TableSymbol father; //父表

    private final ArrayList<TableSymbol> children;
    private int cIndex; //生成目标代码时遍历的属性

    private final int level; //为了打印符号表，而构建的变量

    public TableSymbol(TableSymbol father) {
        this.elements = new HashMap<>();
        this.father = father;
        this.children = new ArrayList<>();
        this.cIndex = 0;

        if (this.father == null) {  //记录根节点
            APIErrorSymTable.getInstance().setFatherTable(this);
            APIGramSymTable.getInstance().setRootTable(this);
            APIIRSymTable.getInstance().setRootTable(this);
            IRFirst.getInstance().setRootTable(this);
        } else {
            this.father.addChild(this);
        }

        this.level = (father == null) ? 0 : father.getLevel() + 1;
    }

    //向符号表中添加元素
    public void addElement(ElementTable elementTable) throws IOException {
        //ErrorHandle.getInstance().BNameRedefinition(elementTable.getName());
        if (this.elements.containsKey(elementTable.getName())) {
            ErrorHandle.getInstance().BNameRedefinition();
        }
        this.elements.put(elementTable.getName(), elementTable);
    }

    public TableSymbol getFather() {
        return father;
    }

    public int getLevel() {
        return this.level;
    }

    //判断符号表是否包含元素
    public boolean contain(String str) {
        return this.elements.containsKey(str);
    }

    //从符号表中获得元素
    public ElementTable getElement(String str) {
        return this.elements.get(str);
    }

    public void addChild(TableSymbol tableSymbol) {
        this.children.add(tableSymbol);
    }

    public TableSymbol getNextChild() {
        return this.children.get(cIndex++);
    }

    public void remove(ElementTable a) {
        this.elements.remove(a.getName());
    }

    public ArrayList<ElementVar> findReNamesVar(String old) {
        ArrayList<ElementVar> ret = new ArrayList<>();
        if (elements.containsKey(old)) {
            ElementTable a = elements.get(old);
            if (a instanceof ElementVar && !a.isGlobal()) {
                ret.add((ElementVar) a);
            }
            return ret;
        }

        for(ElementTable element : elements.values()){
            if(element instanceof ElementVar){
                ElementVar elementVar = (ElementVar) element;
                if(old.equals(elementVar.getOldName())){
                    ret.add(elementVar);
                }
            }
        }
        return ret;
    }
}
