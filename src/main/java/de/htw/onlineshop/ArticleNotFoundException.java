package de.htw.onlineshop;

public class ArticleNotFoundException extends RuntimeException {
    public ArticleNotFoundException(Long id) {
        super("Produkt mit ID " + id + " wurde nicht gefunden.");
    }
}
