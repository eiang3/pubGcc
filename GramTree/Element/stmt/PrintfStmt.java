package GramTree.Element.stmt;

import GramTree.Element.Exp;
import GramTree.Param;
import GramTree.TreeElement;
import GramTree.Word;
import gccBin.Lex.Symbol;
import gccBin.MidCode.original.IRGenerate;
import gccBin.MidCode.original.PrintfFormatStringStore;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
'printf''('FormatString{,Exp}')'';'
 */
public class PrintfStmt extends Stmt {
    private Word printf;
    private Word formatString;
    private final ArrayList<Exp> exps; //倒着的

    public PrintfStmt() {
        super();
        this.exps = new ArrayList<>();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        IRGenerate.getInstance().annotate(this.toString());
        for (Exp exp : exps) {
            exp.midCodeGen(fileWriter, param);
        }
        String s = this.formatString.getToken();

        ArrayList<String> strs = PrintfFormatStringStore
                .getInstance().mySplit(s.substring(1, s.length() - 1));
        int index = 0;
        for (String str : strs) {
            if (str.equals("%d")) {
                PrintfFormatStringStore.getInstance().
                        midCodePrintfExp(fileWriter, exps.get(index++).getMidCode());
            } else {
                PrintfFormatStringStore
                        .getInstance().midCodePrintfStr(fileWriter, str);
            }
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Word) {
            Word word = (Word) treeElement;
            if (word.getSym() == Symbol.PRINTFTK) {
                this.printf = word;
            } else if (word.getSym() == Symbol.STRCON) {
                this.formatString = word;
                PrintfFormatStringStore.getInstance().parse(word.getToken());
            }
        } else if (treeElement instanceof Exp) {
            this.exps.add((Exp) treeElement);
        }
    }

    public Word getPrintf() {
        return printf;
    }

    public Word getFormatString() {
        return formatString;
    }

    public int getExpSize() {
        return exps.size();
    }
}
