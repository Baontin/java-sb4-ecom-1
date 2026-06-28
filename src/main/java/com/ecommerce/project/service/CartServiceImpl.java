package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AuthUtils authUtils;
    private final ModelMapper modelMapper;

    public CartServiceImpl(CartRepository cartRepository,
                           ProductRepository productRepository,
                           CartItemRepository cartItemRepository,
                           AuthUtils authUtils,
                           ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.authUtils = authUtils;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find cart and product
        Cart cart = getOrCreateCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(
                cart.getCartId(),
                productId
        );

        // Perform validations
        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }
        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }
        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                + " less than or equal to the quantity " + product.getQuantity());
        }

        // Create Cart Item
        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        refreshCartItemPrice(newCartItem, product, quantity);
        // Save cart item
        cartItemRepository.save(newCartItem);

        cart.setTotalPrice(cart.getTotalPrice() + newCartItem.getTotalPrice());
        cart.getCartItems().add(newCartItem);
        cartRepository.save(cart);

         // return updated cart
        return mapToCartDTO(cart);
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No cart exists");
        }

        return carts.stream()
                .map(cart -> mapToCartDTO(cart))
                .toList();
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        return mapToCartDTO(cart);
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, int delta) {
        // find cart & product
        String email = authUtils.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);

        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
        if (cartItem == null) {
            throw new APIException("Product is not present in the cart");
        }

        Product product = cartItem.getProduct();
        int newQty = cartItem.getQuantity() + delta;

        // validate
        if (product.getQuantity() < newQty && delta > 0) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity());
        }

        if (newQty > 0) {
            refreshCartItemPrice(cartItem, product, newQty);
            cartItemRepository.save(cartItem);
        } else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }


        // update cartTotal
        double newTotal = cart.getCartItems().stream()
                .mapToDouble(item -> item.getTotalPrice())
                .sum();
        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        // return
        return mapToCartDTO(cart);
    }

    @Transactional
    @Override
    public String deleteCartItemFromCart(Long productId) {
        Cart cart = cartRepository.findCartByEmail(authUtils.loggedInEmail());
        CartItem cartItem = cartItemRepository.findCartItemByCartIdAndProductId(cart.getCartId(), productId);
        if (cartItem == null) {
            throw new APIException("Product is not in this cart.");
        }

        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getTotalPrice());
        cart.getCartItems().remove(cartItem);

        return "Product " + cartItem.getProduct().getProductName() + " removed successfully";
    }

    private Cart getOrCreateCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtils.loggedInEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtils.loggedInUser());
        return cartRepository.save(cart);
    }

    private CartDTO mapToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                })
                .toList();

        cartDTO.setProducts(productDTOs);
        return cartDTO;
    }

    private void refreshCartItemPrice(CartItem cartItem, Product product, int quantity) {
        cartItem.setQuantity(quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cartItem.setTotalPrice(cartItem.getProductPrice() * quantity);
    }

}
