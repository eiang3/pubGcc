package GramTree.Element.cond;

import GramTree.*;
import gccBin.MidCode.AoriginalProcess.IRGenerate;
import gccBin.MidCode.AoriginalProcess.IRTagManage;

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
        String condEnd_label = IRTagManage.getInstance().newLabel();
        p.setCondEndLabel(condEnd_label);

        for(TreeElement treeElement : super.getChildren()){
            treeElement.midCodeGen(fileWriter,p);
        }

        IRGenerate.getInstance().localLabel(condEnd_label);
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof LOrExp){
            this.lOrExp = (LOrExp) treeElement;
        }
    }

}