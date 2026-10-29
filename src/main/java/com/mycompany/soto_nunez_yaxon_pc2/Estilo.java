package com.mycompany.soto_nunez_yaxon_pc2;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
public final class Estilo {
    public static final Color VERDE=new Color(16,107,99), FONDO=new Color(243,247,248), TINTA=new Color(27,48,60);
    private Estilo() { }
    public static void instalar() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch(Exception ignored) { }
        UIManager.put("defaultFont",new Font("SansSerif",Font.PLAIN,14));
        for(Object key: java.util.Collections.list(UIManager.getDefaults().keys())) if(key.toString().endsWith(".font")) UIManager.put(key,new javax.swing.plaf.FontUIResource("SansSerif",Font.PLAIN,14));
        UIManager.put("Panel.background",FONDO); UIManager.put("Label.foreground",TINTA);
        UIManager.put("Table.rowHeight",30); UIManager.put("Table.selectionBackground",new Color(211,235,230)); UIManager.put("Table.selectionForeground",TINTA);
        UIManager.put("TabbedPane.selected",Color.WHITE);
    }
    public static JPanel panel() { JPanel p=new JPanel(new BorderLayout(12,12));p.setBorder(new EmptyBorder(18,18,18,18));return p; }
    public static JLabel titulo(String text) { JLabel l=new JLabel(text);l.setFont(l.getFont().deriveFont(Font.BOLD,24));return l; }
    public static JButton boton(String text) { JButton b=new JButton(text);b.setFocusPainted(false);b.setMargin(new Insets(9,15,9,15));return b; }
    public static JButton principal(String text) { JButton b=boton(text);b.setBackground(VERDE);b.setForeground(Color.WHITE);b.setOpaque(true);return b; }
    public static JTextField campo(String nombre,int max) { JTextField t=new JTextField(20);t.setName(nombre);t.getAccessibleContext().setAccessibleName(nombre);limitar(t,max);return t; }
    public static JTextArea area(String nombre) { JTextArea a=new JTextArea(3,20);a.setLineWrap(true);a.setWrapStyleWord(true);a.setName(nombre);limitar(a,5000);return a; }
    public static void limitar(JTextComponent t,int max) {
        ((AbstractDocument)t.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void insertString(FilterBypass b,int o,String s,AttributeSet a) throws BadLocationException { replace(b,o,0,s,a); }
            @Override public void replace(FilterBypass b,int o,int len,String s,AttributeSet a) throws BadLocationException {
                if(s==null) s="";
                String actual=b.getDocument().getText(0,b.getDocument().getLength());
                String next=actual.substring(0,o)+s+actual.substring(o+len);
                if(next.codePointCount(0,next.length())<=max) super.replace(b,o,len,s,a); else Toolkit.getDefaultToolkit().beep();
            }
        });
    }
    public static void fila(JPanel p,String label,JComponent field) {
        JLabel l=new JLabel(label);l.setLabelFor(field);
        GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=p.getComponentCount()/2;g.anchor=GridBagConstraints.NORTHWEST;g.insets=new Insets(6,0,6,10);p.add(l,g);
        g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(field,g);
    }
    public static JTable tabla(DefaultTableModel m) {
        JTable t=new JTable(m);t.setRowHeight(30);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setAutoCreateRowSorter(true);t.setFillsViewportHeight(true);t.getTableHeader().setReorderingAllowed(false);return t;
    }
    public static DefaultTableModel modelo(String... cols) { return new DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c) { return false; } }; }
    public static void informar(Component p,String msg) { JOptionPane.showMessageDialog(p,msg,"Veterinaria",JOptionPane.INFORMATION_MESSAGE); }
    public static boolean confirmar(Component p,String msg) { return JOptionPane.showConfirmDialog(p,msg,"Confirmar eliminación",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION; }
}
