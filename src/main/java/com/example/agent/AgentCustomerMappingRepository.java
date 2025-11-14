package com.example.agent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class AgentCustomerMappingRepository {

    @PersistenceContext
    EntityManager em;

    public void persist(AgentCustomerMapping m) { em.persist(m); }

    public AgentCustomerMapping findByAgentCustomerId(Long agentCustomerId) {
        var list = em.createQuery("SELECT m FROM AgentCustomerMapping m WHERE m.agentCustomerId = :id", AgentCustomerMapping.class)
                .setParameter("id", agentCustomerId)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}