package gccBin.MidCode.firstProcess;

import java.util.BitSet;
import java.util.HashMap;

public class VarWeb {
    private static BitSet zero = new BitSet();

    private String name;
    private BitSet def;
    private BitSet use;

    public VarWeb(String name,BitSet def,BitSet use) {
        this.name = name;
        this.def = def;
        this.use = use;
    }

    /**
     * 两个网有冲突，即其use集有交集
     * @param varWeb
     * @return
     */
    public boolean collide(VarWeb varWeb){
        BitSet use = varWeb.getUse();
        use.and(varWeb.getUse());
        return !use.equals(zero);
    }
    //private static WebManager


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
}
