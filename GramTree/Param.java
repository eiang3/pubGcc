package GramTree;

public class Param {
    private InheritProp expKind;// const | var | CondExp
    private InheritProp func;
    private InheritProp initial;
    private InheritProp funcRParams;

    private int dimension;
    
    private String whileOrIfEndLabel;
    private String whileEndLabel;
    private String condLabel;


    private int fParamDim; //形参的维数

    public Param(Param param){
        this.expKind = param.getExpKind();
        this.func = param.getFunc();
        this.initial = param.getInitial();
        this.funcRParams = param.getFuncRParams();
        this.dimension = param.getDimension();

        this.whileOrIfEndLabel = param.getWhileOrIfEndLabel();
        this.whileEndLabel = param.getWhileEndLabel();
        this.condLabel = param.getCondLabel();
        this.fParamDim = param.getFParamDim();
    }

    public Param() {
        this.expKind = InheritProp.NULL;

        this.func = InheritProp.NULL;
        this.initial = InheritProp.NULL;
        this.funcRParams = InheritProp.NULL;
    }
    public void setFunc(InheritProp func) {
        this.func = func;
    }

    public InheritProp getFunc() {
        return func;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public InheritProp getExpKind() {
        return expKind;
    }

    public void setExpKind(InheritProp expKind) {
        this.expKind = expKind;
    }

    public String getCondLabel() {
        return condLabel;
    }

    public void setCondLabel(String condLabel) {
        this.condLabel = condLabel;
    }

    public InheritProp getInitial() {
        return initial;
    }

    public void setInitial(InheritProp initial) {
        this.initial = initial;
    }

    public void setFuncRParams(InheritProp funcRParams) {
        this.funcRParams = funcRParams;
    }

    public InheritProp getFuncRParams() {
        return funcRParams;
    }

    public String getWhileEndLabel() {
        return whileEndLabel;
    }

    public void setWhileEndLabel(String whileEndLabel) {
        this.whileEndLabel = whileEndLabel;
    }

    public void setWhileOrIfEndLabel(String whileOrIfEndLabel) {
        this.whileOrIfEndLabel = whileOrIfEndLabel;
    }

    public String getWhileOrIfEndLabel() {
        return whileOrIfEndLabel;
    }

    public int getFParamDim() {
        return fParamDim;
    }

    public void setFParamDim(int fParamDim) {
        this.fParamDim = fParamDim;
    }
}
