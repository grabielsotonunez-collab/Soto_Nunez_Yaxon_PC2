package MODELO;
public record Opcion(int id, String texto, int mascota, int veterinario) {
    public Opcion(int id, String texto) { this(id, texto, 0, 0); }
    @Override public String toString() { return texto; }
}
