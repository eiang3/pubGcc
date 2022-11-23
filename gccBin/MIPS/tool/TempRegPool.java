package gccBin.MIPS.tool;

import gccBin.MIPS.MIPS;
import gccBin.MidCode.Judge;
import gccBin.MidCode.original.IRTagManage;
import gccBin.UnExpect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TempRegPool {
    private static TempRegPool tempRegPool;

    private final HashMap<String, Reg> temp2Reg;

    /**
     * temp名以及它对应的在内存里的偏移
     */
    private final HashMap<String, Integer> temp2off;

    private TempRegPool() {
        temp2Reg = new HashMap<>();
        temp2off = new HashMap<>();
    }

    public static TempRegPool getInstance() {
        if (tempRegPool == null) {
            tempRegPool = new TempRegPool();
        }
        return tempRegPool;
    }

    /**
     * 将一个变量存进常量池，
     * 如果有空闲的t-reg，则返回这个t-reg，
     * 否则返回null
     *
     * @param name 待分配的temp名
     * @return reg || null
     */
    public Reg addToPool(String name, Reg afloat) throws IOException {
        addToPool(name);
        if (inReg(name)) {
            return getReg(name);
        } else {
            return afloat;
        }
    }

    public void addToPool(String name) {
        if (temp2Reg.containsKey(name) || temp2off.containsKey(name)) {
            return;
        }

        if (hasRegToAllocate()) {
            HashSet<Reg> regs = new HashSet<>(temp2Reg.values());
            Reg reg = Reg.getFreeTempReg(regs);
            temp2Reg.put(name, reg);
        } else {
            int ret = MemManager.getInstance().allocation_A_Temp_Mem();
            this.temp2off.put(name, ret);
        }
    }

    /**
     * 当一个temp在ir中被使用后，这个temp不再使用，需要被销毁
     *
     * @param name temp name
     */
    public void delete(String ans, String name) {
        if (ans.equals(name)) return;
        if (Judge.isTemp(name)) {
            if (IRTagManage.getInstance().delete(name)) {
                this.temp2off.remove(name);
                temp2Reg.remove(name);
            }
        }
    }

    public void delete(String name) {
        if (Judge.isTemp(name)) {
            if (IRTagManage.getInstance().delete(name)) {
                this.temp2off.remove(name);
                temp2Reg.remove(name);
            }
        }
    }

    /**
     * reg已经被定义过，这一次是被ir使用(也是最后一次使用)
     * <p>
     * 为了性能考虑，暂时不会删除
     *
     * @param afloat 如果是temp是，存在内存中，则加载至此寄存器
     * @param temp   temp name
     * @return afloat || t-reg
     * @throws IOException *
     */
    public Reg getTempInReg(Reg afloat, String temp) throws IOException {
        if (temp2Reg.containsKey(temp)) {
            return temp2Reg.get(temp);
        } else if (temp2off.containsKey(temp)) {
            MipsIns.lw_ans_num_baseReg(afloat, temp2off.get(temp), Reg.$fp);
            return afloat;
        } else return null;
    }

    /**
     * 将operand的值存进在men的temp
     *
     * @param operand *
     * @param temp    *
     * @throws IOException *
     */
    public void storeToMem(Reg operand, String temp) throws IOException {
        if (!temp2off.containsKey(temp)) {
            UnExpect.printf("temp not in mem");
            return;
        }
        int off = temp2off.get(temp);
        MipsIns.sw_value_num_baseReg(operand, off, Reg.$fp);
    }

    /**
     * 将temp从mem中取出来，并从常量池中删除
     *
     * @param reg  目标寄存器
     * @param temp temp name
     * @throws IOException e
     */
    public void moveFromMem(Reg reg, String temp) throws IOException {
        if (!temp2off.containsKey(temp)) {
            UnExpect.printf("temp not in mem");
            return;
        }
        int off = temp2off.get(temp);
        MipsIns.lw_ans_num_baseReg(reg, off, Reg.$fp);
    }

    /**
     * 判断temp是否在t-reg里
     *
     * @param temp temp name
     * @return *
     */
    public boolean inReg(String temp) {
        return this.temp2Reg.containsKey(temp);
    }

    public Reg getReg(String temp) {
        if (inReg(temp)) {
            return temp2Reg.get(temp);
        }
        UnExpect.unexpect(temp + " not in reg");
        return null;
    }

    /**
     * 判断temp是否在mem里
     *
     * @param temp temp name
     * @return *
     */
    public boolean inMem(String temp) {
        return this.temp2off.containsKey(temp);
    }

    /**
     * 必须和inMem一起使用
     *
     * @param temp *
     * @return *
     */
    public int getOff(String temp) {
        if (inMem(temp)) {
            return temp2off.get(temp);
        }
        UnExpect.unexpect(temp + "is a not in mem");
        return -1;
    }

    public void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

    public boolean hasRegToAllocate() {
        return temp2Reg.size() < Reg.tempNum;
    }

    public ArrayList<Reg> getTempRegInUse() {
        return new ArrayList<>(temp2Reg.values());
    }

    /**
     * 为一个新的变量分配内存地址，并且将旧的变量复制一份过去
     * afloat-use : Reg.r1
     *
     * @param temp new
     * @param old  old
     */
    public void justCopy(String temp, String old) throws IOException {
        Reg tempReg = addToPool(temp, Reg.r1);
        if (temp2Reg.containsKey(old)) {
            Reg reg = temp2Reg.get(old);
            if (TempRegPool.tempRegPool.inReg(temp)) {
                MipsIns.move_reg_reg(tempReg, reg);
            } else {
                storeToMem(reg, temp);
            }
        } else if (temp2off.containsKey(old)) {
            int off = temp2off.get(old);
            if (TempRegPool.tempRegPool.inReg(temp)) {
                MipsIns.lw_ans_num_baseReg(tempReg, off, Reg.$fp);
            } else {
                MipsIns.lw_ans_num_baseReg(tempReg, off, Reg.$fp);
                storeToMem(tempReg, temp);
            }
        }
    }
}
