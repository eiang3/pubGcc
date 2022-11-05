package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * bne end_if
 * b label
 */
public class BLine extends Line {
    private String b;
    private String label;

    public BLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        parse(ele);
    }

    private void parse(String[] ele) {
        this.b = ele[0];
        this.label = ele[1];
    }

    public String getB() {
        return b;
    }

    public String getLabel() {
        return label;
    }
}
