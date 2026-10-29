package pruebas;
import CONEXION.CONEX;
import DAO.*;
import MODELO.*;
import Controlador.Validacion;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
/** Ejecutar exclusivamente sobre la instancia de pruebas importada; nunca en producción. */
@EnabledIfEnvironmentVariable(named="PC2_MYSQL_TEST",matches="true")
class MySQLTest {
    private final MedicamentoDAO medicamentos=new MedicamentoDAO();
    private final ConsultaDAO consultas=new ConsultaDAO();
    private String nombre="TEST_PC2_"+UUID.randomUUID();
    private int idMedicamento,idConsulta;
    @AfterEach void limpiar() throws Exception {
        try(Connection c=CONEX.getConnection()) {
            if(idConsulta>0)try(PreparedStatement p=c.prepareStatement("DELETE FROM consulta WHERE id_consulta=?")){p.setInt(1,idConsulta);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("DELETE FROM medicamento WHERE nombre LIKE ?")){p.setString(1,nombre+"%");p.executeUpdate();}
        }
    }
    @Test void loginRealEInyeccion() throws Exception {
        UsuarioDAO dao=new UsuarioDAO();assertEquals("Andrea Paredes León",dao.autenticar("admin","Admin123*".toCharArray()).empleado());assertNotNull(dao.autenticar("vet01","Vet123*".toCharArray()));
        assertNull(dao.autenticar("admin","incorrecta".toCharArray()));assertNull(dao.autenticar("' OR 1=1 -- ","x".toCharArray()));
        try(Connection c=CONEX.getConnection();Statement s=c.createStatement()){
            try {s.executeUpdate("UPDATE usuario SET estado='Bloqueado' WHERE nombre_usuario='recepcion'");assertNull(dao.autenticar("recepcion","Recepcion123*".toCharArray()));}
            finally{s.executeUpdate("UPDATE usuario SET estado='Activo' WHERE nombre_usuario='recepcion'");}
            try{s.executeUpdate("UPDATE empleado SET activo=0 WHERE id_empleado=2");assertNull(dao.autenticar("vet01","Vet123*".toCharArray()));}
            finally{s.executeUpdate("UPDATE empleado SET activo=1 WHERE id_empleado=2");}
        }
    }
    @Test void medicamentoCRUD() throws Exception {
        assertTrue(medicamentos.listar().stream().anyMatch(m->m.nombre().equals("Antiséptico veterinario")));
        medicamentos.guardar(Validacion.medicamento(0,nombre,"Frasco","10 mg","12","7,50",true));
        Medicamento m=medicamentos.listar().stream().filter(x->x.nombre().equals(nombre)).findFirst().orElseThrow();idMedicamento=m.id();assertEquals(12,m.stock());
        medicamentos.guardar(Validacion.medicamento(m.id(),nombre,"Tableta","20 mg","0","0",false));
        m=medicamentos.listar().stream().filter(x->x.id()==idMedicamento).findFirst().orElseThrow();assertFalse(m.activo());assertEquals(0,m.stock());assertEquals(0,m.precio().signum());
        medicamentos.eliminar(idMedicamento);assertTrue(medicamentos.listar().stream().noneMatch(x->x.id()==idMedicamento));
        assertThrows(SQLException.class,()->medicamentos.eliminar(1));assertTrue(medicamentos.listar().stream().anyMatch(x->x.id()==1));
    }
    private Consulta nueva(Integer cita,int mascota,int vet,String diagnostico){return Validacion.consulta(0,cita,mascota,vet,"2026-10-29 11:00","Inapetencia",diagnostico,"4.80","Seguimiento");}
    private int estadoCita() throws Exception {try(Connection c=CONEX.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT estado='Atendida' FROM cita WHERE id_cita=3")){r.next();return r.getInt(1);}}
    @Test void consultaCRUDYRollback() throws Exception {
        try {
            assertThrows(SQLException.class,()->consultas.guardar(nueva(3,1,3,nombre)));assertEquals(0,estadoCita());
            consultas.guardar(nueva(3,3,3,nombre));
            Consulta q=consultas.listar().stream().filter(x->x.diagnostico().equals(nombre)).findFirst().orElseThrow();idConsulta=q.id();assertEquals("Michi",q.mascotaNombre());assertFalse(q.propietario().isBlank());assertEquals(1,estadoCita());
            assertThrows(SQLException.class,()->consultas.guardar(nueva(3,3,3,nombre+"duplicado")));
            consultas.guardar(Validacion.consulta(idConsulta,3,3,3,"2026-10-29 12:00","",nombre+" actualizado","",""));
            q=consultas.listar().stream().filter(x->x.id()==idConsulta).findFirst().orElseThrow();assertNull(q.peso());assertEquals(nombre+" actualizado",q.diagnostico());
            consultas.eliminar(idConsulta);idConsulta=0;assertEquals(0,estadoCita());
            assertThrows(SQLException.class,()->consultas.eliminar(1));assertTrue(consultas.listar().stream().anyMatch(x->x.id()==1));
        } finally {
            if(idConsulta>0){consultas.eliminar(idConsulta);idConsulta=0;}
        }
    }
    @Test void consultaDirectaYVeterinarioInvalido() throws Exception {
        assertThrows(SQLException.class,()->consultas.guardar(nueva(null,1,1,nombre)));
        consultas.guardar(nueva(null,1,2,nombre));idConsulta=consultas.listar().stream().filter(x->x.diagnostico().equals(nombre)).findFirst().orElseThrow().id();consultas.eliminar(idConsulta);idConsulta=0;
    }
}
