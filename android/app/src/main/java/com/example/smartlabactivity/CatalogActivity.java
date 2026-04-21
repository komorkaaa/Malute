package com.example.smartlabactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.BasketRepository;
import com.example.smartlabactivity.api.ShopRepository;
import com.example.smartlabactivity.api.dto.BasketItemDto;
import com.example.smartlabactivity.api.dto.BasketRecordResponse;
import com.example.smartlabactivity.api.dto.ProductRecordResponse;
import com.example.smartlabactivity.api.dto.ProductsListResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CatalogActivity extends AppCompatActivity {
    private static final String PREF_TOKEN = "auth_token";
    private static final String PREF_USER_ID = "user_id";

    private EditText searchEditText;
    private TextView chipAll;
    private TextView chipWomen;
    private TextView chipMen;
    private LinearLayout productsContainer;
    private TextView emptyTextView;
    private LinearLayout cartBar;
    private TextView cartTitleTextView;
    private TextView cartTotalTextView;

    private final List<ProductRecordResponse> allProducts = new ArrayList<>();
    private BasketRecordResponse currentBasket;
    private String selectedCategory = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        searchEditText = findViewById(R.id.etCatalogSearch);
        chipAll = findViewById(R.id.chipCatalogAll);
        chipWomen = findViewById(R.id.chipCatalogWomen);
        chipMen = findViewById(R.id.chipCatalogMen);
        productsContainer = findViewById(R.id.catalogProductsContainer);
        emptyTextView = findViewById(R.id.tvCatalogEmpty);
        cartBar = findViewById(R.id.catalogCartBar);
        cartTitleTextView = findViewById(R.id.tvCatalogCartTitle);
        cartTotalTextView = findViewById(R.id.tvCatalogCartTotal);
        ImageView profileIcon = findViewById(R.id.ivCatalogProfile);

        chipAll.setOnClickListener(v -> setCategory("all"));
        chipWomen.setOnClickListener(v -> setCategory("women"));
        chipMen.setOnClickListener(v -> setCategory("men"));
        profileIcon.setOnClickListener(v -> startActivity(new Intent(this, UserCardActivity.class)));
        cartBar.setOnClickListener(v -> startActivity(new Intent(this, BasketActivity.class)));

        searchEditText.addTextChangedListener(new SimpleTextWatcher(this::renderProducts));

        BottomNavHelper.setup(this, BottomNavHelper.Screen.CATALOG);
        updateChips();
        loadProducts();
        loadBasket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBasket();
    }

    private void loadProducts() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        if (token.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("Сначала войдите в аккаунт");
            return;
        }

        new ShopRepository().getProducts(token, null, new ShopRepository.ProductsCallback() {
            @Override
            public void onSuccess(ProductsListResponse response) {
                runOnUiThread(() -> {
                    allProducts.clear();
                    if (response.items != null) {
                        allProducts.addAll(response.items);
                    }
                    renderProducts();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        CatalogActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void renderProducts() {
        productsContainer.removeAllViews();
        List<ProductRecordResponse> filtered = filterProducts();
        if (filtered.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("Нет товаров");
            updateCartBar();
            return;
        }

        emptyTextView.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ProductRecordResponse product : filtered) {
            View card = inflater.inflate(R.layout.item_catalog_product, productsContainer, false);
            bindProductCard(card, product);
            productsContainer.addView(card);
        }
        updateCartBar();
    }

    private void bindProductCard(View card, ProductRecordResponse product) {
        ((TextView) card.findViewById(R.id.tvProductTitle)).setText(safe(product.title));
        ((TextView) card.findViewById(R.id.tvProductCategory)).setText(safeCategory(product));
        ((TextView) card.findViewById(R.id.tvProductPrice)).setText(formatPrice(product.price));

        MaterialButton button = card.findViewById(R.id.btnAddToCart);
        boolean inCart = isProductInBasket(product.id);
        if (inCart) {
            button.setText("Убрать");
            button.setBackgroundResource(R.drawable.button_outline_blue);
            button.setTextColor(ContextCompat.getColor(this, R.color.blue_main));
        } else {
            button.setText("Добавить");
            button.setBackgroundResource(R.drawable.button_enable);
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }

        button.setOnClickListener(v -> {
            toggleProductInBasket(product);
        });

        card.setOnClickListener(v -> startActivity(ProductDetailsActivity.createIntent(this, product.id)));
    }

    private List<ProductRecordResponse> filterProducts() {
        List<ProductRecordResponse> result = new ArrayList<>();
        String query = searchEditText.getText().toString().trim().toLowerCase(Locale.ROOT);

        for (ProductRecordResponse product : allProducts) {
            if (!matchesCategory(product)) {
                continue;
            }

            String haystack = (safe(product.title) + " " + safe(product.typeCloses) + " " + safe(product.type))
                    .toLowerCase(Locale.ROOT);
            if (!query.isEmpty() && !haystack.contains(query)) {
                continue;
            }
            result.add(product);
        }
        return result;
    }

    private boolean matchesCategory(ProductRecordResponse product) {
        String text = (safe(product.typeCloses) + " " + safe(product.type)).toLowerCase(Locale.ROOT);
        if ("women".equals(selectedCategory)) {
            return text.contains("жен");
        }
        if ("men".equals(selectedCategory)) {
            return text.contains("муж");
        }
        return true;
    }

    private void setCategory(String category) {
        selectedCategory = category;
        updateChips();
        renderProducts();
    }

    private void updateChips() {
        updateChip(chipAll, "all".equals(selectedCategory));
        updateChip(chipWomen, "women".equals(selectedCategory));
        updateChip(chipMen, "men".equals(selectedCategory));
    }

    private void updateChip(TextView chip, boolean selected) {
        chip.setSelected(selected);
        chip.setTextColor(ContextCompat.getColor(this,
                selected ? android.R.color.white : R.color.gray));
    }

    private void updateCartBar() {
        int count = getBasketCount();
        if (count <= 0) {
            cartBar.setVisibility(View.GONE);
            return;
        }

        cartBar.setVisibility(View.VISIBLE);
        cartTitleTextView.setText("В корзину");
        cartTotalTextView.setText(formatPrice(getBasketTotal()));
    }

    private void loadBasket() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        if (token.isEmpty()) {
            currentBasket = null;
            renderProducts();
            return;
        }

        new BasketRepository().getCurrentBasket(token, new BasketRepository.BasketCallback() {
            @Override
            public void onSuccess(BasketRecordResponse basket) {
                runOnUiThread(() -> {
                    currentBasket = basket;
                    renderProducts();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    currentBasket = null;
                    renderProducts();
                });
            }
        });
    }

    private void toggleProductInBasket(ProductRecordResponse product) {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        String userId = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_USER_ID, "");
        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        List<BasketItemDto> updatedItems = copyBasketItems();
        if (isProductInBasket(product.id)) {
            removeBasketItem(updatedItems, product.id);
        } else {
            addBasketItem(updatedItems, product);
        }

        int count = getItemsCount(updatedItems);
        BasketRepository repository = new BasketRepository();
        if (updatedItems.isEmpty()) {
            if (currentBasket == null) {
                currentBasket = null;
                renderProducts();
                return;
            }
            repository.deleteBasket(token, currentBasket.id, new BasketRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        currentBasket = null;
                        renderProducts();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(
                            CatalogActivity.this,
                            "Ошибка: " + error,
                            Toast.LENGTH_LONG
                    ).show());
                }
            });
            return;
        }

        BasketRepository.BasketCallback callback = new BasketRepository.BasketCallback() {
            @Override
            public void onSuccess(BasketRecordResponse basket) {
                runOnUiThread(() -> {
                    currentBasket = basket;
                    renderProducts();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        CatalogActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_LONG
                ).show());
            }
        };

        if (currentBasket == null) {
            repository.createBasket(token, userId, updatedItems, count, callback);
        } else {
            repository.updateBasket(token, currentBasket.id, userId, updatedItems, count, callback);
        }
    }

    private boolean isProductInBasket(String productId) {
        if (currentBasket == null || currentBasket.items == null) {
            return false;
        }
        for (BasketItemDto item : currentBasket.items) {
            if (productId.equals(item.product_id)) {
                return true;
            }
        }
        return false;
    }

    private List<BasketItemDto> copyBasketItems() {
        List<BasketItemDto> items = new ArrayList<>();
        if (currentBasket == null || currentBasket.items == null) {
            return items;
        }
        for (BasketItemDto item : currentBasket.items) {
            BasketItemDto copy = new BasketItemDto();
            copy.product_id = item.product_id;
            copy.title = item.title;
            copy.price = item.price;
            copy.quantity = item.quantity;
            items.add(copy);
        }
        return items;
    }

    private void addBasketItem(List<BasketItemDto> items, ProductRecordResponse product) {
        for (BasketItemDto item : items) {
            if (product.id.equals(item.product_id)) {
                item.quantity += 1;
                return;
            }
        }

        BasketItemDto item = new BasketItemDto();
        item.product_id = product.id;
        item.title = product.title;
        item.price = product.price;
        item.quantity = 1;
        items.add(item);
    }

    private void removeBasketItem(List<BasketItemDto> items, String productId) {
        for (int i = 0; i < items.size(); i++) {
            if (productId.equals(items.get(i).product_id)) {
                items.remove(i);
                return;
            }
        }
    }

    private int getItemsCount(List<BasketItemDto> items) {
        int total = 0;
        for (BasketItemDto item : items) {
            total += item.quantity;
        }
        return total;
    }

    private int getBasketCount() {
        if (currentBasket == null) {
            return 0;
        }
        return currentBasket.count;
    }

    private double getBasketTotal() {
        if (currentBasket == null || currentBasket.items == null) {
            return 0;
        }
        double total = 0;
        for (BasketItemDto item : currentBasket.items) {
            total += item.price * item.quantity;
        }
        return total;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeCategory(ProductRecordResponse product) {
        String value = safe(product.typeCloses);
        return value.isEmpty() ? "Категория не указана" : value;
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%.0f Р", price);
    }

    private static final class SimpleTextWatcher implements TextWatcher {
        private final Runnable callback;

        private SimpleTextWatcher(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.run();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
