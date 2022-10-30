package GramTree.Element;

import GramTree.Label;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;
import SymbolTableBin.TypeTable;
/*
ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
 */
public class ConstDecl extends TreeFatherNode {
    private TypeTable type;

    public ConstDecl(){
        super();
        super.setLabel(Label.ConstDecl);
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
