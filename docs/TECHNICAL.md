> Documentación técnica de DTouch. Para la presentación del producto, vuelve al [README](../README.md).

# DTouch

DTouch es una app **nueva e independiente**, inspirada en el comportamiento de **J Touch 1.7.0-GP**, pero con identidad propia: paquete `com.dtouch.app`, firma propia, preferencias propias y ficha de Play propia. No comparte paquete, datos ni firma con J Touch, así que ambas apps pueden instalarse en el mismo dispositivo sin chocar.

La app conserva únicamente el concepto de **orbe flotante con gestos personalizables**.

> Estado: **modernizado a Android 17 (API 37)** y compilado/instalado en un Redmi Note 12S (Android 14) y un POCO (Android 16). El build de debug genera `BUILD SUCCESSFUL`, los tests unitarios pasan, la galería se verificó en el Redmi y el consumo de CPU con pantalla apagada se verificó en el POCO. Queda pendiente la revisión visual completa en Android 16–17.

### Validación estática realizada en este equipo

- Parseo de todos los XML: OK.
- Referencias `@string`, `@drawable`, `@mipmap`, `@dimen`, `@color`, `@style`, `@layout`, `@menu`, `@xml` e `@id`: OK.
- Referencias `R.*` en Kotlin: OK.
- Paridad de strings inglés/español: 80/80.
- Balance de llaves y paréntesis en Kotlin: OK.
- Sin artefactos `??` de jadx en el código nuevo.
- **Compilación `:app:assembleDebug`: OK** (Gradle 9.5.0, JDK 21, AGP 9.3.3, Kotlin 2.4.21, SDK 37).
- **Tests `:app:testDebugUnitTest`: OK**, incluido `OrbStyleTest`.
- **Instalación en dispositivo:** Redmi Note 12S (Android 14) y POCO (Android 16); app abierta sin crashes y galería probada.
- **CPU con pantalla apagada (POCO/Android 16):** 0,0–0,5 % y el proceso sigue vivo; se corrigió el `ScreenOffCPUCheckKill`.
- Migración moderna (2026-11-10): built-in Kotlin, version catalog, coroutines, `entries`, `compilerOptions`, `currentWindowMetrics`, `targetSdk 37`.
- Revisión por lectura (2026-11-09): se corrigieron dos fallos que la validación estática no detectaba: un `dp(4)` con `Float` en `FloatingOrbView` (error de compilación) y un `?: return false` en `OrbImageStore.saveFromUri` que hacía fallar siempre la imagen personalizada.
- Corrección de resource linking (2026-11-09): los estilos `TextAppearance.DTouch.Section` y `Widget.DTouch.SectionHeader` no declaraban padre y AAPT buscaba `TextAppearance.DTouch`/`Widget.DTouch`. Se agregó `parent=""`.

---

## 1. Alcance

### Incluido

- Orbe flotante dibujado por un `AccessibilityService`.
- Permiso principal: servicio de accesibilidad.
- Seis gestos configurables:
  - Toque simple.
  - Doble toque.
  - Deslizar arriba.
  - Deslizar abajo.
  - Deslizar a la izquierda.
  - Deslizar a la derecha.
- Acciones disponibles:
  - Ninguna.
  - Apagar pantalla.
  - Atrás.
  - Inicio.
  - Aplicaciones recientes.
  - Panel de notificaciones.
- **Long press siempre mueve el orbe**: se mantiene presionado, se arrastra y se suelta. La posición se guarda por orientación.
- **Sensibilidad de gestos configurable**: slider de 0 a 100 % que ajusta la distancia necesaria para reconocer un swipe (32–80 dp; 50 % = 56 dp). No cambia el long press ni el doble toque.
- **Tamaño del orbe configurable** entre 32 y 80 dp, con vista previa.
- **Opacidad del orbe configurable** entre 0 y 100 %, con vista previa.
- **Imagen personalizada del orbe**: cualquier imagen del selector del sistema (PNG recomendado), guardada localmente y aplicada en vivo.
- **12 estilos de orbe predefinidos**: galería de estilos (Clásico, Sólido, Anillo, Punto, Dúo, Medianoche, Escarcha, Aqua, Arcoíris, Aurora, Púlsar y Cometa), con selección en vivo y persistencia. Arcoíris, Aurora, Púlsar y Cometa tienen animación.
- Los cambios de tamaño/opacidad/imagen/estilo se aplican en vivo al orbe flotante.
- Pantalla de configuración con estado del servicio, mapeo de gestos, permiso de bloqueo (solo Android 6–8) y diálogo “Acerca de”.
- Persistencia local con `SharedPreferences`.
- Textos en inglés y español.
- Sin INTERNET, sin analytics, sin almacenamiento, sin permisos innecesarios.

### Fuera de alcance (a propósito)

No se reconstruyeron estas funciones del J Touch original:

- Captura de pantalla.
- Panel rápido de WiFi/Bluetooth/volumen/rotación.
- Recorte circular automático de la imagen personalizada.
- Auto-hide, anti screen burn, idle alpha.
- Listener de notificaciones.
- Funciones root (`lock_root`, `menu_key`, `close_cur_app`).
- Ajustes de batería/autostart por fabricante.
- Get Pro / IAP.
- Publicidad o cualquier SDK de terceros.

Si más adelante se quieren recuperar, el código original decompilado sigue en `../../app` como referencia y el README explica cómo extender DTouch.

> `../../app` es una carpeta hermana de `DTouch` en el PC de origen (`...\app\app`). No forma parte de este proyecto y **no viaja si solo se copia `DTouch`**. DTouch compila sin ella; copiarla aparte solo si se quiere consultar el código original.

---

## 2. Requisitos para compilar

- **Android Studio** Rabbit 2 (2026.2.2) o superior, compatible con AGP 9.3.3.
- **JDK 21** (JBR 21; AGP 9.3 exige mínimo JDK 17).
- **Android SDK Platform 37 (Android 17)** y **Build Tools 36.0.0**.
- **Gradle 9.5.0** (el wrapper completo está incluido: `gradlew`, `gradlew.bat` y `gradle-wrapper.jar`).
- Dispositivo o emulador **Android 6.0 (API 23)** o superior.
- No se necesita conexión a Internet en el dispositivo: la app no declara `INTERNET`.

### Versiones

| Componente | Versión |
|---|---|
| Android Gradle Plugin | 9.3.3 |
| Gradle | 9.5.0 |
| Kotlin | 2.4.21 (built-in Kotlin) |
| compileSdk | 37 |
| minSdk | 23 |
| targetSdk | 37 |
| AndroidX Core KTX | 1.19.1 |
| AndroidX AppCompat | 1.8.0 |
| AndroidX Activity KTX | 1.13.0 |
| AndroidX Lifecycle Runtime KTX | 2.11.0 |
| Material Components | 1.14.0 |
| Kotlin Coroutines | 1.11.0 |
| JUnit | 4.13.2 |

### Estado del wrapper

El repositorio incluye el wrapper completo:

```text
gradle/wrapper/gradle-wrapper.properties
gradle/wrapper/gradle-wrapper.jar
gradlew
gradlew.bat
```

La distribución configurada es **Gradle 9.5.0**.

---

## 3. Cómo abrir y compilar

### Opción A: Android Studio (recomendada)

1. Abrir Android Studio.
2. `File > Open...` y seleccionar la carpeta `DTouch`.
3. Aceptar la descarga/sincronización de Gradle.
4. La sincronización descarga Gradle 9.5.0 usando el wrapper incluido. Si Android Studio ofrece actualizar AGP o Kotlin, no es necesario para compilar.
5. Verificar que **Android SDK Platform 37** y **Build Tools 36.0.0** estén instalados en `Settings > Languages & Frameworks > Android SDK`.
6. `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
7. El APK queda en:

```text
DTouch/app/build/outputs/apk/debug/app-debug.apk
```

### Opción B: línea de comandos

Con JDK 21 y el wrapper incluido:

```bash
cd DTouch
./gradlew assembleDebug
```

En Windows:

```bat
cd DTouch
gradlew.bat assembleDebug
```

Para release:

```bash
./gradlew assembleRelease
```

> El proyecto no está firmado. Para instalarlo en un dispositivo hace falta un keystore de debug (Android Studio lo genera) o configurar `signingConfigs`.

### Si Gradle falla por el JDK

AGP 9.3.3 y Gradle 9.5.0 esperan JDK 17 o superior. Usar el **JBR 21** de Android Studio (o un JDK 21 instalado) y apuntar el Gradle JDK a esa instalación. Evitar JDK 25 salvo que se verifique la combinación completa.

---

## 4. Instalar y probar

### Instalación

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

O copiar el APK al dispositivo y abrirlo.

DTouch tiene `applicationId = "com.dtouch.app"`, distinto del `com.bs.smarttouch.gp` de J Touch. Por eso **no choca** con la app vieja: se pueden instalar las dos a la vez. DTouch no migra automáticamente las preferencias de J Touch.

> Si aparece `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, hay otra compilación de DTouch instalada con una firma distinta. Desinstalá esa compilación de DTouch primero. Eso no afecta a J Touch.

### Activación inicial

1. Abrir **DTouch**.
2. Tocar **Activar accesibilidad**.
3. Leer el aviso de accesibilidad y tocar **Continuar**.
4. En Ajustes del sistema, ir a **Accesibilidad > Aplicaciones descargadas** (el nombre varía por fabricante).
5. Activar **DTouch**.
6. Volver a la app. El estado debe decir **El servicio de accesibilidad está activo** y el orbe debe aparecer.

Android no permite activar un servicio de accesibilidad por código; el usuario debe hacerlo manualmente. Esto es intencional y es el único permiso obligatorio.

> **Android 13+ y APK fuera de tienda:** si el APK se instaló abriéndolo desde el dispositivo (no con `adb install` ni con Run de Android Studio), el sistema puede mostrar el servicio atenuado o el aviso "Ajuste restringido". Ir a **Ajustes > Aplicaciones > DTouch > ⋮ (menú) > Permitir ajustes restringidos** y volver a intentar la activación. El nombre exacto varía por fabricante y versión; no se probó en este proyecto.

### Atajo por ADB (solo desarrollo)

Para pruebas en un emulador o dispositivo de test se puede activar el servicio por ADB:

```bash
adb shell settings put secure enabled_accessibility_services com.dtouch.app/com.dtouch.app.service.DTouchAccessibilityService
adb shell settings put secure accessibility_enabled 1
```

Para desactivarlo:

```bash
adb shell settings put secure enabled_accessibility_services ""
adb shell settings put secure accessibility_enabled 0
```

> Cuidado: esto reemplaza la lista de servicios de accesibilidad del dispositivo. Usarlo solo en un equipo de pruebas, no en un teléfono personal con otros servicios activos.

### Android 8 o anterior: apagar pantalla

En Android 9+ se usa `GLOBAL_ACTION_LOCK_SCREEN`, que no requiere permisos extra.

En Android 6–8, el bloqueo usa `DevicePolicyManager`. La pantalla principal muestra la sección **Bloqueo de pantalla** y el botón **Activar permiso**. Hay que aceptarlo una vez. Para desinstalar la app, primero hay que desactivar ese permiso.

---

## 5. Gestos y valores por defecto

| Gesto | Acción por defecto | Notas |
|---|---|---|
| Toque simple | Atrás | Si el doble toque está en “Ninguna”, se ejecuta sin retraso. |
| Doble toque | Ninguna | Si se configura, el toque simple espera 280 ms. |
| Deslizar arriba | Apagar pantalla | Android 9+ global action; Android 6–8 requiere admin. |
| Deslizar abajo | Panel de notificaciones | `GLOBAL_ACTION_NOTIFICATIONS`. |
| Deslizar a la izquierda | Atrás | `GLOBAL_ACTION_BACK`. |
| Deslizar a la derecha | Aplicaciones recientes | `GLOBAL_ACTION_RECENTS`. |
| Long press + arrastrar | Mover orbe | Siempre activo; no es configurable. |

### Detalles de interacción

- **Toque**: menos de la distancia configurada en sensibilidad.
- **Swipe**: más de la distancia configurada; se elige el eje dominante. El rango es 32–80 dp y el valor por defecto (50 %) equivale a 56 dp.
- **Long press**: 350 ms; entra en modo mover y el orbe sigue el dedo.
- **Posición**: se guarda por orientación (`portrait`/`landscape`) y se limita a los bordes de la pantalla.
- **Rotación**: al cambiar de orientación se guarda la posición anterior y se carga/crea la de la nueva orientación.
- **Reset**: menú de tres puntos → **Restablecer posición del orbe**.

---

## 6. Permisos y privacidad

### Permisos declarados

| Permiso | Uso |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` (en el servicio) | Mostrar el orbe y ejecutar acciones globales. |
| `BIND_DEVICE_ADMIN` (en el receiver) | Solo Android 6–8, para apagar la pantalla. |

### Permisos que **no** se piden

- `INTERNET`
- `SYSTEM_ALERT_WINDOW`
- `READ/WRITE_EXTERNAL_STORAGE`
- `POST_NOTIFICATIONS`
- `VIBRATE`
- `BLUETOOTH`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`
- `WRITE_SETTINGS`
- `ACCESS_NOTIFICATION_POLICY`

La imagen personalizada se elige con el selector del sistema (`ActivityResultContracts.GetContent`) y se copia al almacenamiento interno de la app. No se pide permiso de almacenamiento.

### Cómo se dibuja el orbe

Se usa:

```kotlin
WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
```

Ese tipo de ventana pertenece al servicio de accesibilidad y **no requiere** el permiso “Mostrar sobre otras apps” (`SYSTEM_ALERT_WINDOW`). La ventana existe solo mientras el servicio está conectado.

La app declara:

```xml
android:canRetrieveWindowContent="false"
```

No lee el contenido de la pantalla. Tampoco usa `dispatchGesture` ni captura eventos más allá de `typeWindowStateChanged` (el servicio no los procesa).

### Datos

- La configuración se guarda localmente en `SharedPreferences` (`dtouch_settings`).
- La imagen personalizada se guarda en el almacenamiento interno de la app (`files/orb_custom.png`), reescalada a 512 px y reencodeada como PNG.
- No hay base de datos, red, analytics ni publicidad.
- La imagen personalizada no se sube a ningún servidor; queda solo en el dispositivo.
- El orbe predeterminado es un PNG local del proyecto.

---

## 7. Arquitectura

```text
DTouch/
├─ app/src/main/
│  ├─ AndroidManifest.xml
│  ├─ java/com/dtouch/app/
│  │  ├─ data/
│  │  │  ├─ GestureAction.kt        # Acciones disponibles
│  │  │  ├─ GestureDefaults.kt      # Defaults del J Touch original
│  │  │  ├─ GestureType.kt          # Gestos reconocidos
│  │  │  ├─ OrbImageStore.kt        # Imagen personalizada en storage interno
│  │  │  ├─ OrbStyle.kt             # Estilos de orbe predefinidos
│  │  │  └─ SettingsRepository.kt   # SharedPreferences + posición + apariencia
│  │  ├─ service/
│  │  │  ├─ DTouchAccessibilityService.kt  # Servicio y acciones globales
│  │  │  ├─ FloatingOrbView.kt             # Máquina de estados táctil
│  │  │  ├─ LockScreenAdminReceiver.kt     # DeviceAdmin (API 23-27)
│  │  │  ├─ OrbAnimatedDrawable.kt         # Rotación/pulso a 12,5 fps
│  │  │  └─ OverlayController.kt           # WindowManager, posición y apariencia
│  │  └─ ui/main/
│  │     └─ MainActivity.kt         # Configuración
│  └─ res/
│     ├─ layout/activity_main.xml
│     ├─ layout/item_setting_row.xml
│     ├─ layout/item_orb_style.xml
│     ├─ drawable/orb.png
│     ├─ drawable/orb_style_*.xml   # bases vectoriales de los estilos
│     ├─ drawable-v24/orb_style_aurora_base.xml  # gradiente sweep
│     ├─ values/strings.xml
│     ├─ values-es/strings.xml
│     └─ xml/accessibility_service_config.xml
└─ README.md
```

### Flujo

```text
MainActivity
  └─ SettingsRepository (SharedPreferences)
        │
        │  el servicio lee la acción en cada gesto
        ▼
DTouchAccessibilityService
  ├─ OverlayController → WindowManager → FloatingOrbView
  └─ dispatchAction()
       ├─ GLOBAL_ACTION_BACK
       ├─ GLOBAL_ACTION_HOME
       ├─ GLOBAL_ACTION_RECENTS
       ├─ GLOBAL_ACTION_NOTIFICATIONS
       └─ GLOBAL_ACTION_LOCK_SCREEN / DeviceAdmin
```

### Máquina de estados de `FloatingOrbView`

```text
ACTION_DOWN
  ├─ programa long press (350 ms)
  └─ escala el orbe a 0.92

ACTION_MOVE
  ├─ si long press ya ocurrió → mover ventana
  └─ si se supera touchSlop → cancelar long press

ACTION_UP
  ├─ si long press ocurrió → guardar posición
  ├─ si distancia > umbral de sensibilidad → ejecutar swipe
  └─ si no → resolver single/double tap

ACTION_CANCEL
  └─ cancelar long press y terminar movimiento
```

---

## 8. Datos y preferencias

Archivo: `dtouch_settings`

| Clave | Tipo | Default | Uso |
|---|---|---|---|
| `gesture_single_tap` | String | `back` | Acción del toque simple. |
| `gesture_double_tap` | String | `none` | Acción del doble toque. |
| `gesture_swipe_up` | String | `lock_screen` | Acción de swipe arriba. |
| `gesture_swipe_down` | String | `notifications` | Acción de swipe abajo. |
| `gesture_swipe_left` | String | `back` | Acción de swipe izquierda. |
| `gesture_swipe_right` | String | `recents` | Acción de swipe derecha. |
| `gesture_sensitivity` | Int | `50` | Sensibilidad 0–100; se traduce a 80–32 dp para reconocer un swipe. |
| `orb_size_dp` | Int | `56` | Tamaño del orbe en dp (32–80). |
| `orb_alpha` | Int | `255` | Opacidad del orbe (0–255; la UI muestra 0–100 %). |
| `orb_style` | String | `classic` | Estilo seleccionado: `classic`, `solid`, `ring`, `dot`, `duo`, `midnight`, `frost`, `aqua`, `rainbow`, `aurora`, `pulse` o `comet`. |
| `orb_x_port` | Int | sin valor | Posición X en vertical. |
| `orb_y_port` | Int | sin valor | Posición Y en vertical. |
| `orb_x_land` | Int | sin valor | Posición X en horizontal. |
| `orb_y_land` | Int | sin valor | Posición Y en horizontal. |

Valores posibles de acción:

```text
none
lock_screen
back
home
recents
notifications
```

Si una clave no existe o tiene un valor desconocido, se usa el default de `GestureDefaults`.

### Imagen personalizada

- Archivo: `files/orb_custom.png` en el almacenamiento interno de la app.
- Se reescala a un máximo de 512 px y se guarda como PNG para conservar transparencia.
- No se guarda una ruta en `SharedPreferences`: la app detecta la imagen por la existencia del archivo.
- “Quitar imagen” borra el archivo y vuelve al estilo guardado.
- La imagen se muestra completa con `FIT_CENTER`; para un acabado circular, usar un PNG ya recortado en círculo.

### Estilos del orbe

- `OrbStyle` define 12 estilos. `classic` reutiliza `drawable/orb.png`; los demás son vectores en `drawable/orb_style_*.xml`.
- `rainbow`, `aurora`, `pulse` y `comet` usan `OrbAnimatedDrawable`, que anima la base vectorial a **12,5 fps** (rotación o pulso). La galería y la vista previa muestran el frame estático; la animación se ve en el orbe flotante.
- `aurora` usa un solo anillo con gradiente sweep en API 24+; en API 23 usa 24 segmentos como fallback.
- `pulse` late con una escala de 0,78 a 1,0 cada 0,9 segundos (ciclo de 1,8 s).
- `comet` es un punto luminoso con estela que orbita en sentido antihorario cada 2,5 segundos.
- Las animaciones se pausan con la pantalla apagada/bloqueada y se reanudan al desbloquear; el Activity no anima miniaturas para no gastar CPU en segundo plano.
- La selección se guarda en `orb_style`; un id desconocido vuelve a `classic`.
- Al elegir un preset se borra `files/orb_custom.png` (si existía) y el orbe se actualiza en vivo mediante `ACTION_ORB_IMAGE_CHANGED`.
- Si hay una imagen personalizada, tiene prioridad sobre el preset. “Quitar imagen” la borra y vuelve al preset guardado.
- La galería se genera desde `OrbStyle.entries` en cuatro filas de tres; no usa RecyclerView ni dependencias nuevas.

---

## 9. Decisiones técnicas

### Kotlin + XML

Se eligió Kotlin + XML + ViewBinding por ser la opción más cercana a la UI original y la más simple de mantener para un overlay basado en `View`. No se usa Compose.

### Stack Android 2026

- AGP 9.3.3 + Gradle 9.5.0 + **built-in Kotlin 2.4.21**; no se aplica `org.jetbrains.kotlin.android`.
- Dependencias y versiones centralizadas en `gradle/libs.versions.toml`.
- `targetSdk 37` (Android 17); Google Play exige 36 desde el 31/08/2026.
- Coroutines (`lifecycleScope` + `Dispatchers.IO`) para guardar/cargar la imagen personalizada.
- `entries` en lugar de `values()`, `compilerOptions` en lugar de `kotlinOptions`.
- `currentWindowMetrics` en API 30+ con fallback a `Display` en API 23–29.
- Animaciones del orbe con `OrbAnimatedDrawable` a 12,5 fps; se pausan con pantalla apagada/bloqueada para evitar el `ScreenOffCPUCheckKill` de Android 16.
- Se mantiene XML/Views para el overlay y la pantalla de ajustes; Compose no aporta una ventaja clara aquí.
- Se mantiene SharedPreferences a propósito: es síncrono, estable y adecuado para 13 claves; DataStore agregaría complejidad asíncrona sin un beneficio de compatibilidad.

### `TYPE_ACCESSIBILITY_OVERLAY`

Ventaja: no requiere `SYSTEM_ALERT_WINDOW` ni la notificación “se muestra sobre otras apps”.

Limitación: el orbe solo existe mientras el servicio de accesibilidad está conectado. Si el sistema mata el servicio, el orbe desaparece hasta que se reconecta.

Si en algún fabricante el overlay de accesibilidad no funciona, el README incluye el fallback a `TYPE_APPLICATION_OVERLAY` (sección 12).

### Acciones globales

`AccessibilityService.performGlobalAction()` es la forma oficial de hacer Atrás, Inicio, Recientes, Notificaciones y Bloqueo (Android 9+) sin root. No se usan coordenadas ni `dispatchGesture`, así que no depende de la resolución ni del contenido de la pantalla.

### DeviceAdmin solo como fallback

En Android 6–8 no existe `GLOBAL_ACTION_LOCK_SCREEN`. Para no perder la función, se incluye un `DeviceAdminReceiver` que solo declara `force-lock`. En Android 9+ no se usa.

### SharedPreferences

El servicio y la Activity están en el mismo proceso. Las lecturas de preferencias son instantáneas: cada gesto consulta su acción al ejecutarse, así que no hace falta reiniciar el servicio al cambiar un mapeo. Se usan broadcasts internos para tres comandos: “reset position”, “aplicar tamaño/opacidad en vivo” y “recargar imagen personalizada”.

### Paquete e identidad

DTouch usa:

```kotlin
namespace = "com.dtouch.app"
applicationId = "com.dtouch.app"
```

Es una identidad completamente nueva:

- No comparte paquete con J Touch (`com.bs.smarttouch.gp`).
- No comparte firma ni keystore.
- No comparte preferencias, datos ni ficha de Google Play.
- Se puede instalar junto a J Touch sin conflicto.
- No es una actualización de J Touch; es una app distinta.

El nombre visible es **DTouch**, el servicio de accesibilidad es `com.dtouch.app.service.DTouchAccessibilityService` y la Activity principal es `com.dtouch.app.ui.main.MainActivity`.

> Antes de publicar, verificá que `com.dtouch.app` esté disponible en Google Play. Si ya está ocupado, usá un `applicationId` único, por ejemplo `com.tudominio.dtouch`, y actualizá también el `namespace`, las carpetas de Kotlin y `accessibility_service_config.xml`.

---

## 10. Cómo cambiar nombre, paquete, icono y versión

### Nombre visible

Editar:

```text
app/src/main/res/values/strings.xml
app/src/main/res/values-es/strings.xml
```

Cambiar `app_name` y `service_label`.

### Paquete

Editar `app/build.gradle.kts`:

```kotlin
namespace = "com.dtouch.app"
applicationId = "com.dtouch.app"
```

Si se cambia `namespace`, también hay que mover las carpetas de Kotlin y actualizar:

```text
app/src/main/res/xml/accessibility_service_config.xml
```

El atributo `android:settingsActivity` usa el nombre completo de `MainActivity`.

### Icono

- API 26+: `res/mipmap-anydpi-v26/ic_launcher.xml` y `ic_launcher_round.xml`.
- API 23–25: PNGs en `res/mipmap-*/ic_launcher.png`.
- El foreground del icono adaptativo está en `res/drawable/ic_launcher_foreground.xml`.
- El orbe está en `res/drawable/orb.png`.

### Versión

Editar `app/build.gradle.kts`:

```kotlin
versionCode = 1
versionName = "1.0.0-DTouch"
```

---

## 11. Cómo extender DTouch

### Agregar una acción

1. Agregar una entrada en `GestureAction.kt`:

```kotlin
MY_ACTION("my_action", R.string.action_my_action)
```

2. Agregar el string en `values/strings.xml` y `values-es/strings.xml`.
3. Manejar la acción en `DTouchAccessibilityService.dispatchAction()`.
4. Si necesita un permiso, declararlo en `AndroidManifest.xml` y pedirlo en `MainActivity`.

### Agregar un gesto

1. Agregar una entrada en `GestureType.kt`.
2. Agregar su default en `GestureDefaults.kt`.
3. Agregar su clave en `SettingsRepository.keyFor()`.
4. Agregar su label en `MainActivity.titleFor()` y en los strings.
5. Detectar el gesto en `FloatingOrbView` y llamar a `listener?.onGesture(...)`.
6. La fila de configuración se genera automáticamente porque `MainActivity` recorre `GestureType.entries`.

### Agregar snap a bordes

El tamaño y la opacidad ya están implementados. Para que el orbe se pegue al borde más cercano al soltarlo:

1. En `OverlayController.onMoveEnd()`, calcular el centro horizontal del orbe.
2. Si el centro está en la mitad izquierda, mover `params.x` a `screenMargin()`; si está en la derecha, a `screenWidth - width - screenMargin()`.
3. Actualizar la vista con `updateView(params)`.
4. Recién después guardar la posición final con `saveCurrentPosition()`.

### Agregar recorte circular a la imagen personalizada

La imagen se muestra completa con `FIT_CENTER`. Para recortarla en círculo:

1. En `OrbImageStore.saveFromUri()`, después de `scaleToFit`, crear un bitmap cuadrado transparente.
2. Dibujar un círculo blanco y aplicar `PorterDuff.Mode.SRC_IN` con el bitmap original, igual que hacía el J Touch original.
3. Guardar el bitmap resultante como PNG.
4. Alternativamente, usar un `ShapeableImageView` con `ShapeAppearanceModel` circular en la vista previa y en el overlay.

### Agregar haptic feedback configurable

`FloatingOrbView` ya usa `performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)` para el long press. Para vibrar en cada acción, agregar un `Vibrator`/`VibratorManager` en el servicio y una preferencia booleana. En Android 13+ se puede usar `VibratorManager`.

---

## 12. Troubleshooting

### El orbe no aparece

1. Verificar que DTouch esté activo en Accesibilidad.
2. Ver logs:

```bash
adb logcat | grep -E "DTouchOverlay|DTouchAccessibilityService|AndroidRuntime"
```

3. Probar desactivar y volver a activar el servicio.
4. En algunos fabricantes (MIUI, EMUI, ColorOS, Funtouch) el sistema puede limitar ventanas de accesibilidad. Probar el fallback:

**Fallback a `TYPE_APPLICATION_OVERLAY`:**

- En `AndroidManifest.xml`, agregar:

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

- En `OverlayController.show()`, cambiar:

```kotlin
WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
```

por:

```kotlin
WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
```

- En `MainActivity`, pedir el permiso con:

```kotlin
Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
```

- En `onResume`, comprobar `Settings.canDrawOverlays(this)`.

### Los gestos no hacen nada

- Verificar que el servicio siga activo.
- Revisar el mapeo en la app.
- En Android 9+ “Apagar pantalla” usa `GLOBAL_ACTION_LOCK_SCREEN`.
- En Android 6–8 “Apagar pantalla” necesita el permiso de administrador.

### El toque simple tarda

Es normal si el doble toque tiene una acción configurada: el sistema espera 280 ms para distinguir single/double. Si no se usa doble toque, ponerlo en **Ninguna**.

### La posición del orbe se mueve sola al rotar

La posición se guarda por orientación. Si se cambia de vertical a horizontal, se usa la posición guardada de horizontal; si no existe, se calcula una nueva. Es el comportamiento esperado.

### No se puede desinstalar

En Android 6–8, si se activó el permiso de administrador, primero hay que desactivarlo desde la app o desde Ajustes > Seguridad > Administradores del dispositivo.

### Error `INSTALL_FAILED_UPDATE_INCOMPATIBLE`

Hay otra compilación de DTouch instalada con una firma distinta. Desinstalar esa compilación de DTouch. Esto no afecta a J Touch, que usa otro paquete (`com.bs.smarttouch.gp`).

### Error de build por JDK/SDK

- Instalar JDK 21 (o 17+).
- Instalar Android SDK Platform 37.
- Instalar Android SDK Build-Tools 36.0.0.
- Verificar `local.properties` con `sdk.dir=...` o dejar que Android Studio lo genere.
- Verificar Gradle 9.5.0 con `.\gradlew.bat --version`.

---

## 13. Checklist de prueba

- [ ] `assembleDebug` compila.
- [ ] `assembleRelease` compila.
- [ ] `testDebugUnitTest` pasa.
- [ ] El APK instalado figura como `com.dtouch.app`.
- [ ] DTouch se puede instalar junto a J Touch sin conflicto.
- [ ] El servicio de accesibilidad aparece como DTouch (`com.dtouch.app/...`).
- [ ] La app abre en inglés y en español.
- [ ] El aviso de accesibilidad aparece antes de abrir Ajustes.
- [ ] El servicio se puede activar y el estado cambia a “activo”.
- [ ] El orbe aparece al activar el servicio.
- [ ] Toque simple = Atrás.
- [ ] Doble toque = Ninguna (sin retraso).
- [ ] Doble toque configurado = acción y retraso de 280 ms en single.
- [ ] Swipe arriba = apagar pantalla.
- [ ] Swipe abajo = panel de notificaciones.
- [ ] Swipe izquierda = Atrás.
- [ ] Swipe derecha = Recientes.
- [ ] Long press + arrastrar mueve el orbe.
- [ ] La posición persiste al cerrar/abrir la app.
- [ ] La posición persiste por orientación.
- [ ] Cambiar el tamaño del orbe se aplica en vivo y persiste.
- [ ] Cambiar la opacidad del orbe se aplica en vivo y persiste.
- [ ] Mover el slider de sensibilidad cambia el umbral de swipe y persiste.
- [ ] Con sensibilidad alta (32 dp) un deslizamiento corto cuenta como swipe; con baja (80 dp) no.
- [ ] La vista previa refleja tamaño y opacidad.
- [ ] Elegir cada uno de los 12 estilos y ver el cambio en vivo.
- [ ] Arcoíris gira en el orbe flotante (la miniatura queda estática).
- [ ] Aurora gira, Púlsar late y Cometa orbita en el orbe flotante.
- [ ] Pantalla apagada: CPU ~0 % y el proceso sigue vivo tras varios minutos.
- [ ] Al desbloquear, la animación del orbe se reanuda.
- [ ] La selección del estilo persiste al reiniciar la app y el servicio.
- [ ] Elegir un preset borra la imagen personalizada; “Quitar imagen” vuelve al preset guardado.
- [ ] Los límites 32/80 dp y 0/100 % funcionan sin romper el overlay.
- [ ] Elegir una imagen PNG con transparencia; se ve en la vista previa y en el orbe.
- [ ] Cancelar el selector no cambia la imagen actual.
- [ ] “Quitar imagen” borra la imagen y vuelve al estilo guardado.
- [ ] La imagen personalizada persiste al reiniciar la app y el servicio.
- [ ] Una imagen enorme se reduce a 512 px sin OOM.
- [ ] Una imagen inválida muestra error y no rompe la app.
- [ ] No se pide permiso de almacenamiento.
- [ ] Reset position funciona.
- [ ] Android 6–8: permiso de administrador y bloqueo.
- [ ] Android 9+: bloqueo sin administrador.
- [ ] Rotación sin crash.
- [ ] Reinicio del dispositivo: el servicio sigue activo o se puede reactivar.
- [ ] Sin permisos de INTERNET/almacenamiento.
- [ ] Sin crashes en API 23, 26, 29, 33 y 35.
- [ ] En un fabricante con restricciones: probar fallback de overlay y batería.

---

## 14. Publicación y política de Google Play

Si se publica en Google Play:

- La app usa `AccessibilityService`. Google Play exige una divulgación prominente y una política de privacidad.
- El manifiesto declara `android:isAccessibilityTool="true"` porque DTouch es una herramienta de asistencia (orbe flotante), no una app de automatización encubierta.
- Mantener la divulgación visible antes de activar el servicio (ya está implementada en `MainActivity`).
- No agregar `INTERNET` si no es necesario; ayuda a la transparencia.
- Target SDK 37 (API 37); Google Play exige 36 desde el 31/08/2026, así que 37 cumple y queda preparado para Android 17.
- DTouch es una app nueva: generá un keystore nuevo para DTouch. No uses ni necesites el keystore de J Touch.
- El `applicationId` nuevo (`com.dtouch.app`) permite publicar DTouch como app independiente sin tocar la ficha de J Touch.

---

## 15. Limitaciones conocidas / pendientes

1. **Compilado, testeado e instalado en Android 14 y Android 16**: el build de debug (`:app:assembleDebug`) y los tests unitarios (`:app:testDebugUnitTest`) pasan. Queda pendiente la revisión visual completa en Android 16–17.
2. **No hay tests de instrumentación**; solo tests unitarios de enums/defaults.
3. **No hay CI**.
4. **Sin revisión visual completa en Android 16/17**: el APK apunta a API 37 y compila con SDK 37; en POCO Android 16 se probaron el servicio y la CPU con pantalla apagada.
5. **Sin snap a bordes**: el orbe queda donde se suelta.
6. **Sin recorte circular automático**: la imagen personalizada se muestra completa (`FIT_CENTER`). Para un acabado circular, usar un PNG ya recortado.
7. **Sin haptics configurables**: solo el long press usa el haptic del sistema.
8. **Sin ocultar en apps específicas**.
9. **Sin captura, panel rápido, root ni listener** (fuera de alcance).
10. **`TYPE_ACCESSIBILITY_OVERLAY`** puede fallar en algunos OEM; el fallback está documentado.
11. **MainActivity en portrait**; el orbe sí funciona en landscape.
12. **Idiomas**: inglés y español. Para agregar otro, crear `values-xx/strings.xml`.
13. **DeviceAdmin en Android 6–8** puede impedir la desinstalación hasta desactivarlo.
14. **Edge-to-edge Android 15–17**: la Activity maneja insets; el overlay usa pantalla completa. Probar en dispositivo real.
15. **El icono legacy de API 23–25** es un PNG generado para DTouch; se puede reemplazar por un arte definitivo en `mipmap-*`.
16. **Android 16–17 (API 36–37)**: targetSdk 37, compilado con SDK 37 y Build Tools 36.0.0. Android 16 probado en POCO (servicio y CPU screen-off); falta la revisión visual completa y Android 17. Android 16 es el mínimo actual de Google Play (31/08/2026).

---

## 16. Referencia del proyecto original

El export de jadx se conserva en el módulo original:

```text
../../app
```

No modificar esa carpeta: es la referencia histórica. Allí están:

- `../../app/src/main/java/com/p037bs/smarttouch/SmartTouchService.java`: lógica original de overlay, gestos y acciones.
- `../../app/src/main/java/com/p037bs/smarttouch/ViewOnTouchListenerC0404b.java`: botón flotante original.
- `../../app/src/main/java/com/p037bs/smarttouch/AlertDialogC0405c.java`: diálogo de estilo/posición.
- `../../app/src/main/java/com/p037bs/smarttouch/MainActivity.java`: pantalla principal original.
- `../../app/src/main/res/values/strings.xml` y `../../app/src/main/res/values/arrays.xml`: textos y catálogo de acciones.

### Diferencias con J Touch

| J Touch original | DTouch |
|---|---|
| Paquete `com.bs.smarttouch.gp` | Paquete `com.dtouch.app`; se pueden instalar juntas |
| Actualización de la ficha de J Touch | App nueva, firma y ficha de Play propias |
| Muchas acciones, root, captura, panel rápido | Solo 6 acciones de navegación/bloqueo |
| `TYPE_APPLICATION_OVERLAY` + `SYSTEM_ALERT_WINDOW` | `TYPE_ACCESSIBILITY_OVERLAY`, sin overlay permission |
| `PreferenceActivity` + ListView | Activity con filas propias |
| Iconos personalizados, tamaño, alfa, idle alpha | Orbe de 32–80 dp, opacidad 0–100 %, imagen personalizada y vista previa |
| Inglés + chino | Inglés + español |
| Java decompilado/ofuscado | Kotlin limpio y comentado |
| Sin tests | Tests unitarios básicos |
| Pro/IAP | Sin IAP |

---

## 17. Créditos

- Reconstrucción: DTouch.
- Inspirado en el comportamiento de J Touch 1.7.0-GP, sin reutilizar su identidad, paquete, firma ni ficha de Play.
- AndroidX, AppCompat y Material Components son Apache 2.0.
- El orbe y los iconos de DTouch son un diseño simple generado para este proyecto.
