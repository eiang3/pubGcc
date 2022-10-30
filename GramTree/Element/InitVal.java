package GramTree.Element;

import GramTree.Label;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
变量初值    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'

0维 Exp
1维 { , , , }
2维 {{,},{,},{,}}
 */

public class InitVal extends TreeFatherNode {
    private final ArrayList<Exp> exps ;

    public InitVal() {
        super();
        super.setLabel(Label.InitVal);
        this.exps = new ArrayList<>();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        super.ergodicMidCode(fileWriter,param);
        for (TreeElement treeElement : super.getChildren()) {
            if (treeElement instanceof Exp) {
                this.exps.add((Exp) treeElement);
            } else if(treeElement instanceof InitVal){
                this.exps.addAll(((InitVal)treeElement).getExps());
            }
        }
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }
}