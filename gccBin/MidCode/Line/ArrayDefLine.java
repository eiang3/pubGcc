package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * arr int a[4]
 * 先未分析类型
 */

public class ArrayDefLine extends Line{
    private String name;
    private int len;

    public ArrayDefLine(String s, int line,TableSymbol tableSymbol,String[] elements){
        super(s,line,tableSymbol);
        parse(elements[2]);
    }

    private void parse(String arr){
        int l = arr.indexOf('[');
        int r = arr.indexOf(']');

        this.name = arr.substring(0,l);
        this.len = Integer.parseInt(arr.substring(l+1,r));
    }

    public String getName() {
        return name;
    }

    public int getLen() {
        return len;
    }
}
