package GramTree.Element;

import GramTree.Label;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;
import SymbolTableBin.TypeTable;

/*
变量声明    VarDecl → BType VarDef { ',' VarDef } ';'
 */

public class VarDecl extends TreeFatherNode {
    private TypeTable type;
    public VarDecl(){
        super();
        super.setLabel(Label.VarDecl);
    }

    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof BType){
            this.type = ((BType)treeElement).getType();
        }
    }

    public TypeTable getType() {
        return type;
    }
}