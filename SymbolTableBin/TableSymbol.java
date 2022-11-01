package SymbolTableBin;

import gccBin.ERROR.ErrorHandle;
import gccBin.MIPS.MIPS;
import gccBin.MidCode.firstProcess.MidCodeFirst;

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

    public TableSymbol(TableSymbol father){
        this.elements = new HashMap<>();
        this.father = father;
        this.children = new ArrayList<>();
        this.cIndex = 0;

        if(this.father == null) {  //记录根节点
            APIErrorSymTable.getInstance().setFatherTable(this);
            APIGramSymTable.getInstance().setRootTable(this);
            APIIRSymTable.getInstance().setRootTable(this);
            MIPS.getInstance().setTableSymbol(this);
            MidCodeFirst.getInstance().setRootTable(this);
        } else {
            this.father.addChild(this);
        }

        this.level = (father == null)? 0 : father.getLevel() + 1;
    }

    //向符号表中添加元素
    public void addElement(ElementTable elementTable) throws IOException {
        ErrorHandle.getInstance().BNameRedefinition(elementTable.getName());

        this.elements.put(elementTable.getName(),elementTable);
        /*for(int i = 0;i < level;i++){
            System.out.print("  ");
        }
        System.out.println(elementTable);*/
    }

    public TableSymbol getFather() {
        return father;
    }

    public int getLevel(){
        return this.level;
    }

    //判断符号表是否包含元素
    public boolean contain(String str){
        return this.elements.containsKey(str);
    }

    //从符号表中获得元素
    public ElementTable getElement(String str){
        return this.elements.get(str);
    }

    public void addChild(TableSymbol tableSymbol){
         this.children.add(tableSymbol);
    }

    public TableSymbol getNextChild(){
        return this.children.get(cIndex++);
    }

    public void remove(ElementTable a){
        this.elements.remove(a.getName());
    }
}
