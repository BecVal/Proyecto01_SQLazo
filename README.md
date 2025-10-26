# Proyecto 01: PumaBank

## Equipo: SQLazo

  - [César Becerra Valencia (322064287)](#César)
  - [Victor Abraham Sánchez Morgado (322606003)](#Victor)
  - [José Luis Cortes Nava (322115437)](#Luis)

Este proyecto es una solución al proyecto 01 de **Modelado y Programación** de la Facultad de Ciencias (UNAM). El objetivo es implementar un sistema para un banco digital llamado **PumaBank**. El sistema debe ser capaz de manejar cuentas bancarias que pueden cambiar de estado, tener diferentes políticas de interés, operar de forma segura, notificar eventos y permitir la contratación de servicios adicionales combinables.

Los patrones de diseño trabajados en esta práctica son: **State, Strategy, Observer, Proxy, Decorator y Facade**.

## Dos maneras de compilar y ejecutar este proyecto:

### Con JDK

Prerrequisitos:

Este proyecto se puede compilar y ejecutar si se tiene instalado JDK (Java Development Kit) versión 17 o superior.

Cómo compilar y ejecutar:

Puedes compilar y ejecutar el programa directamente desde la terminal usando los comandos `javac` y `java`.

**Asegúrate de estar en el directorio raíz del proyecto (`Proyecto01_PumaBank/`) antes de ejecutar los siguientes comandos.**

1.  Compilación

El siguiente comando compilará todos los archivos `.java` que se encuentran en el directorio `src/` y dejará los archivos `.class` compilados en un nuevo directorio llamado `out/`.

```bash
javac -d out -sourcepath src src\mx\unam\ciencias\myp\pumabank\Main.java
```

  * **`javac`**: Es el compilador de Java.
  * **`-d out`**: Le indica al compilador que coloque los archivos compilados (`.class`) en una carpeta llamada `out`.
  * **`src/main/java/mx/unam/ciencias/pumabank/**/*.java`**: Indica la ruta donde se encuentran los archivos `.java` a compilar.

<!-- end list -->

2.  Ejecución

Una vez compilado, puedes ejecutar el programa con el siguiente comando (asumiendo que tu clase principal se llama `App` en el paquete raíz `mx.unam.ciencias.pumabank`):

```bash
java -cp out mx.unam.ciencias.myp.pumabank.Main
```

  * **`java`**: Es la Máquina Virtual de Java (JVM) que ejecuta el código.
  * **`-cp out`** (`-cp` es una abreviatura de `--class-path`): Le indica a la JVM que busque los archivos `.class` en el directorio `out`.
  * **`mx.unam.ciencias.pumabank.App`**: Es el nombre completamente calificado de la clase que contiene el método `main` que queremos ejecutar.

### Con Docker

Prerrequisitos:

Para ejecutar el programa de java con Docker es necesario tener instalado Docker Desktop y tener abierta la aplicación en todo momento.
Este es el link para instalarlo en Ubuntu: [https://docs.docker.com/desktop/setup/install/linux/ubuntu/](https://docs.docker.com/desktop/setup/install/linux/ubuntu/)
Este es el link para instalarlo en Windows: [https://docs.docker.com/desktop/windows/install/](https://docs.docker.com/desktop/windows/install/)
Este es el link para instalarlo en Mac: [https://docs.docker.com/desktop/mac/install/](https://docs.docker.com/desktop/mac/install/)

Cómo compilar y ejecutar:




1.  Descargar la imagen

El comando para descargar la imagen estando en el directorio raíz de la práctica es el siguiente:

```bash
docker build -t pumabank .
```

  * **`docker build`**: Indica a Docker que debe construir una imagen en base al `Dockerfile` que se encuentra en la raíz de la práctica.
  * **`-t pumabank`**: Etiqueta la imagen con el nombre `pumabank` para no tener que usar un ID predeterminado.
  * **`.`**: Indica a Docker que los archivos a copiar y el `Dockerfile` se encuentran en el directorio actual.

<!-- end list -->

2.  Ejecutar el contenedor

Este comando ejecuta un contenedor basado en la imagen que construimos en el paso anterior:

```bash
docker run --rm -it pumabank
```

  * **`docker run`**: Da la instrucción a Docker de crear y ejecutar un contenedor.
  * **`pumabank`**: El nombre de la imagen en la que se basa el contenedor.
  * **`--rm`**: Borra el contenedor al terminar de ejecutarlo.
  * **`-it`**: Indica que el contenedor debe ser interactivo para que podamos interactuar con el programa en tiempo real.

## Análisis de Patrones de Diseño Utilizados

Los patrones cumplen objetivos clave como la flexibilidad, extensibilidad y desacoplamiento de las clases. Estos son los patrones utilizados durante la implementación y el problema que resuelven:

### State

**Problema que Resuelve:** El comportamiento de una cuenta bancaria depende radicalmente de su estado actual. El enunciado especifica los estados: **Active, Overdrawn, Frozen y Closed**.
**Solución:** En lugar de tener un método `withdraw()` con un `switch` o múltiples `if-else` para comprobar el estado, el patrón **State** nos permite encapsular el comportamiento específico de cada estado en su propia clase. Creamos una interfaz `AccountState` y clases concretas como `ActiveState`, `OverdrawnState`, etc. La clase `Account` delega la operación a su objeto de estado actual, permitiendo cambiar de estado dinámicamente sin alterar la clase `Account`.

### Strategy

**Problema que Resuelve:** El banco debe ofrecer "distintos esquemas de interés para adaptarse a cada perfil (Monthly, Annual, Premium)". Estos esquemas son algoritmos intercambiables.
**Solución:** El patrón **Strategy** nos permite definir una familia de algoritmos (los cálculos de interés), encapsular cada uno en una clase separada y hacerlos intercambiables. Creamos la interfaz `InterestCalculation` y clases concretas como `MonthlyInterest` y `AnnualInterest`. La clase `Account` "tiene una" estrategia de interés y simplemente la utiliza, sin saber los detalles de su implementación. Esto permite añadir nuevos planes de interés en el futuro sin modificar `Account`.

### Observer

**Problema que Resuelve:** El sistema necesita "notificar en tiempo real sobre cualquier evento importante" y "generar un archivo .txt con el registro mensual". Múltiples componentes (un logger, un notificador push, etc.) necesitan reaccionar a eventos en la cuenta.
**Solución:** El patrón **Observer** crea una relación de suscripción donde `Account` actúa como el "Sujeto". Mantiene una lista de "Observadores" (`Observer`). Cuando ocurre un evento (ej. un retiro), `Account` notifica a todos sus observadores llamando a su método `notify(String event)`. Esto desacopla a `Account` de los notificadores; ella no sabe (ni le importa) qué hacen, solo les avisa.

### Proxy

**Problema que Resuelve:** Se requiere un "acceso remoto de manera segura por medio de un mecanismo de verificación (NIP)". Las operaciones como retiros o consultas de saldo deben ser validadas antes de ejecutarse.
**Solución:** El patrón **Proxy** (específicamente un *Proxy de Protección*) actúa como un intermediario que implementa la misma interfaz `IAccount` que la `Account` real. El cliente interactúa con el `AccountProxy`. Cuando se invoca `proxy.withdraw(monto, nip)`, el proxy primero realiza la lógica de autenticación del NIP. Si es exitosa, delega la llamada a `Account` real. Si falla, niega el acceso. Esto añade la capa de seguridad sin contaminar la lógica de negocio de la clase `Account`.

### Decorator

**Problema que Resuelve:** El banco ofrece "servicios complementarios que el cliente podrá contratar opcionalmente" (seguro antifraude, programa de recompensas, alertas premium) y estos "son combinables".
**Solución:** Usar herencia para crear todas las combinaciones posibles (`CuentaConSeguro`, `CuentaConSeguroYRecompensas`, etc.) es inviable. El patrón **Decorator** nos permite "envolver" un objeto `ICuenta` con nuevas responsabilidades de forma dinámica. Creamos una clase abstracta `AccountDecorator` y clases concretas como `AntiFraudDecorator`. Un cliente puede tener una cuenta que sea `new PremiumAlertsDecorator(new AntiFraudDecorator(new Account(...)))`. Cada decorador añade su funcionalidad y luego llama al método del objeto que envuelve.

### Facade

**Problema que Resuelve:** El sistema bancario se vuelve complejo. Para crear una cuenta funcional, se necesita instanciar un `Client`, un `Account`, asignarle un `State` inicial, una `Strategy` de interés, envolverla en un `Proxy` y, opcionalmente, en varios `Decorator`.
**Solución:** El patrón **Facade** provee una interfaz unificada y simple para este subsistema complejo. Creamos la clase `PumaBankFacade` que se convierte en el único punto de entrada para el usuario (el `Main.java`). Esta fachada ofrece métodos simples como `createAccount(String clientId, double initialBalance, String pin, String interestType, List<String> services)` o `withdraw(String accountId, double amount, String pin)`, ocultando toda la lógica de creación y orquestación de objetos.