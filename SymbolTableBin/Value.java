package SymbolTableBin;

/*
用于常量回填
 */

public class Value {
    private int num;

    public Value() {
        this.num = -1;
    }

    public Value(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
