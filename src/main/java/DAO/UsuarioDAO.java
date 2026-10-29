package DAO;
import CONEXION.CONEX;
import MODELO.Usuario;
import java.sql.*;
import java.security.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class UsuarioDAO {
    public Usuario autenticar(String nombre, char[] clave) throws SQLException {
        try (Connection c = CONEX.getConnection(); PreparedStatement p = c.prepareStatement(
            "SELECT u.*, CONCAT(e.nombres,' ',e.apellidos) empleado FROM usuario u JOIN empleado e ON e.id_empleado=u.id_empleado WHERE u.nombre_usuario=? AND u.estado='Activo' AND e.activo=1")) {
            p.setString(1,nombre);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                ByteBuffer bytes = StandardCharsets.UTF_8.encode(CharBuffer.wrap(clave));
                byte[] data = new byte[bytes.remaining()]; bytes.get(data);
                byte[] hash;
                try { hash = MessageDigest.getInstance("SHA-256").digest(data); }
                catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
                finally { Arrays.fill(data,(byte)0); if (bytes.hasArray()) Arrays.fill(bytes.array(),(byte)0); }
                byte[] expected;
                try { expected = HexFormat.of().parseHex(r.getString("password_hash")); }
                catch (IllegalArgumentException ex) { return null; }
                if (!MessageDigest.isEqual(hash,expected)) return null;
                Usuario u = new Usuario(r.getInt("id_usuario"),r.getString("nombre_usuario"),r.getString("empleado"),r.getString("rol"));
                try (PreparedStatement update = c.prepareStatement("UPDATE usuario SET ultimo_acceso=CURRENT_TIMESTAMP WHERE id_usuario=?")) {
                    update.setInt(1,u.id()); update.executeUpdate();
                }
                return u;
            }
        }
    }
}
