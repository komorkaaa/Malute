package com.example.smartlabactivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartlabactivity.api.BasketRepository;
import com.example.smartlabactivity.api.ShopRepository;
import com.example.smartlabactivity.api.dto.BasketItemDto;
import com.example.smartlabactivity.api.dto.BasketRecordResponse;
import com.example.smartlabactivity.api.dto.ProductRecordResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {
    private static final String EXTRA_PRODUCT_ID = "extra_product_id";
    private static final String PREF_TOKEN = "auth_token";
    private static final String PREF_USER_ID = "user_id";

    private TextView titleTextView;
    private TextView categoryTextView;
    private TextView descriptionTextView;
    private TextView approximateCostTextView;
    private TextView errorTextView;
    private MaterialButton actionButton;
    private ProductRecordResponse currentProduct;
    private BasketRecordResponse currentBasket;

    public static Intent createIntent(Context context, String productId) {
        Intent intent = new Intent(context, ProductDetailsActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        titleTextView = findViewById(R.id.tvProductDetailsTitle);
        categoryTextView = findViewById(R.id.tvProductDetailsCategory);
        descriptionTextView = findViewById(R.id.tvProductDetailsDescription);
        approximateCostTextView = findViewById(R.id.tvProductApproximateCost);
        errorTextView = findViewById(R.id.tvProductDetailsError);
        actionButton = findViewById(R.id.btnProductAction);
        ImageView backButton = findViewById(R.id.btnProductBack);

        backButton.setOnClickListener(v -> finish());
        actionButton.setOnClickListener(v -> handleAction());

        loadProduct();
        loadBasket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBasket();
    }

    private void loadProduct() {
        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");

        if (productId == null || productId.isEmpty() || token.isEmpty()) {
            showError("Не удалось открыть товар");
            return;
        }

        new ShopRepository().getProductById(token, productId, new ShopRepository.ProductCallback() {
            @Override
            public void onSuccess(ProductRecordResponse product) {
                runOnUiThread(() -> {
                    currentProduct = product;
                    bindProduct();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showError("Ошибка: " + error));
            }
        });
    }

    private void bindProduct() {
        if (currentProduct == null) {
            return;
        }

        errorTextView.setVisibility(View.GONE);
        titleTextView.setText(safe(currentProduct.title));
        categoryTextView.setText(safeCategory(currentProduct.typeCloses));
        descriptionTextView.setText(safeDescription(currentProduct.description));
        approximateCostTextView.setText(safeApproximateCost(currentProduct.approximate_cost));
        updateActionButton();
    }

    private void handleAction() {
        if (currentProduct == null) {
            return;
        }

        if (isProductInBasket()) {
            startActivity(new Intent(this, BasketActivity.class));
            return;
        }

        addCurrentProductToBasket();
    }

    private void updateActionButton() {
        if (currentProduct == null) {
            actionButton.setEnabled(false);
            actionButton.setText("Загрузка...");
            return;
        }

        actionButton.setEnabled(true);
        if (isProductInBasket()) {
            actionButton.setText("Перейти в корзину");
        } else {
            actionButton.setText(String.format(Locale.getDefault(),
                    "Добавить за %.0f Р", currentProduct.price));
        }
    }

    private void showError(String text) {
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(text);
        actionButton.setEnabled(false);
        actionButton.setText("Недоступно");
    }

    private void loadBasket() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        if (token.isEmpty()) {
            currentBasket = null;
            updateActionButton();
            return;
        }

        new BasketRepository().getCurrentBasket(token, new BasketRepository.BasketCallback() {
            @Override
            public void onSuccess(BasketRecordResponse basket) {
                runOnUiThread(() -> {
                    currentBasket = basket;
                    updateActionButton();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    currentBasket = null;
                    updateActionButton();
                });
            }
        });
    }

    private void addCurrentProductToBasket() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        String userId = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_USER_ID, "");
        if (token.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            return;
        }

        List<BasketItemDto> items = copyBasketItems();
        BasketItemDto item = new BasketItemDto();
        item.product_id = currentProduct.id;
        item.title = currentProduct.title;
        item.price = currentProduct.price;
        item.quantity = 1;
        items.add(item);

        BasketRepository repository = new BasketRepository();
        BasketRepository.BasketCallback callback = new BasketRepository.BasketCallback() {
            @Override
            public void onSuccess(BasketRecordResponse basket) {
                runOnUiThread(() -> {
                    currentBasket = basket;
                    Toast.makeText(ProductDetailsActivity.this,
                            "Товар добавлен в корзину", Toast.LENGTH_SHORT).show();
                    updateActionButton();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        ProductDetailsActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_LONG
                ).show());
            }
        };

        if (currentBasket == null) {
            repository.createBasket(token, userId, items, getItemsCount(items), callback);
        } else {
            repository.updateBasket(token, currentBasket.id, userId, items, getItemsCount(items), callback);
        }
    }

    private boolean isProductInBasket() {
        if (currentBasket == null || currentBasket.items == null || currentProduct == null) {
            return false;
        }
        for (BasketItemDto item : currentBasket.items) {
            if (currentProduct.id.equals(item.product_id)) {
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
        for (BasketItemDto source : currentBasket.items) {
            BasketItemDto item = new BasketItemDto();
            item.product_id = source.product_id;
            item.title = source.title;
            item.price = source.price;
            item.quantity = source.quantity;
            items.add(item);
        }
        return items;
    }

    private int getItemsCount(List<BasketItemDto> items) {
        int total = 0;
        for (BasketItemDto item : items) {
            total += item.quantity;
        }
        return total;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeCategory(String value) {
        String text = safe(value);
        return text.isEmpty() ? "Категория не указана" : text;
    }

    private String safeDescription(String value) {
        String text = safe(value);
        return text.isEmpty() ? "Описание пока не добавлено." : text;
    }

    private String safeApproximateCost(String value) {
        String text = safe(value);
        return text.isEmpty() ? "Не указан" : text;
    }
}
