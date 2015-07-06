# GUIA JPA

El API de Persistence Java (JPA) es una especificación independiente de proveedor  para el mapeo de objetos Java a las tablas de bases de datos relacionales. Implementaciones de esta especificación permite a los desarrolladores de aplicaciones abstraer del producto de base de datos específica con la que están trabajando y les permiten implementar operaciones CRUD (crear, leer, actualizar y eliminar) las operaciones de tal manera que el mismo código funciona en diferentes productos de base de datos. Estos marcos no sólo manejan el código que interactúa con la base de datos (el código JDBC), sino también  mapear los tipos de estructuras de datos utilizadas por la aplicación.

Los 3 componentes de JPA son:

*  Entidades(Entities): En las versiones actuales las entidades JPA son POJO's. Las versiones anteriores de JPA se obligados a extender como subclase  de las clases proporcionadas por JPA, pero este enfoque hacía más difíciles realizar pruebas debido a dichas dependecies, las nuevas versiones de JPA no requieren que las entidades sean subclase de alguna clase de Framework.

* Metadatos objeto-relacional: El desarrollador de la aplicación debe proporcionar la asignación entre las clases Java y sus atributos a las tablas y columnas de la base de datos. Esto se puede hacer cualquiera de los archivos de configuración mínimas dedicados o en la versión más reciente también por anotaciones.

* Java Persistence Query Language (JPQL): Como JPA tiene como objetivo abstracto a partir del producto de base de datos específica, el framework también proporciona un langauge consultas dedicado que se puede utilizar en lugar de SQL. Esta traducción de JPQL a SQL permite que las implementaciones del framework de soporte a diferentes dialectos de bases de datos y permite que el desarrollador ejecutar consultas en una base de datos de forma independiente asu vendor.

En este tutorial vamos a través de diferentes aspectos del framework y desarrollaremos una sencilla aplicación Java SE que almacena y recupera datos desde una base de datos relacional. Usaremos las siguientes bibliotecas/entornos:

* maven >= 3.0 como tool de Build
* JPA 2.0 contenida en en Java Enterprise Edition (JEE) 6.0
* Framework Hibernate como una implementacion de JPA (4.3.8.Final)
* H2 como base relacional version 1.3.176


###Project setup

Como primer paso vamos a crear un proyecto simple maven desde linea de comandos:


>mvn archetype:create -DgroupId=com.javacodegeeks.ultimate -DartifactId=jpa

```
01|-- src
02|   |-- main
03|   |   `-- java
04|   |       `-- com
05|   |           `-- javacodegeeks
06|   |                `-- ultimate
07|   `-- test
08|   |   `-- java
09|   |       `-- com
10|   |           `-- javacodegeeks
11|   |                `-- ultimate
12`-- pom.xml
```

The libraries our implementation depends on are added to the dependencies section of the pom.xml file in the following way:
```xml
<properties>
    <jee.version>7.0</jee.version>
    <h2.version>1.3.176</h2.version>
    <hibernate.version>4.3.8.Final</hibernate.version>
</properties>
<dependencies>
    <dependency>
        <groupId>javax</groupId>
        <artifactId>javaee-api</artifactId>
        <version>${jee.version}</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>${h2.version}</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-entitymanager</artifactId>
        <version>${hibernate.version}</version>
    </dependency>
</dependencies>
```

Para tener una mejor visión de conjunto de las versiones separadas, definimos cada versión como una propiedad Maven y referencia más adelante en la sección de dependencias.

3.1. EntityManager and Persistence Unit

Ahora empezamos a implementar nuestra primera funcionalidad JPA. Vamos a empezar con una clase simple que proporciona un método run() que se invoca en el método principal de la aplicación:

```java
public class Main {
    private static final Logger LOGGER = Logger.getLogger("JPA");
    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }


    public void run() {
        EntityManagerFactory factory = null;
        EntityManager entityManager = null;
        try {
            factory = Persistence.createEntityManagerFactory("PersistenceUnit");
            entityManager = factory.createEntityManager();
            persistPerson(entityManager);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }
    ...

```

###1. Casi toda la interacción con JPA se hace a través del EntityManager. Para obtener una instancia de un EntityManager, tenemos que crear una instancia de la EntityManagerFactory. Normalmente sólo necesitamos una EntityManagerFactory por  "unidad de persistencia" por aplicación. Una unidad de persistencia es un conjunto de clases de la JPA que se gestiona junto con la configuración de base de datos en un archivo llamado persistence.xml


```xml
<persistence xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd" version="1.0">
<persistence-unit name="PersistenceUnit" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.ejb.HibernatePersistence</provider>
        <properties>
            <property name="connection.driver_class" value="org.h2.Driver"/>
            <property name="hibernate.connection.url" value="jdbc:h2:~/jpa"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.hbm2ddl.auto" value="create"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
    </persistence>
```

Este archivo se crea en la carpeta src/main/resource/META-IN del proyecto Maven. Como se puede ver, definimos una unidad de persistencia  con el nombre *PersistenceUnit* que tiene el tipo de transacción RESOURCE_LOCAL. El tipo de transacción determina cómo las transacciones se manejan en la aplicación.

En nuestra aplicación de ejemplo no tenemos contenedor JEE por lo que tenemos que manejar las transacciones nosotros mismos, de ahí que se especifique  **RESOURCE_LOCAL**. Cuando se utiliza un contenedor JEE entonces el contenedor es responsable de la creación de la EntityManagerFactory y sólo le proporciona que EntityManager. El contenedor también se encarga del comienzo y final de cada transacción. En ese caso se proporcionará el valor **JTA**.
  

###2. En persistence.xml 

Se informa al proveedor de JPA sobre la base de datos que queremos utilizar. Esto se hace mediante la especificación del controlador JDBC que Hibernate debe utilizar. Como queremos usar la base de datos [H2](www.h2database.com), la propiedad **connection.driver_class** se establece en el valor org.h2.Driver.
 
  + Tenemos que decirle a Hibernate el dialecto JDBC que debe utilizar. Como Hibernate proporciona una implementación de dialecto dedicado para H2, elegimos éste con la propiedad **hibernate.dialect**. Con este dialecto de Hibernate es capaz de crear las sentencias SQL apropiados para la base de datos de H2.


Por último, pero no menos importante ofrecemos tres opciones que vienen muy útil en el desarrollo de una nueva aplicación, pero que no sería utilizado en entornos de producción. El primero de ellos es la propiedad **hibernate.hbm2ddl.auto** que le dice a Hibernate como crear todas las tablas a partir de cero desde el inicio. Si ya existe la tabla, se eliminará. En nuestra aplicación de ejemplo esta es una buena característica que podemos confiar en el hecho de que la base de datos está vacía en la a principios y que todos los cambios que hemos hecho en el esquema desde nuestra último inicio de la aplicación se reflejan en el esquema.

La segunda opción es **hibernate.show_sql** que se le dice a Hibernate para que imprima cada declaración SQL que se emite a la base de datos en la línea de comandos. Con esta opción habilitada podemos rastrear fácilmente todas las declaraciones y echar un vistazo si todo funciona como se esperaba. Y finalmente le decimos a Hibernate que imprima de una manera agradable la salida SQL para una mejor legibilidad estableciendo la  propiedad hibernate.format_sql en true.


 ###3. Regresando al la tecla...
 
Después de haber obtenido una instancia de la **EntityManagerFactory** y de ella una instancia de EntityManager podemos utilizarlos en el método **persistPerson** para salvar algunos datos en la base de datos. Ten en cuenta que después de lo que hemos hecho nuestro trabajo tenemos que cerrar tanto el EntityManager así como la EntityManagerFactory.
   + 4.1) Transacciones

El EntityManager representa una unidad de persistencia y por lo tanto vamos a necesitar en la aplicacion **RESOURCE_LOCAL** sólo una instancia del EntityManager. Una unidad de persistencia es una memoria caché para las entidades que representan partes del estado almacenados en la base de datos, así como una conexión a la base de datos. Con el fin de almacenar datos en la base de datos, por lo tanto tenemos que pasarlo al EntityManager y con ello a la caché subyacente. En caso de que quiera crear una nueva fila en la base de datos, esto se hace invocando el método persist () en el EntityManager como se demuestra en el siguiente código:

```java
 private void persistPerson(EntityManager entityManager) {
 	EntityTransaction transaction = entityManager.getTransaction();
	try {
		transaction.begin();
		Person person = new Person();
		person.setFirstName("Homer");
		person.setLastName("Simpson");
		entityManager.persist(person);
		transaction.commit();
	} catch (Exception e) {
		if (transaction.isActive()) {
			transaction.rollback();
		}
	}
 }
 
 ```
 
 
 Pero antes de que podamos llamar a **persist()** tenemos que abrir una nueva transacción llamando **transaction.begin()** en un nuevo objeto de transacciones que hemos recuperado del EntityManager. Si omitimos este llamado, Hibernate podría lanzar una **IllegalStateException** que nos dice que nos hemos olvidado de ejecutar el persisten() dentro de una transacción:

Después de llamar a persistir () tenemos que confirmar (*commit*) la transacción, es decir, enviar los datos a la base de datos y almacenarla allí. En caso de que sea lanzada una excepción dentro del bloque try, tenemos que deshacer (*Rollback*) la transacción hemos comenzado antes. Pero como sólo podemos deshacer transacciones activas, tenemos que comprobar antes si la transacción actual ya está en marcha, ya que puede ocurrir que la excepción se produce dentro de la convocatoria **transaction.begin ()**.

###5. Tables

La clase Person es mapeada para a la tabla T_PERSON agregando la anotacion @Entity:

```java
@Entity
@Table(name = "T_PERSON")
public class Person {
	private Long id;
	private String firstName;
	private String lastName;
	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "FIRST_NAME")
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	@Column(name = "LAST_NAME")
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	}
```
 	
Por otro lado se puede especificar más información para cada columna usando los otros atributos que la anotación @Column ofrece:

```java
@Column(name = "FIRST_NAME", length = 100, nullable = false, unique = false)
```


Intentar insertar nulo en "FIRST_NAME" en esta tabla provocaría una violación de constraint en la base de datos y hacer que la transacción actual haga un rollback.

Las dos anotaciones @Id y @GeneratedValue dicen a JPA que este valor es la clave principal de esta tabla y que debe ser generado de forma automática


En el código de ejemplo anterior, hemos añadido las anotaciones JPA a los métodos getter para cada campo que se debe asignar a una columna de base de datos. Otra forma sería anotando el campo directamente en lugar de su método getter.


```java
@Entity
@Table(name = "T_PERSON")
public class Person {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "LAST_NAME")
    private String lastName;
    ...
```

Las dos formas son más o menos iguales, la única diferencia que tienen juega un papel cuando se desea anular anotaciones para los campos en subclases. Como veremos en el curso ulterior de este tutorial, es posible extender una entidad existente con el fin de heredar sus campos. Cuando ponemos las anotaciones JPA sobre el terreno, no podemos ignorar que lo que podamos reemplazando el método getter correspondiente.

Uno también tiene que prestar atención para guardar el camino para anotar entidades del mismo para jerarquía de una entidad. se puede mezclar la anotación de los campos y métodos dentro de un proyecto JPA, pero dentro de una entidad y todas sus subclases se debe ser consistente. Si tiene que cambiar la forma de anotación dentro de una jerarquía subclase, puede utilizar el acceso de anotaciones JPA para especificar que una determinada subclase utiliza de una manera diferente para anotar campos y métodos:
    
```java
@Entity
@Table(name = "T_GEEK")
@Access(AccessType.PROPERTY)

public class Geek extends Person {
...
```


El fragmento de código anterior le dice a JPA que esta clase va a utilizar las anotaciones en el nivel de método, mientras que la superclase puede tener anotaciones a nivel campo.


```SQL
Hibernate: drop table T_PERSON if exists

Hibernate: create table T_PERSON (id bigint generated by default as identity, FIRST_NAME varchar(255), LAST_NAME varchar(255), primary key (id))

Hibernate: insert into T_PERSON (id, FIRST_NAME, LAST_NAME) values (null, ?, ?)

```

Como podemos ver, Hibernate *Dropea* la tabla T_PERSON  si existe y vuelve a crearla después. Se crea la tabla con dos columnas de tipo varchar (255) (FIRST_NAME, LAST_NAME) y una columna llamada Identificación de tipo *big int*. La última columna se define como clave principal y sus valores son generadas automáticamente por la base de datos cuando insertamos un nuevo valor.

Podemos comprobar que todo es correcto con el Shell que se incluye con H2. Para utilizar esta Shell sólo tenemos la h2-1.3.176.jar archivo jar:

>java -cp h2-1.3.176.jar org.h2.tools.Shell -url jdbc:h2:~/jpa

```sql

sql> select * from T_PERSON;

ID | FIRST_NAME | LAST_NAME
1  | Homer      | Simpson

(4 rows, 4 ms)

```

El resultado del query anterior muestra que la tabla T_PERSON realmente contiene un registro con id 1 y con valores  en first name y lastname


###4. Herencia

Después de haber llevado a cabo la configuracio0n en este caso de uso fácil, nos vamos ahora a considerar casos de uso más complejos. 

Supongamos que queremos almacenar junto a personas también información sobre los aficiones-geek y de su lenguaje de programación favorito. Como los *geeks* también son personas, nos modelamos esto en el mundo Java como relación subclase de persona:


```java
@Entity
@Table(name = "T_GEEK")
public class Geek extends Person {

	private String favouriteProgrammingLanguage;
	
	private List<Project> projects = new ArrayList<Project>();
	
	@Column(name = "FAV_PROG_LANG")
	
	public String getFavouriteProgrammingLanguage() {
	
			return favouriteProgrammingLanguage;
			
	}
	
	public void setFavourit frrzfdxeProgrammingLanguage(String favouriteProgrammingLanguage) {
	
		this.favouriteProgrammingLanguage = favouriteProgrammingLanguage;
		
	}
	...
	}
```

Agregando las anotaciones @Entity y @Table a la clase le deja a Hibernate crear la nueva tabla T_GEEK:


