package MODELO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record Consulta(int id, Integer cita, int mascota, int veterinario, LocalDateTime fecha,
                       String sintomas, String diagnostico, BigDecimal peso, String observaciones,
                       String mascotaNombre, String propietario, String veterinarioNombre) { }
