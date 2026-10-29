package Controlador;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
public final class ReporteCSV {
    private ReporteCSV() { }
    public static void exportar(JTable tabla,Path archivo) throws IOException {
        try(BufferedWriter w=Files.newBufferedWriter(archivo,StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            for(int col=0;col<tabla.getColumnCount();col++) {if(col>0)w.write(';');w.write(celda(tabla.getColumnName(col)));}w.newLine();
            for(int row=0;row<tabla.getRowCount();row++) {
                for(int col=0;col<tabla.getColumnCount();col++) {if(col>0)w.write(';');w.write(celda(tabla.getValueAt(row,col)));}w.newLine();
            }
        }
    }
    public static String celda(Object valor) {
        String s=valor==null?"":valor.toString();
        String stripped=s.stripLeading();
        if(!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0))>=0) s="'"+s;
        return "\""+s.replace("\"","\"\"")+"\"";
    }
}
