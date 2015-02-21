# jpa-guide
guia de JPA 


1)  Nearly all interaction with the JPA is done through the EntityManager. To obtain an instance of an EntityManager, we have to create an instance of the EntityManagerFactory. Normally we only need one EntityManagerFactory for one “persistence unit” per application. A persistence unit is a set of JPA classes that is managed together with the database configuration in a file called persistence.xml:

<?xml version="1.0" encoding="UTF-8" ?>
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence
 http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd" version="1.0">

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


This file is created in the src/main/resource/META-INF folder of the maven project. As you can see, we define one persistence-unit with the name PersistenceUnit that has the transaction-type RESOURCE_LOCAL. The transaction-type determines how transactions are handled in the application.

In our sample application we want to handle them on our own, hence we specify here RESOURCE_LOCAL. When you use a JEE container then the container is responsible for setting up the EntityManagerFactory and only provides you he EntityManager. The container then also handles the begin and end of each transaction. In this case you would provide the value JTA.


2) in persistence.xml is to inform the JPA provider about the database we want to use. This is done by specifying the JDBC driver that Hibernate should use. As we want to use the H2 database (www.h2database.com), the property connection.driver_class is set to the value org.h2.Driver.

3) We have to tell Hibernate is the JDBC dialect that it should use. As Hibernate provides a dedicated dialect implementation for H2, we choose this one with the property hibernate.dialect. With this dialect Hibernate is capable of creating the appropriate SQL statements for the H2 database.

Last but not least we provide three options that come handy when developing a new application, but that would not be used in production environments. The first of them is the property hibernate.hbm2ddl.auto that tells Hibernate to create all tables from scratch at startup. If the table already exists, it will be deleted. In our sample application this is a good feature as we can rely on the fact that the database is empty at the beginnig and that all changes we have made to the schema since our last start of the application are reflected in the schema.

The second option is hibernate.show_sql that tells Hibernate to print every SQL statement that it issues to the database on the command line. With this option enabled we can easily trace all statements and have a look if everything works as expected. And finally we tell Hibernate to pretty print the SQL for better readability by setting the property hibernate.format_sql to true.