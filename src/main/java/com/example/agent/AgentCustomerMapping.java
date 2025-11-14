package com.example.agent;

import jakarta.persistence.*;

@Entity
@Table(name = "agent_customer_mappings")
public class AgentCustomerMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long agentCustomerId;

    private Long taxiCustomerId;
    private Long hotelCustomerId;
    private Long flightCustomerId;

    public Long getId() { return id; }

    public Long getAgentCustomerId() { return agentCustomerId; }
    public void setAgentCustomerId(Long agentCustomerId) { this.agentCustomerId = agentCustomerId; }

    public Long getTaxiCustomerId() { return taxiCustomerId; }
    public void setTaxiCustomerId(Long taxiCustomerId) { this.taxiCustomerId = taxiCustomerId; }

    public Long getHotelCustomerId() { return hotelCustomerId; }
    public void setHotelCustomerId(Long hotelCustomerId) { this.hotelCustomerId = hotelCustomerId; }

    public Long getFlightCustomerId() { return flightCustomerId; }
    public void setFlightCustomerId(Long flightCustomerId) { this.flightCustomerId = flightCustomerId; }
}