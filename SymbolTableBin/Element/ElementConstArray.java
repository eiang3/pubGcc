package SymbolTableBin.Element;

import SymbolTableBin.TypeTable;

import java.util.ArrayList;

public class ElementConstArray extends ElementTable {
    private final ArrayList<ArrayList<Integer>> array;

    public ElementConstArray(String name, TypeTable type,
                             TypeTable decl, int dimension,
                              ArrayList<ArrayList<Integer>> a,int falseRow) {
        super(name, type, decl, dimension,falseRow);
        this.array = a;
    }

    /**
     *得到二维数组的第二维长度
     */
    public int getLen(){
        return array.get(0).size();
    }

    public int getConstArrValue(int i) {
        return array.get(0).get(i);
    }

    public int getConstArrValue(int i, int j) {
        return array.get(i).get(j);
    }

}
