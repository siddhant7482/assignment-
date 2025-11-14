package com.example.taxi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CustomerRepository {
    @Inject
    EntityManager em;

    @Transactional
    public Customer create(Customer c) {
        em.persist(c);
        return c;
    }

    public List<Customer> listAll() {
        return em.createQuery("select c from Customer c", Customer.class).getResultList();
    }

    public Customer findByEmail(String email) {
        var list = em.createQuery("select c from Customer c where c.email = :email", Customer.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public Customer findById(Long id) { return em.find(Customer.class, id); }

    @Transactional
    public boolean deleteById(Long id) {
        Customer c = em.find(Customer.class, id);
        if (c == null) return false;
        em.remove(c);
        return true;
    }
}