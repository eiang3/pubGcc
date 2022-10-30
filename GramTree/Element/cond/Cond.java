package GramTree.Element.cond;

import GramTree.*;

import java.io.FileWriter;
import java.io.IOException;

/*
 Cond → LOrExp
 */
public class Cond extends TreeFatherNode {
    private LOrExp lOrExp;

    public Cond(){
        super();
        super.setLabel(Label.Cond);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        Param p = new Param(param);
        p.setExpKind(InheritProp.CondExp);
        for(TreeElement treeElement : super.getChildren()){
            treeElement.midCodeGen(fileWriter,p);
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof LOrExp){
            this.lOrExp = (LOrExp) treeElement;
        }
    }

}