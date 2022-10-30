package gccBin.MidCode.firstProcess;

import SymbolTableBin.TableSymbol;

import java.util.ArrayList;
import java.util.BitSet;

public class VarNode {
    private String name;
    private TableSymbol tableSymbol;

    private ArrayList<Integer> genSite;
    private BitSet genSet;
    private BitSet useSet;

    public VarNode(String name,TableSymbol tableSymbol){
        this.name = name;
        this.tableSymbol = tableSymbol;

        genSet = new BitSet();
        useSet = new BitSet();
        genSite = new ArrayList<>();
    }

    public void addGen(int index){
        genSite.add(index);
        genSet.set(index);
    }

    public void addUse(int index){
        useSet.set(index);
    }
}
