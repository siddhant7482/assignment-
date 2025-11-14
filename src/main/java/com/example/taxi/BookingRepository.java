package com.example.taxi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BookingRepository {
    @Inject
    EntityManager em;

    @Transactional
    public Booking create(Booking b) {
        em.persist(b);
        return b;
    }

    public Booking findByTaxiAndDate(Taxi taxi, LocalDate date) {
        var list = em.createQuery("select b from Booking b where b.taxi = :taxi and b.date = :date", Booking.class)
                .setParameter("taxi", taxi)
                .setParameter("date", date)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Booking> listByCustomerId(Long customerId) {
        return em.createQuery("select b from Booking b where b.customer.id = :cid", Booking.class)
                .setParameter("cid", customerId)
                .getResultList();
    }

    public List<Booking> listAll() {
        return em.createQuery("select b from Booking b", Booking.class).getResultList();
    }

    public Booking findById(Long id) { return em.find(Booking.class, id); }

    @Transactional
    public boolean deleteById(Long id) {
        Booking b = em.find(Booking.class, id);
        if (b == null) return false;
        em.remove(b);
        return true;
    }
}