package MODELO;
import java.math.BigDecimal;
public record Medicamento(int id, String nombre, String presentacion, String concentracion,
                          int stock, BigDecimal precio, boolean activo) { }
