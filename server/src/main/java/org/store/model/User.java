package org.store.model;
import java.sql.Timestamp;

/**
 * Entity class representing a registered User in the system.
 * <p>
 * This class maps to the {@code elstore_users} table in the database.
 * It stores authentication details (hashed password), contact information,
 * and the user's role (permissions).
 * </p>
 */
public class User {
    /** Unique identifier for the user (Primary Key) */
    private Long id;

    /** Display name of the user */
    private String username;

    /**
     * Encrypted password hash.
     * <p><b>Security Note:</b> Plain text passwords should never be stored here permanently.</p>
     */
    private String password;

    /** User's email address, acting as the unique login identifier */
    private String email;

    /** Contact phone number */
    private String phone;

    /** Physical address for delivery */
    private String address;

    /**
     * Authorization role.
     * <p>Expected values:</p>
     * <ul>
     * <li>"ADMIN" - Has full access to manage products and view orders.</li>
     * <li>"CLIENT" - Can browse products and make purchases.</li>
     * </ul>
     */
    private String role;

    /** Timestamp of account creation */
    private Timestamp createdAt;

    /**
     * Default constructor.
     * Required for serialization/deserialization libraries (e.g., Gson) and reflection.
     */
    public User() {}

    /**
     * Constructs a new User with essential registration details.
     *
     * @param username The display name.
     * @param email    The unique email address.
     * @param password The raw password (which should be hashed before saving) or the hashed password.
     * @param role     The access role (usually 'CLIENT' for new registrations).
     */
    public User(String username, String email, String password, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}