package com.urlshortner.repository;

import com.urlshortner.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY = the Data Access Layer. This is where all DB operations happen.
 *
 * By extending JpaRepository<Url, Long>, Spring auto-generates:
 *   - save(entity)          → INSERT or UPDATE
 *   - findById(id)          → SELECT by primary key
 *   - findAll()             → SELECT all rows
 *   - deleteById(id)        → DELETE by primary key
 *   - count()               → COUNT(*)
 *   ... and many more
 *
 * We don't write any SQL for basic CRUD — Spring Data JPA does it automatically!
 *
 * For custom queries, we use:
 *   1. Method naming convention → Spring generates SQL from the method name
 *   2. @Query annotation        → Write JPQL (Java-style SQL) manually
 */
@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    /**
     * Spring reads this method name and generates:
     * SELECT * FROM urls WHERE short_code = ?
     *
     * Returns Optional<Url> — forces the caller to handle "not found" case.
     * No NullPointerException risk.
     */
    Optional<Url> findByShortCode(String shortCode);

    /**
     * Spring generates:
     * SELECT * FROM urls WHERE original_url = ?
     *
     * Used to check if we've already shortened this URL before.
     */
    Optional<Url> findByOriginalUrl(String originalUrl);

    /**
     * Checks existence without loading the full entity.
     * Spring generates: SELECT COUNT(*) > 0 FROM urls WHERE short_code = ?
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Custom JPQL query to increment click count.
     *
     * @Modifying → tells Spring this is an UPDATE/DELETE (not a SELECT)
     * @Query      → JPQL (note: "Url" is the Entity class name, not the table name)
     *
     * We use this instead of fetch → modify → save to avoid race conditions
     * (two users clicking at the same time).
     */
    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(String shortCode);
}
