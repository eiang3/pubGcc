package SymbolTableBin;

import gccBin.MIPS.tool.Reg;

/*
符号表基本元素 ok
 */
public class ElementTable {
    private final String name; //名称
    private final TypeTable type ; //INT,VOID
    private final TypeTable decl;  //CONST,VAR,FUNC_F_PARAM,,FUNC
    private final int dimension ; //数值.函数(0)  一维数组(1)  二维数组(2)
    //
    private final Position position; //位置 生成目标时查询
    private boolean global;

    public ElementTable(String name, TypeTable type, TypeTable decl,
                        int dimension){
        this.name = name;
        this.type = type;
        this.decl = decl;
        this.dimension = dimension;
        this.position  = new Position();
        this.global = false;
    }

    public String getName() {
        return name;
    }

    public TypeTable getDecl() {
        return decl;
    }

    public int getDimension() {
        return dimension;
    }

    public Position getPosition() {
        return position;
    }

    public TypeTable getType() {
        return type;
    }

    //为了打印可视化符号表而建立的方法
    @Override
    public String toString(){
        return String.format("name: %s, type: %s, kind: %s dimension: %d",
                this.name,this.type,this.decl,this.dimension);
    }

    public String getMidCodeName(){
        return this.name;
    }

    public void setMemOff(int off){
        position.setOff(off);
    }

    public int getMemOff(){
        if(isHasReg()) return -1;
        return position.getOff();
    }

    public Reg getReg(){
        if(isHasReg()) return position.getReg();
        return null;
    }

    public boolean isHasReg(){
        return position.isHasReg();
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
