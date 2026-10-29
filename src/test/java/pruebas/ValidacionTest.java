package pruebas;
import Controlador.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.math.BigDecimal;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import static org.junit.jupiter.api.Assertions.*;
class ValidacionTest {
    @ParameterizedTest @ValueSource(strings={"", " ", "-1", "1.1", "abc", "2147483648", "1e2", "+1"})
    void stockInvalido(String s){assertThrows(IllegalArgumentException.class,()->Validacion.stock(s));}
    @Test void stockLimites(){assertEquals(0,Validacion.stock("0"));assertEquals(Integer.MAX_VALUE,Validacion.stock("2147483647"));}
    @ParameterizedTest @ValueSource(strings={"-1", "1.001", "1e2", "NaN", "100000000.00", "1,2.3", "+2", ".5"})
    void precioInvalido(String s){assertThrows(IllegalArgumentException.class,()->Validacion.decimal(s,"Precio","99999999.99",false,false));}
    @Test void limitesDecimal(){assertEquals(new BigDecimal("0.00"),Validacion.decimal("0","Precio","99999999.99",false,false));assertEquals(new BigDecimal("99999999.99"),Validacion.decimal("99999999,99","Precio","99999999.99",false,false));assertNull(Validacion.decimal(" ","Peso","9999.99",true,true));assertThrows(IllegalArgumentException.class,()->Validacion.decimal("0","Peso","9999.99",true,true));assertThrows(IllegalArgumentException.class,()->Validacion.decimal("10000","Peso","9999.99",true,true));}
    @ParameterizedTest @ValueSource(strings={"2026-02-29 12:00", "2026-04-31 09:10", "2026-10-29 24:00", "29/10/2026 12:00", "", "0001-01-01 00:00"})
    void fechaInvalida(String s){assertThrows(IllegalArgumentException.class,()->Validacion.fecha(s));}
    @Test void bisiesto(){assertEquals(29,Validacion.fecha("2028-02-29 12:00").getDayOfMonth());assertThrows(IllegalArgumentException.class,()->Validacion.dia("2026-02-29"));}
    @Test void texto(){assertThrows(IllegalArgumentException.class,()->Validacion.texto("  ","Nombre",100,true));assertThrows(IllegalArgumentException.class,()->Validacion.texto("x".repeat(101),"Nombre",100,true));assertEquals("Antiséptico",Validacion.texto(" Antiséptico ","Nombre",100,true));assertEquals("",Validacion.texto(null,"Opcional",80,false));}
    @Test void combosYDiagnostico(){assertThrows(IllegalArgumentException.class,()->Validacion.consulta(0,null,0,2,"2026-10-29 12:00","","Diagnóstico","",""));assertThrows(IllegalArgumentException.class,()->Validacion.consulta(0,null,1,0,"2026-10-29 12:00","","Diagnóstico","",""));assertThrows(IllegalArgumentException.class,()->Validacion.consulta(0,null,1,2,"2026-10-29 12:00",""," ","",""));}
    @Test void csvProtegeYConservaOrden() throws Exception {
        Path f=Files.createTempFile("reporte", ".csv");
        try {
            SwingUtilities.invokeAndWait(()->{
                JTable t=new JTable(new DefaultTableModel(new Object[][]{{"Max","=1+2"},{"Luna","Texto; con \"comillas\""}},new Object[]{"Mascota","Diagnóstico"}));
                t.setAutoCreateRowSorter(true);t.getRowSorter().toggleSortOrder(0);
                try {ReporteCSV.exportar(t,f);}catch(Exception e){throw new RuntimeException(e);}
            });
            String s=Files.readString(f);assertTrue(s.contains("\"'"+"=1+2\""));assertTrue(s.contains("\"Texto; con \"\"comillas\"\"\""));assertTrue(s.indexOf("Luna")<s.indexOf("Max"));
        } finally {Files.deleteIfExists(f);}
    }
}
