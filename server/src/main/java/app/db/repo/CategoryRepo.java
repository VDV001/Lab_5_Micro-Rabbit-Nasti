package app.db.repo;

import app.db.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Integer>
{
    @Query("select count(c) from Product p inner join Category c on c.id = p.category where c.id = ?1")
    long countInCategory(Integer category);
}