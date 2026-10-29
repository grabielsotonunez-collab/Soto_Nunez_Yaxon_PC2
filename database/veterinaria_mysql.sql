-- ============================================================
-- BASE DE DATOS: VETERINARIA
-- Motor: MySQL 8.x / XAMPP
-- Incluye tablas, relaciones y datos de prueba.
-- IMPORTANTE: Las contraseñas de demostración usan SHA2 solo para
-- facilitar pruebas SQL. En producción, generar/verificar hashes
-- con bcrypt o Argon2 desde la aplicación.
-- Usuario demo: admin | Contraseña demo: Admin123*
-- Usuario demo: vet01 | Contraseña demo: Vet123*
-- Usuario demo: recepcion | Contraseña demo: Recepcion123*
-- ============================================================

CREATE DATABASE IF NOT EXISTS veterinaria
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE veterinaria;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS detalle_receta;
DROP TABLE IF EXISTS receta;
DROP TABLE IF EXISTS medicamento;
DROP TABLE IF EXISTS detalle_tratamiento;
DROP TABLE IF EXISTS tratamiento;
DROP TABLE IF EXISTS consulta;
DROP TABLE IF EXISTS cita;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS empleado;
DROP TABLE IF EXISTS mascota;
DROP TABLE IF EXISTS raza;
DROP TABLE IF EXISTS especie;
DROP TABLE IF EXISTS cliente;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE cliente (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  documento VARCHAR(15) NOT NULL UNIQUE,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  telefono VARCHAR(20),
  correo VARCHAR(120) UNIQUE,
  direccion VARCHAR(180),
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE especie (
  id_especie INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE raza (
  id_raza INT AUTO_INCREMENT PRIMARY KEY,
  id_especie INT NOT NULL,
  nombre VARCHAR(60) NOT NULL,
  CONSTRAINT fk_raza_especie FOREIGN KEY (id_especie)
    REFERENCES especie(id_especie),
  UNIQUE (id_especie, nombre)
) ENGINE=InnoDB;

CREATE TABLE mascota (
  id_mascota INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  id_raza INT NOT NULL,
  nombre VARCHAR(80) NOT NULL,
  fecha_nacimiento DATE,
  sexo ENUM('Macho','Hembra','No especificado') NOT NULL DEFAULT 'No especificado',
  peso DECIMAL(6,2),
  color VARCHAR(50),
  observaciones TEXT,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente(id_cliente),
  CONSTRAINT fk_mascota_raza FOREIGN KEY (id_raza)
    REFERENCES raza(id_raza),
  CONSTRAINT chk_mascota_peso CHECK (peso IS NULL OR peso > 0)
) ENGINE=InnoDB;

CREATE TABLE empleado (
  id_empleado INT AUTO_INCREMENT PRIMARY KEY,
  documento VARCHAR(15) NOT NULL UNIQUE,
  nombres VARCHAR(80) NOT NULL,
  apellidos VARCHAR(80) NOT NULL,
  cargo ENUM('Administrador','Veterinario','Recepcionista','Vendedor','Asistente') NOT NULL,
  telefono VARCHAR(20),
  correo VARCHAR(120) UNIQUE,
  fecha_contratacion DATE,
  activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  id_empleado INT NOT NULL UNIQUE,
  nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  rol ENUM('Administrador','Veterinario','Recepcion','Vendedor') NOT NULL,
  estado ENUM('Activo','Bloqueado','Inactivo') NOT NULL DEFAULT 'Activo',
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ultimo_acceso DATETIME NULL,
  CONSTRAINT fk_usuario_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado(id_empleado)
) ENGINE=InnoDB;

CREATE TABLE cita (
  id_cita INT AUTO_INCREMENT PRIMARY KEY,
  id_mascota INT NOT NULL,
  id_empleado INT NOT NULL,
  fecha_hora DATETIME NOT NULL,
  motivo VARCHAR(180) NOT NULL,
  estado ENUM('Programada','Confirmada','Atendida','Cancelada','No asistio')
    NOT NULL DEFAULT 'Programada',
  observaciones TEXT,
  CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota)
    REFERENCES mascota(id_mascota),
  CONSTRAINT fk_cita_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado(id_empleado)
) ENGINE=InnoDB;

CREATE TABLE consulta (
  id_consulta INT AUTO_INCREMENT PRIMARY KEY,
  id_cita INT UNIQUE,
  id_mascota INT NOT NULL,
  id_empleado INT NOT NULL,
  fecha_consulta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sintomas TEXT,
  diagnostico TEXT NOT NULL,
  peso DECIMAL(6,2),
  observaciones TEXT,
  CONSTRAINT fk_consulta_cita FOREIGN KEY (id_cita)
    REFERENCES cita(id_cita),
  CONSTRAINT fk_consulta_mascota FOREIGN KEY (id_mascota)
    REFERENCES mascota(id_mascota),
  CONSTRAINT fk_consulta_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado(id_empleado),
  CONSTRAINT chk_consulta_peso CHECK (peso IS NULL OR peso > 0)
) ENGINE=InnoDB;

CREATE TABLE tratamiento (
  id_tratamiento INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL UNIQUE,
  descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE detalle_tratamiento (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_consulta INT NOT NULL,
  id_tratamiento INT NOT NULL,
  indicaciones TEXT NOT NULL,
  duracion VARCHAR(60),
  CONSTRAINT fk_dt_consulta FOREIGN KEY (id_consulta)
    REFERENCES consulta(id_consulta),
  CONSTRAINT fk_dt_tratamiento FOREIGN KEY (id_tratamiento)
    REFERENCES tratamiento(id_tratamiento),
  UNIQUE (id_consulta, id_tratamiento)
) ENGINE=InnoDB;

CREATE TABLE medicamento (
  id_medicamento INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  presentacion VARCHAR(80),
  concentracion VARCHAR(60),
  stock INT NOT NULL DEFAULT 0,
  precio DECIMAL(10,2) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT chk_medicamento_stock CHECK (stock >= 0),
  CONSTRAINT chk_medicamento_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE receta (
  id_receta INT AUTO_INCREMENT PRIMARY KEY,
  id_consulta INT NOT NULL,
  fecha_emision DATE NOT NULL,
  observaciones TEXT,
  CONSTRAINT fk_receta_consulta FOREIGN KEY (id_consulta)
    REFERENCES consulta(id_consulta)
) ENGINE=InnoDB;

CREATE TABLE detalle_receta (
  id_detalle_receta INT AUTO_INCREMENT PRIMARY KEY,
  id_receta INT NOT NULL,
  id_medicamento INT NOT NULL,
  dosis VARCHAR(80) NOT NULL,
  frecuencia VARCHAR(80) NOT NULL,
  duracion VARCHAR(80) NOT NULL,
  cantidad INT NOT NULL,
  CONSTRAINT fk_dr_receta FOREIGN KEY (id_receta)
    REFERENCES receta(id_receta),
  CONSTRAINT fk_dr_medicamento FOREIGN KEY (id_medicamento)
    REFERENCES medicamento(id_medicamento),
  CONSTRAINT chk_dr_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  categoria VARCHAR(60) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  stock_minimo INT NOT NULL DEFAULT 0,
  precio_venta DECIMAL(10,2) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT chk_producto_stock CHECK (stock >= 0),
  CONSTRAINT chk_producto_stock_min CHECK (stock_minimo >= 0),
  CONSTRAINT chk_producto_precio CHECK (precio_venta >= 0)
) ENGINE=InnoDB;

CREATE TABLE venta (
  id_venta INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente INT NOT NULL,
  id_empleado INT NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
  impuesto DECIMAL(10,2) NOT NULL DEFAULT 0,
  total DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado ENUM('Pendiente','Pagada','Anulada','Parcial') NOT NULL DEFAULT 'Pendiente',
  CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente(id_cliente),
  CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado(id_empleado),
  CONSTRAINT chk_venta_importes CHECK (subtotal >= 0 AND impuesto >= 0 AND total >= 0)
) ENGINE=InnoDB;

CREATE TABLE detalle_venta (
  id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_dv_venta FOREIGN KEY (id_venta)
    REFERENCES venta(id_venta),
  CONSTRAINT fk_dv_producto FOREIGN KEY (id_producto)
    REFERENCES producto(id_producto),
  CONSTRAINT chk_dv_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_dv_importes CHECK (precio_unitario >= 0 AND descuento >= 0)
) ENGINE=InnoDB;

CREATE TABLE pago (
  id_pago INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL UNIQUE,
  fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  monto DECIMAL(10,2) NOT NULL,
  metodo_pago ENUM('Efectivo','Tarjeta','Transferencia','Yape','Plin','Otro') NOT NULL,
  referencia VARCHAR(100),
  CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta)
    REFERENCES venta(id_venta),
  CONSTRAINT chk_pago_monto CHECK (monto > 0)
) ENGINE=InnoDB;

-- ===================== DATOS DE PRUEBA =====================

INSERT INTO cliente (documento,nombres,apellidos,telefono,correo,direccion) VALUES
('70100001','María','Gonzales Ruiz','987111001','maria.gonzales@example.com','Jr. Las Flores 120'),
('70100002','José','Ramírez Soto','987111002','jose.ramirez@example.com','Av. Central 245'),
('70100003','Lucía','Fernández Díaz','987111003','lucia.fernandez@example.com','Calle Los Pinos 330'),
('70100004','Carlos','Mendoza Rojas','987111004','carlos.mendoza@example.com','Jr. Unión 410'),
('70100005','Ana','Torres Silva','987111005','ana.torres@example.com','Av. América 515'),
('70100006','Pedro','Vásquez León','987111006','pedro.vasquez@example.com','Calle Sol 601'),
('70100007','Rosa','Castillo Pérez','987111007','rosa.castillo@example.com','Jr. Amazonas 702'),
('70100008','Miguel','Flores Campos','987111008','miguel.flores@example.com','Av. Perú 803');

INSERT INTO especie (nombre) VALUES ('Perro'),('Gato'),('Ave'),('Conejo');

INSERT INTO raza (id_especie,nombre) VALUES
(1,'Labrador'),(1,'Poodle'),(1,'Mestizo'),(2,'Siamés'),(2,'Persa'),
(2,'Doméstico de pelo corto'),(3,'Canario'),(4,'Enano holandés');

INSERT INTO mascota (id_cliente,id_raza,nombre,fecha_nacimiento,sexo,peso,color,observaciones) VALUES
(1,1,'Max','2021-03-10','Macho',24.50,'Dorado','Vacunas por revisar'),
(1,2,'Luna','2022-07-15','Hembra',6.20,'Blanco','Control anual'),
(2,4,'Michi','2020-11-20','Macho',4.80,'Gris','Gato de interior'),
(3,3,'Rocky','2019-05-02','Macho',18.00,'Marrón','Antecedente de alergia'),
(4,5,'Nala','2023-01-12','Hembra',3.90,'Crema','Sin observaciones'),
(5,6,'Pelusa','2022-09-08','Hembra',4.10,'Negro','Control dental'),
(6,2,'Toby','2020-02-18','Macho',7.00,'Caramelo','Actividad física moderada'),
(7,7,'Piolín','2024-04-01','Macho',0.15,'Amarillo','Revisión general'),
(8,8,'Copito','2023-06-21','Macho',1.30,'Blanco','Revisar alimentación');

INSERT INTO empleado (documento,nombres,apellidos,cargo,telefono,correo,fecha_contratacion) VALUES
('41000001','Andrea','Paredes León','Administrador','944200001','andrea.paredes@vet.local','2024-01-10'),
('41000002','Luis','Herrera Díaz','Veterinario','944200002','luis.herrera@vet.local','2024-02-15'),
('41000003','Carolina','Salas Vega','Veterinario','944200003','carolina.salas@vet.local','2024-03-01'),
('41000004','Diego','Núñez Castro','Recepcionista','944200004','diego.nunez@vet.local','2024-04-05'),
('41000005','Paola','Ríos Campos','Vendedor','944200005','paola.rios@vet.local','2024-05-20');

-- Contraseñas de prueba: Admin123*, Vet123*, Vet456*, Recepcion123*, Venta123*
INSERT INTO usuario (id_empleado,nombre_usuario,password_hash,rol) VALUES
(1,'admin',SHA2('Admin123*',256),'Administrador'),
(2,'vet01',SHA2('Vet123*',256),'Veterinario'),
(3,'vet02',SHA2('Vet456*',256),'Veterinario'),
(4,'recepcion',SHA2('Recepcion123*',256),'Recepcion'),
(5,'vendedor',SHA2('Venta123*',256),'Vendedor');

INSERT INTO cita (id_mascota,id_empleado,fecha_hora,motivo,estado,observaciones) VALUES
(1,2,'2026-10-20 09:00:00','Control general','Atendida','Control de rutina'),
(2,2,'2026-10-21 10:30:00','Vacunación','Atendida','Revisar carnet'),
(3,3,'2026-10-29 11:00:00','Consulta por inapetencia','Confirmada','Ayuno no requerido'),
(4,2,'2026-10-30 15:00:00','Revisión dermatológica','Programada','Posible alergia'),
(5,3,'2026-10-31 09:30:00','Control de peso','Programada',NULL),
(6,2,'2026-11-02 12:00:00','Limpieza dental','Cancelada','Reprogramar');

INSERT INTO consulta (id_cita,id_mascota,id_empleado,fecha_consulta,sintomas,diagnostico,peso,observaciones) VALUES
(1,1,2,'2026-10-20 09:10:00','Sin síntomas relevantes','Paciente clínicamente estable',24.50,'Control anual'),
(2,2,2,'2026-10-21 10:40:00','Sin síntomas','Apta para evaluación de vacunación',6.20,'Verificar esquema vacunal');

INSERT INTO tratamiento (nombre,descripcion) VALUES
('Control preventivo','Evaluación general y seguimiento'),
('Higiene dental','Limpieza y cuidado oral'),
('Manejo dermatológico','Cuidados para afecciones de piel'),
('Plan nutricional','Recomendaciones de alimentación');

INSERT INTO detalle_tratamiento (id_consulta,id_tratamiento,indicaciones,duracion) VALUES
(1,1,'Programar control periódico según indicación veterinaria','12 meses'),
(2,1,'Mantener actualizado el calendario de vacunación','Según calendario veterinario');

INSERT INTO medicamento (nombre,presentacion,concentracion,stock,precio) VALUES
('Antiséptico veterinario','Frasco 100 ml','Uso tópico',25,18.50),
('Antiparasitario veterinario','Tableta','Según presentación',40,12.00),
('Solución ótica veterinaria','Frasco  gotas','Uso ótico',15,26.90),
('Suplemento veterinario','Frasco 120 ml','Según etiqueta',18,32.00);

INSERT INTO receta (id_consulta,fecha_emision,observaciones) VALUES
(1,'2026-10-20','Ejemplo académico; seguir siempre la prescripción veterinaria');

INSERT INTO detalle_receta (id_receta,id_medicamento,dosis,frecuencia,duracion,cantidad) VALUES
(1,1,'Según evaluación profesional','Según indicación','Según indicación',1),
(1,4,'Según etiqueta y criterio veterinario','Según indicación','Según indicación',1);

INSERT INTO producto (nombre,categoria,stock,stock_minimo,precio_venta) VALUES
('Alimento canino adulto 3 kg','Alimentos',30,5,45.90),
('Alimento felino adulto 1.5 kg','Alimentos',24,5,32.50),
('Collar ajustable','Accesorios',20,4,15.00),
('Correa mediana','Accesorios',18,4,22.00),
('Juguete de cuerda','Juguetes',25,5,12.90),
('Arena sanitaria 5 kg','Higiene',16,4,28.00),
('Shampoo para mascotas','Higiene',12,3,19.90),
('Plato de acero','Accesorios',14,3,17.50);

INSERT INTO venta (id_cliente,id_empleado,fecha,subtotal,impuesto,total,estado) VALUES
(1,5,'2026-10-20 10:00:00',45.90,0.00,45.90,'Pagada'),
(2,5,'2026-10-21 11:15:00',45.40,0.00,45.40,'Pagada'),
(3,5,'2026-10-22 16:20:00',32.50,0.00,32.50,'Pendiente');

INSERT INTO detalle_venta (id_venta,id_producto,cantidad,precio_unitario,descuento) VALUES
(1,1,1,45.90,0.00),
(2,3,1,15.00,0.00),
(2,4,1,22.00,0.00),
(2,5,1,12.90,4.50),
(3,2,1,32.50,0.00);

INSERT INTO pago (id_venta,fecha_pago,monto,metodo_pago,referencia) VALUES
(1,'2026-10-20 10:05:00',45.90,'Yape','DEMO-PAGO-001'),
(2,'2026-10-21 11:20:00',45.40,'Efectivo','DEMO-PAGO-002');

-- ===================== CONSULTAS DE VERIFICACIÓN =====================
SELECT * FROM usuario;
SELECT m.id_mascota, m.nombre AS mascota, c.nombres, c.apellidos
FROM mascota m
JOIN cliente c ON c.id_cliente = m.id_cliente
ORDER BY m.id_mascota;
SELECT v.id_venta, v.fecha, v.total, v.estado, c.nombres, c.apellidos
FROM venta v JOIN cliente c ON c.id_cliente = v.id_cliente
ORDER BY v.id_venta;
