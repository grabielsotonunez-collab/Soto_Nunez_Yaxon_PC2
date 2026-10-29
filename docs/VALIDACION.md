# Validación de la entrega

Fecha: 29 de octubre de 2026.

## Entorno comprobado

- Compilación: Eclipse Temurin JDK 17, Maven 3.9.9.
- Base de datos: MySQL 8.4 en una instancia aislada, importando el SQL original con `--default-character-set=utf8mb4`.
- Interfaz: Swing en Xvfb, resolución 1440 × 960.
- El SQL entregado permanece idéntico al archivo recibido.

## Resultado automatizado

`mvn verify`, con `PC2_MYSQL_TEST=true` y conexión dirigida exclusivamente a la base desechable: **32 casos ejecutados, 0 fallos, 0 errores y 0 omitidos**.

| Área | Evidencia |
|---|---|
| Validación | 28 casos: stock, precio, peso, límites, campos vacíos, fechas reales, año bisiesto, selecciones obligatorias, CSV y orden de filas |
| Autenticación MySQL | Credenciales válidas/incorrectas, intento de inyección, usuario bloqueado, empleado inactivo, nombres con tildes |
| Medicamentos MySQL | Crear, listar, actualizar, desactivar, eliminar y rechazar borrado de medicamento recetado |
| Consultas MySQL | Crear con/sin cita, editar, eliminar, rechazar veterinario no habilitado, coincidencia cita/mascota/veterinario, cita duplicada y rollback |
| Integridad | Se conserva la consulta con receta/tratamiento; la cita se actualiza dentro de la misma transacción |

Los 4 métodos de integración contienen varias comprobaciones cada uno. En `mvn test` sin la variable de habilitación, esos 4 casos se omiten explícitamente; los 28 casos unitarios siguen ejecutándose.

## Prueba funcional de los formularios

Se ejecutó `pruebas.InterfazSmoke` usando **el JAR empaquetado** y una pantalla virtual. Resultado: **UI SMOKE OK**.

Comprobaciones:

1. Apertura del formulario de acceso y validación del usuario obligatorio.
2. Rechazo de contraseña incorrecta y acceso con `admin` del SQL.
3. Carga de cuatro medicamentos y dos consultas demo.
4. Avisos al intentar guardar un medicamento sin nombre o con stock negativo.
5. Creación, selección, edición, búsqueda y eliminación de un medicamento desde el formulario.
6. Avisos por mascota sin seleccionar y peso cero en una consulta.
7. Creación, selección, edición y eliminación de una consulta desde el formulario.
8. Rechazo de una fecha de filtro inexistente, filtrado por texto y exportación CSV de los resultados.
9. Cierre de sesión y retorno al acceso.
10. Capturas reales de los tres formularios incluidas en esta carpeta. Verificación de que quedan solo los 4 medicamentos y 2 consultas originales al terminar.

Para repetir la prueba visual, compila primero `mvn test-compile package`, configura las variables de una base **desechable con los datos demo originales**, habilita `PC2_MYSQL_TEST=true` y ejecuta:

```sh
# Linux/macOS; en Windows cambia ':' por ';' en el classpath.
java -cp target/test-classes:target/Soto_Nunez_Yaxon_PC2.jar pruebas.InterfazSmoke
# En Linux sin escritorio: anteponer xvfb-run -a
```

No ejecutes esta prueba sobre una base con trabajo real: crea y elimina registros temporales y supone los datos demo iniciales.

## Límites de la verificación

- Se verificaron código, conexión, consultas SQL y formularios en Linux. No se ejecutó NetBeans ni el lanzador `.bat` en Windows; se entregan el proyecto Maven, la acción F6 y las instrucciones correspondientes.
- Se verificó la exportación CSV. La impresión usa `JTable.print` y el diálogo nativo; no se confirmó una impresión física ni un controlador PDF del equipo del estudiante.
- La conexión del equipo del estudiante requiere su servidor MySQL y su clave local. No se puede comprobar desde este entorno su servicio en `127.0.0.1:15327`.
- El ZIP contiene el JAR con dependencias, código, SQL y documentación. No incluye JDK, servidor MySQL ni contraseñas privadas.
