package CONEXION;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Misma conexión JDBC, adaptada únicamente al esquema veterinaria. */
public final class CONEX {
    private static final String PUERTO = "15327";
    private static final String USUARIO = "YAXON";
    private static final String URL = "jdbc:mysql://127.0.0.1:" + PUERTO
        + "/veterinaria?useSSL=false&connectionTimeZone=LOCAL"
        + "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
        + "&connectTimeout=5000&socketTimeout=15000";
    private CONEX() { }
    public static Connection getConnection() throws SQLException {
        Properties local = new Properties();
        Path file = Path.of(System.getProperty("clinica.config", "config/database.properties"));
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { local.load(reader); }
            catch (IOException ex) { throw new SQLException("No se pudo leer la configuración local.", ex); }
        }
        String url = option("clinica.url", "CLINICA_URL", local, URL);
        String usuario = option("clinica.usuario", "CLINICA_USUARIO", local, USUARIO);
        String clave = option("clinica.clave", "CLINICA_CLAVE", local, "");
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException ex) { throw new SQLException("No se encontró MySQL Connector/J. Recarga las dependencias del POM.", ex); }
        return DriverManager.getConnection(url, usuario, clave);
    }
    private static String option(String key, String env, Properties p, String fallback) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(env);
        return value != null ? value : p.getProperty(key, fallback);
    }
}
