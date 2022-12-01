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
     * if(var is "$RET") : return $v0
     * if(var has reg_x) : return reg_x
     * else              :
     * load in afloat
     * return afloat
     */
    public static Reg getValueInReg_t_v(Reg afloat, String var, TableSymbol tableSymbol) throws IOException {
        if (var.equals("$RET")) return Reg.$v0;
        if (Judge.isVar(var)) {
            if(var.equals("tmp$0")){
                int b = 1;
            }
            ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, var);
            if(elementTable == null){
                int a = 1;
            }
            if (elementTable.isHasReg()) {
                return elementTable.getReg();
            } else {
                if (elementTable.isGlobal()) {
                    MipsIns.lw_ans_label(afloat, var);
                } else {
                    int varOff = elementTable.getMemOff();
                    MipsIns.lw_ans_num_baseReg(afloat, varOff, Reg.$fp);
                }
                return afloat;
            }
        } else if (Judge.isTemp(var)) {
            return TempRegPool.getInstance().temp_in_Reg(afloat, var);
        }
        return null;
    }

    /**
     * if(s is num) : li num
     * else : call getValueInReg_t_v
     */
    public static Reg getValueInReg_t_v_n(Reg ans, String s, TableSymbol tableSymbol) throws IOException {
        if (s.equals("$RET")) return Reg.$v0;
        if (Judge.isNumber(s)) {
            MipsIns.li_ans_num(ans, Integer.parseInt(s));
            return ans;
        } else if (Judge.isVarOrTemp(s)) {
            return getValueInReg_t_v(ans, s, tableSymbol);
        }
        UnExpect.unexpect("not a exp getValueInReg");
        return null;
    }

    public static void getValueInSpecialReg_t_v_n(Reg ans, String s, TableSymbol tableSymbol) throws IOException {
        Reg reg = getValueInReg_t_v_n(ans, s, tableSymbol);
        if (!reg.equals(ans)) {
            MipsIns.move_reg_reg(ans, reg);
        }
    }

    public static void getValueInSpecialReg_t_v(Reg ans, String s, TableSymbol tableSymbol) throws IOException {
        Reg reg = getValueInReg_t_v(ans, s, tableSymbol);
        //assert reg != null;
        if (!reg.equals(ans)) {
            MipsIns.move_reg_reg(ans, reg);
        }
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
    public static void get_Array_Value_In_Reg(Reg ansReg, String array, ElementTable elementTable, TableSymbol tableSymbol) throws IOException {
        String arrName = SubOp.getArrName(array);
        String arrSub = SubOp.getArrSubscript(array);

        Reg subReg = getValueInReg_t_v(Reg.r2, arrSub, tableSymbol);
        if (elementTable.isGlobal()) {
            if (Judge.isNumber(arrSub)) {
                MipsIns.lw_ans_label_num(ansReg, arrName, Integer.parseInt(arrSub) * 4);
            } else {
                MipsIns.sll_ans_regx_num(ansReg, subReg, 2);//
                MipsIns.lw_ans_label_base(ansReg, arrName, ansReg);
            }
        } else if (Judge.isFParam(elementTable)) {
            Reg address = getFParamInReg(elementTable, Reg.r1);
            if (Judge.isNumber(arrSub)) {
                MipsIns.lw_ans_num_baseReg(ansReg, Integer.parseInt(arrSub) * 4, address);
            } else {
                MipsIns.sll_ans_regx_num(ansReg, subReg, 2);//
                MipsIns.add_ans_reg_regOrNum(ansReg, ansReg, address); //地址
                MipsIns.lw_ans_num_baseReg(ansReg, 0, ansReg);
            }
        } else if (Judge.isLocal(elementTable)) {
            int arrOff = elementTable.getMemOff();
            if (Judge.isNumber(arrSub)) {
                MipsIns.lw_ans_num_baseReg(ansReg, Integer.parseInt(arrSub) * 4 + arrOff, Reg.$fp);
            } else {
                MipsIns.sll_ans_regx_num(ansReg, subReg, 2);//
                MipsIns.add_ans_reg_regOrNum(ansReg, ansReg, Reg.$fp); //地址
                MipsIns.lw_ans_num_baseReg(ansReg, arrOff, ansReg);
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

    //////////////////////////////////////////////////////////////////////////////
    // next is static handle for mips generate
    //////////////////////////////////////////////////////////////////////////////

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
        get_Array_Value_In_Reg(obj, array, elementTable, tableSymbol); //
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(obj, answer); //
        }
        TempRegPool.getInstance().delete(answer, arrSub);
    }

    /**
     * a[vtn] = vtn
     *
     * @param answer      *
     * @param exp         *
     * @param tableSymbol *
     * @throws IOException *
     */
    public static void assignVTNToArr(String answer, String exp, TableSymbol tableSymbol) throws IOException {
        String arrName = SubOp.getArrName(answer);
        String arrSub = SubOp.getArrSubscript(answer);
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, arrName);
        //get value in reg.r1
        Reg value = getValueInReg_t_v_n(Reg.r1, exp, tableSymbol);
        //getAddress
        if (elementTable.isGlobal()) {
            if (Judge.isNumber(arrSub)) {
                MipsIns.sw_value_label_num(value, arrName, Integer.parseInt(arrSub) * 4);
            } else if (Judge.isVarOrTemp(arrSub)) {
                getValueInSpecialReg_t_v(Reg.l1, arrSub, tableSymbol);
                MipsIns.sll_ans_regx_num(Reg.l1, Reg.l1, 2);//
                MipsIns.sw_value_label_base(value, arrName, Reg.l1);
            } else UnExpect.unexpect("assignExpToArr 2");
        } else if (Judge.isLocal(elementTable)) {
            int ansOff = elementTable.getMemOff();
            if (Judge.isNumber(arrSub)) {
                ansOff = ansOff + Integer.parseInt(arrSub) * 4;
                MipsIns.sw_value_num_baseReg(value, ansOff, Reg.$fp);
            } else if (Judge.isVarOrTemp(arrSub)) {
                getValueInSpecialReg_t_v(Reg.l1, arrSub, tableSymbol);
                MipsIns.sll_ans_regx_num(Reg.l1, Reg.l1, 2);//
                MipsIns.add_ans_reg_regOrNum(Reg.l1, Reg.l1, Reg.$fp);
                MipsIns.sw_value_num_baseReg(value, ansOff, Reg.l1);
            } else UnExpect.unexpect("assignExpToArr 3");
        } else if (Judge.isFParam(elementTable)) {
            Reg address = getFParamInReg(elementTable, Reg.l1);
            if (Judge.isNumber(arrSub)) {
                MipsIns.sw_value_num_baseReg(value, Integer.parseInt(arrSub) * 4, address);
            } else if (Judge.isVarOrTemp(arrSub)) {
                getValueInSpecialReg_t_v(Reg.l2, arrSub, tableSymbol);
                MipsIns.sll_ans_regx_num(Reg.l2, Reg.l2, 2);//
                MipsIns.add_ans_reg_regOrNum(Reg.l2, Reg.l2, address);
                MipsIns.sw_value_base(value, Reg.l2);
            } else UnExpect.unexpect("assignExpToArr 5");
        } else UnExpect.unexpect("assignExpToArr 4");

        TempRegPool.getInstance().delete(answer, arrSub);
        TempRegPool.getInstance().delete(answer, exp);
    }

    /**
     * a = vtn
     *
     * @param answer      *
     * @param exp         *
     * @param tableSymbol *
     */
    public static void assignVTNToVar(String answer, String exp, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, answer);
        if (elementTable.isHasReg()) {
            Reg ans = elementTable.getReg();
            getValueInSpecialReg_t_v_n(ans, exp, tableSymbol);
        } else {
            Reg reg = getValueInReg_t_v_n(Reg.r1, exp, tableSymbol);
            if (elementTable.isGlobal()) {
                MipsIns.sw_value_label(reg, answer);
            } else {
                int ansOff = elementTable.getMemOff();
                MipsIns.sw_value_num_baseReg(reg, ansOff, Reg.$fp);
            }
        }
        TempRegPool.getInstance().delete(answer, exp);
    }

    /**
     * t = vtn
     * <p>
     * 不存在a是数组地址的情况
     * ir中，这种情况都为 t = array_address >> 2
     *
     * @param temp        *
     * @param var         *
     * @param tableSymbol *
     */
    public static void assignVTN_ToTemp(String temp, String var, TableSymbol tableSymbol) throws IOException {
        TempRegPool.getInstance().addToPool(temp);
        if (TempRegPool.getInstance().inReg(temp)) {
            Reg reg = TempRegPool.getInstance().getReg(temp);
            getValueInSpecialReg_t_v_n(reg, var, tableSymbol);
        } else if (TempRegPool.getInstance().inMem(temp)) {
            Reg ret = getValueInReg_t_v_n(Reg.r1, var, tableSymbol);
            TempRegPool.getInstance().storeToMem(ret, temp);
        } else UnExpect.unexpect("assignVarToTemp 1");
    }

    //*********************************************************************************/
    /**
     * ok
     * temp1 = (-|!) vtn
     *
     * @param answer temp1
     * @param op     -|!
     * @param temp   temp2|var
     * @throws IOException e
     */
    public static void assignOne(String answer, String op, String temp, TableSymbol tableSymbol) throws IOException {
        Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        Reg reg = getValueInReg_t_v(Reg.r1, temp, tableSymbol);

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
            } else if (Judge.isVarOrTemp(temp1) && Judge.isNumber(temp2)) {
                assignTwo_VT_Number(answer, temp1, op, num2, tableSymbol);
            } else if (Judge.isNumber(temp1) && Judge.isVarOrTemp(temp2)) {
                assignTwo_Number_Temp(answer, num1, op, temp2, tableSymbol);
            } else if (Judge.isVarOrTemp(temp1) && Judge.isVarOrTemp(temp2)) {
                assignTwo_Two_VT(answer, temp1, op, temp2, tableSymbol);
            } else UnExpect.unexpect("assignTwo");
        }
    }

    public static void assignTwo_Two_VT(String answer, String temp1, String op, String temp2, TableSymbol tableSymbol) throws IOException {
        Reg t1 = getValueInReg_t_v(Reg.r1, temp1, tableSymbol);
        Reg t2 = getValueInReg_t_v(Reg.r2, temp2, tableSymbol);

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
    public static void assignTwo_VT_Number(String answer, String temp1, String op, int number, TableSymbol tableSymbol) throws IOException {
        Reg t1 = getValueInReg_t_v(Reg.r1, temp1, tableSymbol);

        Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
        MipsIns.compute_ans_reg_op_num(ansReg, t1, op, number, Reg.r2);
        if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(ansReg, answer);
        }
        TempRegPool.getInstance().delete(answer, temp1);
    }

    public static void assignTwo_Number_Temp(String answer, int num1, String op, String temp2, TableSymbol tableSymbol) throws IOException {
        Reg t2 = getValueInReg_t_v(Reg.r2, temp2, tableSymbol);
        if (Judge.isPlus(op)) {
            assignTwo_VT_Number(answer, temp2, op, num1, tableSymbol);
        } else {
            MipsIns.li_ans_num(Reg.r1, num1);
            Reg ansReg = TempRegPool.getInstance().addToPool(answer, Reg.l1);
            MipsIns.compute_ans_r1_op_r2(ansReg, Reg.r1, op, t2);
            if (TempRegPool.getInstance().inMem(answer)) {
                TempRegPool.getInstance().storeToMem(ansReg, answer);
            }
        }
        TempRegPool.getInstance().delete(answer, temp2);
    }

    public void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

//    /**
//     * load array[exp]_value in ans_reg
//     * afloat-reg in use : r1,r2
//     * <p>
//     * for t = a[exp] use
//     *
//     * @param ansReg       *
//     * @param array        *
//     * @param elementTable *
//     */
//    public static void get_Array_Value_In_Reg(Reg ansReg, String array, ElementTable elementTable, TableSymbol tableSymbol) throws IOException {
//        String arrName = SubOp.getArrName(array);
//        String arrSub = SubOp.getArrSubscript(array);
//
//        Reg subReg = getValueInReg_t_v(Reg.r2, arrSub, tableSymbol);
//
//        if (elementTable.isGlobal()) {
//            if (Judge.isNumber(arrSub)) {
//                MipsIns.lw_ans_label_num(ansReg, arrName, Integer.parseInt(arrSub) * 4);
//            } else {
//                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
//                MipsIns.lw_ans_label_base(ansReg, arrName, subReg);
//            }
//        } else if (Judge.isFParam(elementTable)) {
//            Reg address = getFParamInReg(elementTable, Reg.r1);
//            if (Judge.isNumber(arrSub)) {
//                MipsIns.lw_ans_num_baseReg(ansReg, Integer.parseInt(arrSub) * 4, address);
//            } else {
//                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
//                MipsIns.add_ans_reg_regOrNum(subReg, subReg, address); //地址
//                MipsIns.lw_ans_num_baseReg(ansReg, 0, subReg);
//            }
//        } else if (Judge.isLocal(elementTable)) {
//            int arrOff = elementTable.getMemOff();
//            if (Judge.isNumber(arrSub)) {
//                MipsIns.lw_ans_num_baseReg(ansReg, Integer.parseInt(arrSub) * 4 + arrOff, Reg.$fp);
//            } else {
//                MipsIns.sll_ans_regx_num(subReg, subReg, 2);//
//                MipsIns.add_ans_reg_regOrNum(subReg, subReg, Reg.$fp); //地址
//                MipsIns.lw_ans_num_baseReg(ansReg, arrOff, subReg);
//            }
//        } else UnExpect.unexpect("assignArrToTemp 1");
//    }
}
