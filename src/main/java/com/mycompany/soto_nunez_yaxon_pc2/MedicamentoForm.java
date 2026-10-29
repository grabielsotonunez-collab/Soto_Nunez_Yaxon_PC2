package com.mycompany.soto_nunez_yaxon_pc2;
import Controlador.*;
import DAO.MedicamentoDAO;
import MODELO.Medicamento;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
public final class MedicamentoForm extends JPanel {
    private final MedicamentoDAO dao=new MedicamentoDAO();
    private final JTextField nombre=Estilo.campo("Nombre",100), presentacion=Estilo.campo("Presentación",80), concentracion=Estilo.campo("Concentración",60), stock=Estilo.campo("Stock",10), precio=Estilo.campo("Precio",11), buscar=Estilo.campo("Buscar medicamento",100);
    private final JCheckBox activo=new JCheckBox("Activo",true);
    private final DefaultTableModel modelo=Estilo.modelo("ID","Nombre","Presentación","Concentración","Stock","Precio S/","Estado");
    private final JTable tabla=Estilo.tabla(modelo);
    private final JLabel estado=new JLabel("Cargando medicamentos…"), editor=new JLabel("Nuevo medicamento");
    private List<Medicamento> datos=new ArrayList<>();
    private int id;
    public MedicamentoForm() {
        super(new BorderLayout(14,14));setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel cab=new JPanel(new BorderLayout(12,12));cab.add(Estilo.titulo("Medicamentos"),BorderLayout.NORTH);
        JPanel busqueda=new JPanel(new BorderLayout(8,8));busqueda.add(new JLabel("Buscar:"),BorderLayout.WEST);busqueda.add(buscar);JButton recargar=Estilo.boton("Actualizar listado");busqueda.add(recargar,BorderLayout.EAST);cab.add(busqueda);add(cab,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridBagLayout());
        Estilo.fila(form,"",editor);Estilo.fila(form,"Nombre *",nombre);Estilo.fila(form,"Presentación",presentacion);Estilo.fila(form,"Concentración",concentracion);Estilo.fila(form,"Stock *",stock);Estilo.fila(form,"Precio S/ *",precio);Estilo.fila(form,"Estado",activo);
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton nuevo=Estilo.boton("Nuevo"),guardar=Estilo.principal("Guardar"),eliminar=Estilo.boton("Eliminar");acciones.add(nuevo);acciones.add(guardar);acciones.add(eliminar);
        JPanel lado=new JPanel(new BorderLayout(8,8));lado.add(form,BorderLayout.NORTH);lado.add(acciones,BorderLayout.SOUTH);
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,lado,new JScrollPane(tabla));split.setResizeWeight(0.38);split.setDividerLocation(420);add(split);add(estado,BorderLayout.SOUTH);
        nuevo.addActionListener(e->limpiar());recargar.addActionListener(e->cargar());guardar.addActionListener(e->guardar());eliminar.addActionListener(e->eliminar());
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()) seleccionar();});
        buscar.getDocument().addDocumentListener(new DocumentListener() { public void insertUpdate(DocumentEvent e){filtrar();} public void removeUpdate(DocumentEvent e){filtrar();} public void changedUpdate(DocumentEvent e){filtrar();} });
        int[] anchos={45,230,150,150,65,90,85};for(int i=0;i<anchos.length;i++)tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        limpiar();cargar();
    }
    private void filtrar() { ((TableRowSorter<?>)tabla.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?iu)"+Pattern.quote(buscar.getText()),1,2,3));estado.setText(tabla.getRowCount()+" medicamentos visibles · Selecciona una fila para editar. * Obligatorio"); }
    private void cargar() { Tareas.ejecutar(this,dao::listar,list->{datos=list;modelo.setRowCount(0);for(Medicamento m:list) modelo.addRow(new Object[]{m.id(),m.nombre(),m.presentacion(),m.concentracion(),m.stock(),m.precio(),m.activo()?"Activo":"Inactivo"});limpiar();filtrar();}); }
    private void seleccionar() {
        int row=tabla.getSelectedRow();if(row<0)return;
        Medicamento m=datos.get(tabla.convertRowIndexToModel(row));id=m.id();editor.setText("Editar medicamento #"+id);
        nombre.setText(m.nombre());presentacion.setText(m.presentacion());concentracion.setText(m.concentracion());stock.setText(""+m.stock());precio.setText(m.precio().toPlainString());activo.setSelected(m.activo());
    }
    private void limpiar() { id=0;tabla.clearSelection();editor.setText("Nuevo medicamento");nombre.setText("");presentacion.setText("");concentracion.setText("");stock.setText("0");precio.setText("");activo.setSelected(true); }
    private void guardar() {
        try {
            Medicamento m=Validacion.medicamento(id,nombre.getText(),presentacion.getText(),concentracion.getText(),stock.getText(),precio.getText(),activo.isSelected());
            Tareas.ejecutar(this,()->{dao.guardar(m);return true;},ok->{Estilo.informar(this,"Medicamento guardado correctamente.");cargar();});
        } catch(IllegalArgumentException e){Tareas.error(this,e);}
    }
    private void eliminar() {
        if(id==0) {Estilo.informar(this,"Selecciona un medicamento para eliminar.");return;}
        if(!Estilo.confirmar(this,"¿Eliminar el medicamento #"+id+" — "+nombre.getText()+"?")) return;
        int seleccionado=id;Tareas.ejecutar(this,()->{dao.eliminar(seleccionado);return true;},ok->{Estilo.informar(this,"Medicamento eliminado.");cargar();});
    }
}
