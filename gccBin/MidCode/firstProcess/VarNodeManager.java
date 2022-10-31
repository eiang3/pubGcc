package gccBin.MidCode.firstProcess;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.Line;

import java.util.BitSet;
import java.util.HashMap;

public class VarNodeManager {
    private static VarNodeManager varNodeManager;

    private final HashMap<String, VarNode> name2Node;

    private VarNodeManager(){
        name2Node = new HashMap<>();
    }

    public static VarNodeManager getInstance(){
        if(varNodeManager == null){
            varNodeManager = new VarNodeManager();
        }
        return varNodeManager;
    }

    public void distributeGenAndUseToVar(Line line) {
        if (line.getGen()!=null) {
            name2Node.get(line.getGen()).addGen(line.getIndex());
        }
        for (String name : line.getUse()) {
            name2Node.get(name).addUse(line.getIndex());
        }
    }

    /**
     * 在分析到VarDefLine使用
     * @param name
     * @param tableSymbol
     */
    public void addVarNode(String name, TableSymbol tableSymbol) {
        VarNode node = new VarNode(name, tableSymbol);
        this.name2Node.put(name, node);
    }

    /**
     * 交互，得到一各Var的gen集
     * @param name
     * @return
     */
    public BitSet getOneVarGen(String name){
        VarNode varNode = name2Node.get(name);
        if(varNode == null) return null;
        return (BitSet) varNode.getGenSet().clone();
    }
    /**
     * 得到所有var的定义——使用链
     */
    public VarNode getOneVar(String name){
       return name2Node.get(name);
    }
}
