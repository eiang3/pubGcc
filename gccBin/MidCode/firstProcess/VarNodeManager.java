package gccBin.MidCode.firstProcess;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementVar;
import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.Line;
import gccBin.MidCode.LineManager;

import java.io.IOException;
import java.util.*;

public class VarNodeManager {
    private static VarNodeManager varNodeManager;

    private final HashMap<String, VarNode> name2Node;

    private VarNodeManager() {
        name2Node = new HashMap<>();
        name2Web_readyToFormClash = new HashMap<>();
    }

    public static VarNodeManager getInstance() {
        if (varNodeManager == null) {
            varNodeManager = new VarNodeManager();
        }
        return varNodeManager;
    }

    public void distributeGenAndUseToVar(Line line) {
        if (line != null) {
            if (line.getGen() != null) {
                name2Node.get(line.getGen()).addGen(line.getIndex());
            }
            for (String name : line.getUse()) {
                name2Node.get(name).addUse(line.getIndex());
            }
        }
    }

    /**
     * @param name
     * @param tableSymbol
     */
    public void addVarNode(String name, TableSymbol tableSymbol) {
        VarNode node = new VarNode(name, tableSymbol);
        this.name2Node.put(name, node);
    }

    /**
     * 将只定义不使用的变量移去
     *
     * @param name
     */
    public void removeVarNode(String name) {
        this.name2Node.remove(name);
    }

    /**
     * 交互，得到一各Var的gen集
     *
     * @param name
     * @return
     */
    public BitSet getOneVarGen(String name) {
        VarNode varNode = name2Node.get(name);
        if (varNode == null) return null;
        return (BitSet) varNode.getGenSet().clone();
    }

    /**
     * 得到所有var的定义——使用链
     */
    public VarNode getOneVar(String name) {
        return name2Node.get(name);
    }

    public void generateWeb() {
        for (String name : name2Node.keySet()) {
            name2Node.get(name).generateWeb();
        }
    }


    /***
     * 加入未划分的冲突图，ok更新符号表表项，ok对相应lines和进行重命名。
     */
    private final HashMap<String, VarWeb> name2Web_readyToFormClash; //最后保存下来的全部冲突变量

    public HashMap<String, VarWeb> getName2Web_readyToFormClash() {
        return name2Web_readyToFormClash;
    }

    public void renewSymTableAndLine() throws IOException {
        // bug ? 不会用
        HashSet<String> varNames = new HashSet<>(name2Node.keySet());
        ;
        //
        for (String name : varNames) {
            VarNode node = name2Node.get(name);
            HashMap<Integer, VarWeb> web = node.getWeb();
            TableSymbol tableSymbol = node.getTableSymbol();

            if (web.size() == 1) {
                ArrayList<VarWeb> webs = new ArrayList<>(web.values());
                webs.get(0).setTableSymbol(tableSymbol);
                name2Web_readyToFormClash.put(name, webs.get(0));
                continue;
            }

            // bug ? 不存在?
            ElementVar elementVar = (ElementVar)
                    APIIRSymTable.getInstance().findElementRecur(tableSymbol, name);

            int now = 0;
            for (int key : web.keySet()) {
                VarWeb varWeb = web.get(key); //一个web就是一个新变量
                String newName = elementVar.getName() + "$$" + now;
                //变量的新名字 这个变量可能之前就被重命名过一次
                varWeb.setTableSymbol(tableSymbol);
                varWeb.setName(newName); //对web网进行重命名。
                now++;
                //对相应lines和进行重命名
                LineManager.getInstance().reGenNameLine(
                        varWeb.getDef(), name, newName);

                LineManager.getInstance().reUseNameLine(
                        varWeb.getUse(), name, newName);
                //更新符号表表项(仅仅是重命名)
                ElementVar t1 = elementVar.myCopy(newName);
                tableSymbol.addElement(t1);
                //加入未划分的冲突图
                name2Web_readyToFormClash.put(newName, varWeb);
            }
            tableSymbol.remove(elementVar);
        }
    }

    /**
     * 得到变量冲突图
     */
    public void getClashGraph() {
        // 优化 ？
        ArrayList<String> varNames = new ArrayList<>(name2Web_readyToFormClash.keySet());
        for (int i = 0; i < varNames.size(); i++) {
            for (int j = i+1; j < varNames.size(); j++){
                String name1 = varNames.get(i);
                String name2 = varNames.get(j);
                VarNode x = VarNodeManager.getInstance().getOneVar(name1);
                VarNode y = VarNodeManager.getInstance().getOneVar(name2);
                VarWeb xx = name2Web_readyToFormClash.get(name1);
                VarWeb yy = name2Web_readyToFormClash.get(name2);
                if(varNodeClashVarWeb(x,yy) ||
                varNodeClashVarWeb(y,xx)){
                    xx.addClash(yy);
                    yy.addClash(xx);
                }
            }
        }
    }

    private static final BitSet zero = new BitSet();
    private boolean varNodeClashVarWeb(VarNode node,VarWeb web){
        BitSet active = node.getActiveScope();
        BitSet def = web.getDef();
        active.and(def);
        return !active.equals(zero);
    }
}
