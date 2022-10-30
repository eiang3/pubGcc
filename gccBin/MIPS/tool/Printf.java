package gccBin.MIPS.tool;

import gccBin.MIPS.Operand;
import gccBin.MIPS.Reg;
import gccBin.MIPS.tool.Compute;

import java.io.FileWriter;
import java.io.IOException;

public class Printf {
    private static int i = 0;

    public static void printf(FileWriter fileWriter, Operand t1) throws IOException {
        String str = t1.getName();
        if(t1.isPrintfD()){
            //Compute.get().pop(Reg.A0);
            fileWriter.write("li $v0,1\n");
            fileWriter.write("syscall\n\n");
        }
        if(str.contains("str_")) {
            fileWriter.write("la $a0," + str+"\n");
            fileWriter.write("li $v0,4\n");
            fileWriter.write("syscall\n\n");
            i++;
        }
    }
}
