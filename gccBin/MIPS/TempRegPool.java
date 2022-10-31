package gccBin.MIPS;

import java.util.HashMap;
import java.util.HashSet;

public class TempRegPool {
    private static TempRegPool tempRegPool;

    private final HashMap<String,Reg> temp2Reg;
    private final HashMap<Reg,String> reg2Temp;

    private TempRegPool(){
        temp2Reg = new HashMap<>();
        reg2Temp = new HashMap<>();
    }

    public boolean addToPool(String name){
        Reg reg = Reg.getATempReg(reg2Temp);
        if(reg == null) return false;
        addTuple(name,reg);
        return true;
    }

    private void addTuple(String name,Reg reg){
        temp2Reg.put(name,reg);
        reg2Temp.put(reg,name);
    }
}
