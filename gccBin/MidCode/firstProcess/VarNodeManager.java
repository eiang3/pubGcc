package gccBin.MidCode.firstProcess;

import SymbolTableBin.Element.ElementVar;
import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.Line;
import gccBin.MidCode.LineManager;

import java.io.IOException;
import java.util.*;

public class VarNodeManager {
    private static VarNodeManager varNodeManager;

    private final HashMap<String, VarNode> name2Node;

    private final HashMap<String, VarWeb> name2Web; //最后保存下来的全部冲突变量

    private VarNodeManager() {
        name2Node = new HashMap<>();
        name2Web = new HashMap<>();
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
     * 向待分配节点集添加节点
     *
     * @param name        *
     * @param tableSymbol *
     */
    public void addVarNode(String name, TableSymbol tableSymbol) {
        VarNode node = new VarNode(name, tableSymbol);
        this.name2Node.put(name, node);
    }

    /**
     * 交互，得到一各Var的gen集
     *
     * @param name *
     * @return *
     */
    public BitSet getOneVarGen(String name) {
        VarNode varNode = name2Node.get(name);
        if (varNode == null) return null;
        return (BitSet) varNode.getGenSet().clone();
    }

    public VarNode getOneVar(String name) {
        return name2Node.get(name);
    }

    public void generateWeb() {
        ArrayList<String> names = new ArrayList<>(name2Node.keySet());
        for (String name : names) {
            name2Node.get(name).generateWeb();
        }
    }

    /**
     * 在对一个变量的多个网进行分离
     * 并更新符号表和line
     *
     * @throws IOException *
     */
    public void separateVarWebs() throws IOException {
        HashSet<String> varNames = new HashSet<>(name2Node.keySet());
        for (String name : varNames) {

            VarNode node = name2Node.get(name);
            HashMap<Integer, VarWeb> varWebs = node.getWeb();
            TableSymbol tableSymbol = node.getTableSymbol();

            if (varWebs.size() == 1) {
                ArrayList<VarWeb> webs = new ArrayList<>(varWebs.values());
                webs.get(0).setTableSymbol(tableSymbol);
                name2Web.put(name, webs.get(0));
                continue;
            }

            //assert right
            ElementVar elementVar = (ElementVar) tableSymbol.getElement(name);

            int now = 0;
            for (int key : varWebs.keySet()) {
                VarWeb varWeb = varWebs.get(key); //一个web就是一个新变量
                String newName = elementVar.getName() + "$$" + now;
                now++;

                varWeb.setTableSymbol(tableSymbol);
                varWeb.setName(newName); //对web网进行重命名。
                //对相应lines和进行重命名
                LineManager.getInstance().reGenNameLine(
                        varWeb.getDef_set(), name, newName);

                LineManager.getInstance().reUseNameLine(
                        varWeb.getUse_set(), name, newName);
                //更新符号表表项(仅仅是重命名)

                ElementVar t1 = elementVar.myCopy(newName);
                tableSymbol.addElement(t1);
                //加入未划分的冲突图
                name2Web.put(newName, varWeb);
            }
            tableSymbol.remove(elementVar);
        }
    }

    /**
     * 得到变量冲突图
     */
    public void getClashGraph() {
        ArrayList<String> varNames = new ArrayList<>(name2Web.keySet());
        for (int i = 0; i < varNames.size(); i++) {
            for (int j = i + 1; j < varNames.size(); j++) {
                String name1 = varNames.get(i);
                String name2 = varNames.get(j);
                VarWeb xx = name2Web.get(name1);
                VarWeb yy = name2Web.get(name2);
                if (xx.collide(yy)) {
                    xx.addClash(yy);
                    yy.addClash(xx);
                }
            }
        }
    }

    /**
     * 将只定义不使用的变量移去
     *
     * @param name *
     */
    public void removeVarNode(String name) {
        this.name2Node.remove(name);
    }

    public HashMap<String, VarWeb> getName2Web() {
        return name2Web;
    }

    public VarWeb getOneVarWeb(String name) {
        return name2Web.get(name);
    }

    public void printfVarNodeMessage() {
        for (String var : name2Node.keySet()) {
            VarNode varNode = name2Node.get(var);
            HashMap<Integer, BitSet> defUseChain = varNode.getDefUseChain();
            System.out.println(var);
            for (int i : defUseChain.keySet()) {
                System.out.println(i);
                System.out.println(defUseChain.get(i));
            }
            System.out.println();
        }
    }
}
