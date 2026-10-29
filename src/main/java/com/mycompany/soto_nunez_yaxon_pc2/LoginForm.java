package com.mycompany.soto_nunez_yaxon_pc2;
import Controlador.*;
import DAO.UsuarioDAO;
import MODELO.Usuario;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;
public final class LoginForm extends JPanel {
    private final JTextField usuario=Estilo.campo("Usuario",50);
    private final JPasswordField clave=new JPasswordField(20);
    private final JButton ingresar=Estilo.principal("Iniciar sesión");
    public LoginForm(Consumer<Usuario> acceso) {
        super(new GridBagLayout());
        JPanel card=Estilo.panel();card.setPreferredSize(new Dimension(510,410));
        JPanel titulo=new JPanel(new GridLayout(0,1,0,12));titulo.add(Estilo.titulo("Clínica veterinaria"));titulo.add(new JLabel("Soto_Nunez_Yaxon_PC2 · Acceso al sistema"));card.add(titulo,BorderLayout.NORTH);
        JPanel form=new JPanel(new GridBagLayout());clave.setName("Contraseña");clave.getAccessibleContext().setAccessibleName("Contraseña");Estilo.limitar(clave,128);
        Estilo.fila(form,"Usuario *",usuario);Estilo.fila(form,"Contraseña *",clave);
        JLabel aviso=new JLabel("Solo usuarios y empleados activos pueden ingresar.");
        JPanel pie=new JPanel(new GridLayout(0,1,0,10));pie.add(ingresar);pie.add(aviso);card.add(form,BorderLayout.CENTER);card.add(pie,BorderLayout.SOUTH);add(card);
        ingresar.addActionListener(e->entrar(acceso));clave.addActionListener(e->ingresar.doClick());usuario.addActionListener(e->clave.requestFocusInWindow());
    }
    private void entrar(Consumer<Usuario> acceso) {
        char[] password=clave.getPassword();
        try {
            String nombre=Validacion.texto(usuario.getText(),"Usuario",50,true);
            if(password.length==0) throw new IllegalArgumentException("Ingresa tu contraseña.");
            Tareas.ejecutar(this,()-> { try { return new UsuarioDAO().autenticar(nombre,password); } finally { Arrays.fill(password,'\0'); } },u->{
                clave.setText("");
                if(u==null) { Estilo.informar(this,"Usuario o contraseña incorrectos, o cuenta no habilitada.");clave.requestFocusInWindow(); }
                else acceso.accept(u);
            });
        } catch(IllegalArgumentException ex) { Arrays.fill(password,'\0');Tareas.error(this,ex); }
    }
}
