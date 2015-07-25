package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import jpa.Main;
import jpa.entity.Person;

import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.Test;

public class TransactionsTest {
	
	
	private static final Logger out = Logger
			.getLogger(TransactionsTest.class);
	private Main main;
	private List<Person> listPerson=null;



	@Before
	public void setUp() throws Exception {
		
		Main main=new  Main(); 
	}

	@Test
	public void test() {
		EntityManagerFactory factory = null;
		EntityManager entityManager = null;
		try {
			factory = Persistence.createEntityManagerFactory("PersistenceUnit");
			entityManager = factory.createEntityManager();
			assertNotNull(factory);
			assertNotNull(entityManager);
			main.persistGeek(entityManager);
//			listPerson=main.loadPersons(entityManager);
//			assertNotNull(listPerson);
//			out.error(listPerson.size());
//			
//			for (Person p : listPerson){
//				
//				out.info(p.getFirstName()+" "+p.getPhones());
//			}
		}catch (Exception e) {
			out.error(e.getMessage());
		}finally {
			if (entityManager != null) {
				entityManager.close();
			}
			if (factory != null) {
				factory.close();
			}
		}
		
		
	}

}
