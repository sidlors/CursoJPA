package jpa;

import jpa.entity.*;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
			persistGeek(entityManager);
			loadPersons(entityManager);
			addPhones(entityManager);
			createProject(entityManager);
			queryProject(entityManager);
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

	private void queryProject(EntityManager entityManager) {
		TypedQuery<Project> query = entityManager.createQuery("from Project p where p.projectPeriod.startDate = :startDate", Project.class).setParameter("startDate", createDate(1, 1, 2015));
		List<Project> resultList = query.getResultList();
		for (Project project : resultList) {
			LOGGER.info(project.getProjectPeriod().getStartDate().toString());
		}
	}

	private void createProject(EntityManager entityManager) {
		List<Geek> resultList = entityManager.createQuery("from Geek where favouriteProgrammingLanguage = :fpl", Geek.class).setParameter("fpl", "Java").getResultList();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		Project project = new Project();
		project.setTitle("Java Project");
		project.setProjectType(Project.ProjectType.TIME_AND_MATERIAL);
		Period period = new Period();
		period.setStartDate(createDate(1, 1, 2015));
		period.setEndDate(createDate(31, 12, 2015));
		project.setProjectPeriod(period);
		for (Geek geek : resultList) {
			project.getGeeks().add(geek);
			geek.getProjects().add(project);
		}
		entityManager.persist(project);
		transaction.commit();
	}

	private Date createDate(int day, int month, int year) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.DAY_OF_MONTH, day);
		gc.set(Calendar.MONTH, month - 1);
		gc.set(Calendar.YEAR, year);
		gc.set(Calendar.HOUR_OF_DAY, 0);
		gc.set(Calendar.MINUTE, 0);
		gc.set(Calendar.SECOND, 0);
		gc.set(Calendar.MILLISECOND, 0);
		return new Date(gc.getTimeInMillis());
	}

	private void addPhones(EntityManager entityManager) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Person> query = builder.createQuery(Person.class);
		Root<Person> personRoot = query.from(Person.class);
		query.where(builder.and(
				builder.equal(personRoot.get("firstName"), "Homer"),
				builder.equal(personRoot.get("lastName"), "Simpson")));
		List<Person> resultList = entityManager.createQuery(query).getResultList();
		for (Person person : resultList) {
			Phone phone = new Phone();
			phone.setNumber("+49 1234 456789");
			entityManager.persist(phone);
			person.getPhones().add(phone);
			phone.setPerson(person);
		}
		transaction.commit();
	}

	private void persistPerson(EntityManager entityManager) {
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			Person person = new Person();
			person.setFirstName("Homer");
			person.setLastName("Simpson");
			entityManager.persist(person);
			IdCard idCard = new IdCard();
			idCard.setIdNumber("4711");
			idCard.setIssueDate(new Date());
			person.setIdCard(idCard);
			entityManager.persist(idCard);
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	private void persistGeek(EntityManager entityManager) {
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		Geek geek = new Geek();
		geek.setFirstName("Gavin");
		geek.setLastName("Coffee");
		geek.setFavouriteProgrammingLanguage("Java");
		entityManager.persist(geek);
		geek = new Geek();
		geek.setFirstName("Thomas");
		geek.setLastName("Micro");
		geek.setFavouriteProgrammingLanguage("C#");
		entityManager.persist(geek);
		geek = new Geek();
		geek.setFirstName("Christian");
		geek.setLastName("Cup");
		geek.setFavouriteProgrammingLanguage("Java");
		entityManager.persist(geek);
		transaction.commit();
	}

	private void loadPersons(EntityManager entityManager) {
		entityManager.clear();
		TypedQuery<Person> query = entityManager.createQuery("from Person p left join fetch p.phones", Person.class);
		List<Person> resultList = query.getResultList();
		for (Person person : resultList) {
			StringBuilder sb = new StringBuilder();
			sb.append(person.getFirstName()).append(" ").append(person.getLastName());
			if (person instanceof Geek) {
				Geek geek = (Geek)person;
				sb.append(" ").append(geek.getFavouriteProgrammingLanguage());
			}
			IdCard idCard = person.getIdCard();
			if (idCard != null) {
				sb.append(" ").append(idCard.getIdNumber()).append(" ").append(idCard.getIssueDate());
			}
			List<Phone> phones = person.getPhones();
			for (Phone phone : phones) {
				sb.append(" ").append(phone.getNumber());
			}
			LOGGER.info(sb.toString());
		}
	}
}
