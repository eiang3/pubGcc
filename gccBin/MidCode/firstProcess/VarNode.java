package gccBin.MidCode.firstProcess;

import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;
import gccBin.MIPS.MIPS;
import gccBin.MidCode.LineManager;

import java.util.*;

public class VarNode {
    private final String name;
    private final TableSymbol tableSymbol;

    private final ArrayList<Integer> genSite; //gen——line对应的标号。

    private final HashMap<Integer, BitSet> defUseChain;

    private final HashMap<Integer, VarWeb> web;

    private final BitSet genSet;
    private final BitSet useSet;

    /**
     * 节点的活跃范围
     */
    //private final BitSet activeScope;

    public VarNode(String name, TableSymbol tableSymbol) {
        this.name = name;
        this.tableSymbol = tableSymbol;
        web = new HashMap<>();
        defUseChain = new HashMap<>();
        genSet = new BitSet();
        useSet = new BitSet();
        genSite = new ArrayList<>();
        //activeScope = new BitSet();
    }



    public BitSet getGenSet() {
        return genSet;
    }

    public void addGen(int index) {
        genSite.add(index);
        defUseChain.put(index, new BitSet());
        genSet.set(index);
    }

    public void addUse(int index) {
        useSet.set(index);
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @param index
     * @param bitSet
     */
    public void renewUseDefChain(int index, BitSet bitSet) {
        BitSet use = (BitSet) useSet.clone();
        use.and(bitSet);
        this.defUseChain.get(index).or(use);
    }

    /**
     * 定义使用链计算完毕后，形成网。
     * bug ？
     */
    public void generateWeb() {
        if(defUseChain.size() == 0){
            ElementTable elementTable = tableSymbol.getElement(name);
            elementTable.setUseless(true);
            VarNodeManager.getInstance().removeVarNode(name);
            return;
        }

        for (int i : defUseChain.keySet()) {
            BitSet def = new BitSet();
            def.set(i);
            BitSet use = (BitSet) defUseChain.get(i).clone();
            web.put(i, new VarWeb(name, def, use));
        }

        ArrayList<Integer> tag = new ArrayList<>(genSite);
        //tag 集最后应该是答案web的key集
        //对每一个def点进行遍历，一遍会将所有与他冲突的点加入；
        do {
            for (int i : genSite) {
                if (tag.contains(i)) { //说明这个def点没有被消除（加入其他web）
                    Iterator<Integer> it = tag.iterator();
                    while (it.hasNext()) {
                        int k = it.next();
                        if (k != i && web.get(i).canMerge(web.get(k))) {
                            web.get(i).merge(web.get(k));
                            web.remove(k);
                            it.remove();
                        }
                    }
                }
            }
        }while(!webDone());
    }

    /**
     * 判断目前的网络是否冲突
     */
    private boolean webDone() {
        ArrayList<VarWeb> web = new ArrayList<>(this.web.values());
        for (int i = 0; i < web.size(); i++) {
            for (int k = i + 1; k < web.size(); k++) {
                if (web.get(i).canMerge(web.get(k))) {
                    return false;
                }
            }
        }
        return true;
    }

    public HashMap<Integer, VarWeb> getWeb() {
        return web;
    }

    public TableSymbol getTableSymbol() {
        return tableSymbol;
    }

    public HashMap<Integer, BitSet> getDefUseChain() {
        return defUseChain;
    }
}
