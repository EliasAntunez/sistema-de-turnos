# 🏗️ Análisis Arquitectónico: Flujo de Cliente en Sistema de Turnos Multi-Empresa

**Fecha**: 11 de enero de 2026  
**Rol**: Arquitecto de Software Senior especializado en SaaS Multi-Tenant  
**Alcance**: Flujo completo de cliente invitado y registrado

---

## 📊 Executive Summary

### ✅ Fortalezas del Diseño Actual
1. **Separación clara** entre cliente invitado y registrado
2. **Aislamiento por empresa** mediante slug en URL
3. **Flujo de conversión** invitado→registrado bien pensado
4. **Mobile-first** con Tailwind CSS y sticky header
5. **Seguridad adecuada** con username compuesto y BCrypt

### ⚠️ Riesgos Críticos Identificados
1. **Inconsistencia en validación de teléfono registrado**
2. **Falta de persistencia de sesión** (solo memoria)
3. **Ambigüedad UX**: Cliente registrado debe iniciar sesión manualmente
4. **Posible conflicto de datos**: Invitado puede reservar con teléfono ya registrado
5. **Navegación poco clara**: No hay indicador de si debes loguearte o no

### 🎯 Nivel de Madurez: **7/10**
- Funcionalidad core: ✅ Completa
- Seguridad: ✅ Básica correcta, ⚠️ Mejoras recomendadas
- UX: ⚠️ Funcional pero confusa en escenarios edge
- Escalabilidad: ✅ Arquitectura preparada

---

## 🔍 Análisis Detallado por Flujo

### 1. Flujo de Cliente Invitado (Sin Cuenta)

#### 1.1 Acceso Inicial
```
Usuario → https://app.com/reservar/peluqueria-jaja
         → ReservarView.vue carga
         → clienteStore.isAuthenticated === false
         → Header muestra: "Ingresar | Registrarse"
```

**✅ Correcto**:
- URL pública sin autenticación
- No requiere login para navegar
- Carga info de empresa sin credenciales

#### 1.2 Proceso de Reserva
```
Paso 1: Selecciona servicio → OK (sin auth)
Paso 2: Selecciona profesional → OK (sin auth)
Paso 3: Selecciona fecha → OK (sin auth)
Paso 4: Selecciona hora → OK (sin auth)
Paso 5: Confirma datos:
  - Si NO logueado → Formulario: nombre, teléfono*, email
  - Si SÍ logueado → Usa datos del store
```

**✅ Correcto**:
- Permite reservar sin cuenta (principio KISS)
- Pide mínimo de datos (nombre + teléfono)
- Email opcional (reduce fricción)

#### 1.3 Creación del Cliente en BD
```java
// ServicioTurno.crearTurnoPublico()
Cliente cliente = repositorioCliente
    .findByEmpresaAndTelefonoAndActivoTrue(empresa, telefono)
    .orElseGet(() -> {
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setEmpresa(empresa);
        nuevoCliente.setNombre(nombreCliente);
        nuevoCliente.setTelefono(telefonoCliente);
        nuevoCliente.setEmail(emailCliente);
        nuevoCliente.setTieneUsuario(false); // ⚠️ Crítico
        nuevoCliente.setValidadoPorSms(false);
        nuevoCliente.setActivo(true);
        return repositorioCliente.save(nuevoCliente);
    });
```

**✅ Correcto**:
- `tieneUsuario=false` diferencia invitado de registrado
- Busca cliente existente por teléfono antes de crear
- Vincula a empresa específica

**⚠️ RIESGO CRÍTICO #1**: Validación SMS
```
Problema: validadoPorSms=false pero el sistema permite reservar
Impacto: Posible spam, reservas falsas, suplantación de identidad
Estado actual: NO implementada la validación SMS real
```

**⚠️ RIESGO CRÍTICO #2**: Conflicto de Identidad
```sql
Escenario:
1. Juan reserva como invitado: tel=+5491112345678, tieneUsuario=false
2. Juan crea cuenta: tel=+5491112345678, tieneUsuario=true
3. Pedro (malicioso) reserva como invitado: tel=+5491112345678
   
¿Qué pasa?
→ findByEmpresaAndTelefonoAndActivoTrue() encuentra a Juan (registrado)
→ Le asocia el turno a Juan
→ Pedro nunca recibe confirmación
→ Juan ve un turno que no reservó

CAUSA: El query no filtra por tieneUsuario
```

#### 1.4 Modal Post-Reserva
```vue
<div class="cta-cuenta">
  <h3>¿Querés gestionar tus turnos más fácil?</h3>
  <button @click="irCrearCuenta">Crear mi cuenta gratis</button>
</div>
```

**✅ Correcto**:
- Incentiva conversión después del turno confirmado
- Momento ideal (post-commitment)
- Pre-carga teléfono en query param

**⚠️ PROBLEMA UX #1**: Confusión sobre Requisito
```
Mensaje actual: "Crear mi cuenta gratis"
Expectativa usuario: Puedo crear cuenta en cualquier momento

Realidad backend:
if (!cliente.getTieneUsuario()) { ... }
else throw "Ya tiene cuenta"

Si cliente fue creado como invitado → OK ✅
Si cliente nunca reservó → FALLA ❌
```

---

### 2. Flujo de Registro de Cliente

#### 2.1 Acceso al Registro
```
Opción A: Desde modal post-reserva
  /empresa/peluqueria-jaja/registro-cliente?telefono=+549...
  → Teléfono readonly ✅

Opción B: Desde header "Registrarse"
  /empresa/peluqueria-jaja/registro-cliente
  → Teléfono editable ✅

Opción C: Desde login "¿No tienes cuenta? Regístrate"
  /empresa/peluqueria-jaja/registro-cliente
  → Teléfono editable ✅
```

**✅ Correcto**: Múltiples puntos de entrada

#### 2.2 Validación de Pre-Requisito
```java
// ServicioAutenticacionCliente.registrarCliente()
Cliente cliente = repositorioCliente
    .findByEmpresaAndTelefonoAndActivoTrue(empresa, request.getTelefono())
    .orElseThrow(() -> new ValidacionException(
        "No se encontró un cliente con ese teléfono. 
         Debe reservar al menos un turno antes de registrarse."
    ));

if (cliente.getTieneUsuario()) {
    throw new ValidacionException("Este cliente ya tiene una cuenta");
}
```

**✅ Correcto**: Lógica de negocio clara
**⚠️ PROBLEMA UX #2**: Mensaje de error confuso

```
Escenario: Usuario nuevo va directo a "Registrarse"
Resultado: "Debe reservar al menos un turno antes"
Sentimiento: ¿Por qué? ¿Es obligatorio reservar para tener cuenta?

Comparación con competidores:
- Booking.com: Puedes crear cuenta sin reservar ❌
- Airbnb: Puedes crear cuenta sin reservar ❌
- OpenTable: Puedes crear cuenta sin reservar ❌

¿Es esto realmente necesario?
```

#### 2.3 Actualización del Cliente
```java
cliente.setEmail(request.getEmail());
cliente.setContrasena(passwordEncoder.encode(request.getContrasena()));
cliente.setTieneUsuario(true); // ⚠️ Cambia el estado
cliente = repositorioCliente.save(cliente);
```

**✅ Correcto**: No crea duplicado, actualiza existente

**⚠️ PROBLEMA ARQUITECTÓNICO #1**: Identidad Dual
```
Antes del registro:
Cliente(id=1, telefono=+549..., tieneUsuario=false) ← Turno 1, 2, 3

Después del registro:
Cliente(id=1, telefono=+549..., tieneUsuario=true) ← Turno 1, 2, 3

¿Qué pasa si alguien más reserva con ese teléfono?
→ findByEmpresaAndTelefonoAndActivoTrue() sigue encontrando id=1
→ Le asigna turnos a la cuenta registrada
→ Posible suplantación ⚠️
```

---

### 3. Flujo de Cliente Registrado

#### 3.1 Login
```
URL: /empresa/peluqueria-jaja/login-cliente
Input: Teléfono + Contraseña
Backend: POST /api/publico/empresa/peluqueria-jaja/login-cliente
Username: "cliente:peluqueria-jaja:+5491112345678"
```

**✅ Correcto**:
- Username compuesto evita colisiones multi-empresa
- Session-based auth (JSESSIONID cookie)
- AuthenticationManager con UserDetailsService unificado

#### 3.2 Post-Login
```typescript
// LoginClienteView.vue
if (response.data.exito) {
  clienteStore.setCliente(response.data.datos) // ⚠️ Solo memoria
  router.push(`/empresa/${empresaSlug}/mis-turnos`)
}
```

**⚠️ PROBLEMA CRÍTICO #3**: Persistencia de Sesión
```javascript
// cliente.ts
const cliente = ref<ClienteInfo | null>(null) // ⚠️ Se pierde en F5

¿Qué pasa si el usuario recarga la página?
→ clienteStore.cliente === null
→ isAuthenticated === false
→ Header muestra "Ingresar | Registrarse" (aunque tenga sesión activa)
→ Si va a /mis-turnos, el guard no detecta auth
```

**Solución esperada**:
```typescript
// Persistir en localStorage o sessionStorage
const cliente = ref<ClienteInfo | null>(
  JSON.parse(localStorage.getItem('cliente') || 'null')
)

// O mejor: endpoint GET /api/cliente/perfil para recuperar sesión
```

#### 3.3 Acceso a Vista Protegida
```typescript
// router/index.ts
{
  path: '/empresa/:empresaSlug/mis-turnos',
  name: 'MisTurnos',
  meta: { requiresAuth: true }
}
```

**⚠️ PROBLEMA CRÍTICO #4**: Guard de Navegación Faltante
```typescript
// No hay beforeEach que valide requiresAuth
router.beforeEach((to, from, next) => {
  // ¿Dónde está esto? ❌
  if (to.meta.requiresAuth && !clienteStore.isAuthenticated) {
    next(`/empresa/${to.params.empresaSlug}/login-cliente`)
  } else {
    next()
  }
})
```

**Estado actual**: Si escribes la URL directamente, el componente carga y LUEGO falla al hacer fetch (401)

---

### 4. Escenarios Críticos (Edge Cases)

#### 4.1 Cliente Registrado entra a /reservar/{slug}
```
Estado: Sesión activa (JSESSIONID válida)
Navegación: /reservar/peluqueria-jaja
Comportamiento actual:
  1. ReservarView carga
  2. clienteStore.isAuthenticated === false (perdió estado en refresh)
  3. Header muestra "Ingresar | Registrarse"
  4. Paso 5: Pide datos de invitado (aunque tenga sesión!)

Comportamiento esperado:
  1. ReservarView carga
  2. Detecta JSESSIONID → fetch /api/cliente/perfil
  3. clienteStore.setCliente(datos)
  4. Header muestra "Mis Turnos | Salir" + badge con nombre
  5. Paso 5: Auto-rellena datos del cliente registrado
```

**⚠️ IMPACTO**: Cliente registrado es tratado como invitado

#### 4.2 Invitado usa Teléfono de Cliente Registrado
```sql
Datos:
  Cliente(id=1, telefono=+549111, tieneUsuario=true, nombre="Juan")
  
Acción: Invitado reserva con telefono=+549111, nombre="Pedro"

Código actual:
  Cliente cliente = repositorioCliente
    .findByEmpresaAndTelefonoAndActivoTrue(empresa, "+549111")
    .orElseGet(...) // ⬅️ Encuentra id=1 (Juan)
  
  Turno turno = new Turno();
  turno.setCliente(cliente); // ⬅️ Asociado a Juan ❌
  turno.setNombreCliente("Pedro"); // ⬅️ Inconsistencia ⚠️
```

**Resultado**: Turno aparece en historial de Juan, pero con nombre "Pedro"

**⚠️ IMPACTO SEGURIDAD**: Posible reserva de turnos en nombre de otro

#### 4.3 Dos Empresas, Mismo Teléfono
```sql
Empresa A:
  Cliente(id=1, empresaId=1, telefono=+549111, tieneUsuario=true)

Empresa B:
  Cliente(id=2, empresaId=2, telefono=+549111, tieneUsuario=false)

Username formato:
  "cliente:empresaA:+549111" → Cliente id=1 ✅
  "cliente:empresaB:+549111" → Cliente id=2 ✅
```

**✅ CORRECTO**: Aislamiento por empresa funciona bien

#### 4.4 Cliente con Sesión en Empresa A entra a Empresa B
```
Estado:
  JSESSIONID válida
  clienteStore.cliente = { empresaId: 1, empresaNombre: "Peluquería A" }

Navegación: /reservar/barberia-b

Comportamiento actual:
  Header muestra: "👤 Juan | Mis Turnos | Salir"
  Si hace clic en "Mis Turnos" → /empresa/barberia-b/mis-turnos
  Backend: GET /api/cliente/mis-turnos
  
  ¿Qué devuelve?
```

**⚠️ PROBLEMA CRÍTICO #5**: Filtrado de Turnos por Empresa
```java
// ControladorCliente.java - ¿Existe este filtro?
@GetMapping("/mis-turnos")
public List<Turno> obtenerMisTurnos(Authentication auth) {
    String username = auth.getName(); // "cliente:empresaA:+549111"
    // ⚠️ Debe parsear empresaSlug del username
    // ⚠️ O usar empresaSlug del path
    // ¿Cómo filtra por empresa?
}
```

**Sin ver el código completo, asumo**:
- Si filtra por username completo → ✅ Correcto
- Si filtra solo por cliente.id sin empresa → ❌ Verá turnos de ambas empresas

---

## 🛡️ Análisis de Seguridad

### 5.1 Aislamiento Multi-Tenant
```
Nivel: ✅ BUENO (con reservas)

Fortalezas:
+ Username incluye empresaSlug: "cliente:{slug}:{tel}"
+ Cada query busca por empresa + teléfono
+ No hay cross-tenant queries evidentes

Debilidades:
⚠️ Cliente.id es único global (no scoped por empresa)
⚠️ Si un endpoint usa solo cliente.id, puede haber leaks
⚠️ Falta validación de empresaSlug en requests
```

**Ejemplo de vulnerabilidad potencial**:
```http
POST /api/cliente/cancelar-turno
Body: { turnoId: 123 }

Si NO valida que turno.cliente.empresa === clienteAutenticado.empresa
→ Puede cancelar turnos de otra empresa ⚠️
```

### 5.2 Validación de Identidad
```
Nivel: ⚠️ DÉBIL

Problemas:
❌ No hay validación SMS real (validadoPorSms=false siempre)
❌ Cualquiera puede reservar con cualquier teléfono
❌ Cliente registrado puede ser "secuestrado" por invitado

Mejoras necesarias:
1. Enviar código SMS al reservar como invitado
2. Validar código antes de crear turno
3. En registro: validar que controla el teléfono
4. Bloquear reservas invitadas en teléfonos registrados
```

### 5.3 Gestión de Sesiones
```
Nivel: ✅ ACEPTABLE

Fortalezas:
+ Session-based con JSESSIONID HttpOnly
+ CSRF protection habilitado
+ Máximo 1 sesión activa por usuario
+ Sesiones expiradas redirigen a /session-expired

Debilidades:
⚠️ Cliente store en memoria (no persiste en reload)
⚠️ No hay refresh token mechanism
⚠️ Si sesión expira durante reserva, se pierde todo
```

---

## 📱 Análisis de UX Mobile

### 6.1 Navegación Principal
```
Nivel: ✅ BUENO (después de mejoras recientes)

Fortalezas:
+ Header sticky con iconos compactos
+ Stepper responsive (números en móvil)
+ Botones touch-friendly (≥44x44px)
+ Tailwind CSS bien aplicado

Mejoras sugeridas:
⚠️ "Ingresar" se reduce a icono en móvil muy pequeño
⚠️ No hay indicador de "Ya estás logueado, no necesitas ingresar"
⚠️ Badge de usuario autenticado podría ser más prominente
```

### 6.2 Flujo de Conversión Invitado→Registrado
```
Nivel: ⚠️ CONFUSO

Puntos de fricción:
1. Modal post-reserva: ¿Por qué crear cuenta si ya reservé?
2. Mensaje: "Debe reservar al menos un turno antes" ← ¿Por qué?
3. No explica beneficios claros de tener cuenta
4. Si ya está logueado, sigue mostrando "Registrarse" en header

Mejora sugerida:
- Modal con bullet points claros de beneficios
- Permitir registro sin turno previo (UX estándar)
- Ocultar "Registrarse" si ya tiene cuenta
```

### 6.3 Acceso a Historial
```
Nivel: ⚠️ POCO CLARO

Problemas:
1. Cliente registrado entra a /reservar/{slug}
   → No ve botón "Mis Turnos" prominente
   → Está en header pero pasa desapercibido
   
2. Cliente invitado ve turno confirmado
   → No sabe que puede crear cuenta para ver historial
   → Modal es el único punto de conversión

Mejora sugerida:
- Banner superior: "Tenés X turnos confirmados. Creá tu cuenta para verlos"
- Botón "Ver mi historial" más visible en móvil
```

---

## 🚀 Análisis de Escalabilidad

### 7.1 Arquitectura Multi-Empresa
```
Nivel: ✅ EXCELENTE

Fortalezas:
+ Slug en URL evita IDs numéricos
+ Cada empresa aislada por queries
+ Un cliente puede tener cuentas en N empresas
+ Username diferenciado por empresa

Capacidad estimada:
- Empresas: Ilimitadas (slug único)
- Clientes por empresa: Millones (indexed queries)
- Turnos: Sin límite arquitectónico

Cuellos de botella:
⚠️ Query findByEmpresaAndTelefonoAndActivoTrue() en cada reserva
  → Índice compuesto recomendado: (empresa_id, telefono, activo)
```

### 7.2 Performance de Consultas
```
Reserva invitada:
1. findBySlugAndActivaTrue(slug) → Indexed ✅
2. findByEmpresaAndTelefonoAndActivoTrue() → Needs index ⚠️
3. save(Cliente) → O(1) ✅
4. save(Turno) → O(1) ✅

Tiempo estimado: <100ms con índices correctos

Login:
1. AuthenticationManager.authenticate()
   → ServicioDetallesUnificado
   → ServicioDetallesCliente.loadUserByUsername()
   → findByEmpresaAndTelefonoAndTieneUsuarioTrueAndActivoTrue()
   → Needs index ⚠️

Tiempo estimado: 50-150ms
```

**Índices recomendados**:
```sql
CREATE INDEX idx_cliente_empresa_telefono 
  ON cliente(empresa_id, telefono, activo);

CREATE INDEX idx_cliente_empresa_telefono_usuario
  ON cliente(empresa_id, telefono, tiene_usuario, activo);

CREATE INDEX idx_turno_cliente_fecha
  ON turno(cliente_id, fecha, estado);
```

---

## 🔧 Propuestas de Mejora (Incrementales)

### Prioridad 1 (CRÍTICO - Semana 1)

#### 1.1 Persistencia de Sesión Cliente
```typescript
// stores/cliente.ts
export const useClienteStore = defineStore('cliente', () => {
  const cliente = ref<ClienteInfo | null>(
    JSON.parse(localStorage.getItem('cliente_session') || 'null')
  )
  
  function setCliente(data: ClienteInfo) {
    cliente.value = data
    localStorage.setItem('cliente_session', JSON.stringify(data))
  }
  
  function logout() {
    cliente.value = null
    localStorage.removeItem('cliente_session')
  }
  
  return { cliente, setCliente, logout }
})
```

**Impacto**: Resuelve problema de reload perdiendo sesión

#### 1.2 Guard de Navegación
```typescript
// router/index.ts
router.beforeEach((to, from, next) => {
  const clienteStore = useClienteStore()
  
  if (to.meta.requiresAuth && !clienteStore.isAuthenticated) {
    next({
      name: 'LoginCliente',
      params: { empresaSlug: to.params.empresaSlug },
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})
```

**Impacto**: Redirige correctamente a login si no autenticado

#### 1.3 Validación de Teléfono Registrado
```java
// ServicioTurno.crearTurnoPublico()
Cliente clienteExistente = repositorioCliente
    .findByEmpresaAndTelefonoAndTieneUsuarioTrueAndActivoTrue(empresa, telefono);

if (clienteExistente != null) {
    throw new ValidacionException(
        "Este teléfono ya tiene una cuenta registrada. " +
        "Por favor inicie sesión para reservar."
    );
}

Cliente cliente = repositorioCliente
    .findByEmpresaAndTelefonoAndTieneUsuarioFalseAndActivoTrue(empresa, telefono)
    .orElseGet(() -> crearNuevoClienteInvitado(...));
```

**Impacto**: Evita conflicto de identidad y suplantación

---

### Prioridad 2 (IMPORTANTE - Semana 2)

#### 2.1 Detección Automática de Sesión en ReservarView
```typescript
// ReservarView.vue
onMounted(async () => {
  // Cargar empresa
  empresa.value = await publicoService.obtenerEmpresa(empresaSlug.value)
  
  // Si tiene localStorage pero no store, intentar recuperar sesión
  if (!clienteStore.isAuthenticated) {
    const savedCliente = localStorage.getItem('cliente_session')
    if (savedCliente) {
      try {
        // Validar que la sesión siga activa
        const perfil = await api.obtenerPerfil(empresaSlug.value)
        clienteStore.setCliente(perfil.data.datos)
      } catch {
        // Sesión expirada, limpiar localStorage
        localStorage.removeItem('cliente_session')
      }
    }
  }
  
  await cargarServicios()
})
```

**Impacto**: Cliente registrado es reconocido al entrar a /reservar/{slug}

#### 2.2 Pre-llenado de Datos en Paso 5
```vue
<!-- ReservarView.vue - Paso 5 -->
<div v-if="clienteAutenticado">
  <div class="bg-blue-50 p-4 rounded-md mb-4">
    <p class="text-sm text-blue-800">
      Reservando como: <strong>{{ clienteStore.cliente.nombre }}</strong>
    </p>
    <p class="text-xs text-blue-600 mt-1">
      Teléfono: {{ clienteStore.cliente.telefono }}
    </p>
  </div>
  <button @click="confirmarReserva" class="btn-primary">
    Confirmar Turno
  </button>
</div>

<div v-else class="formulario-cliente">
  <!-- Formulario actual -->
</div>
```

**Impacto**: Experiencia diferenciada para registrado vs invitado

#### 2.3 Permitir Registro sin Turno Previo (Opcional)
```java
@Transactional
public ClienteAutenticadoResponse registrarClienteSinTurno(
    String empresaSlug, 
    RegistroClienteRequest request
) {
    Empresa empresa = buscarEmpresa(empresaSlug);
    
    // Buscar cliente existente
    Optional<Cliente> existente = repositorioCliente
        .findByEmpresaAndTelefonoAndActivoTrue(empresa, request.getTelefono());
    
    Cliente cliente;
    if (existente.isPresent()) {
        cliente = existente.get();
        if (cliente.getTieneUsuario()) {
            throw new ValidacionException("Ya tiene cuenta");
        }
        // Actualizar invitado existente
        cliente.setEmail(request.getEmail());
        cliente.setContrasena(passwordEncoder.encode(request.getContrasena()));
        cliente.setTieneUsuario(true);
    } else {
        // Crear nuevo cliente SIN turno previo
        cliente = new Cliente();
        cliente.setEmpresa(empresa);
        cliente.setNombre(request.getNombre()); // ← Agregar al DTO
        cliente.setTelefono(request.getTelefono());
        cliente.setEmail(request.getEmail());
        cliente.setContrasena(passwordEncoder.encode(request.getContrasena()));
        cliente.setTieneUsuario(true);
        cliente.setValidadoPorSms(false); // Validar después
        cliente.setActivo(true);
    }
    
    cliente = repositorioCliente.save(cliente);
    return mapearRespuesta(cliente, empresa);
}
```

**Impacto**: UX estándar, reduce fricción

---

### Prioridad 3 (DESEABLE - Semana 3-4)

#### 3.1 Validación SMS Real
```java
@Service
public class ServicioSMS {
    public String enviarCodigoVerificacion(String telefono) {
        String codigo = generarCodigo6Digitos();
        // Integración con Twilio, AWS SNS, etc.
        smsProvider.enviar(telefono, "Tu código: " + codigo);
        redis.set("sms:" + telefono, codigo, 5, TimeUnit.MINUTES);
        return codigo;
    }
    
    public boolean validarCodigo(String telefono, String codigo) {
        String codigoGuardado = redis.get("sms:" + telefono);
        return codigo.equals(codigoGuardado);
    }
}
```

**Flujo**:
1. Invitado llena formulario paso 5 → Botón "Enviar código"
2. Backend envía SMS con código 6 dígitos
3. Input para ingresar código → "Verificar"
4. Si válido → Crea turno + Cliente con `validadoPorSms=true`

**Impacto**: Seguridad, previene spam

#### 3.2 Indicador Visual de Estado de Autenticación
```vue
<!-- ReservarView.vue - Header -->
<div v-if="clienteAutenticado" class="bg-green-50 border-l-4 border-green-500 p-3 mb-4">
  <div class="flex items-center">
    <svg class="w-5 h-5 text-green-500 mr-2">...</svg>
    <p class="text-sm text-green-800">
      Sesión activa como <strong>{{ clienteStore.cliente.nombre }}</strong>
    </p>
  </div>
</div>
```

**Impacto**: Claridad visual, reduce confusión

#### 3.3 Filtrado Explícito por Empresa en Endpoints
```java
@GetMapping("/mis-turnos")
public List<Turno> obtenerMisTurnos(
    @PathVariable String empresaSlug,  // ← Agregar
    Authentication auth
) {
    String username = auth.getName();
    String[] parts = username.split(":");
    String slugDesdeUsername = parts[1];
    
    // Validación cruzada
    if (!empresaSlug.equals(slugDesdeUsername)) {
        throw new ForbiddenException("Empresa no coincide");
    }
    
    // Query con filtro explícito
    return repositorioTurno.findByCliente_Empresa_SlugAndCliente_Telefono(
        empresaSlug,
        parts[2]
    );
}
```

**Impacto**: Seguridad multi-tenant reforzada

---

## 📋 Checklist de Validación Final

### Funcionalidad Core
- [x] Cliente invitado puede reservar sin cuenta
- [x] Cliente invitado ve modal de conversión
- [x] Registro requiere turno previo
- [ ] ⚠️ Registro funciona SIN turno previo (mejora sugerida)
- [x] Login con teléfono + contraseña
- [x] Cliente registrado ve historial
- [ ] ⚠️ Cliente registrado puede reservar sin re-login (parcial)

### Seguridad
- [x] Aislamiento por empresa (username compuesto)
- [x] Contraseñas encriptadas (BCrypt)
- [x] CSRF protection en endpoints autenticados
- [ ] ❌ Validación SMS implementada
- [ ] ⚠️ Bloqueo de reservas invitadas con tel. registrado
- [x] Sesiones stateful con expiración

### UX
- [x] Header responsive mobile-first
- [x] Stepper adaptativo
- [ ] ⚠️ Persistencia de sesión en reload
- [ ] ⚠️ Detección automática de sesión activa
- [ ] ⚠️ Indicador claro de estado de auth
- [x] Botones touch-friendly

### Escalabilidad
- [x] Arquitectura multi-tenant correcta
- [ ] ⚠️ Índices de BD optimizados
- [x] Queries scoped por empresa
- [x] Sin hard-coding de IDs

---

## 🎯 Recomendaciones Finales

### Implementar YA (Esta semana)
1. **Persistencia de sesión en localStorage** (2 horas)
2. **Guard de navegación** (1 hora)
3. **Validación de teléfono registrado en reserva invitada** (3 horas)

**ROI**: Evita bugs críticos y confusión masiva de usuarios

### Implementar PRONTO (Próximas 2 semanas)
1. **Detección automática de sesión en ReservarView** (4 horas)
2. **Pre-llenado de datos para cliente registrado** (2 horas)
3. **Permitir registro sin turno previo** (6 horas)

**ROI**: Mejora UX significativa, reduce fricción

### Implementar EVENTUALMENTE (Backlog)
1. **Validación SMS con código** (20 horas + integración)
2. **Dashboard del cliente** (40 horas)
3. **Notificaciones push/email** (30 horas)

**ROI**: Features premium, diferenciación competitiva

---

## 🏆 Conclusión

### Nivel de Madurez Global: **7/10**

**Lo que está bien**:
- Arquitectura multi-tenant sólida ✅
- Separación invitado/registrado clara ✅
- Seguridad básica correcta ✅
- Mobile-first bien implementado ✅

**Lo que necesita mejoras urgentes**:
- Persistencia de sesión ⚠️
- Validación de identidad (SMS) ⚠️
- Conflicto tel. registrado ⚠️
- UX de conversión confusa ⚠️

**Veredicto**:
El sistema es **funcional y escalable**, pero tiene **3-4 bugs críticos de UX y seguridad** que generarán confusión y posibles problemas de datos. Con las **6 mejoras de Prioridad 1 y 2** implementadas, subiría a **9/10**.

**Tiempo estimado para alcanzar producción-ready**: 2-3 semanas de desarrollo.

---

**Firmado**: Arquitecto de Software Senior  
**Fecha**: 11 de enero de 2026
