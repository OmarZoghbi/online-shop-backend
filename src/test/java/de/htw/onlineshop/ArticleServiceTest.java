package de.htw.onlineshop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Article service")
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    private Article createArticle(Long id, String name, BigDecimal price, String category) {
        Article article = new Article(
                name,
                "Beschreibung",
                price,
                category,
                "https://example.com/bild.png",
                "Neu",
                true
        );
        article.setId(id);
        return article;
    }

    @Test
    @DisplayName("should return all products")
    void getAllArticlesReturnsAllProducts() {
        Article articleOne = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        Article articleTwo = createArticle(2L, "T-Shirt", BigDecimal.valueOf(19.99), "Mode");

        when(articleRepository.findAll()).thenReturn(List.of(articleOne, articleTwo));

        List<Article> actual = articleService.getAllArticles();

        assertEquals(2, actual.size());
        assertEquals("Laptop", actual.get(0).getName());
        assertEquals("T-Shirt", actual.get(1).getName());
        verify(articleRepository).findAll();
    }

    @Test
    @DisplayName("should return product by id when product exists")
    void getArticleByIdReturnsProductWhenProductExists() {
        Article article = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        Article actual = articleService.getArticleById(1L);

        assertEquals(1L, actual.getId());
        assertEquals("Laptop", actual.getName());
        verify(articleRepository).findById(1L);
    }

    @Test
    @DisplayName("should throw exception when product by id does not exist")
    void getArticleByIdThrowsExceptionWhenProductDoesNotExist() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> articleService.getArticleById(99L)
        );

        assertEquals("Produkt mit ID 99 wurde nicht gefunden.", exception.getMessage());
        verify(articleRepository).findById(99L);
    }

    @Test
    @DisplayName("should return products matching a category")
    void getArticlesByCategoryReturnsMatchingProducts() {
        Article article = createArticle(1L, "USB-Kabel", BigDecimal.valueOf(9.99), "Elektronik");
        when(articleRepository.findByCategoryIgnoreCase("Elektronik")).thenReturn(List.of(article));

        List<Article> actual = articleService.getArticlesByCategory("Elektronik");

        assertEquals(1, actual.size());
        assertEquals("USB-Kabel", actual.get(0).getName());
        assertEquals("Elektronik", actual.get(0).getCategory());
        verify(articleRepository).findByCategoryIgnoreCase("Elektronik");
    }

    @Test
    @DisplayName("should create a product when product data is valid")
    void createArticleReturnsSavedProductWhenArticleIsValid() {
        Article input = createArticle(null, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        Article saved = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");

        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        Article actual = articleService.createArticle(input);

        assertEquals(1L, actual.getId());
        assertEquals("Laptop", actual.getName());
        assertEquals(BigDecimal.valueOf(899.99), actual.getPrice());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("should remove id before creating a product")
    void createArticleRemovesIdBeforeSavingProduct() {
        Article input = createArticle(99L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        Article saved = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");

        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article articleToSave = invocation.getArgument(0);
            assertNull(articleToSave.getId());
            return saved;
        });

        Article actual = articleService.createArticle(input);

        assertEquals(1L, actual.getId());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("should reject product when name is empty")
    void createArticleThrowsExceptionWhenNameIsEmpty() {
        Article input = createArticle(null, "", BigDecimal.valueOf(899.99), "Elektronik");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> articleService.createArticle(input)
        );

        assertEquals("Der Produktname darf nicht leer sein.", exception.getMessage());
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("should reject product when price is negative")
    void createArticleThrowsExceptionWhenPriceIsNegative() {
        Article input = createArticle(null, "Laptop", BigDecimal.valueOf(-1.00), "Elektronik");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> articleService.createArticle(input)
        );

        assertEquals("Der Produktpreis muss 0 oder größer sein.", exception.getMessage());
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("should update a product when product exists")
    void updateArticleReturnsUpdatedProductWhenProductExists() {
        Article existing = createArticle(1L, "Alter Name", BigDecimal.valueOf(10.00), "Alt");
        Article input = createArticle(null, "Neuer Name", BigDecimal.valueOf(29.99), "Autozubehör");
        Article saved = createArticle(1L, "Neuer Name", BigDecimal.valueOf(29.99), "Autozubehör");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        Article actual = articleService.updateArticle(1L, input);

        assertEquals(1L, actual.getId());
        assertEquals("Neuer Name", actual.getName());
        assertEquals(BigDecimal.valueOf(29.99), actual.getPrice());
        assertEquals("Autozubehör", actual.getCategory());
        verify(articleRepository).findById(1L);
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("should throw exception when updating a product that does not exist")
    void updateArticleThrowsExceptionWhenProductDoesNotExist() {
        Article input = createArticle(null, "Neuer Name", BigDecimal.valueOf(29.99), "Autozubehör");
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> articleService.updateArticle(99L, input)
        );

        assertEquals("Produkt mit ID 99 wurde nicht gefunden.", exception.getMessage());
        verify(articleRepository).findById(99L);
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    @DisplayName("should delete a product when product exists")
    void deleteArticleDeletesProductWhenProductExists() {
        Article article = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        articleService.deleteArticle(1L);

        verify(articleRepository).findById(1L);
        verify(articleRepository).delete(article);
    }

    @Test
    @DisplayName("should throw exception when deleting a product that does not exist")
    void deleteArticleThrowsExceptionWhenProductDoesNotExist() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        ArticleNotFoundException exception = assertThrows(
                ArticleNotFoundException.class,
                () -> articleService.deleteArticle(99L)
        );

        assertEquals("Produkt mit ID 99 wurde nicht gefunden.", exception.getMessage());
        verify(articleRepository).findById(99L);
        verify(articleRepository, never()).delete(any(Article.class));
    }
}
