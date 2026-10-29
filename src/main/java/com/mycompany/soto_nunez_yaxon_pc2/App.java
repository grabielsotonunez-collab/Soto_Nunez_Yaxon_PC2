package com.mycompany.soto_nunez_yaxon_pc2;
import MODELO.Usuario;
import javax.swing.*;
import java.awt.*;
public final class App {
    private final JFrame frame=new JFrame("Soto_Nunez_Yaxon_PC2 · Clínica veterinaria");
    private App() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);frame.setMinimumSize(new Dimension(1080,720));frame.setSize(1360,820);frame.setLocationRelativeTo(null);login();frame.setVisible(true);
    }
    private void login() {frame.setContentPane(new LoginForm(this::abrir));frame.getRootPane().setDefaultButton(null);frame.revalidate();frame.repaint();}
    private void abrir(Usuario usuario) {
        JPanel root=new JPanel(new BorderLayout());
        JPanel bar=new JPanel(new BorderLayout(12,12));bar.setBorder(BorderFactory.createEmptyBorder(16,22,16,22));bar.setBackground(Estilo.VERDE);
        JLabel nombre=new JLabel("VETERINARIA  /  "+usuario.empleado()+" · "+usuario.rol());nombre.setForeground(Color.WHITE);nombre.setFont(nombre.getFont().deriveFont(Font.BOLD,16));bar.add(nombre);
        JButton salir=Estilo.boton("Cerrar sesión");bar.add(salir,BorderLayout.EAST);root.add(bar,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("Medicamentos",new MedicamentoForm());tabs.addTab("Consultas y reportes",new ConsultaForm());root.add(tabs);
        salir.addActionListener(e->{if(JOptionPane.showConfirmDialog(frame,"¿Cerrar sesión? Los cambios sin guardar se perderán.","Cerrar sesión",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)login();});
        frame.setContentPane(root);frame.revalidate();frame.repaint();
    }
    public static void main(String[] args) {SwingUtilities.invokeLater(()->{Estilo.instalar();new App();});}
}
