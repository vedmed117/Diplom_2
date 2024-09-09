package org.praktikum.orders;

import java.util.Arrays;
import java.util.List;

public class OrderRequest {


    private static final List<String> VALID_INGREDIENTS = Arrays.asList("61c0c5a71d1f82001bdaaa6d");
    private static final List<String> INVALID_INGREDIENT_HASH = Arrays.asList("test");
    private static final List<String> NO_INGREDIENTS = Arrays.asList();

    private List<String> ingredients;

    public OrderRequest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public static List<String> getValidIngredients() {
        return VALID_INGREDIENTS;
    }

    public static List<String> getInvalidIngredientHash() {
        return INVALID_INGREDIENT_HASH;
    }

    public static List<String> getNoIngredients() {
        return NO_INGREDIENTS;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}
