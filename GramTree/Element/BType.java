package GramTree.Element;

import GramTree.Label;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;
import GramTree.Word;
import SymbolTableBin.TypeTable;
import gccBin.Lex.Symbol;
/*
BType → 'int'
 */
public class BType extends TreeFatherNode {
    private TypeTable type;

    public BType(){
        super();
        super.setLabel(Label.BType);
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof Word){
            Word word =(Word) treeElement;
            if(word.getSym() == Symbol.INTTK){
                this.type = TypeTable.INT;
            } else {
                this.type = TypeTable.UNDEFINED;
            }
        }
    }

    public TypeTable getType() {
        return type;
    }
}