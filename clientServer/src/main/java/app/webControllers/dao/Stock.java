package app.webControllers.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock
{
    private Integer id;
    private Integer product;
    private Double count;
    public Stock(Integer id)
    {
        this.id = id;
    }
}
