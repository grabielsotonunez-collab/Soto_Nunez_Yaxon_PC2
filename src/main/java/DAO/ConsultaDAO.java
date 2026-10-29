package DAO;
import CONEXION.CONEX;
import MODELO.*;
import java.sql.*;
import java.util.*;

public final class ConsultaDAO {
    public List<Consulta> listar() throws SQLException {
        List<Consulta> list = new ArrayList<>();
        String sql = "SELECT q.*, m.nombre mascota, CONCAT(cl.nombres,' ',cl.apellidos) propietario, CONCAT(e.nombres,' ',e.apellidos) veterinario FROM consulta q JOIN mascota m ON m.id_mascota=q.id_mascota JOIN cliente cl ON cl.id_cliente=m.id_cliente JOIN empleado e ON e.id_empleado=q.id_empleado ORDER BY q.fecha_consulta DESC,q.id_consulta DESC";
        try (Connection c=CONEX.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) list.add(new Consulta(r.getInt("id_consulta"),r.getObject("id_cita",Integer.class),r.getInt("id_mascota"),r.getInt("id_empleado"),r.getTimestamp("fecha_consulta").toLocalDateTime(),r.getString("sintomas"),r.getString("diagnostico"),r.getBigDecimal("peso"),r.getString("observaciones"),r.getString("mascota"),r.getString("propietario"),r.getString("veterinario")));
        }
        return list;
    }
    public List<Opcion> mascotas() throws SQLException {
        return opciones("SELECT m.id_mascota, CONCAT(m.nombre,' — ',cl.nombres,' ',cl.apellidos, IF(m.activo=1,'',' [inactiva]')) FROM mascota m JOIN cliente cl ON cl.id_cliente=m.id_cliente ORDER BY m.nombre");
    }
    public List<Opcion> veterinarios() throws SQLException {
        return opciones("SELECT id_empleado,CONCAT(nombres,' ',apellidos, IF(activo=1,'',' [inactivo]')) FROM empleado WHERE cargo='Veterinario' ORDER BY nombres");
    }
    private List<Opcion> opciones(String sql) throws SQLException {
        List<Opcion> out=new ArrayList<>();
        try(Connection c=CONEX.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) out.add(new Opcion(r.getInt(1),r.getString(2)));
        }
        return out;
    }
    public List<Opcion> citas(int consulta) throws SQLException {
        List<Opcion> list=new ArrayList<>();
        String sql="SELECT c.*,m.nombre FROM cita c JOIN mascota m ON m.id_mascota=c.id_mascota LEFT JOIN consulta q ON q.id_cita=c.id_cita WHERE (q.id_consulta IS NULL AND c.estado IN ('Programada','Confirmada')) OR q.id_consulta=? ORDER BY c.fecha_hora";
        try(Connection c=CONEX.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,consulta);
            try(ResultSet r=p.executeQuery()) { while(r.next()) list.add(new Opcion(r.getInt("id_cita"),"#"+r.getInt("id_cita")+" · "+r.getString("nombre")+" · "+r.getTimestamp("fecha_hora"),r.getInt("id_mascota"),r.getInt("id_empleado"))); }
        }
        return list;
    }
    public void guardar(Consulta q) throws SQLException {
        try(Connection c=CONEX.getConnection()) {
            c.setAutoCommit(false);
            try {
                Integer anterior=null;
                if(q.id()!=0) {
                    try(PreparedStatement p=c.prepareStatement("SELECT id_cita FROM consulta WHERE id_consulta=? FOR UPDATE")) {
                        p.setInt(1,q.id()); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("La consulta ya no existe.","02000"); anterior=r.getObject(1,Integer.class); }
                    }
                }
                validarActivo(c,"SELECT activo FROM mascota WHERE id_mascota=?",q.mascota(),"Selecciona una mascota activa.");
                validarActivo(c,"SELECT activo FROM empleado WHERE id_empleado=? AND cargo='Veterinario'",q.veterinario(),"Selecciona un veterinario activo.");
                if(q.cita()!=null) {
                    try(PreparedStatement p=c.prepareStatement("SELECT * FROM cita WHERE id_cita=? FOR UPDATE")) {
                        p.setInt(1,q.cita()); try(ResultSet r=p.executeQuery()) {
                            if(!r.next()) throw new SQLException("La cita no existe.","45000");
                            if(r.getInt("id_mascota")!=q.mascota() || r.getInt("id_empleado")!=q.veterinario()) throw new SQLException("La mascota y el veterinario deben coincidir con la cita.","45000");
                            String estado=r.getString("estado");
                            if(!Objects.equals(anterior,q.cita()) && !Set.of("Programada","Confirmada").contains(estado)) throw new SQLException("La cita no está disponible.","45000");
                        }
                    }
                }
                String sql=q.id()==0 ? "INSERT INTO consulta(id_cita,id_mascota,id_empleado,fecha_consulta,sintomas,diagnostico,peso,observaciones) VALUES(?,?,?,?,?,?,?,?)" : "UPDATE consulta SET id_cita=?,id_mascota=?,id_empleado=?,fecha_consulta=?,sintomas=?,diagnostico=?,peso=?,observaciones=? WHERE id_consulta=?";
                try(PreparedStatement p=c.prepareStatement(sql)) {
                    p.setObject(1,q.cita(),Types.INTEGER); p.setInt(2,q.mascota()); p.setInt(3,q.veterinario()); p.setTimestamp(4,Timestamp.valueOf(q.fecha()));
                    p.setString(5,q.sintomas()); p.setString(6,q.diagnostico()); p.setBigDecimal(7,q.peso()); p.setString(8,q.observaciones());
                    if(q.id()!=0) p.setInt(9,q.id()); p.executeUpdate();
                }
                if(anterior!=null && !Objects.equals(anterior,q.cita())) estado(c,anterior,"Confirmada");
                if(q.cita()!=null) estado(c,q.cita(),"Atendida");
                c.commit();
            } catch(SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
    private void validarActivo(Connection c,String sql,int id,String mensaje) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)) { p.setInt(1,id); try(ResultSet r=p.executeQuery()) { if(!r.next() || !r.getBoolean(1)) throw new SQLException(mensaje,"45000"); } }
    }
    private void estado(Connection c,int id,String estado) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("UPDATE cita SET estado=? WHERE id_cita=?")) { p.setString(1,estado);p.setInt(2,id);p.executeUpdate(); }
    }
    public void eliminar(int id) throws SQLException {
        try(Connection c=CONEX.getConnection()) {
            c.setAutoCommit(false);
            try {
                Integer cita;
                try(PreparedStatement p=c.prepareStatement("SELECT id_cita FROM consulta WHERE id_consulta=? FOR UPDATE")) {
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("La consulta ya no existe.","02000");cita=r.getObject(1,Integer.class); }
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM consulta WHERE id_consulta=?")) { p.setInt(1,id);p.executeUpdate(); }
                if(cita!=null) estado(c,cita,"Confirmada");
                c.commit();
            } catch(SQLException | RuntimeException e) { c.rollback();throw e; }
        }
    }
}
