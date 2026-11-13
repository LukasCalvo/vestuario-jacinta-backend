# Jacinta Backend

Spring Boot, Java 25 y H2. Estructura: controller, service, repository, model y dto.

Ejecutar desde esta carpeta:

```bash
./mvnw spring-boot:run
```

La BD se guarda en `data/jacinta.mv.db`. El esquema está en
`src/main/resources/db/schema.sql` y se crea al iniciar sin borrar datos.

- Tablas: usuario, categoria, producto, boleta y detalle_boleta.
- Stock por producto; tallas y colores en JSON.
- Compras con cuenta o como invitado. Se conserva el historial.
- Precios netos en CLP, IVA del 19 % y envío gratis.
- Usuarios y productos se desactivan con `activo`.

Conexión configurable con `DB_URL`, `DB_USER` y `DB_PASSWORD`.
