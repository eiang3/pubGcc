package gccBin.MidCode.firstProcess;

import gccBin.MIPS.tool.Reg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 */
public class RegAllocation {
    private static RegAllocation regAllocation;

    private final ArrayList<VarWeb> orderUse = new ArrayList<>();
    private final HashSet<VarWeb> orderSearch = new HashSet<>();

    private ArrayList<VarWeb> leave;

    private RegAllocation() {
    }

    public static RegAllocation getInstance() {
        if (regAllocation == null) {
            regAllocation = new RegAllocation();
        }
        return regAllocation;
    }

    /**
     * 变量名和对应的变量
     *
     * @param newName2Web *
     */
    public void addNodeToLeaveSet(HashMap<String, VarWeb> newName2Web) {
        this.leave = new ArrayList<>(newName2Web.values());
    }

    public void add(VarWeb varWeb) {
        this.orderUse.add(varWeb);
    }

    public void finishRegAllocation() {
        if (leave.size() == 0) return; //没有网

        while (leave.size() != 1) {
            VarWeb varWeb = getOneNode();
            if (varWeb != null) {
                add(varWeb);
                this.leave.remove(varWeb);
            } else {
                selectOneNodeInMem();
            }
        }

        VarWeb lastOne = leave.get(0);
        lastOne.setReg(Reg.$s0);
        for (int i = orderUse.size() - 1; i >= 0; i--) {
            VarWeb varWeb = orderUse.get(i);
            varWeb.allocReg(leave);
            this.leave.add(varWeb);
        }
    }

    /**
     * Step one : find a node link less than Reg.storeNum
     */
    public VarWeb getOneNode() {
        for (VarWeb varWeb : leave) {
            if (varWeb.getClashSub().size() < Reg.storeNum) {
                return varWeb;
            }
        }
        return null;
    }

    /**
     * 选一个web变量存在mem里
     */
    public void selectOneNodeInMem() {
        VarWeb varWeb = leave.get(0);
        leave.remove(varWeb);
        varWeb.removeFromGraph();
    }

}
