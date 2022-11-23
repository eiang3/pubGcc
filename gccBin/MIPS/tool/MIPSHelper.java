package gccBin.MIPS.tool;

import SymbolTableBin.*;
import SymbolTableBin.Element.ElementTable;
import gccBin.MIPS.MIPS;
import gccBin.MIPS.SubOp;
import gccBin.MidCode.Judge;
import gccBin.UnExpect;

import java.io.IOException;

public class MIPSHelper {

    /**
     * 并将应该写入mips的信息传出,将取出相应的数到特定的寄存器，但是对于数字的话，不需要存到
     * 寄存器里，因为数字可以直接计算
     * 如果值是保存在寄存器里的，就输出寄存器，如果值是保存在
     * 内存里的，就先把值取到临时寄存器
     */
    public static Reg getValueInReg(Reg ans, String s) throws IOException {
        if (Judge.isNumber(s)) {
            MipsIns.li_ans_num(ans, Integer.parseInt(s));
            return ans;
        } else if (Judge.isTemp(s)) {
            return TempRegPool.getInstance().getTempInReg(ans, s);
        }
        UnExpect.unexpect("not a exp getValueInReg");
        return null;
    }

    // next is static handle for mips generate

    /**
     * t1 = a[exp]
     *
     * @param answer      *
     * @param array       *
     * @param tableSymbol *
     */
    public static void assignArrToTemp(String answer, String array, TableSymbol tableSymbol) throws IOException {
        String arrName = SubOp.getArrName(array);
        String arrSub = SubOp.getArrSubscript(array);
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, arrName);
        Reg obj = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        get_Array_Value_In_Reg(obj, array, elementTable); //
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(obj, answer); //
        }
        TempRegPool.getInstance().delete(answer, arrSub);
    }

    /**
     * load array[exp]_value in ans_reg
     * afloat-reg in use : r1,r2
     * <p>
     * for t = a[exp] use
     *
     * @param ansReg       *
     * @param array        *
     * @param elementTable *
     */
    public static void get_Array_Value_In_Reg(Reg ansReg, String array, ElementTable elementTable) throws IOException {
        String arrName = SubOp.getArrName(array);
        String arrSub = SubOp.getArrSubscript(array);
        Reg subReg = TempRegPool.getInstance().getTempInReg(Reg.r2, arrSub);

        if (elementTable.isGlobal()) {
            if (Judge.isNumber(arrSub)) {
                MipsIns.lw_ans_label_num(ansReg, arrName, Integer.parseInt(arrSub) * 4);
            } else {
                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
                MipsIns.lw_ans_label_base(ansReg, arrName, subReg);
            }
        } else if (Judge.isFParam(elementTable)) {
            Reg address = getFParamInReg(elementTable, Reg.r1);
            if (Judge.isNumber(arrSub)) {
                MipsIns.lw_ans_num_baseReg(ansReg, Integer.parseInt(arrSub) * 4, address);
            } else {
                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
                MipsIns.add_ans_reg_regOrNum(subReg, subReg, address); //地址
                MipsIns.lw_ans_num_baseReg(ansReg, 0, subReg);
            }
        } else if (Judge.isLocal(elementTable)) {
            int arrOff = elementTable.getMemOff();
            if (Judge.isNumber(arrSub)) {
                MipsIns.lw_ans_num_baseReg(ansReg, Integer.parseInt(arrSub) * 4 + arrOff, Reg.$fp);
            } else {
                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
                MipsIns.add_ans_reg_regOrNum(subReg, subReg, Reg.$fp); //地址
                MipsIns.lw_ans_num_baseReg(ansReg, arrOff, subReg);
            }
        } else UnExpect.unexpect("assignArrToTemp 1");
    }

    /**
     * if(FParam in ax) return ax
     * else get FParam_Value in afloat ,return afloat
     *
     * @param elementTable *
     * @param afloat       *
     * @return *
     * @throws IOException *
     */
    public static Reg getFParamInReg(ElementTable elementTable, Reg afloat) throws IOException {
        if (!Judge.isFParam(elementTable)) {
            UnExpect.unexpect("getFParamInReg: not a FParam");
            return null;
        }
        Reg ret;
        if (elementTable.isHasReg()) {
            ret = elementTable.getReg();
        } else {
            int off = elementTable.getMemOff();
            MipsIns.lw_ans_num_baseReg(afloat, off, Reg.$fp);
            ret = afloat;
        }
        return ret;
    }

    /**
     * a[exp] = exp
     *
     * @param answer      *
     * @param exp         *
     * @param tableSymbol *
     * @throws IOException *
     */
    public static void assignExpToArr(String answer, String exp, TableSymbol tableSymbol) throws IOException {

        String arrName = SubOp.getArrName(answer);
        String arrSub = SubOp.getArrSubscript(answer);
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, arrName);
        //get value in reg.r1
        Reg value = null;
        if (Judge.isTemp(exp)) {
            value = TempRegPool.getInstance().getTempInReg(Reg.r1, exp);
        } else if (Judge.isNumber(exp)) {
            value = Reg.r1;
            MipsIns.li_ans_num(Reg.r1, Integer.parseInt(exp));
        } else UnExpect.unexpect("assignExpToArr 1");

        //getAddress
        if (elementTable.isGlobal()) {
            if (Judge.isNumber(arrSub)) {
                MipsIns.sw_value_label_num(value, arrName, Integer.parseInt(arrSub) * 4);
            } else if (Judge.isTemp(arrSub)) {
                Reg arrOff = TempRegPool.getInstance().getTempInReg(Reg.l1, arrSub);
                MipsIns.sll_ans_regx_num(arrOff, arrOff, 2);//
                MipsIns.sw_value_label_base(value, arrName, arrOff);
            } else UnExpect.unexpect("assignExpToArr 2");
        } else if (Judge.isLocal(elementTable)) {
            int ansOff = elementTable.getMemOff();
            if (Judge.isNumber(arrSub)) {
                ansOff = ansOff + Integer.parseInt(arrSub) * 4;
                MipsIns.sw_value_num_baseReg(value, ansOff, Reg.$fp);
            } else if (Judge.isTemp(arrSub)) {
                Reg subReg = TempRegPool.getInstance().getTempInReg(Reg.l1, arrSub);
                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
                MipsIns.add_ans_reg_regOrNum(subReg, subReg, Reg.$fp);
                MipsIns.sw_value_num_baseReg(value, ansOff, subReg);
            } else UnExpect.unexpect("assignExpToArr 3");
        } else if (Judge.isFParam(elementTable)) {
            Reg address = getFParamInReg(elementTable, Reg.l1);
            if (Judge.isNumber(arrSub)) {
                MipsIns.sw_value_num_baseReg(value, Integer.parseInt(arrSub) * 4, address);
            } else if (Judge.isTemp(arrSub)) {
                Reg subReg = TempRegPool.getInstance().getTempInReg(Reg.l2, arrSub);
                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
                MipsIns.add_ans_reg_regOrNum(subReg, subReg, address);
                MipsIns.sw_value_base(value, subReg);
            } else UnExpect.unexpect("assignExpToArr 5");
        } else UnExpect.unexpect("assignExpToArr 4");

        TempRegPool.getInstance().delete(answer, arrSub);
        TempRegPool.getInstance().delete(answer, exp);
    }

    /**
     * a = exp
     *
     * @param answer      *
     * @param exp         *
     * @param tableSymbol *
     */
    public static void assignExpToVar(String answer, String exp, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, answer);
        if (elementTable.isHasReg()) {
            Reg ans = elementTable.getReg();
            if (TempRegPool.getInstance().inReg(exp)) {
                Reg t = TempRegPool.getInstance().getReg(exp);
                MipsIns.move_reg_reg(ans, t);
            } else if (TempRegPool.getInstance().inMem(exp)) {
                int off = TempRegPool.getInstance().getOff(exp);
                MipsIns.lw_ans_num_baseReg(ans, off, Reg.$fp);
            } else if (Judge.isNumber(exp)) {
                MipsIns.li_ans_num(ans, Integer.parseInt(exp));
            } else UnExpect.unexpect("assignExpToVar 1");
        } else {
            if (elementTable.isGlobal()) {
                if (Judge.isTemp(exp)) {
                    Reg value = TempRegPool.getInstance().getTempInReg(Reg.r1, exp);
                    MipsIns.sw_value_label(value, answer);
                } else if (Judge.isNumber(exp)) {
                    MipsIns.li_ans_num(Reg.r1, Integer.parseInt(exp));
                    MipsIns.sw_value_label(Reg.r1, answer);
                } else UnExpect.unexpect("assignExpToVar 2");
            } else {
                int ansOff = elementTable.getMemOff();
                if (Judge.isTemp(exp)) {
                    Reg value = TempRegPool.getInstance().getTempInReg(Reg.r1, exp);
                    MipsIns.sw_value_num_baseReg(value, ansOff, Reg.$fp);
                } else if (Judge.isNumber(exp)) {
                    MipsIns.li_ans_num(Reg.r1, Integer.parseInt(exp));
                    MipsIns.sw_value_num_baseReg(Reg.r1, ansOff, Reg.$fp);
                } else UnExpect.unexpect("assignExpToVar 1");
            }
        }
        TempRegPool.getInstance().delete(answer, exp);
    }

    /**
     * t = a
     * <p>
     * 不存在a是数组地址的情况
     * ir中，这种情况都为 t = array_address >> 2
     *
     * @param temp        *
     * @param var         *
     * @param tableSymbol *
     */
    public static void assignVarToTemp(String temp, String var, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, var);
        TempRegPool.getInstance().addToPool(temp);
        if (elementTable.isHasReg()) {
            Reg varReg = elementTable.getReg();
            if (TempRegPool.getInstance().inReg(temp)) {
                Reg reg = TempRegPool.getInstance().getReg(temp);
                MipsIns.move_reg_reg(reg, varReg);
            } else if (TempRegPool.getInstance().inMem(temp)) {
                TempRegPool.getInstance().storeToMem(varReg, temp);
            } else UnExpect.unexpect("assignVarToTemp 1");
        } else {
            if (elementTable.isGlobal()) {
                if (TempRegPool.getInstance().inReg(temp)) {
                    Reg reg = TempRegPool.getInstance().getReg(temp);
                    MipsIns.lw_ans_label(reg, var);
                } else if (TempRegPool.getInstance().inMem(temp)) {
                    MipsIns.lw_ans_label(Reg.r1, var);
                    TempRegPool.getInstance().storeToMem(Reg.r1, temp);
                } else UnExpect.unexpect("assignVarToTemp 1");
            } else {
                int varOff = elementTable.getMemOff();
                if (TempRegPool.getInstance().inReg(temp)) {
                    Reg reg = TempRegPool.getInstance().getReg(temp);
                    MipsIns.lw_ans_num_baseReg(reg, varOff, Reg.$fp);
                } else if (TempRegPool.getInstance().inMem(temp)) {
                    MipsIns.lw_ans_num_baseReg(Reg.r1, varOff, Reg.$fp);
                    TempRegPool.getInstance().storeToMem(Reg.r1, temp);
                } else UnExpect.unexpect("assignVarToTemp 1");
            }
        }
    }

    /**
     * t = num
     *
     * @param ans temp
     * @param num num
     */
    public static void assignNumberToTemp(String ans, int num) throws IOException {
        Reg ansReg = TempRegPool.getInstance().addToPool(ans, Reg.l1);
        MipsIns.li_ans_num(ansReg, num);
        if (TempRegPool.getInstance().inMem(ans)) {
            TempRegPool.getInstance().storeToMem(ansReg, ans);
        }
    }
    //*********************************************************************************/

    /**
     * ok
     * temp1 = (-|!) temp2
     *
     * @param answer temp1
     * @param op     -|!
     * @param temp   temp2
     * @throws IOException e
     */
    public static void assignOne(String answer, String op, String temp) throws IOException {
        Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        Reg reg = TempRegPool.getInstance().getTempInReg(Reg.r1, temp);
        MipsIns.negOrNot_ans_reg(ansReg, op, reg);
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(ansReg, answer);
        }
        TempRegPool.getInstance().delete(answer, temp);
    }

    /**
     * t1 = (-|!) number
     *
     * @param answer t1
     * @param op     -|!
     * @param number number
     * @throws IOException e
     */
    public static void assignOne(String answer, String op, int number) throws IOException {
        Reg ans = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        if (op.equals("-")) {
            MipsIns.li_ans_num(ans, number * -1);
        } else if (op.equals("!")) {
            number = (number == 0) ? 1 : 0;
            MipsIns.li_ans_num(ans, number);
        } else UnExpect.printf(op + " is not ! or -");

        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(ans, answer);
        }
    }

    /**
     * ans = address >> 2
     * ans = t1 << 2
     * ans = exp1 +-/*% exp2
     *
     * @param answer      ans
     * @param temp1       temp1
     * @param op          op
     * @param temp2       temp2
     * @param tableSymbol table
     * @throws IOException e
     */
    public static void assignTwo(String answer, String temp1, String op, String temp2, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, temp1);
        if (Judge.isAddress(elementTable, temp1)) { //assert ans = address >> 2
            Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
            if (elementTable.isGlobal()) { //全局数组传参，是个人才
                MipsIns.la_ans_label(ansReg, temp1);
                MipsIns.srl_ans_regx_numReg(ansReg, ansReg, 2);
            } else if (Judge.isFParam(elementTable)) {
                Reg fReg = getFParamInReg(elementTable, Reg.r1);
                MipsIns.srl_ans_regx_numReg(ansReg, fReg, 2);
            } else {
                int off = elementTable.getMemOff();
                MipsIns.address_srl_2(ansReg, off);
            }
            if (TempRegPool.getInstance().inMem(answer)) {
                TempRegPool.getInstance().storeToMem(ansReg, answer);
            }
        } else {
            int num1 = (Judge.isNumber(temp1)) ? Integer.parseInt(temp1) : 0;
            int num2 = (Judge.isNumber(temp2)) ? Integer.parseInt(temp2) : 0;
            if (Judge.isNumber(temp1) && Judge.isNumber(temp2)) {
                assignTwo_TwoNumber(answer, num1, op, num2);
            } else if (Judge.isTemp(temp1) && Judge.isNumber(temp2)) {
                assignTwo_Temp_Number(answer, temp1, op, num2);
            } else if (Judge.isNumber(temp1) && Judge.isTemp(temp2)) {
                assignTwo_Number_Temp(answer, num1, op, temp2);
            } else if (Judge.isTemp(temp1) && Judge.isTemp(temp2)) {
                assignTwo_TwoTemp(answer, temp1, op, temp2);
            } else UnExpect.unexpect("assignTwo");
        }
    }

    public static void assignTwo_TwoTemp(String answer, String temp1, String op, String temp2) throws IOException {
        Reg t1 = TempRegPool.getInstance().getTempInReg(Reg.r1, temp1);
        Reg t2 = TempRegPool.getInstance().getTempInReg(Reg.r2, temp2);
        Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        MipsIns.compute_ans_r1_op_r2(ansReg, t1, op, t2);
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(ansReg, answer);
        }
        TempRegPool.getInstance().delete(answer, temp1);
        TempRegPool.getInstance().delete(answer, temp2);
    }

    public static void assignTwo_TwoNumber(String answer, int num1, String op, int num2) throws IOException {
        Reg ans = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        int result = SubOp.compute(num1, op, num2);
        MipsIns.li_ans_num(ans, result);
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(ans, answer);
        }
    }

    //ok
    public static void assignTwo_Temp_Number(String answer, String temp1, String op, int number) throws IOException {
        Reg t1 = TempRegPool.getInstance().getTempInReg(Reg.r1, temp1);
        Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        MipsIns.compute_ans_reg_op_num(ansReg, t1, op, number, Reg.r2);
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(ansReg, answer);
        }
        TempRegPool.getInstance().delete(answer, temp1);
    }

    public static void assignTwo_Number_Temp(String answer, int num1, String op, String temp2) throws IOException {
        Reg t2 = TempRegPool.getInstance().getTempInReg(Reg.r2, temp2);
        if (Judge.isPlus(op)) {
            assignTwo_Temp_Number(answer, temp2, op, num1);
        } else {
            MipsIns.li_ans_num(Reg.r1, num1);
            Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
            MipsIns.compute_ans_r1_op_r2(ansReg, Reg.r1, op, t2);
            if (TempRegPool.getInstance().inMem(answer)) {
                TempRegPool.getInstance().storeToMem(ansReg, answer);
            } else UnExpect.tempNotInMemAndReg(answer);
        }
        TempRegPool.getInstance().delete(answer, temp2);
    }

    public void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

}
