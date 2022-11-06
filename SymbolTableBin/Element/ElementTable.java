package SymbolTableBin.Element;

import SymbolTableBin.Position;
import SymbolTableBin.TableSymbol;
import SymbolTableBin.TypeTable;
import gccBin.MIPS.tool.Reg;
import gccBin.UnExpect;

import java.io.IOException;

/*
符号表基本元素 ok
 */
public class ElementTable {
    private String name; //名称
    private final TypeTable type ; //INT,VOID
    private final TypeTable decl;  //CONST,VAR,FUNC_F_PARAM,,FUNC
    private final int dimension ; //数值.函数(0)  一维数组(1)  二维数组(2)
    //
    private final Position position; //位置 生成目标时查询
    private boolean global;
    private boolean useless; //只定义不使用的变量

    private int subScript;  //如果有重命名的变量，需要添加下标保证它们的名字不一样。

    private int falseRow;

    public ElementTable(String name, TypeTable type, TypeTable decl,
                        int dimension,int falseRow){
        this.name = name;
        this.type = type;
        this.decl = decl;
        this.dimension = dimension;
        this.position  = new Position();
        this.global = false;
        this.subScript = -1;
        this.falseRow = falseRow;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setMemOff(int off){
        position.setOff(off);
    }

    public int getMemOff(){
        if(isHasReg()) return -1;
        if(position.getOff() < 0) {
            UnExpect.unexpect("element get MemOff that not in mem");
            return -1;
        }
        return position.getOff();
    }

    public Reg getReg(){
        if(isHasReg()) return position.getReg();
        UnExpect.unexpect("element get Reg that not in reg");
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

    public boolean isUseless() {
        return useless;
    }

    public void setUseless(boolean useless) {
        this.useless = useless;
    }

    public void setReg(Reg reg){
        this.position.setReg(reg);
    }

    //************************ 变量重命名 ************************//
    public void setSubScript(int subScript) {
        this.subScript = subScript;
    }

    public String getIRName() {
        if (this.subScript == -1) {
            return getName();
        } else {
            return getName() + "$" + subScript;
        }
    }

    public void refreshName(TableSymbol tableSymbol) throws IOException {
        tableSymbol.remove(this);
        setName(this.getIRName());
        tableSymbol.addElement(this);
    }

    public int getFalseRow() {
        return falseRow;
    }
}
