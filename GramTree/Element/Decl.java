package GramTree.Element;

import GramTree.Label;
import GramTree.TreeFatherNode;

/*
Decl → ConstDecl | VarDecl
 */
public class Decl extends TreeFatherNode {
    public Decl(){
        super();
        super.setLabel(Label.Decl);
    }
}
