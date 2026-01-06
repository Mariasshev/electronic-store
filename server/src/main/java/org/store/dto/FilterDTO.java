package org.store.dto;

import java.util.List;
import java.util.Map;

public class FilterDTO {
    private List<String> brands;  // Список брендів
    private Map<String, List<String>> specifications;  // Список характеристик (Назва -> Список можливих значень)

    public FilterDTO(List<String> brands, Map<String, List<String>> specifications) {
        this.brands = brands;
        this.specifications = specifications;
    }

    public List<String> getBrands() {
        return brands;
    }
    public void setBrands(List<String> brands) {
        this.brands = brands;
    }
    public Map<String, List<String>> getSpecifications() {
        return specifications;
    }
    public void setSpecifications(Map<String, List<String>> specifications) {
        this.specifications = specifications;
    }

}