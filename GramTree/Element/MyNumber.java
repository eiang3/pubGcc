package GramTree.Element;

import GramTree.*;
import gccBin.Lex.Symbol;
import gccBin.MidCode.MidCode;

import java.io.FileWriter;
import java.io.IOException;

/*
数值  Number → IntConst
 */
public class MyNumber extends TreeFatherNode {
    private Word intConst;

    private int value;

    public MyNumber() {
        super();
        super.setLabel(Label.Number);
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word &&
                ((Word) treeElement).getSym() == Symbol.INTCON) {
            this.intConst = (Word) treeElement;
            this.value = Integer.parseInt(intConst.getToken());
        }
    }

    public int getValue() {
        return value;
    }

    public String getMidCode() throws IOException {
        return intConst.getToken();
    }
}