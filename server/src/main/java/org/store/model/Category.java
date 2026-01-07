package org.store.model;

/**
 * Entity class representing a product category.
 * Maps to the 'elstore_categories' table in the database.
 * <p>
 * Categories are used to group products logically (e.g., "Smartphones", "Laptops").
 * Each product belongs to one specific category.
 * </p>
 */
public class Category {

    /** Unique identifier for the category */
    private int id;

    /** Display name of the category */
    private String name;

    /**
     * Default constructor.
     */
    public Category() {}

    /**
     * Constructs a category with an ID and a name.
     * @param id The category ID.
     * @param name The category name.
     */
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}