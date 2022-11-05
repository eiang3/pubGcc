package gccBin.MIPS.tool;

import java.util.ArrayList;
import java.util.HashSet;

public enum Reg {
    $ra,
    $fp,
    $sp,
    $zero,
    $a0,
    $a1,
    $a2,
    $a3,
    $v0,
    $v1,
    $s0,
    $s1,
    $s2,
    $s3,
    $s4,
    $s5,
    $s6,
    $s7,
    $t0,
    $t1,
    $t2,
    $t3,
    $t4,
    $t5,
    $t6,
    $t7,
    $t8,
    $t9;

    public static final int tempNum = 6;
    public static final int storeNum = 8;

    public static final Reg l1 = $t6;
    public static final Reg l2 = $t7;
    public static final Reg r1 = $t8;
    public static final Reg r2 = $t9;


    public static Reg getAStoreReg(ArrayList<Reg> arr) {
        if (!arr.contains($s0)) {
            return $s0;
        } else if (!arr.contains($s1)) {
            return $s1;
        } else if (!arr.contains($s2)) {
            return $s2;
        } else if (!arr.contains($s3)) {
            return $s3;
        } else if (!arr.contains($s4)) {
            return $s4;
        } else if (!arr.contains($s5)) {
            return $s5;
        } else if (!arr.contains($s6)) {
            return $s6;
        } else if (!arr.contains($s7)) {
            return $s7;
        }
        return null;
    }

    public static Reg getFreeTempReg(HashSet<Reg> arr) {
        if (!arr.contains($t0)) {
            return $t0;
        } else if (!arr.contains($t1)) {
            return $t1;
        } else if (!arr.contains($t2)) {
            return $t2;
        } else if (!arr.contains($t3)) {
            return $t3;
        } else if (!arr.contains($t4)) {
            return $t4;
        } else if (!arr.contains($t5)) {
            return $t5;
        } //t6，t7，t8，t9给我倒腾使用
        return null;
    }

    public static Reg getFParamReg(int index) {
        if (index == 1) {
            return $a0;
        } else if (index == 2) {
            return $a1;
        } else if (index == 3) {
            return $a2;
        } else if (index == 4) {
            return $a3;
        }
        return null;
    }
}
