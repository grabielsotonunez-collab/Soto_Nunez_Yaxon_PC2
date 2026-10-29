package DAO;
import CONEXION.CONEX;
import MODELO.Medicamento;
import java.sql.*;
import java.util.*;
public final class MedicamentoDAO {
    public List<Medicamento> listar() throws SQLException {
        List<Medicamento> list = new ArrayList<>();
        try (Connection c = CONEX.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM medicamento ORDER BY id_medicamento DESC"); ResultSet r = p.executeQuery()) {
            while(r.next()) list.add(new Medicamento(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getInt(5),r.getBigDecimal(6),r.getBoolean(7)));
        }
        return list;
    }
    public void guardar(Medicamento m) throws SQLException {
        String sql = m.id()==0 ? "INSERT INTO medicamento(nombre,presentacion,concentracion,stock,precio,activo) VALUES(?,?,?,?,?,?)" :
            "UPDATE medicamento SET nombre=?,presentacion=?,concentracion=?,stock=?,precio=?,activo=? WHERE id_medicamento=?";
        try (Connection c = CONEX.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1,m.nombre()); p.setString(2,m.presentacion()); p.setString(3,m.concentracion());
            p.setInt(4,m.stock()); p.setBigDecimal(5,m.precio()); p.setBoolean(6,m.activo());
            if(m.id()!=0) p.setInt(7,m.id());
            if(p.executeUpdate()!=1) throw new SQLException("El medicamento ya no existe. Actualiza el listado.","02000");
        }
    }
    public void eliminar(int id) throws SQLException {
        try (Connection c = CONEX.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM medicamento WHERE id_medicamento=?")) {
            p.setInt(1,id); if(p.executeUpdate()!=1) throw new SQLException("El medicamento ya no existe.","02000");
        }
    }
}
