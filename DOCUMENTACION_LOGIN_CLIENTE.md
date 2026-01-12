# 📚 Documentación: Sistema de Login de Clientes

## 🎯 Resumen Ejecutivo

El sistema implementa **autenticación dual** para dos tipos de usuarios:
1. **Usuarios del sistema** (Admin, Dueño, Profesional) → Login tradicional con email
2. **Clientes** (personas que reservan turnos) → Login con teléfono

---

## 🏗️ Arquitectura de Autenticación

### **Frontend (Vue.js + TypeScript)**

#### **Tecnologías Usadas**
- **Tailwind CSS v3**: Framework CSS utility-first para estilos
  - ¿Por qué ahora?: Se instaló porque los formularios de cliente son públicos y necesitan verse profesionales sin tener acceso al sistema de estilos interno de admin
  - Instalado en: `frontend/package.json` con PostCSS y Autoprefixer
  - Configurado en: `tailwind.config.js` y `src/style.css`

- **Vue Router**: Navegación entre vistas
- **Pinia Stores**: Gestión de estado (almacena datos del cliente autenticado)
- **Axios**: Cliente HTTP para comunicarse con el backend

#### **Rutas Públicas**
```typescript
// router/index.ts
{
  path: '/reservar/:empresaSlug',
  name: 'Reservar',
  component: ReservarView
},
{
  path: '/empresa/:empresaSlug/registro-cliente',
  name: 'RegistroCliente',
  component: RegistroClienteView
},
{
  path: '/empresa/:empresaSlug/login-cliente',
  name: 'LoginCliente',
  component: LoginClienteView
},
{
  path: '/empresa/:empresaSlug/mis-turnos',
  name: 'MisTurnos',
  component: MisTurnosView,
  meta: { requiresAuth: true } // Protegida
}
```

#### **Store de Cliente** (`stores/cliente.ts`)
```typescript
interface ClienteState {
  cliente: ClienteAutenticado | null
  isAuthenticated: boolean
}

// Métodos principales:
- setCliente(data): Guarda cliente autenticado
- logout(): Limpia sesión
- isAuthenticated: Computed que verifica si hay cliente
```

#### **API Client** (`services/api.ts`)
```typescript
// Endpoints principales
loginCliente(empresaSlug: string, credentials: LoginClienteRequest)
  → POST /api/publico/empresa/{empresaSlug}/login-cliente

registrarCliente(empresaSlug: string, data: RegistroClienteRequest)
  → POST /api/publico/empresa/{empresaSlug}/registro-cliente

obtenerMisTurnos(empresaSlug: string)
  → GET /api/cliente/mis-turnos

// Interceptor de Axios
- Excluye CSRF para /api/publico/**
- Agrega CSRF token automáticamente para endpoints protegidos
```

---

### **Backend (Spring Boot + Spring Security)**

#### **Endpoints Públicos** (`ControladorPublico.java`)

##### **1. Registro de Cliente**
```java
POST /api/publico/empresa/{empresaSlug}/registro-cliente

Request Body:
{
  "telefono": "+54 9 11 1234-5678",
  "email": "cliente@example.com",
  "contrasena": "Pass1234"
}

Flujo:
1. Busca cliente existente por teléfono
2. Si no existe → crea nuevo cliente
3. Si existe → actualiza email y contraseña
4. Marca tieneUsuario = true
5. Vincula turnos anteriores sin cuenta
6. Retorna ClienteAutenticadoResponse
```

##### **2. Login de Cliente**
```java
POST /api/publico/empresa/{empresaSlug}/login-cliente

Request Body:
{
  "telefono": "+54 9 11 1234-5678",
  "contrasena": "Pass1234"
}

Flujo:
1. Construye username: "cliente:{empresaSlug}:{telefono}"
2. Crea UsernamePasswordAuthenticationToken
3. Autentica con AuthenticationManager
4. Crea contexto de Spring Security
5. Guarda sesión en HttpSession
6. Retorna ClienteAutenticadoResponse
```

#### **Endpoints Protegidos** (`ControladorCliente.java`)

```java
GET /api/cliente/mis-turnos
→ Requiere rol "CLIENTE" autenticado

GET /api/cliente/perfil
→ Requiere rol "CLIENTE" autenticado
```

---

## 🔐 Sistema de Autenticación Dual

### **Formato de Username**

| Tipo Usuario | Username Format | Ejemplo |
|-------------|----------------|---------|
| Usuario Sistema | `email` | `admin@empresa.com` |
| Cliente | `cliente:{empresaSlug}:{telefono}` | `cliente:peluqueria-jaja:+5491112345678` |

### **UserDetailsService Unificado**

```java
@Service
@Primary
public class ServicioDetallesUnificado implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        // Decisión basada en prefijo
        if (username.startsWith("cliente:")) {
            return servicioDetallesCliente.loadUserByUsername(username);
        }
        return servicioDetallesUsuario.loadUserByUsername(username);
    }
}
```

**¿Por qué NO implementan UserDetailsService los servicios auxiliares?**
- Para evitar **recursión infinita** (StackOverflowError)
- Solo el `ServicioDetallesUnificado` implementa la interfaz con `@Primary`
- Los demás son `@Component` simples con métodos públicos

---

## 🎭 Roles y Permisos

### **Rol: CLIENTE**

#### **Permisos del Cliente**
```java
@PreAuthorize("hasAuthority('CLIENTE')")
```

✅ **Lo que PUEDE hacer:**
- Ver sus propios turnos (`GET /api/cliente/mis-turnos`)
- Ver su perfil (`GET /api/cliente/perfil`)
- Reservar nuevos turnos (como público, no requiere login)
- Acceder a vistas públicas de empresas

❌ **Lo que NO puede hacer:**
- Acceder al panel de administración (`/admin/*`)
- Gestionar profesionales, servicios, configuración
- Ver turnos de otros clientes
- Modificar datos de la empresa

#### **Comparación de Roles**

| Función | SUPER_ADMIN | DUENO | PROFESIONAL | CLIENTE |
|---------|------------|-------|-------------|---------|
| Gestionar empresas | ✅ | ❌ | ❌ | ❌ |
| Gestionar usuarios | ✅ | ✅ | ❌ | ❌ |
| Gestionar servicios | ✅ | ✅ | ❌ | ❌ |
| Gestionar turnos todos | ✅ | ✅ | ❌ | ❌ |
| Ver sus propios turnos | ✅ | ✅ | ✅ | ✅ |
| Reservar turnos | ✅ | ✅ | ✅ | ✅ |
| Acceder panel admin | ✅ | ✅ | ✅ | ❌ |

---

## 🔄 Flujo de Registro y Vinculación de Turnos

### **Caso 1: Cliente Nuevo (sin reservas previas)**
```
1. Usuario va a /empresa/peluqueria-jaja/registro-cliente
2. Ingresa: teléfono, email, contraseña
3. Backend crea Cliente con tieneUsuario=true
4. No hay turnos previos para vincular
5. Redirige a /empresa/peluqueria-jaja/mis-turnos (vacío)
```

### **Caso 2: Cliente con Reservas Previas (sin cuenta)**
```
1. Cliente reservó turno como invitado (sin cuenta)
   → Cliente creado con tieneUsuario=false
   
2. Cliente crea cuenta con MISMO teléfono
   → Backend encuentra cliente existente
   → Actualiza: tieneUsuario=true, email, contrasena
   
3. Vinculación automática de turnos:
   - Busca todos los turnos con ese teléfono
   - Los asocia al cliente recién registrado
   
4. Redirige a /mis-turnos (muestra historial completo)
```

### **Caso 3: Registro desde Reserva Exitosa**
```
1. Cliente reserva turno sin cuenta
2. Modal muestra: "¡Turno confirmado!"
3. Botón: "Crear cuenta para ver historial"
4. Redirige a /registro-cliente?telefono=+549...
5. Teléfono pre-cargado (readonly)
6. Al registrarse, vincula todos sus turnos anteriores
```

### **Caso 4: Registro Directo (sin reserva)**
```
1. Cliente ve botón "Registrarse" en header
2. Va a /empresa/peluqueria-jaja/registro-cliente
3. Teléfono es EDITABLE (no viene pre-cargado)
4. Si usa mismo teléfono de reservas anteriores:
   → Turnos se vinculan automáticamente
5. Si usa teléfono nuevo:
   → No se vinculan turnos (empezará con historial vacío)
```

---

## 🛡️ Seguridad

### **CSRF Protection**
- **Habilitado** para endpoints del sistema (`/api/auth/**`)
- **Deshabilitado** para endpoints públicos (`/api/publico/**`)
- Frontend lee cookie `XSRF-TOKEN` y la envía como header

### **Sesiones Stateful**
```java
sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1) // Solo 1 sesión activa por usuario
)
```

### **Contraseñas**
- **Encriptación**: BCrypt (`BCryptPasswordEncoder`)
- **Validación Frontend**: Mínimo 8 caracteres, mayúscula, minúscula, número
- **Validación Backend**: Pattern regex en DTO

### **Autorización**
```java
// Configuración Spring Security
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/publico/**").permitAll()
    .requestMatchers("/api/cliente/**").hasAuthority("CLIENTE")
    .requestMatchers("/api/admin/**").hasAnyAuthority("SUPER_ADMIN", "DUENO")
    .anyRequest().authenticated()
)
```

---

## 🎨 Diseño y UX

### **¿Por qué Tailwind CSS?**

#### **Antes (sin Tailwind)**
- Estilos globales en `App.vue` y componentes
- Difícil mantener consistencia
- Estilos mezclados con lógica de negocio
- No optimizado para producción

#### **Ahora (con Tailwind v3)**
```vue
<!-- Ejemplo: Botón con estados hover, focus, disabled -->
<button class="
  w-full 
  py-2 px-4 
  bg-blue-600 hover:bg-blue-700 
  text-white font-medium 
  rounded-md shadow-sm
  focus:ring-2 focus:ring-blue-500
  disabled:bg-gray-400 disabled:cursor-not-allowed
">
  Iniciar Sesión
</button>
```

✅ **Ventajas**:
- Clases utility-first (rapid prototyping)
- Purging automático (solo CSS usado en producción)
- Responsive design fácil (`sm:`, `md:`, `lg:`)
- Estados interactivos simples (`hover:`, `focus:`, `disabled:`)
- Mantenimiento más fácil (todo en el template)

#### **Decisión Arquitectónica**
- **Vistas públicas** (Login, Registro, Reservar) → Tailwind CSS
- **Panel admin** → Estilos existentes (Bootstrap o custom)
- **Componentes compartidos** → Gradualmente migrar a Tailwind

---

## 📁 Estructura de Archivos

```
frontend/src/
├── views/
│   ├── ReservarView.vue          # Vista pública reserva turnos
│   ├── RegistroClienteView.vue   # Formulario registro (Tailwind)
│   ├── LoginClienteView.vue      # Formulario login (Tailwind)
│   └── MisTurnosView.vue         # Historial del cliente (protegido)
│
├── stores/
│   └── cliente.ts                # Pinia store (estado del cliente)
│
├── services/
│   ├── api.ts                    # Axios client con interceptors
│   └── publico.ts                # Servicios públicos (reservas)
│
├── router/
│   └── index.ts                  # Definición de rutas + guards
│
├── style.css                     # Tailwind directives (@tailwind base)
└── main.ts                       # Punto de entrada

backend/src/main/java/com/example/sitema_de_turnos/
├── controlador/
│   ├── ControladorPublico.java   # Login, registro, reservas
│   └── ControladorCliente.java   # Endpoints protegidos CLIENTE
│
├── servicio/
│   ├── ServicioDetallesUnificado.java      # @Primary UserDetailsService
│   ├── ServicioDetallesCliente.java        # Auxiliar (no implementa interfaz)
│   ├── ServicioDetallesUsuario.java        # Auxiliar (no implementa interfaz)
│   └── ServicioAutenticacionCliente.java   # Lógica registro/login
│
├── configuracion/
│   └── ConfiguracionSeguridad.java         # Spring Security config
│
├── dto/
│   ├── LoginClienteRequest.java
│   ├── RegistroClienteRequest.java
│   └── ClienteAutenticadoResponse.java
│
└── modelo/
    └── Cliente.java                        # Entity JPA
```

---

## 🚀 Flujo Completo: Desde Registro hasta Ver Turnos

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Usuario Anónimo visita peluqueria-jaja                  │
│    → http://localhost:5173/reservar/peluqueria-jaja        │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Reserva un Turno (sin cuenta)                           │
│    - Selecciona servicio, profesional, fecha, hora         │
│    - Ingresa: nombre, teléfono, email                      │
│    → POST /api/publico/empresa/peluqueria-jaja/turnos     │
│    → Cliente creado: tieneUsuario=false                    │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Modal: "¡Turno Confirmado!"                             │
│    Botón: "Crear cuenta para ver historial"               │
│    Clic → /empresa/peluqueria-jaja/registro-cliente       │
│           ?telefono=+5491112345678                         │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Formulario de Registro (Tailwind)                       │
│    - Teléfono: +5491112345678 (readonly, pre-cargado)     │
│    - Email: cliente@example.com (editable)                 │
│    - Contraseña: Pass1234 (mín 8, mayús+minús+núm)        │
│    → POST /api/publico/empresa/peluqueria-jaja/           │
│              registro-cliente                              │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. Backend: Registro y Vinculación                         │
│    a) Busca cliente por teléfono (existe del paso 2)      │
│    b) Actualiza: tieneUsuario=true, email, contrasena     │
│    c) Vincula turnos anteriores con ese teléfono          │
│    d) Retorna ClienteAutenticadoResponse                   │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. Frontend: Guardar en Store y Redirigir                 │
│    clienteStore.setCliente(response.data.datos)            │
│    router.push('/empresa/peluqueria-jaja/mis-turnos')     │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Vista "Mis Turnos" (Protegida)                         │
│    → GET /api/cliente/mis-turnos                           │
│    → Muestra historial completo (incluye turno del paso 2)│
│    → Header muestra: "👤 Juan" + "Cerrar Sesión"          │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. Próximo Login (desde otro dispositivo)                 │
│    → /empresa/peluqueria-jaja/login-cliente               │
│    → Ingresa teléfono + contraseña                         │
│    → Backend construye username:                           │
│       "cliente:peluqueria-jaja:+5491112345678"            │
│    → AuthenticationManager.authenticate()                  │
│    → ServicioDetallesUnificado detecta prefijo "cliente:" │
│    → Delega a ServicioDetallesCliente                     │
│    → Autentica y crea sesión                               │
│    → Redirige a /mis-turnos                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🐛 Problemas Resueltos

### **1. StackOverflowError en Login**
**Causa**: `ServicioDetallesCliente` y `ServicioDetallesUsuario` implementaban `UserDetailsService`, causando recursión infinita.

**Solución**: Solo `ServicioDetallesUnificado` implementa la interfaz con `@Primary`. Los demás son componentes auxiliares.

### **2. Formulario de Registro sin Estilos**
**Causa**: Tailwind CSS no estaba instalado.

**Solución**: Instalado Tailwind v3 con PostCSS y configurado en `tailwind.config.js`.

### **3. Login Devolvía HTML en lugar de JSON**
**Causa**: Spring Security redirigía al formulario de login por defecto.

**Solución**: Exclusión de CSRF para `/api/publico/**` y handlers personalizados.

### **4. Teléfono Readonly Bloqueaba Registro Directo**
**Causa**: Campo siempre readonly, asumiendo que viene de reserva.

**Solución**: Readonly condicional solo si viene `?telefono=` en query params.

---

## 📝 Preguntas Frecuentes

### **¿Por qué no usar JWT?**
Sesiones stateful permiten invalidar sesiones instantáneamente (ej: cambio de contraseña).

### **¿Por qué username complejo para clientes?**
Permite diferenciar clientes de distintas empresas con mismo teléfono en BD compartida.

### **¿Qué pasa si cambio el teléfono de un cliente?**
Los turnos ya vinculados permanecen, pero reservas futuras con el nuevo teléfono no se vincularán automáticamente.

### **¿Puedo usar el mismo email en distintas empresas?**
Sí, porque el identificador único es `{empresaSlug}:{telefono}`, no el email.

---

## 🔧 Mantenimiento

### **Agregar Nuevo Endpoint Protegido para Cliente**
```java
@RestController
@RequestMapping("/api/cliente")
public class ControladorCliente {
    
    @GetMapping("/nuevo-endpoint")
    @PreAuthorize("hasAuthority('CLIENTE')")
    public ResponseEntity<?> nuevoEndpoint(Authentication auth) {
        // Obtener cliente actual
        String username = auth.getName(); // "cliente:empresaSlug:telefono"
        // ... lógica
    }
}
```

### **Agregar Campo al Perfil del Cliente**
1. Agregar campo en `Cliente.java` (entity)
2. Actualizar `ClienteAutenticadoResponse.java` (DTO)
3. Actualizar `RegistroClienteRequest.java` si es editable
4. Actualizar formularios Vue con Tailwind
5. Ejecutar migración de BD

---

## 📊 Métricas y Logs

### **Logs Importantes**
```java
// ServicioDetallesUnificado
log.debug("Intentando autenticar usuario: {}", username);
log.debug("Delegando a ServicioDetallesCliente");

// ControladorPublico
log.info("Login exitoso - Cliente: {} - Empresa: {}", telefono, empresaSlug);
log.warn("Error en login de cliente: {}", e.getMessage());
```

### **Monitoreo**
- Revisar logs de autenticación fallida
- Sesiones activas en `SessionRegistry`
- Tiempos de respuesta de endpoints públicos

---

## 🎓 Conclusión

El sistema de login de clientes está diseñado para:
1. **Simplicidad**: Un solo teléfono + contraseña
2. **Vinculación automática**: Historial completo al registrarse
3. **Seguridad**: Sesiones stateful, CSRF, BCrypt
4. **UX profesional**: Tailwind CSS para vistas públicas
5. **Escalabilidad**: Multi-empresa con username único

**Última actualización**: 11 de enero de 2026
