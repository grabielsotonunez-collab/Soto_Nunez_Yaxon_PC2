package pruebas;

import com.mycompany.soto_nunez_yaxon_pc2.App;
import Controlador.ReporteCSV;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.*;

/** Prueba funcional con pantalla real o Xvfb; usar solo con la BD de pruebas. */
public final class InterfazSmoke {
    private static JFrame frame;
    private static final List<String> mensajes=new CopyOnWriteArrayList<>();
    private static <T> T edt(Callable<T> task) throws Exception {
        FutureTask<T> f=new FutureTask<>(task);SwingUtilities.invokeAndWait(f);return f.get();
    }
    private static void esperar(Callable<Boolean> check,String contexto) throws Exception {
        long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(20);
        while(System.nanoTime()<end){if(edt(check))return;Thread.sleep(75);}
        throw new AssertionError("Tiempo agotado: "+contexto);
    }
    private static List<Component> arbol(Component p){List<Component> out=new ArrayList<>();out.add(p);if(p instanceof Container c)for(Component x:c.getComponents())out.addAll(arbol(x));return out;}
    private static <T> T uno(Component p,Class<T> type,Predicate<T> match){return arbol(p).stream().filter(type::isInstance).map(type::cast).filter(match).findFirst().orElseThrow();}
    private static JTextField campo(Component p,String n){return uno(p,JTextField.class,c->n.equals(c.getName()));}
    private static JButton boton(Component p,String n){return uno(p,JButton.class,c->n.equals(c.getText()));}
    private static void click(Component p,String n) throws Exception {edt(()->{boton(p,n).doClick();return null;});}
    private static void texto(Component p,String n,String v) throws Exception {edt(()->{campo(p,n).setText(v);return null;});}
    private static boolean contiene(JTable t,String value){for(int i=0;i<t.getRowCount();i++)for(int j=0;j<t.getColumnCount();j++)if(value.equals(t.getValueAt(i,j)))return true;return false;}
    private static void seleccionar(JTable t,String value) throws Exception {edt(()->{for(int i=0;i<t.getRowCount();i++)for(int j=0;j<t.getColumnCount();j++)if(value.equals(t.getValueAt(i,j))){t.setRowSelectionInterval(i,i);return null;}throw new AssertionError("No se encontró "+value);});}
    private static void foto(String nombre) throws Exception {
        Rectangle rect=edt(()->frame.getBounds());BufferedImage image=new Robot().createScreenCapture(rect);ImageIO.write(image,"png",Path.of("docs",nombre+".png").toFile());
    }
    public static void main(String[] args) throws Exception {
        if(!"true".equals(System.getenv("PC2_MYSQL_TEST")))throw new IllegalStateException("Solo ejecutar en BD de prueba: PC2_MYSQL_TEST=true");
        Files.createDirectories(Path.of("docs"));
        edt(()->{
            new javax.swing.Timer(60,e->{
                for(Window w:Window.getWindows())if(w instanceof JDialog d && d.isVisible()) {
                    for(Component c:arbol(d))if(c instanceof JOptionPane p){mensajes.add(String.valueOf(p.getMessage()));p.setValue(JOptionPane.YES_OPTION);d.dispose();}
                }
            }).start();return null;
        });
        App.main(new String[0]);
        esperar(()->{for(Window w:Window.getWindows())if(w instanceof JFrame f && f.isVisible()){frame=f;return true;}return false;},"inicio");
        foto("01_login");
        click(frame,"Iniciar sesión");esperar(()->mensajes.stream().anyMatch(s->s.contains("Usuario: es obligatorio")),"usuario requerido");
        texto(frame,"Usuario","admin");texto(frame,"Contraseña","incorrecta");click(frame,"Iniciar sesión");esperar(()->mensajes.stream().anyMatch(s->s.contains("incorrectos")),"clave incorrecta");
        texto(frame,"Contraseña","Admin123*");click(frame,"Iniciar sesión");
        esperar(()->arbol(frame).stream().anyMatch(c->c instanceof JTabbedPane),"login correcto");
        JTabbedPane tabs=edt(()->uno(frame,JTabbedPane.class,t->true));
        Component meds=edt(()->tabs.getComponentAt(0));Component cons=edt(()->tabs.getComponentAt(1));
        esperar(()->boton(meds,"Guardar").isEnabled() && boton(cons,"Guardar").isEnabled(),"carga inicial");
        JTable mt=edt(()->uno(meds,JTable.class,t->true));JTable ct=edt(()->uno(cons,JTable.class,t->true));
        int inicialM=edt(mt::getRowCount),inicialC=edt(ct::getRowCount);
        if(inicialM!=4 || inicialC!=2)throw new AssertionError("Datos demo inesperados");
        foto("02_medicamentos");
        click(meds,"Guardar");esperar(()->mensajes.stream().anyMatch(s->s.contains("Nombre: es obligatorio")),"nombre requerido");
        String item="PRUEBA_UI_PC2";
        texto(meds,"Nombre",item);texto(meds,"Presentación","Frasco 50 ml");texto(meds,"Concentración","10 mg");texto(meds,"Stock","-1");texto(meds,"Precio","12,50");
        click(meds,"Guardar");esperar(()->mensajes.stream().anyMatch(s->s.contains("Stock: ingresa")),"stock inválido");
        texto(meds,"Stock","8");click(meds,"Guardar");esperar(()->contiene(mt,item) && boton(meds,"Guardar").isEnabled(),"alta medicamento");
        seleccionar(mt,item);texto(meds,"Nombre",item+" EDITADO");click(meds,"Guardar");esperar(()->contiene(mt,item+" EDITADO") && boton(meds,"Guardar").isEnabled(),"actualización medicamento");
        texto(meds,"Buscar medicamento",item);esperar(()->mt.getRowCount()==1,"búsqueda medicamento");
        seleccionar(mt,item+" EDITADO");click(meds,"Eliminar");esperar(()->mt.getRowCount()==0 && boton(meds,"Guardar").isEnabled(),"eliminación medicamento");texto(meds,"Buscar medicamento","");
        edt(()->{tabs.setSelectedIndex(1);return null;});foto("03_consultas");
        click(cons,"Guardar");esperar(()->mensajes.stream().anyMatch(s->s.contains("Selecciona una mascota")),"mascota requerida");
        edt(()->{List<JComboBox> combos=arbol(cons).stream().filter(JComboBox.class::isInstance).map(JComboBox.class::cast).toList();combos.get(1).setSelectedIndex(1);combos.get(2).setSelectedIndex(1);uno(cons,JTextArea.class,a->"Diagnóstico".equals(a.getName())).setText(item);return null;});
        texto(cons,"Peso","0");click(cons,"Guardar");esperar(()->mensajes.stream().anyMatch(s->s.contains("Peso: usa")),"peso positivo");
        texto(cons,"Peso","1.50");click(cons,"Guardar");esperar(()->contiene(ct,item) && boton(cons,"Guardar").isEnabled(),"alta consulta");
        seleccionar(ct,item);esperar(()->boton(cons,"Guardar").isEnabled() && arbol(cons).stream().anyMatch(c->c instanceof JLabel l && l.getText().startsWith("Editar consulta #")),"selección consulta");
        edt(()->{uno(cons,JTextArea.class,a->"Diagnóstico".equals(a.getName())).setText(item+" EDITADO");return null;});texto(cons,"Peso","");click(cons,"Guardar");esperar(()->contiene(ct,item+" EDITADO") && boton(cons,"Guardar").isEnabled(),"actualización consulta");
        texto(cons,"Desde","2026-02-29");click(cons,"Filtrar");esperar(()->mensajes.stream().anyMatch(s->s.contains("Filtro de fecha")),"filtro fecha inválida");
        texto(cons,"Desde","");texto(cons,"Buscar consulta",item);click(cons,"Filtrar");esperar(()->ct.getRowCount()==1,"filtro reporte");
        edt(()->{ReporteCSV.exportar(ct,Path.of("/tmp/ui-reporte.csv"));return null;});if(!Files.readString(Path.of("/tmp/ui-reporte.csv")).contains(item+" EDITADO"))throw new AssertionError("CSV incorrecto");
        seleccionar(ct,item+" EDITADO");esperar(()->boton(cons,"Guardar").isEnabled() && arbol(cons).stream().anyMatch(c->c instanceof JLabel l && l.getText().startsWith("Editar consulta #")),"selección eliminar consulta");click(cons,"Eliminar");esperar(()->ct.getRowCount()==0 && boton(cons,"Guardar").isEnabled(),"eliminación consulta");
        click(cons,"Limpiar filtros");esperar(()->ct.getRowCount()==inicialC,"reporte restaurado");
        click(frame,"Cerrar sesión");esperar(()->arbol(frame).stream().anyMatch(c->c instanceof JButton b && "Iniciar sesión".equals(b.getText())),"cierre de sesión");
        System.out.println("UI SMOKE OK: login, validaciones, CRUD medicamentos/consultas, filtros, CSV y cierre de sesión.");
        edt(()->{frame.dispose();return null;});System.exit(0);
    }
}
