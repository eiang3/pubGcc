package GramTree.Element;

import GramTree.*;

import java.io.FileWriter;
import java.io.IOException;
/*
常量表达式   ConstExp → AddExp 注：使用的Ident 必须是常量
 */
public class ConstExp extends TreeFatherNode {
    //for midCodeAttribute
    private int value; ////ConstExp

    private AddExp addExp;


    public ConstExp(){
        super();
        super.setLabel(Label.ConstExp);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        Param p = new Param(param);
        p.setExpKind(InheritProp.ConstExp);
        addExp.midCodeGen(fileWriter,p);
        this.value = addExp.getValue();
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof  AddExp){
            this.addExp = (AddExp) treeElement;
        }
    }

    public int getValue() {
        return value;
    }
}