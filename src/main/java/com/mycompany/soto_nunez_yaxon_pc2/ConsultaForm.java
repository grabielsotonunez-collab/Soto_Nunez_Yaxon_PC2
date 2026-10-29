package com.mycompany.soto_nunez_yaxon_pc2;
import Controlador.*;
import DAO.ConsultaDAO;
import MODELO.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.text.MessageFormat;
public final class ConsultaForm extends JPanel {
    private final ConsultaDAO dao=new ConsultaDAO();
    private final JComboBox<Opcion> mascota=new JComboBox<>(), veterinario=new JComboBox<>(), cita=new JComboBox<>();
    private final JTextField fecha=Estilo.campo("Fecha de consulta",16),peso=Estilo.campo("Peso",7),buscar=Estilo.campo("Buscar consulta",100),desde=Estilo.campo("Desde",10),hasta=Estilo.campo("Hasta",10);
    private final JTextArea sintomas=Estilo.area("Síntomas"),diagnostico=Estilo.area("Diagnóstico"),observaciones=Estilo.area("Observaciones");
    private final JLabel editor=new JLabel("Nueva consulta"),estado=new JLabel("Cargando consultas…");
    private final DefaultTableModel modelo=Estilo.modelo("ID","Fecha","Mascota","Propietario","Veterinario","Diagnóstico","Peso kg","Cita","Síntomas","Observaciones");
    private final JTable tabla=Estilo.tabla(modelo);
    private List<Consulta> datos=new ArrayList<>();private int id;private boolean llenando;
    private record Datos(List<Consulta> consultas,List<Opcion> mascotas,List<Opcion> veterinarios,List<Opcion> citas) { }
    public ConsultaForm() {
        super(new BorderLayout(12,12));setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel cab=new JPanel(new BorderLayout(6,10));cab.add(Estilo.titulo("Consultas y reporte clínico"),BorderLayout.NORTH);
        JPanel filtros=new JPanel(new FlowLayout(FlowLayout.LEFT));buscar.setColumns(12);desde.setColumns(9);hasta.setColumns(9);
        filtros.add(new JLabel("Buscar:"));filtros.add(buscar);filtros.add(new JLabel("Desde:"));filtros.add(desde);filtros.add(new JLabel("Hasta:"));filtros.add(hasta);
        desde.setToolTipText("aaaa-MM-dd (opcional)");hasta.setToolTipText("aaaa-MM-dd (opcional)");
        JButton aplicar=Estilo.boton("Filtrar"),todos=Estilo.boton("Limpiar filtros");filtros.add(aplicar);filtros.add(todos);cab.add(filtros);add(cab,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridBagLayout());Estilo.fila(form,"",editor);Estilo.fila(form,"Cita (opcional)",cita);Estilo.fila(form,"Mascota *",mascota);Estilo.fila(form,"Veterinario *",veterinario);Estilo.fila(form,"Fecha y hora *",fecha);fecha.setToolTipText("aaaa-MM-dd HH:mm");
        Estilo.fila(form,"Síntomas",new JScrollPane(sintomas));Estilo.fila(form,"Diagnóstico *",new JScrollPane(diagnostico));Estilo.fila(form,"Peso kg",peso);Estilo.fila(form,"Observaciones",new JScrollPane(observaciones));
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton nuevo=Estilo.boton("Nueva"),guardar=Estilo.principal("Guardar"),eliminar=Estilo.boton("Eliminar");acciones.add(nuevo);acciones.add(guardar);acciones.add(eliminar);
        JPanel lado=new JPanel(new BorderLayout());lado.add(new JScrollPane(form));lado.add(acciones,BorderLayout.SOUTH);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);for(int i=0;i<tabla.getColumnCount();i++)tabla.getColumnModel().getColumn(i).setPreferredWidth(i==5?260:150);tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        JPanel reporte=new JPanel(new BorderLayout(6,6));reporte.add(new JScrollPane(tabla));
        JPanel herramientas=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton refrescar=Estilo.boton("Actualizar"),csv=Estilo.boton("Exportar CSV"),imprimir=Estilo.boton("Imprimir reporte");herramientas.add(refrescar);herramientas.add(csv);herramientas.add(imprimir);reporte.add(herramientas,BorderLayout.SOUTH);
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,lado,reporte);split.setDividerLocation(480);split.setResizeWeight(0.4);add(split);add(estado,BorderLayout.SOUTH);
        cita.addActionListener(e->{if(!llenando){Opcion o=(Opcion)cita.getSelectedItem();if(o!=null && o.id()>0){elegir(mascota,o.mascota());elegir(veterinario,o.veterinario());}}});
        tabla.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())seleccionar();});
        nuevo.addActionListener(e->cargar());refrescar.addActionListener(e->cargar());guardar.addActionListener(e->guardar());eliminar.addActionListener(e->eliminar());aplicar.addActionListener(e->filtrar());buscar.addActionListener(e->filtrar());
        todos.addActionListener(e->{buscar.setText("");desde.setText("");hasta.setText("");filtrar();});csv.addActionListener(e->exportar());imprimir.addActionListener(e->imprimir());
        cargar();
    }
    private void cargar() {
        Tareas.ejecutar(this,()->new Datos(dao.listar(),dao.mascotas(),dao.veterinarios(),dao.citas(0)),d->{
            llenando=true;datos=d.consultas();opciones(mascota,d.mascotas(),"Selecciona mascota");opciones(veterinario,d.veterinarios(),"Selecciona veterinario");opciones(cita,d.citas(),"Sin cita (atención directa)");
            modelo.setRowCount(0);for(Consulta q:datos)modelo.addRow(new Object[]{q.id(),q.fecha().format(Validacion.FECHA),q.mascotaNombre(),q.propietario(),q.veterinarioNombre(),q.diagnostico(),q.peso(),q.cita(),q.sintomas(),q.observaciones()});
            limpiar();llenando=false;filtrar();
        });
    }
    private void opciones(JComboBox<Opcion> combo,List<Opcion> list,String prompt) { combo.removeAllItems();combo.addItem(new Opcion(0,prompt));list.forEach(combo::addItem); }
    private void elegir(JComboBox<Opcion> c,int id) { for(int i=0;i<c.getItemCount();i++)if(c.getItemAt(i).id()==id){c.setSelectedIndex(i);return;}c.setSelectedIndex(0); }
    private int seleccionado(JComboBox<Opcion> c) { Opcion o=(Opcion)c.getSelectedItem();return o==null?0:o.id(); }
    private void limpiar() {id=0;tabla.clearSelection();editor.setText("Nueva consulta");fecha.setText(LocalDateTime.now().format(Validacion.FECHA));peso.setText("");sintomas.setText("");diagnostico.setText("");observaciones.setText("");mascota.setSelectedIndex(0);veterinario.setSelectedIndex(0);cita.setSelectedIndex(0);}
    private void seleccionar() {
        if(llenando || tabla.getSelectedRow()<0)return;
        Consulta q=datos.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
        // Incluye la cita de la consulta actual, aunque ya figure como atendida.
        Tareas.ejecutar(this,()->dao.citas(q.id()),list->{
            llenando=true;id=q.id();editor.setText("Editar consulta #"+id);opciones(cita,list,"Sin cita (atención directa)");elegir(cita,q.cita()==null?0:q.cita());elegir(mascota,q.mascota());elegir(veterinario,q.veterinario());fecha.setText(q.fecha().format(Validacion.FECHA));sintomas.setText(q.sintomas());diagnostico.setText(q.diagnostico());peso.setText(q.peso()==null?"":q.peso().toPlainString());observaciones.setText(q.observaciones());llenando=false;
        });
    }
    private void guardar() {
        try {
            int idCita=seleccionado(cita);
            Consulta q=Validacion.consulta(id,idCita==0?null:idCita,seleccionado(mascota),seleccionado(veterinario),fecha.getText(),sintomas.getText(),diagnostico.getText(),peso.getText(),observaciones.getText());
            Tareas.ejecutar(this,()->{dao.guardar(q);return true;},ok->{Estilo.informar(this,"Consulta guardada correctamente.");cargar();});
        } catch(IllegalArgumentException ex){Tareas.error(this,ex);}
    }
    private void eliminar() {
        if(id==0){Estilo.informar(this,"Selecciona una consulta para eliminar.");return;}
        if(!Estilo.confirmar(this,"¿Eliminar la consulta #"+id+"? Solo es posible si no tiene recetas ni tratamientos."))return;
        int elegido=id;Tareas.ejecutar(this,()->{dao.eliminar(elegido);return true;},ok->{Estilo.informar(this,"Consulta eliminada.");cargar();});
    }
    private void filtrar() {
        try {
            LocalDate inicio=Validacion.dia(desde.getText()),fin=Validacion.dia(hasta.getText());
            if(inicio!=null && fin!=null && inicio.isAfter(fin)) throw new IllegalArgumentException("Desde no puede ser posterior a Hasta.");
            String texto=buscar.getText().strip().toLowerCase(Locale.ROOT);
            ((TableRowSorter<?>)tabla.getRowSorter()).setRowFilter(new RowFilter<TableModel,Integer>() {
                @Override public boolean include(Entry<? extends TableModel,? extends Integer> e) {
                    Consulta q=datos.get(e.getIdentifier());LocalDate f=q.fecha().toLocalDate();
                    String contenido=(q.mascotaNombre()+" "+q.propietario()+" "+q.veterinarioNombre()+" "+q.diagnostico()+" "+q.sintomas()+" "+q.observaciones()).toLowerCase(Locale.ROOT);
                    return (inicio==null || !f.isBefore(inicio)) && (fin==null || !f.isAfter(fin)) && contenido.contains(texto);
                }
            });estado.setText(tabla.getRowCount()+" consultas visibles de "+datos.size()+" · El reporte exporta las filas y el orden visibles. * Obligatorio");
        } catch(IllegalArgumentException ex){Tareas.error(this,ex);}
    }
    private void exportar() {
        if(tabla.getRowCount()==0){Estilo.informar(this,"No hay consultas visibles para exportar.");return;}
        JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File("reporte_consultas.csv"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path path=chooser.getSelectedFile().toPath();
        if(Files.exists(path) && !Estilo.confirmar(this,"El archivo ya existe. ¿Deseas reemplazarlo?"))return;
        try{ReporteCSV.exportar(tabla,path);Estilo.informar(this,"Reporte exportado correctamente.");}catch(Exception ex){Tareas.error(this,ex);}
    }
    private void imprimir() {
        if(tabla.getRowCount()==0){Estilo.informar(this,"No hay consultas visibles para imprimir.");return;}
        try{tabla.print(JTable.PrintMode.FIT_WIDTH,new MessageFormat("Veterinaria · Reporte de consultas"),new MessageFormat("Página {0}"));}catch(Exception ex){Tareas.error(this,ex);}
    }
}
