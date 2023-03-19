package com.bobocode;

import com.bobocode.entity.Person;
import com.bobocode.session.SessionImpl;
import com.bobocode.session.SessionFactoryImpl;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Demo {

    public static void main(String[] args) {
        SessionFactoryImpl sessionFactory = getSessionFactory();
        SessionImpl session = sessionFactory.createSession();

        Person firstPerson = session.findById(Person.class, 1L);
        Person secondPerson = session.findById(Person.class, 2L);

        System.out.println(firstPerson);
        System.out.println(secondPerson);
    }

    private static SessionFactoryImpl getSessionFactory() {
        return new SessionFactoryImpl(getDataSource());
    }

    private static DataSource getDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }

}
