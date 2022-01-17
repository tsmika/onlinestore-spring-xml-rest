package by.brest.karas.model;

import java.util.Objects;

public class Customer {

    private Integer customerId;

    private String login;

    private String password;

    private Role role;

    private Boolean isActual;

    public Customer() {
    }

    public Customer(/*Integer customerId,*/ String login, String password, Role role, Boolean isActual) {
//        this.customerId = customerId;
        this.login = login;
        this.password = password;
        this.role = role;
        this.isActual = isActual;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getIsActual() {
        return isActual;
    }

    public void setIsActual(Boolean actual) {
        isActual = actual;
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "customerId=" + customerId +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ", isActual=" + isActual +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId) && Objects.equals(login, customer.login) && Objects.equals(password, customer.password) && role == customer.role && Objects.equals(isActual, customer.isActual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, login, password, role, isActual);
    }
}
