package gccBin.MIPS.tool;

import gccBin.MIPS.Operand;
import gccBin.MIPS.Operate;
import gccBin.MIPS.Reg;

import java.io.FileWriter;
import java.io.IOException;

public class Jump {
    private static Jump jump;

    private Jump() {}

    public static Jump get(){
        if(jump == null){
            jump = new Jump();
        }
        return jump;
    }


}
