# POS para taquería

Este es mi primer proyecto completo. Es un sistema para una taquería familiar: la mesera toma el pedido en su celular y le aparece al instante a la cocina en una pantalla, todo por la red local, sin internet.

Este repositorio es el servidor. La app de la mesera está aparte, en `mesera_app`.

## Qué hace

- La mesera arma el pedido por mesa y le puede poner notas, como "sin cebolla".
- La cocina ve los pedidos en tarjetas y los va moviendo de "Por hacer" a "En preparación" a "Listo". Suena un pitido cuando llega uno nuevo.
- Se cobra por mesa y la app calcula el cambio.
- Se pueden agregar y editar productos desde la app, y marcarlos como agotados.
- Saca un reporte del día (ventas, lo más vendido y cada pedido) y lo guarda en un archivo de texto dentro de una carpeta por mes.

Algo que me importó: la app nunca manda precios, solo el producto y la cantidad. El servidor busca el precio real y calcula el total, así nadie puede cambiarlo desde el celular.

## Cómo funciona

```
App de la mesera ──► Servidor (Spring Boot) ──► PostgreSQL
                          │
                          └──► Pantalla de cocina (WebSocket)
```

## Con qué lo hice

Java, Spring Boot, PostgreSQL, WebSocket y JUnit con Mockito para probar el cálculo de los totales.

## Cómo correrlo

1. Instalar Java 17 o más y PostgreSQL.
2. Crear una base de datos llamada `taqueria_pos`.
3. Copiar `src/main/resources/application.properties.example` como `application.properties` y poner mi usuario y contraseña de PostgreSQL.
4. Ejecutar `PosApplication` desde el IDE.
5. Abrir `http://localhost:8080/cocina.html` para ver la pantalla de cocina.

Las tablas se crean solas.

Para usarlo desde un celular, los dos tienen que estar en la misma red wifi y el firewall tiene que dejar pasar el puerto 8080.

## Rutas principales

| Método | Ruta | Para qué sirve |
|---|---|---|
| GET / POST | `/api/productos` | Ver o crear productos |
| PUT | `/api/productos/{id}` | Editar un producto |
| GET / POST | `/api/categorias` | Ver o crear categorías |
| GET / POST | `/api/mesas` | Ver o crear mesas |
| POST | `/api/ordenes` | Enviar un pedido |
| GET | `/api/ordenes/activas` | Pedidos sin cobrar |
| PUT | `/api/ordenes/{id}/estado` | Pasar el pedido al siguiente estado |
| GET | `/api/reportes/diario` | Reporte de ventas del día |
| POST | `/api/reportes/diario/archivo` | Guardar el reporte en un archivo |

## Cosas que le falta o que sé que no son perfectas

- No tiene inicio de sesión. Lo pensé para una red local de confianza, así que cualquiera que llegue al servidor puede usarlo.
- El reporte cuenta los pedidos por el día en que se crearon, no por el día en que se cobraron.
- Un producto que ya se vendió no se puede borrar, solo marcarlo como agotado.
- Todavía me falta respaldar la base de datos automáticamente.

## Lo que aprendí

Spring Boot y JPA, cómo hacer una API REST con validaciones, WebSocket para avisar en tiempo real, escribir pruebas con Mockito, y manejar Git y GitHub .
