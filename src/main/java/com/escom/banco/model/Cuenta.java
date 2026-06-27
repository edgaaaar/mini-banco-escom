package com.escom.banco.model;

public class Cuenta {
    private String id;
    private String propietario;
    private double balance;

    public Cuenta() {}

    public Cuenta(String id, String propietario, double balance) {
        this.id = id;
        this.propietario = propietario;
        this.balance = balance;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}