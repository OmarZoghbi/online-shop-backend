package de.htw.onlineshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@DisplayName("Article controller")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
    void getAllProductsReturnsProductsAndStatusOk() throws Exception {
        Article articleOne = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        Article articleTwo = createArticle(2L, "T-Shirt", BigDecimal.valueOf(19.99), "Mode");

        when(articleService.getAllArticles()).thenReturn(List.of(articleOne, articleTwo));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("T-Shirt"));

        verify(articleService).getAllArticles();
    }

    @Test
    @DisplayName("should return product by id when product exists")
    void getProductByIdReturnsProductAndStatusOk() throws Exception {
        Article article = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        when(articleService.getArticleById(1L)).thenReturn(article);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(articleService).getArticleById(1L);
    }


    @Test
    @DisplayName("should return products matching a category")
    void getProductsByCategoryReturnsMatchingProductsAndStatusOk() throws Exception {
        Article article = createArticle(1L, "USB-Kabel", BigDecimal.valueOf(9.99), "Elektronik");
        when(articleService.getArticlesByCategory("Elektronik")).thenReturn(List.of(article));

        mockMvc.perform(get("/products/category/Elektronik"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].category").value("Elektronik"));

        verify(articleService).getArticlesByCategory("Elektronik");
    }

    @Test
    @DisplayName("should create a product and return status 201")
    void createProductReturnsCreatedProductAndStatusCreated() throws Exception {
        Article input = createArticle(null, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        Article saved = createArticle(1L, "Laptop", BigDecimal.valueOf(899.99), "Elektronik");
        when(articleService.createArticle(any(Article.class))).thenReturn(saved);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(articleService).createArticle(any(Article.class));
    }



    @Test
    @DisplayName("should update a product and return status 200")
    void updateProductReturnsUpdatedProductAndStatusOk() throws Exception {
        Article input = createArticle(null, "Neuer Name", BigDecimal.valueOf(29.99), "Autozubehör");
        Article updated = createArticle(1L, "Neuer Name", BigDecimal.valueOf(29.99), "Autozubehör");
        when(articleService.updateArticle(eq(1L), any(Article.class))).thenReturn(updated);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Neuer Name"))
                .andExpect(jsonPath("$.price").value(29.99));

        verify(articleService).updateArticle(eq(1L), any(Article.class));
    }

    @Test
    @DisplayName("should delete a product and return status 204")
    void deleteProductReturnsNoContentWhenProductWasDeleted() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(articleService).deleteArticle(1L);
    }

    @Test
    @DisplayName("should return 404 when product by id does not exist")
    void getProductByIdReturnsNotFoundWhenProductDoesNotExist() throws Exception {
        when(articleService.getArticleById(99L)).thenThrow(new ArticleNotFoundException(99L));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").value(not("")));

        verify(articleService).getArticleById(99L);
    }

    @Test
    @DisplayName("should return 400 when product data is invalid")
    void createProductReturnsBadRequestWhenArticleIsInvalid() throws Exception {
        Article input = createArticle(null, "", BigDecimal.valueOf(899.99), "Elektronik");
        when(articleService.createArticle(any(Article.class)))
                .thenThrow(new IllegalArgumentException("Der Produktname darf nicht leer sein."));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Der Produktname darf nicht leer sein."));

        verify(articleService).createArticle(any(Article.class));
    }

}
