package by.brest.karas.service.web_app;

import by.brest.karas.model.CartRecord;
import by.brest.karas.model.Customer;
import by.brest.karas.model.Product;
import by.brest.karas.model.dto.CartRecordDto;
import by.brest.karas.service.CartRecordDtoService;
import by.brest.karas.service.CartRecordService;
import by.brest.karas.service.CustomerService;
import by.brest.karas.service.ProductService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

/**
 * Product controller.
 */
@Controller
@RequestMapping("/admins")
public class AdminController {

    private final ProductService productService;

    private final CustomerService customerService;

    private final CartRecordService cartRecordService;

    private final CartRecordDtoService cartRecordDtoService;

    private final BCryptPasswordEncoder passwordEncoder;

    public AdminController(ProductService productService,
                           CustomerService customerService,
                           CartRecordService cartRecordService,
                           CartRecordDtoService cartRecordDtoService,
                           BCryptPasswordEncoder passwordEncoder) {
        this.productService = productService;
        this.customerService = customerService;
        this.cartRecordService = cartRecordService;
        this.cartRecordDtoService = cartRecordDtoService;
        this.passwordEncoder = passwordEncoder;
    }

    private Integer getPrincipalId(Principal principal) {
        return customerService.findByLogin(principal.getName()).get().getCustomerId();
    }

    @ModelAttribute("login")
    public String getAdminLogin(Principal principal) {
        return customerService.findById(getPrincipalId(principal)).get().getLogin();
    }

    @ModelAttribute("admin_id")
    public String getAdminId(Principal principal) {
        return getPrincipalId(principal).toString();
    }

    @ModelAttribute("principal_role")
    public String getPrincipalRole() {
        return "admin";
    }

    /////////////////////// PRODUCTS
    @GetMapping("/{admin_id}/products")
    public String goToProductPage(
            @PathVariable("admin_id") Integer adminId,
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter,
            @RequestParam(value = "view", required = false, defaultValue = "") String view,
            Model model) {

        model.addAttribute("filter", filter);
        model.addAttribute("view", view);
        model.addAttribute("admin_id", adminId);

        if (filter == null) {
            model.addAttribute("products", productService.findAll());
        } else model.addAttribute("products", productService.findProductsByDescription(filter));

        return "products";
    }

    @GetMapping(value = "/{admin_id}/products/{product_id}")
    public String goToProductInfoPage(
            @PathVariable(value = "product_id") Integer productId,
            Model model) {

        model.addAttribute("product", productService.findById(productId));

        return "product_info";
    }

    @GetMapping("/{admin_id}/products/new")
    public String goToNewProductForm(@ModelAttribute Product product) {
        return "new_product_form";
    }

    @PostMapping("/{admin_id}/products/new")
    public String createProduct(
            @ModelAttribute("product") @Valid Product product
            , BindingResult bindingResult
            , Principal principal) {

        if (bindingResult.hasErrors()) {
            return "new_product_form";
        }

        Integer adminId = getPrincipalId(principal);

        product.setPicture("add picture");
        product.setChangedBy(adminId);
        productService.create(product);

        return "redirect:/admins/{admin_id}/products";
    }

    @GetMapping("/{admin_id}/products/{product_id}/edit")
    public String goToEditProductPage(Model model, @PathVariable("product_id") Integer productId) {
        model.addAttribute("product", productService.findById(productId));
        return "edit_product";
    }

    @PatchMapping("/{admin_id}/products/{product_id}/edit")
    public String editProduct(
            @PathVariable("product_id") Integer productId,
            @PathVariable("admin_id") Integer adminId,
            @ModelAttribute("product") @Valid Product product,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "edit_product";
        }

        product.setProductId(productId);
        product.setChangedBy(adminId);
        productService.update(product);

        return "redirect:/admins/{admin_id}/products";
    }

    @GetMapping("/{admin_id}/products/{product_id}/delete")
    public String deleteProductById(
            @PathVariable("product_id") Integer productId) {

//        LOGGER.debug("Delete cart record by customer id and product id ({},{}) ", customerId, productId);
//        LOGGER.debug("delete({},{})", id, model);
        productService.delete(productId);

        return "redirect:/admins/{admin_id}/products";
    }
    //^^^^^^^^^^^^^^^^^^^^ PRODUCTS

    /////////////////////// CUSTOMERS
    @GetMapping(value = "/{admin_id}/customers")
    public String goToCustomerPage(
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter,
            Model model) {

        model.addAttribute("filter", filter);

        if (filter == null) {
            model.addAttribute("customers", customerService.findAll());
        } else model.addAttribute("customers", customerService.searchCustomersByLogin(filter));

        return "customers";
    }

    @GetMapping(value = "/{admin_id}/customers/{customer_id}")
    public String goToCustomerInfoPage(
            @PathVariable(value = "customer_id") Integer customerId,
            Model model) {

        model.addAttribute("customer", customerService.findById(customerId).get());

        return "customer_info";
    }

    @GetMapping("/{admin_id}/customers/{customer_id}/edit")
    public String goToCustomerEditPage(
            @PathVariable("customer_id") Integer customerId,
            Model model) {

        model.addAttribute("customer_id", customerId);
        model.addAttribute("customer", customerService.findById(customerId).get());

        return "edit_customer";
    }

    @PatchMapping("/{admin_id}/customers/{customer_id}/edit")
    public String updateCustomer(
            @ModelAttribute("customer") @Valid Customer customer,
            BindingResult bindingResult,
            @PathVariable("customer_id") Integer customerId,
            Model model) {


        model.addAttribute("customer_id", customerId);

        if (bindingResult.hasErrors())
            return "edit_customer";

        System.out.println(customer);

        if(customer.getIsActual()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        } else {
            customer.setPassword(passwordEncoder.encode("deleted customer password"));
        }

        customer.setCustomerId(customerId);
        customerService.update(customer);

        return "redirect:/admins/{admin_id}/customers";
    }

    @GetMapping("/{admin_id}/customers/{customer_id}/delete")
    public String deleteCartRecordByCustomerIdAndProductId( @PathVariable("customer_id") Integer customerId) {

//        LOGGER.debug("Delete customer {}", customerId);
//        LOGGER.debug("delete({},{})", id, model);
        customerService.delete(customerId);

        return "redirect:/admins/{admin_id}/customers";
    }


    //^^^^^^^^^^^^^^^^^^^^ CUSTOMERS

    /////////////////////// CART
    @GetMapping("/{admin_id}/cart/products")
    public String goToCartPage(
            @PathVariable("admin_id") Integer adminId,
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter,
            Model model) {

        List<CartRecordDto> cartRecordDtos = cartRecordDtoService.findCartRecordDtosByCustomerId(adminId, filter);
        model.addAttribute("filter", filter);
        model.addAttribute("cart_lines", (cartRecordDtos.size() != 0) ? cartRecordDtos : null);
        model.addAttribute("cart_sum_total", (cartRecordDtos.size() != 0) ? cartRecordDtoService.findCartRecordDtosSumByCustomerId(adminId, filter) : "0.00");

        return "cart";
    }

    @GetMapping("/{admin_id}/cart/products/{product_id}/edit")
    public String goToNewCartRecordForm() {
        return "redirect:/admins/{admin_id}/cart/products/{product_id}/new";
    }

    @GetMapping("/{admin_id}/cart/products/{product_id}/new")
    public String goToNewCartRecordForm(
            @ModelAttribute("cartRecord") CartRecord cartRecord,
            @PathVariable("admin_id") Integer adminId,
            @PathVariable("product_id") Integer productId,
            Model model) {

        Product product = productService.findById(productId);
        model.addAttribute("productId", productId);
        model.addAttribute("productDescription", product.getShortDescription());
        model.addAttribute("productPrice", product.getPrice());
        model.addAttribute("cartRecord", cartRecord);

        if (cartRecordService.isCartRecordExist(adminId, productId)) {
            model.addAttribute("quantity", cartRecordService.findCartRecordsByCustomerIdAndProductId(adminId, productId).get(0).getQuantity());
        }

        return "new_cart_record_form";
    }

    @PostMapping("/{admin_id}/cart/products/{product_id}")
    public String createCartRecord(
            @ModelAttribute("cartRecord") @Valid CartRecord cartRecord
            , BindingResult bindingResult
            , @PathVariable("product_id") Integer productId
            , @PathVariable("admin_id") Integer adminId
            , Model model) {

        Product product = productService.findById(productId);
        model.addAttribute("productId", productId);
        model.addAttribute("productDescription", product.getShortDescription());
        model.addAttribute("productPrice", product.getPrice());
        model.addAttribute("cartRecord", cartRecord);

        if (bindingResult.hasErrors()) {
            return "new_cart_record_form";
        }

        cartRecord.setCustomerId(adminId);
        cartRecord.setProductId(productId);
        cartRecordService.create(cartRecord);

        return "redirect:/admins/{admin_id}/cart/products";
    }

    @GetMapping("/{admin_id}/cart/products/{product_id}/delete")
    public String deleteCartRecordByCustomerIdAndProductId(
            @PathVariable("product_id") Integer productId,
            @PathVariable("admin_id") Integer adminId) {

//        LOGGER.debug("Delete cart record by customer id and product id ({},{}) ", customerId, productId);
//        LOGGER.debug("delete({},{})", id, model);
        cartRecordService.delete(adminId, productId);

        return "redirect:/admins/{admin_id}/cart/products";
    }


    ///////////////////////  ^^^^CART
}
