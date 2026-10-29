package Controlador;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
public final class Tareas {
    private Tareas() { }
    public static <T> void ejecutar(JComponent panel, Callable<T> trabajo, Consumer<T> resultado) {
        List<Component> activos=new ArrayList<>(); bloquear(panel,activos);
        Window w=SwingUtilities.getWindowAncestor(panel);
        if(w!=null) w.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception { return trabajo.call(); }
            @Override protected void done() {
                activos.forEach(c->c.setEnabled(true));
                if(w!=null) w.setCursor(Cursor.getDefaultCursor());
                try { resultado.accept(get()); }
                catch(Exception ex) {
                    Throwable cause=ex;
                    while(cause.getCause()!=null && !(cause instanceof SQLException)) cause=cause.getCause();
                    error(panel,cause);
                }
            }
        }.execute();
    }
    private static void bloquear(Container c,List<Component> activos) {
        for(Component ch:c.getComponents()) {
            if(ch.isEnabled()) { activos.add(ch); ch.setEnabled(false); }
            if(ch instanceof Container sub) bloquear(sub,activos);
        }
    }
    public static void error(Component parent,Throwable ex) {
        String msg;
        if(ex instanceof SQLException sql) {
            int code=sql.getErrorCode();String state=sql.getSQLState();
            if(code==1451) msg="No se puede eliminar: el registro tiene recetas o tratamientos asociados. Conserva el historial; en medicamentos puedes desmarcar Activo.";
            else if(code==1062) msg="Esta cita ya tiene una consulta. Actualiza el listado y selecciona otra cita.";
            else if("45000".equals(state) || "02000".equals(state)) msg=sql.getMessage();
            else if(code==1045) msg="MySQL rechazó el usuario o la clave. Revisa config/database.properties (no son las credenciales de inicio de sesión).";
            else if(code==1049 || code==1146) msg="Falta la base veterinaria o sus tablas. Importa database/veterinaria_mysql.sql en MySQL.";
            else if(code==1213 || code==1205) msg="Otro usuario está modificando estos datos. Actualiza y vuelve a intentarlo.";
            else msg="No se pudo completar la operación de base de datos. Verifica MySQL, puerto, permisos y configuración local. SQLState: " + state;
        } else msg=ex.getMessage()==null ? "No se pudo completar la operación." : ex.getMessage();
        JOptionPane.showMessageDialog(parent,msg,"Revisa los datos",JOptionPane.WARNING_MESSAGE);
    }
}
