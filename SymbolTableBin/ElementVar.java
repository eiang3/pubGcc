package SymbolTableBin;

public class ElementVar extends ElementTable{
    private int subScript;  //如果有重命名的变量，需要添加下标保证它们的名字不一样。

    public ElementVar(String name, TypeTable type){
        super(name,type,TypeTable.VAR,0);
        this.subScript = -1;
    }

    public void setSubScript(int subScript) {
        this.subScript = subScript;
    }

    @Override
    public String getMidCodeName(){
        if(this.subScript == -1) {
            return super.getName();
        } else {
            return super.getName() + "$" + subScript;
        }
    }
}
