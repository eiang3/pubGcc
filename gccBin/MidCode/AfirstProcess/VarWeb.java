package gccBin.MidCode.AfirstProcess;

import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;
import gccBin.MIPS.tool.Reg;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

/**
 * 将重名但无关系的节点重命名，形成网
 */
public class VarWeb {
    private static final BitSet zero = new BitSet();

    private TableSymbol tableSymbol; //网对应的变量所在符号表

    private String name; //网的名字
    private final BitSet def_set; //网的定义点
    private final BitSet use_set; //网的使用节点
    private final BitSet activeScope; //网的活跃范围

    private final HashSet<VarWeb> clash; //冲突网集
    private final HashSet<VarWeb> clashSub; //冲突集的副本，用于图着色算法

    private Reg reg; //分配到的寄存器

    public VarWeb(String name, BitSet def, BitSet use) {
        this.name = name;
        this.def_set = (BitSet) def.clone();
        this.use_set = (BitSet) use.clone();
        this.clash = new HashSet<>();
        this.clashSub = new HashSet<>();
        this.activeScope = new BitSet();
    }

    /**
     * 当选中一个变量在内存中，需要把这个变量从冲突图中移走
     */
    public void removeFromGraph() {
        for (VarWeb varWeb : clash) {
            varWeb.remove(this);
        }
    }

    public void remove(VarWeb varWeb) {
        this.clashSub.remove(varWeb);
        this.clash.remove(varWeb);
    }

    /**
     * 扩展节点活跃范围
     *
     * @param bitSet 要扩展的活跃范围
     */
    public void extendActiveScope(BitSet bitSet) {
        activeScope.or(bitSet);
    }

    public BitSet getActiveScope() {
        return (BitSet) activeScope.clone();
    }

    /**
     * 同一个变量的，两个网有交点，即其use集有交集，则可以合并
     *
     * @param varWeb 要合并的另一个网
     * @return *
     */
    public boolean canMerge(VarWeb varWeb) {
        BitSet use = varWeb.getUse_set();
        use.and(this.use_set);
        return !use.equals(zero);
    }

    /**
     * 只负责web的合并，不负责web的删除
     *
     * @param varWeb *
     */
    public void merge(VarWeb varWeb) {
        if (varWeb != this) {
            def_set.or(varWeb.getDef_set());
            use_set.or(varWeb.getUse_set());
        }
    }

    /***
     * 如果一个web的变量在另一个网的定义点处活跃，说明这两个web的变量冲突
     * @param varWeb *
     * @return *
     */
    public boolean collide(VarWeb varWeb) {
        BitSet otherActive = varWeb.getActiveScope();
        BitSet otherDef = varWeb.getDef_set();
        otherActive.and(this.def_set);
        otherDef.and(this.activeScope);
        return !(otherDef.equals(zero) && otherActive.equals(zero));
    }

    public void addClash(VarWeb varWeb) {
        if (varWeb != this) {
            this.clash.add(varWeb);
            this.clashSub.add(varWeb);
        }
    }

    /**
     * 为这个web变量分配寄存器，
     * 同时更新符号表表项对应的寄存器分配情况
     *
     * @param reg *
     */
    public void setReg(Reg reg) {
        ElementTable elementTable = tableSymbol.getElement(name);
        elementTable.getPosition().setReg(reg);
        this.reg = reg;
    }

    /**
     * 为这个变量分配一个reg
     * (在图着色法里，按逆序加入此节点，不和原有节点冲突的一个寄存器)
     * 默认会成功
     *
     * @param leave *
     */
    public void allocReg(ArrayList<VarWeb> leave) {
        ArrayList<Reg> arr = new ArrayList<>();
        for (VarWeb varWeb : clash) {
            if (leave.contains(varWeb)) {
                arr.add(varWeb.getReg());
            }
        }
        Reg reg1 = Reg.getAStoreReg(arr);
        setReg(reg1);
    }

//***********************set get 方法*******************************//

    public BitSet getUse_set() {
        return (BitSet) use_set.clone();
    }

    public BitSet getDef_set() {
        return (BitSet) def_set.clone();
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<VarWeb> getClashSub() {
        return clashSub;
    }

    public Reg getReg() {
        return reg;
    }

    public void setTableSymbol(TableSymbol tableSymbol) {
        this.tableSymbol = tableSymbol;
    }

    public String getName() {
        return name;
    }
}
