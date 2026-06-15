package de.htw.onlineshop;


import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public List<Article> getArticlesByCategory(String category) {
        return articleRepository.findByCategoryIgnoreCase(category);
    }

    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    public Article createArticle(Article article) {
        article.setId(null);
        validateArticle(article);
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, Article updatedArticle) {
        Article existingArticle = getArticleById(id);

        existingArticle.setName(updatedArticle.getName());
        existingArticle.setDescription(updatedArticle.getDescription());
        existingArticle.setPrice(updatedArticle.getPrice());
        existingArticle.setCategory(updatedArticle.getCategory());
        existingArticle.setImageUrl(updatedArticle.getImageUrl());
        existingArticle.setBadge(updatedArticle.getBadge());
        existingArticle.setAvailable(updatedArticle.isAvailable());

        validateArticle(existingArticle);
        return articleRepository.save(existingArticle);
    }

    public void deleteArticle(Long id) {
        Article existingArticle = getArticleById(id);
        articleRepository.delete(existingArticle);
    }

    private void validateArticle(Article article) {
        if (article.getName() == null || article.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Der Produktname darf nicht leer sein.");
        }

        if (article.getPrice() == null || article.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Der Produktpreis muss 0 oder größer sein.");
        }
    }
}
