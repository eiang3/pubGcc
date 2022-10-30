package GramTree.Element;

import GramTree.*;
import gccBin.Lex.Symbol;

import java.io.FileWriter;

/*
单目运算符   UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中
 */

public class UnaryOp extends TreeFatherNode {
    public Word word;

    public UnaryOp(){
        super();
        super.setLabel(Label.UnaryOp);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param){
        this.word = (Word) super.getChildren().get(0);
    }
    @Override
    public void addChildOperate(TreeElement treeElement) {
        if(treeElement instanceof Word){
            this.word = (Word) treeElement;
        }
    }
    public Word getWord() {
        return word;
    }

    public Symbol getSymbol(){
        return word.getSym();
    }
}