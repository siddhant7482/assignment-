package com.example.taxi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class TaxiRepository {
    @Inject
    EntityManager em;

    @Transactional
    public Taxi create(Taxi t) {
        em.persist(t);
        return t;
    }

    public Taxi findByRegistration(String registration) {
        var list = em.createQuery("select t from Taxi t where t.registration = :r", Taxi.class)
                .setParameter("r", registration)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public Taxi findById(Long id) { return em.find(Taxi.class, id); }

    public List<Taxi> listAll() {
        return em.createQuery("select t from Taxi t", Taxi.class).getResultList();
    }

    @Transactional
    public boolean deleteById(Long id) {
        Taxi t = em.find(Taxi.class, id);
        if (t == null) return false;
        em.remove(t);
        return true;
    }
}