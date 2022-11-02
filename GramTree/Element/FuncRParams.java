package GramTree.Element;

import GramTree.*;
import GramTree.Element.UnaryExp.FuncUnaryExp;
import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementFunc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
函数实参表   FuncRParams → Exp { ',' Exp }
 */
public class FuncRParams extends TreeFatherNode {
    private final ArrayList<Exp> exps;

    //midCode
    private String funcName;

    public FuncRParams() {
        super();
        super.setLabel(Label.FuncRParams);
        this.exps = new ArrayList<>();
    }

    @Override
    public void midCodeGen(FileWriter fileWriter, Param param) throws IOException {
        TreeFatherNode treeFatherNode = super.getFather();
        if (treeFatherNode instanceof FuncUnaryExp) {
            this.funcName = ((FuncUnaryExp) treeFatherNode).getFuncName();
        }
        ElementFunc elementFunc = APIIRSymTable.getInstance().getFuncElement(funcName);
        ArrayList<FuncFParam> fParams = elementFunc.getParams();
        int index = 0;
        for (Exp exp : exps) {
            Param p = new Param(param);
            p.setFuncRParams(InheritProp.FuncRParam);
            p.setFParamDim(fParams.get(index++).getDimension());
            exp.midCodeGen(fileWriter, p);
        }
    }

    @Override
    public void addChildOperate(TreeElement treeElement) {
        if (treeElement instanceof Exp) {
            this.exps.add((Exp) treeElement);
        }
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }
}