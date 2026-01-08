package org.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a product category.
 * Maps to the 'elstore_categories' table in the database.
 * <p>
 * Categories are used to group products logically (e.g., "Smartphones", "Laptops").
 * Each product belongs to one specific category.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;
    private String name;
}