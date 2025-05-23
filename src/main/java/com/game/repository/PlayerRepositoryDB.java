package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.connection.useUnicode", true);
        properties.put("hibernate.connection.characterEncoding", "UTF-8");
        properties.put("hibernate.connection.charSet", "UTF-8");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "mysql-root-password");

        this.sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()){
            NativeQuery<Player> query = session.createNativeQuery("SELECT * FROM rpg.player", Player.class);
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()){
            Query<Long> playerGetAllCount = session.createNamedQuery("player_getAllCount", Long.class);
            return playerGetAllCount.uniqueResult().intValue();
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();

            session.save(player);
            transaction.commit();
            return player;
        } catch (RuntimeException e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();
            throw e;
        }

    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Player merge = (Player) session.merge(player);
            transaction.commit();
            return merge;
        } catch (RuntimeException e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();
            throw e;
        }

    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Player player = session.get(Player.class, id);
            return Optional.ofNullable(player);
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        } catch (RuntimeException e) {
            sessionFactory.getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}