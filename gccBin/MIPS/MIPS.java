package gccBin.MIPS;

import SymbolTableBin.*;
import gccBin.MidCode.Line.ArrayDefLine;
import gccBin.MidCode.Line.AssignLine;
import gccBin.MidCode.Line.Line;
import gccBin.MidCode.LineManager;
import gccBin.MidCode.original.PrintfFormatStringStore;

import javax.xml.bind.Element;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MIPS {
    private static MIPS instance;

    private FileWriter fileWriter; //和MM绑定

    private MIPS() {
    }

    public static MIPS getInstance() {
        if (instance == null) {
            instance = new MIPS();
        }
        return instance;
    }

    public void openMipsWriter() throws IOException {
        File file = new File("mips.txt");
        this.fileWriter = new FileWriter(file.getName());
    }

    public void closeMIPSWriter() throws IOException {
        this.fileWriter.close();
    }

    public void beginTransLate() throws IOException {
        LineManager.getInstance().beginErgodic();
        Line line = LineManager.getInstance().nextLine();
        TableSymbol tableSymbol = line.getTableSymbol();
    }

    public void arrayLineTranslate(TableSymbol tableSymbol,ArrayDefLine arrayDefLine) throws IOException {
        if (tableSymbol.getFather() == null) { //全局数组
            int len = arrayDefLine.getLen();
            String arrName = arrayDefLine.getName();

            ElementTable elementTable = APIIRSymTable.getInstance().findGlobalElement(
                    arrayDefLine.getName());
            writeN(".data");
            write("  "+arrayDefLine.getName());
            if(elementTable instanceof ElementConstArray){
                write(".word ");
                for(int i = 0;i<len;i++){
                    AssignLine assignLine = (AssignLine) LineManager.
                            getInstance().nextLine();
                    write(assignLine.getT1()+",");
                }
                write("\n");
            } else if (elementTable instanceof ElementVarArray) {
                writeN(".space "+len*4);

            }
        }
    }

    public void pre() throws IOException { //先打出来
        fileWriter.write(".data\n");
        fileWriter.write("str_ : .asciiz \"\\n\"\n");
        int i = 0;
        for (String str : PrintfFormatStringStore.getInstance().getFormatsStrings()) {
            fileWriter.write("str_" + i + ": " + ".asciiz \"" + str + "\"\n");
            i++;
        }
        fileWriter.write(".text\n");
    }

    public String annotate(String s){
        return "# " + s + " #";
    }

    public void writeN(String s) throws IOException {
        fileWriter.write(s + "\n");
    }

    public void write(String s) throws IOException {
        fileWriter.write(s + "\n");
    }
}
