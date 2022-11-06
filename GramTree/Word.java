package GramTree;

import gccBin.Lex.Symbol;

import java.io.FileWriter;
import java.io.IOException;
/*
词法分析的基础元素，语法树的叶节点 ok
 */
public class Word extends TreeElement{
    private final Symbol sym;
    private final String token;
    private final int row;
    private final int falseRow;

    public Word(Symbol sym,String token,int row,int falseRow){
        this.sym = sym;
        this.token = token;
        this.row = row;
        this.falseRow = falseRow;
    }

    public Symbol getSym(){
        return this.sym;
    }

    public String getToken(){
        return token;
    }

    public int getRow() {
        return row;
    }

    public void travel(FileWriter fileWriter) throws IOException {
        String str = this.sym + " " + this.token;
        fileWriter.write(str + '\n');
        System.out.println(str);
    }

    @Override
    public String toString(){
        return token;
    }

    public int getFalseRow() {
        return falseRow;
    }
}
