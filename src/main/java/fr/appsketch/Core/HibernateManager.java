package fr.appsketch.Core;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

import java.util.Set;

public class HibernateManager {
    private static final SessionFactory sessionFactory;

    static {
        System.out.println("Initialisation de Hibernate...");

        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:test.db");
        configuration.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.show_sql", "true");

        Reflections reflections = new Reflections("fr.appsketch");
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);
        for (Class<?> entity : entities) {
            configuration.addAnnotatedClass(entity);
            System.out.println("Enregistré comme entité : " + entity.getName());
        }

        StandardServiceRegistryBuilder builder =
                new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());

        sessionFactory = configuration.buildSessionFactory(builder.build());
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}
