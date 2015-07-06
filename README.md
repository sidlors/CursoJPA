# jpa-guide



  1. Casi toda la interacción con JPA se hace a través del EntityManager. Para obtener una instancia de un EntityManager, tenemos que crear una instancia de la EntityManagerFactory. Normalmente sólo necesitamos una EntityManagerFactory por  "unidad de persistencia" por aplicación. Una unidad de persistencia es un conjunto de clases de la JPA que se gestiona junto con la configuración de base de datos en un archivo llamado persistence.xml


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
  

  2. En persistence.xml se informa al proveedor de JPA sobre la base de datos que queremos utilizar. Esto se hace mediante la especificación del controlador JDBC que Hibernate debe utilizar. Como queremos usar la base de datos [H2](www.h2database.com), la propiedad **connection.driver_class** se establece en el valor org.h2.Driver.
  3.  Tenemos que decirle a Hibernate el dialecto JDBC que debe utilizar. Como Hibernate proporciona una implementación de dialecto dedicado para H2, elegimos éste con la propiedad **hibernate.dialect**. Con este dialecto de Hibernate es capaz de crear las sentencias SQL apropiados para la base de datos de H2.


Por último, pero no menos importante ofrecemos tres opciones que vienen muy útil en el desarrollo de una nueva aplicación, pero que no sería utilizado en entornos de producción. El primero de ellos es la propiedad **hibernate.hbm2ddl.auto** que le dice a Hibernate como crear todas las tablas a partir de cero desde el inicio. Si ya existe la tabla, se eliminará. En nuestra aplicación de ejemplo esta es una buena característica que podemos confiar en el hecho de que la base de datos está vacía en la a principios y que todos los cambios que hemos hecho en el esquema desde nuestra último inicio de la aplicación se reflejan en el esquema.

La segunda opción es **hibernate.show_sql** que se le dice a Hibernate para que imprima cada declaración SQL que se emite a la base de datos en la línea de comandos. Con esta opción habilitada podemos rastrear fácilmente todas las declaraciones y echar un vistazo si todo funciona como se esperaba. Y finalmente le decimos a Hibernate que imprima de una manera agradable la salida SQL para una mejor legibilidad estableciendo la  propiedad hibernate.format_sql en true.


 4. Returning to code...
 
Después de haber obtenido una instancia de la EntityManagerFactory y de ella una instancia de EntityManager podemos utilizarlos en el método **persistPerson** para salvar algunos datos en la base de datos. Ten en cuenta que después de lo que hemos hecho nuestro trabajo tenemos que cerrar tanto el EntityManager así como la EntityManagerFactory.
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

Después de llamar a persistir () tenemos que confirmar (*commit*) la transacción, es decir, enviar los datos a la base de datos y almacenarla allí. En caso de que sea lanzada una excepción dentro del bloque try, tenemos que deshacer (*Rollback*) la transacción hemos comenzado antes. Pero como sólo podemos deshacer transacciones activas, tenemos que comprobar antes si la transacción actual ya está en marcha, ya que puede ocurrir que la excepción se produce dentro de la convocatoria transaction.begin ().

After calling persist() we have to commit the transaction, i.e. send the data to the database and store it there. In case an exception is thrown within the try block, we have to rollback the transaction we have begun before. But as we can only rollback active transactions, we have to check before if the current transaction is already running, as it may happen that the exception is thrown within the transaction.begin() call.


<h3> 4.2) Tables</h3>

<p>The class Person is mapped to the database table T_PERSON by adding the annotation @Entity:</p>

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
 	
 	
<p>On the other hand you can specify more information for each column by using the other attributes that the @Column annotation provides:</p>

```java
@Column(name = "FIRST_NAME", length = 100, nullable = false, unique = false)
```

<p>Trying to insert null as first name into this table would provoke a constraint violation on the database and cause the current transaction to roll back.</p>

<p>The two annotations @Id and @GeneratedValue tell JPA that this value is the primary key for this table and that it should be generated automatically.</p> 	


<p>In the example code above we have added the JPA annotations to the getter methods for each field that should be mapped to a database column. Another way would be to annotate the field directly instead of the getter method:</p>

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
    
<p>The two ways are more or less equal, the only difference they have plays a role when you want to override annotations for fields in subclasses. As we will see in the further course of this tutorial, it is possible to extend an existing entity in order to inherit its fields. When we place the JPA annotations at the field level, we cannot override them as we can by overriding the corresponding getter method.


<br/>
One also has to pay attention to keep the way to annotate entities the same for one entity hierarchy. You can mix annotation of fields and methods within one JPA project, but within one entity and all its subclasses is must be consistent. If you need to change the way of annotatio within a subclass hierarchy, you can use the JPA annotation Access to specify that a certain subclass uses a different way to annotate fields and methods:</p>


```java
@Entity
@Table(name = "T_GEEK")
@Access(AccessType.PROPERTY)

public class Geek extends Person {
...
```

<p>
The code snippet above tells JPA that this class is going to use annotations on the method level, whereas the superclass may have used annotations on field level.</p>

<br/>
When we run the code above, Hibernate will issue the following queries to our local H2 database:
<br/><br/>
<code>
Hibernate: drop table T_PERSON if exists

Hibernate: create table T_PERSON (id bigint generated by default as identity, FIRST_NAME varchar(255), LAST_NAME varchar(255), primary key (id))

Hibernate: insert into T_PERSON (id, FIRST_NAME, LAST_NAME) values (null, ?, ?)
</code>

<p>As we can see, Hibernate first drops the table T_PERSON if it exists and re-creates it afterwards. If creates the table with two columns of the type varchar(255) (FIRST_NAME, LAST_NAME) and one column named id of type bigint. The latter column is defined as primary key and its values are automatically generated by the database when we insert a new value.
<br/>
We can check that everything is correct by using the Shell that ships with H2. In order to use this Shell we just need the jar archive h2-1.3.176.jar:</p>


>java -cp h2-1.3.176.jar org.h2.tools.Shell -url jdbc:h2:~/jpa



```sql

sql> select * from T_PERSON;

ID | FIRST_NAME | LAST_NAME
1  | Homer      | Simpson

(4 rows, 4 ms)

```

The query result above shows us that the table T_PERSON actually contains one row with id 1 and values for first name and last name.


###4. Inheritance

After having accomplished the setup and this easy use case, we turn towards some more complex use cases. Let’s assume we want to store next to persons also information about geeks and about their favourite programming language. As geeks are also persons, we would model this in the Java world as subclass relation to Person:

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

<p>Adding the annotations @Entity and @Table to the class lets Hibernate create the new table T_GEEK:</p>


