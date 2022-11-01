package gccBin.MidCode.firstProcess;

import SymbolTableBin.ElementTable;
import SymbolTableBin.TableSymbol;
import gccBin.MIPS.tool.Reg;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

public class VarWeb {
    private static BitSet zero = new BitSet();
    private TableSymbol tableSymbol;

    private String name;
    private BitSet def;
    private BitSet use;

    private final HashSet<VarWeb> clash;
    /////////////////////////////////////为了图着色算法
    private final HashSet<VarWeb> clashSub;

    private Reg reg;
    public VarWeb(String name,BitSet def,BitSet use) {
        this.name = name;
        this.def = def;
        this.use = use;
        this.clash = new HashSet<>();
        this.clashSub = new HashSet<>();
    }

    /**
     * 同一个变量的，两个网有交点，即其use集有交集，即可以合并
     * @param varWeb
     * @return
     */
    public boolean canMerge(VarWeb varWeb){
        BitSet use = varWeb.getUse();
        use.and(varWeb.getUse());
        return !use.equals(zero);
    }
    //private static WebManager

    public boolean collide(VarWeb varWeb){
        BitSet otherUse = varWeb.getUse();
        BitSet otherDef = varWeb.getDef();
        otherUse.and(getUse());
        otherDef.and(getDef());
        return !(otherDef.equals(zero) && otherUse.equals(zero));
    }

    public void addClash(VarWeb varWeb){
        this.clash.add(varWeb);
        this.clashSub.add(varWeb);
    }

    public BitSet getUse() {
        return (BitSet) use.clone();
    }

    public void merge(VarWeb varWeb){
        def.or(varWeb.getDef());
        use.or(varWeb.getUse());
    }

    public BitSet getDef(){
        return (BitSet) def.clone();
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<VarWeb> getClashSub() {
        return clashSub;
    }

    /**
     * 断掉和其他节点的联系
     */
    public void removeFromGraph(){
        for(VarWeb varWeb:clashSub){
            varWeb.remove(this);
        }
    }

    public void remove(VarWeb varWeb){
        this.clashSub.remove(varWeb);
        this.clash.remove(varWeb);
    }

    public void setTableSymbol(TableSymbol tableSymbol) {
        this.tableSymbol = tableSymbol;
    }

    public void setReg(Reg reg){
        ElementTable elementTable = tableSymbol.getElement(name);
        elementTable.getPosition().setReg(reg);
        this.reg = reg;
    }

    public Reg getReg() {
        return reg;
    }

    public void setReg(ArrayList<VarWeb> leave){
        ArrayList<Reg> arr = new ArrayList<>();
        for(VarWeb varWeb:clash){
            if(leave.contains(varWeb)){
                arr.add(varWeb.getReg());
            }
        }
        Reg reg1 = Reg.getAStoreReg(arr);
        setReg(reg1);
    }
}
