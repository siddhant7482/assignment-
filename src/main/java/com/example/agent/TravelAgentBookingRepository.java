package com.example.agent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class TravelAgentBookingRepository {

    @PersistenceContext
    EntityManager em;

    public void persist(TravelAgentBooking booking) {
        em.persist(booking);
    }

    public TravelAgentBooking findById(Long id) {
        return em.find(TravelAgentBooking.class, id);
    }

    public List<TravelAgentBooking> findAll() {
        return em.createQuery("SELECT b FROM TravelAgentBooking b ORDER BY b.id", TravelAgentBooking.class)
                .getResultList();
    }

    public List<TravelAgentBooking> findByCustomerId(Long customerId) {
        return em.createQuery("SELECT b FROM TravelAgentBooking b WHERE b.customerId = :cid ORDER BY b.id", TravelAgentBooking.class)
                .setParameter("cid", customerId)
                .getResultList();
    }

    public void delete(TravelAgentBooking booking) {
        TravelAgentBooking managed = em.contains(booking) ? booking : em.merge(booking);
        em.remove(managed);
    }
}