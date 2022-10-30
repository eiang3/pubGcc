package GramTree.Element;

import GramTree.Label;
import GramTree.TreeElement;
import GramTree.TreeFatherNode;
import GramTree.Word;
import SymbolTableBin.TypeTable;
import gccBin.Lex.Symbol;
/*
函数类型    FuncType → 'void' | 'int'
 */
public class FuncType extends TreeFatherNode {
    private TypeTable type;

    public FuncType() {
        super();
        super.setLabel(Label.FuncType);
    }

    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word) {
            Word word = (Word) treeElement;
            this.type = (word.getSym() == Symbol.INTTK) ? TypeTable.INT :
                    (word.getSym() == Symbol.VOIDTK) ? TypeTable.VOID :
                            TypeTable.UNDEFINED;
        }
    }

    public TypeTable getType() {
        return type;
    }
}