package shopping;

import customer.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CartTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ShoppingServiceImpl shoppingService;

    private final Customer customer = new Customer(1, "123");

    /**
     * При добавлении в корзину кол-ва продуктов, равных максимальному
     * не должно выбрасываться исключение
     */
    @Test
    public void addCorrectProductCountTest() {
        Cart cart = shoppingService.getCart(customer);
        Product product = new Product("product", 1);

        cart.add(product, 1);

        assertTrue(cart.getProducts().containsKey(product));
    }
}
