package com.example.agent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class AgentCustomerRepository {

    @PersistenceContext
    EntityManager em;

    public void persist(AgentCustomer c) { em.persist(c); }

    public AgentCustomer findById(Long id) { return em.find(AgentCustomer.class, id); }

    public AgentCustomer findByEmail(String email) {
        var list = em.createQuery("SELECT c FROM AgentCustomer c WHERE c.email = :email", AgentCustomer.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<AgentCustomer> listAll() {
        return em.createQuery("SELECT c FROM AgentCustomer c ORDER BY c.id", AgentCustomer.class).getResultList();
    }
}