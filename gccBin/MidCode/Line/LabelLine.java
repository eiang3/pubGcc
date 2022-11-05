package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * label:
 */
public class LabelLine extends Line {
    private final String label;

    public LabelLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        this.label = ele[0].substring(0, ele[0].length() - 1); //去掉结尾的:
    }

    public String getLabel() {
        return label;
    }
}
