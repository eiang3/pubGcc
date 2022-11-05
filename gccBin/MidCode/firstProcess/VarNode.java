package gccBin.MidCode.firstProcess;

import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;

import java.util.*;

/**
 * 待分配寄存器的节点集
 * ok
 */
public class VarNode {
    private final String name; //name
    private final TableSymbol tableSymbol; //这个节点所在的符号表


    private final ArrayList<Integer> genSite; //gen——line对应的标号。
    private final BitSet genSet;
    private final BitSet useSet;

    private final HashMap<Integer, BitSet> defUseChain;
    //定义使用链,每个gen点都对应一个定义使用链
    private final HashMap<Integer, VarWeb> web; //定义使用链形成的网

    public VarNode(String name, TableSymbol tableSymbol) {
        this.name = name;
        this.tableSymbol = tableSymbol;

        web = new HashMap<>();
        defUseChain = new HashMap<>();
        genSet = new BitSet();
        useSet = new BitSet();
        genSite = new ArrayList<>();
    }

    public void addGen(int index) {
        genSite.add(index);
        genSet.set(index);
        defUseChain.put(index, new BitSet());
    }

    public void addUse(int index) {
        useSet.set(index);
    }

    /**
     * @param index  定义点索引
     * @param bitSet 可能包含该节点的使用点的line
     */
    public void renewUseDefChain(int index, BitSet bitSet) {
        BitSet use = (BitSet) useSet.clone();
        use.and(bitSet);
        this.defUseChain.get(index).or(use);
    }

    /**
     * 定义使用链计算完毕后，形成网。
     */
    public void generateWeb() {
        //如果defUseChain == 0说明定以后没有被使用，不需要为其分配reg
        //也说明这是一个useless的变量
        if (defUseChain.size() == 0) {
            ElementTable elementTable = tableSymbol.getElement(name);
            elementTable.setUseless(true);
            VarNodeManager.getInstance().removeVarNode(name);
            return;
        }

        //首先将每个定义使用链形成一个网
        for (int i : defUseChain.keySet()) {
            BitSet def = new BitSet();
            def.set(i);
            BitSet use = (BitSet) defUseChain.get(i).clone();
            web.put(i, new VarWeb(name, def, use));
        }

        ArrayList<Integer> tag = new ArrayList<>(genSite);

        while (!webDone()) {
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
        }
    }

    /**
     * 判断目前的网络是否可以继续合并
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

    //*************************** get set 方法 ******************************************//
    public HashMap<Integer, VarWeb> getWeb() {
        return web;
    }

    public TableSymbol getTableSymbol() {
        return tableSymbol;
    }

    public HashMap<Integer, BitSet> getDefUseChain() {
        return defUseChain;
    }

    public BitSet getGenSet() {
        return (BitSet) genSet.clone();
    }

    public String getName() {
        return name;
    }

    public BitSet getUseSet() {
        return (BitSet) useSet.clone();
    }
}
