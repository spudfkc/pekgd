package org.pekgd.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;

/**
 *
 * @author ncc
 *
 */
public class User {

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField
    private String name;

    @DatabaseField
    private String password;

    public User() { /* needed for ORMLite */ }

    public User(String name, String password) {
        this.id = UUID.randomUUID();
        this.setName(name);
        this.setPassword(password);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
