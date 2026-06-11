# Frontend Vue para microservicios Spring Boot

Este proyecto consume dos microservicios:

- `ms-productos`: `http://localhost:8085/productos`
- `ms-pedidos`: `http://localhost:8086/api/pedidos`
- `system-server / Eureka`: `http://localhost:8761`

## Instalación

```bash
npm install
npm run dev
```

## Variables de entorno

Crear un archivo `.env` en la raíz del proyecto:

```env
VITE_PRODUCTOS_API_URL=http://localhost:8085/productos
VITE_PEDIDOS_API_URL=http://localhost:8086/api/pedidos
VITE_EUREKA_URL=http://localhost:8761
```

## Requisito importante en Spring Boot

Si el navegador marca error de CORS, agregar en cada controlador:

```java
@CrossOrigin(origins = "http://localhost:5174")
```

Ejemplo:

```java
@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "http://localhost:5174")
public class ProductoController {
}
```

## Orden de ejecución recomendado

1. Levantar `system-server` en el puerto `8761`.
2. Levantar `ms-productos` en el puerto `8085`.
3. Levantar `ms-pedidos` en el puerto `8086`.
4. Ejecutar este frontend con `npm run dev`.
