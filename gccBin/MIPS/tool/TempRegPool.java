package gccBin.MIPS.tool;

import gccBin.MIPS.MIPS;

import java.io.IOException;
import java.util.HashMap;

public class TempRegPool {
    private static TempRegPool tempRegPool;

    private final HashMap<String, Reg> temp2Reg;
    private final HashMap<Reg, String> reg2Temp;

    //存在内存中的临时变量其名字和相对帧指针的偏移关系。
    private final HashMap<String, Integer> temp2off;

    private TempRegPool() {
        temp2Reg = new HashMap<>();
        reg2Temp = new HashMap<>();
        temp2off = new HashMap<>();
    }

    public static TempRegPool getInstance(){
        if(tempRegPool == null){
            tempRegPool = new TempRegPool();
        }
        return tempRegPool;
    }

    public boolean addToPool(String name) {
        if (reg2Temp.size() < Reg.tempNum) {
            Reg reg = Reg.getATempReg(reg2Temp);
            if (reg == null) {
                return false;
            }
            temp2Reg.put(name, reg);
            reg2Temp.put(reg, name);
            return true;
        } else {
            int ret = MemManager.getInstance().allocationTempMem();
            this.temp2off.put(name, ret);
            return false;
        }
    }

    private void delete(String name){
        this.temp2off.remove(name);
        Reg reg = temp2Reg.get(name);
        temp2Reg.remove(name);
        reg2Temp.remove(reg);
    }

    /**
     * reg已经被定义过，这一次是被ir使用
     *
     * 将临时变量temp存在reg里，然后返回。
     *
     * 如果temp本身就存在在一个reg里，就返回这个reg，同时释放这个reg
     * //一个临时变量只能用两次（定义+使用，取值是使用）
     * 否则，根据temp是第几个操作数，从内存中取出相应的reg
     * op1_>reg8  , op2 _>reg9
     */
    public Reg getTempInReg(Reg ans,String s) throws IOException {
        Reg ret = null;
        if(temp2Reg.containsKey(s)){
            ret = temp2Reg.get(s);
            delete(s);
            return ret;
        } else {
            MIPSIns.lw_number_reg(ans,temp2off.get(s),Reg.$fp);
            delete(s);
            return ans;
        }
        // 这里会造成fp空间的浪费 ？
    }

    /**
     * 前提：确定reg已经在reg里
     * @param s
     * @return
     * @throws IOException
     */
    public Reg findTempReg(String s) throws IOException {
        return temp2Reg.get(s);
    }

    /**
     * 这个函数不供中间代码使用，只是生成mips汇编时的额外操作。
     *
     * pre：确定已经在内存中
     * @param name
     */
    public void storeToMem(Reg reg,String name) throws IOException {
        int off = temp2off.get(name);
        MIPSIns.sw_number_reg(reg,off,Reg.$fp);
    }


    public void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

    public void writeNotNext(String s) throws IOException {
        MIPS.getInstance().writeNotNext(s);
    }

    public boolean inReg(String s){
        return temp2Reg.containsKey(s);
    }

    public boolean inMem(String s){
        return temp2off.containsKey(s);
    }
}
