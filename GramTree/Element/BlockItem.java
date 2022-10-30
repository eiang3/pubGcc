package GramTree.Element;

import GramTree.Element.stmt.Stmt;
import GramTree.Label;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;
/*
语句块项    BlockItem → Decl | Stmt
 */
public class BlockItem extends TreeFatherNode {
    private Stmt stmt;

    public BlockItem(){
        super();
        super.setLabel(Label.BlockItem);
    }

    @Override
    public void addChildOperate(TreeElement treeElement){
        if(treeElement instanceof Stmt){
            this.stmt = (Stmt) treeElement;
        }
    }

    public Stmt getStmt() {
        return stmt;
    }
}