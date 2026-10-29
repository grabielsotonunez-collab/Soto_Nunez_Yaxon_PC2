package Controlador;
import MODELO.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.*;
public final class Validacion {
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private Validacion() { }
    public static String texto(String valor, String campo, int max, boolean requerido) {
        String t = valor == null ? "" : valor.strip();
        if (requerido && t.isEmpty()) throw new IllegalArgumentException(campo + ": es obligatorio.");
        if (t.codePointCount(0,t.length()) > max) throw new IllegalArgumentException(campo + ": máximo " + max + " caracteres.");
        if (t.codePoints().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\t'))
            throw new IllegalArgumentException(campo + ": contiene caracteres no válidos.");
        return t;
    }
    public static int stock(String valor) {
        try {
            if (!valor.strip().matches("[0-9]+")) throw new NumberFormatException();
            return Integer.parseInt(valor.strip());
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Stock: ingresa un entero entre 0 y 2147483647."); }
    }
    public static BigDecimal decimal(String valor, String campo, String max, boolean opcional, boolean positivo) {
        if (valor == null || valor.isBlank()) {
            if (opcional) return null;
            throw new IllegalArgumentException(campo + ": es obligatorio.");
        }
        try {
            String v = valor.strip().replace(',', '.');
            if (!v.matches("[0-9]+(?:\\.[0-9]{1,2})?")) throw new NumberFormatException();
            BigDecimal n = new BigDecimal(v).setScale(2, RoundingMode.UNNECESSARY);
            if (n.compareTo(new BigDecimal(max)) > 0 || (positivo && n.signum() <= 0)) throw new NumberFormatException();
            return n;
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException(campo + ": usa " + (positivo ? "un valor mayor que 0" : "un valor desde 0") + " hasta " + max + " con máximo 2 decimales.");
        }
    }
    public static LocalDateTime fecha(String valor) {
        try {
            LocalDateTime f = LocalDateTime.parse(valor.strip(), FECHA);
            if (f.getYear() < 1000 || f.getYear() > 9999) throw new DateTimeException("rango");
            return f;
        } catch (DateTimeException ex) { throw new IllegalArgumentException("Fecha: usa aaaa-MM-dd HH:mm y una fecha real (años 1000 a 9999)."); }
    }
    public static LocalDate dia(String valor) {
        if (valor.isBlank()) return null;
        try { return LocalDate.parse(valor.strip(), DateTimeFormatter.ISO_LOCAL_DATE); }
        catch (DateTimeException ex) { throw new IllegalArgumentException("Filtro de fecha: usa aaaa-MM-dd y una fecha real."); }
    }
    public static Medicamento medicamento(int id, String nombre, String presentacion, String concentracion, String stock, String precio, boolean activo) {
        return new Medicamento(id,texto(nombre,"Nombre",100,true),texto(presentacion,"Presentación",80,false),
            texto(concentracion,"Concentración",60,false),stock(stock),decimal(precio,"Precio", "99999999.99", false,false),activo);
    }
    public static Consulta consulta(int id, Integer cita, int mascota, int veterinario, String fecha, String sintomas, String diagnostico, String peso, String observaciones) {
        if (mascota <= 0) throw new IllegalArgumentException("Selecciona una mascota.");
        if (veterinario <= 0) throw new IllegalArgumentException("Selecciona un veterinario.");
        return new Consulta(id,cita,mascota,veterinario,fecha(fecha),texto(sintomas,"Síntomas",5000,false),
            texto(diagnostico,"Diagnóstico",5000,true),decimal(peso,"Peso", "9999.99",true,true),
            texto(observaciones,"Observaciones",5000,false),"","", "");
    }
}
