package fr.appsketch.Core;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HibernateManagerTest {

    @Test
    void testGetSessionFactory_ReturnsSingleton() {
        SessionFactory factory1 = HibernateManager.getSessionFactory();
        SessionFactory factory2 = HibernateManager.getSessionFactory();

        assertNotNull(factory1);
        assertSame(factory1, factory2, "Should return the same instance (singleton)");
    }

    @Test
    void testGetSessionFactory_IsNotClosed() {
        SessionFactory factory = HibernateManager.getSessionFactory();

        assertNotNull(factory);
        assertFalse(factory.isClosed(), "SessionFactory should be open");
    }

    @Test
    void testOpenSession_ReturnsValidSession() {
        SessionFactory factory = HibernateManager.getSessionFactory();
        Session session = factory.openSession();

        assertNotNull(session);
        assertTrue(session.isOpen());

        session.close();
    }

    @Test
    void testMultipleSessions_WorkIndependently() {
        SessionFactory factory = HibernateManager.getSessionFactory();
        Session session1 = factory.openSession();
        Session session2 = factory.openSession();

        assertNotNull(session1);
        assertNotNull(session2);
        assertNotSame(session1, session2, "Each call should return a new Session");

        session1.close();
        session2.close();
    }
}
