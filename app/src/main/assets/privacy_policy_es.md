# Política de privacidad de NavXS

Última actualización: 14 de septiembre de 2026

## 1. Información general

NavXS es una aplicación Android que puede mostrar un overlay de navegación con botones fijos para aplicaciones seleccionadas.

El desarrollador de esta aplicación solo recopila, almacena o procesa datos personales en la medida técnicamente necesaria para el funcionamiento de la aplicación.

## 2. Procesamiento local en el dispositivo

NavXS procesa la información generalmente de forma local en el dispositivo.

Esto incluye en particular:

- el nombre de paquete de la aplicación actualmente en primer plano
- la selección de aplicaciones para las que debe activarse el overlay
- los ajustes de overlay definidos por el usuario, como posiciones, colores, tamaños, transparencia y temas
- el estado de si el servicio de accesibilidad está activado
- el URI de una imagen que el usuario selecciona opcionalmente como fondo del editor de posicionamiento, junto con la opacidad elegida para ella

## 3. Finalidad del procesamiento

Esta información se procesa exclusivamente para:

- detectar si el overlay debe mostrarse para la aplicación actualmente abierta
- mostrar u ocultar el overlay
- proporcionar las acciones de navegación configuradas
- guardar y restaurar localmente los ajustes seleccionados por el usuario

## 4. Servicio de accesibilidad

NavXS utiliza la API del servicio de accesibilidad de Android (AccessibilityService). NavXS no es una herramienta de accesibilidad para personas con discapacidad; el servicio es necesario para la función principal de la app: el overlay de navegación.

### 4.1 Consentimiento

Antes de solicitar el servicio de accesibilidad, NavXS muestra un aviso que explica cómo se utiliza el servicio. El usuario elige entre dos botones: «Rechazar» y «Aceptar». Solo después de pulsar «Aceptar», NavXS abre los ajustes de accesibilidad de Android, donde el usuario debe activar el servicio manualmente. «Rechazar» cierra la app sin activar nada. Salir del aviso de cualquier otra forma no se considera consentimiento. El servicio puede desactivarse en cualquier momento en los ajustes de accesibilidad de Android.

### 4.2 Para qué se utiliza el servicio

El servicio se utiliza exclusivamente para:

- detectar el nombre de paquete de la app que se está ejecutando en primer plano
- decidir si el overlay debe mostrarse para esa app
- mostrar los botones de navegación flotantes sobre las apps seleccionadas
- ejecutar las acciones de navegación Atrás, Inicio y Apps recientes cuando el usuario pulsa el botón correspondiente

### 4.3 Lo que el servicio no hace

Técnicamente, el servicio de accesibilidad tiene acceso a la información de las ventanas de la app en primer plano. NavXS utiliza este acceso únicamente para determinar el nombre de paquete de esa app. NavXS no lee, almacena ni transmite textos escritos, contraseñas, contenido de mensajes ni ningún otro contenido de la pantalla. NavXS no realiza acciones en nombre del usuario, salvo las acciones de navegación mencionadas, y solo cuando el usuario pulsa un botón.

Este servicio no se utiliza con fines publicitarios, de seguimiento ni de análisis, y los datos no se venden ni se comparten.

## 5. Sin transmisión a servidores externos

Actualmente, NavXS no transmite las aplicaciones seleccionadas, los nombres de paquetes de aplicaciones en primer plano ni los ajustes del overlay a servidores externos u otros servicios externos.

El procesamiento tiene lugar localmente en el dispositivo.

## 6. Almacenamiento de datos

Los ajustes y selecciones de aplicaciones almacenados por NavXS se guardan localmente en el dispositivo.

Estos datos permanecen en el dispositivo hasta que el usuario los modifica, se eliminan borrando los datos de la aplicación, o se borran al desinstalar la aplicación.

Si el usuario selecciona una captura de pantalla como fondo del editor de posicionamiento, NavXS guarda únicamente el URI de esa imagen junto con el permiso de lectura concedido por el sistema. El archivo de imagen en sí no se copia, no se guarda en un directorio de la app, no se transfiere al servicio de accesibilidad y no se transmite. Al restablecer el fondo se elimina el URI guardado y se libera de nuevo el permiso de lectura.

## 7. Permisos

NavXS requiere en particular el permiso de accesibilidad activado explícitamente por el usuario para que la aplicación pueda proporcionar su funcionalidad de overlay.

Además, solo se utilizan los accesos técnicamente necesarios para el funcionamiento de la aplicación.

## 8. Terceros

Actualmente, NavXS no utiliza servicios de terceros, herramientas de análisis ni publicidad para procesar los datos descritos anteriormente.

## 9. Cambios en esta política de privacidad

Esta política de privacidad puede actualizarse si es necesario para reflejar cambios en la aplicación o requisitos legales.

## 10. Contacto

Si tiene alguna pregunta sobre privacidad, puede contactar al desarrollador.

Correo electrónico: navxs@robnice.de
