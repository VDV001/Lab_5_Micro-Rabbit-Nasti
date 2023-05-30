package app.webControllers;

import app.webControllers.dao.Category;
import app.webControllers.dao.Product;
import app.requests.Requests;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/")
public class CategoriesController
{
    private final Requests requests;
    @Autowired
    public CategoriesController(Requests requests) {
        this.requests = requests;
    }

    @GetMapping("/")
    public ModelAndView getAll()
    {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("categories", requests.getAllCategories());

        return mav;
    }

    @GetMapping("/categories/newcategory")
    public String showNewCategoryForm(Category category, Model model)
    {
        model.addAttribute(category);
        return "add-category";
    }

    @GetMapping("/categories/{id}/newproduct")
    public String showNewProductForm(Product product, Model model, @PathVariable String id)
    {
        model.addAttribute(product);
        model.addAttribute("category", id);
        return "add-product";
    }

    @PostMapping("/categories/{id}/addproduct")
    public String addProduct(@Valid Product product, BindingResult result, Model model, @PathVariable String id)
    {
        if (result.hasErrors())
        {
            return "add-product";
        }
        product = (Product) model.getAttribute("product");
        assert product != null;
        Category category = requests.getCategoryById(Integer.parseInt(id));
        product = requests.createProduct(product.getName(), product.getMetric(), category.getId());
        requests.createStock(product);
        return "redirect:/categories/products/" + id;
    }

    @PostMapping("/categories/addcategory")
    public String addCategory(@Valid Category category, BindingResult result, Model model)
    {
        if (result.hasErrors())
        {
            return "add-category";
        }
        category = (Category) model.getAttribute("category");
        assert category != null;
        requests.createCategory(category.getName());
        return "redirect:/";
    }

    @GetMapping("/categories/products/{id}")
    public ModelAndView getAllProducts(@PathVariable String id, Model model)
    {
        ModelAndView mav = new ModelAndView("products");
        Category category = requests.getCategoryById(Integer.parseInt(id));
        mav.addObject("category", category.getName());
        List<Product> products = requests.getProductsByCategory(category);
        for (int i = 0; i < products.size(); i++)
        {
            products.get(i).setCount(requests.getCountOnStock(products.get(i)));
        }
        mav.addObject("catId", id);
        mav.addObject("products", products);
        return mav;
    }
    @GetMapping("/categories/product/{id}/edit/{prId}")
    public String showProductUpdateForm(@PathVariable("id") Integer id, Model model, @PathVariable String prId)
    {
        Product product = requests.getProductById(Integer.parseInt(prId));
        model.addAttribute("category", id);
        model.addAttribute("product", product);
        return "update-product";
    }
    @PostMapping("/categories/{id}/updateProduct/{prId}")
    public String updateProduct(@PathVariable("id") Integer id, @Valid Product product,
                                BindingResult result, Model model, @PathVariable String prId)
    {
        if (result.hasErrors()) {
            product.setId(Integer.valueOf(prId));
            return "update-product";
        }
        product.setId(Integer.valueOf(prId));
        product.setCategory(id);
        requests.updateProduct(product);
        return "redirect:/categories/products/" + id;
    }

    @GetMapping("/categories/edit/{id}")
    public String showCategoryUpdateForm(@PathVariable("id") Integer id, Model model)
    {
        Category category = requests.getCategoryById(id);
        model.addAttribute("category", category);
        return "update-category";
    }

    @PostMapping("/categories/updateCategory/{id}")
    public String updateCategory(@PathVariable("id") Integer id, Model model,
                                 @Valid Category category,
                                 BindingResult result)
    {
        if (result.hasErrors()) {
            category.setId(id);
            return "update-category";
        }
        category.setId(id);
        requests.updateCategory(category);
        return "redirect:/";
    }
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, Model model)
    {
        requests.deleteCategory(id);
        return "redirect:/";
    }

    @GetMapping("/categories/product/{catId}/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, Model model, @PathVariable String catId)
    {
        requests.deleteProduct(id);
        return "redirect:/categories/products/" + catId;
    }

    @GetMapping("/categories/product/{id}/order/{prId}")
    public String showOrderForm(@PathVariable("id") Integer id, Model model, @PathVariable String prId, @Valid Product product)
    {
        Category category = requests.getCategoryById(id);
        model.addAttribute("catId", id);
        model.addAttribute("id", prId);
        product = requests.getProductById(Integer.parseInt(prId));
        model.addAttribute("product", product);
        return "product-order";
    }
    @PostMapping("/categories/product/{catId}/order/{id}")
    public String orderProduct(@PathVariable("id") Integer id, @Valid Product product,
                               BindingResult result, Model model, @PathVariable String catId)
    {
        model.addAttribute("error", "");
        try
        {
            requests.orderProduct(product, product.getCount());
        }
        catch (Exception e)
        {
            model.addAttribute("error", "Order error: " + e.getMessage());
            model.addAttribute("catId", catId);
            model.addAttribute("id", id);
            product = requests.getProductById(id);
            model.addAttribute("product", product);
            return "product-order";
        }

        return "redirect:/categories/products/" + catId;
    }

    @GetMapping("/categories/product/{id}/stockOrder/{prId}")
    public String showStockOrderForm(@PathVariable("id") Integer id, Model model, @PathVariable String prId, @Valid Product product)
    {
        Category category = requests.getCategoryById(id);
        model.addAttribute("catId", id);
        model.addAttribute("id", prId);
        product = requests.getProductById(Integer.parseInt(prId));
        model.addAttribute("product", product);
        return "stock-order";
    }
    @PostMapping("/categories/product/{catId}/stockOrder/{id}")
    public String orderProductStock(@PathVariable("id") Integer id, @Valid Product product,
                               BindingResult result, Model model, @PathVariable String catId)
    {
        requests.orderStock(product, product.getCount());
        return "redirect:/categories/products/" + catId;
    }
}
