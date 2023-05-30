package app.db.repo;

import app.db.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepo extends JpaRepository<Stock, Integer>
{
    Stock findByProduct(Integer product);
}