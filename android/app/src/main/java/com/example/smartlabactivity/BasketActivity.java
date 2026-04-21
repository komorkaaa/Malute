package com.example.smartlabactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartlabactivity.api.BasketRepository;
import com.example.smartlabactivity.api.OrderRepository;
import com.example.smartlabactivity.api.dto.BasketItemDto;
import com.example.smartlabactivity.api.dto.BasketRecordResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class BasketActivity extends AppCompatActivity {
    private static final String PREF_TOKEN = "auth_token";
    private static final String PREF_USER_ID = "user_id";

    private LinearLayout itemsContainer;
    private TextView emptyTextView;
    private TextView totalTextView;
    private View summaryLayout;
    private MaterialButton checkoutButton;
    private BasketRecordResponse currentBasket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        itemsContainer = findViewById(R.id.basketItemsContainer);
        emptyTextView = findViewById(R.id.tvBasketEmpty);
        totalTextView = findViewById(R.id.tvBasketTotal);
        summaryLayout = findViewById(R.id.basketSummaryLayout);
        checkoutButton = findViewById(R.id.btnCheckout);

        findViewById(R.id.btnBasketBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnBasketClear).setOnClickListener(v -> {
            clearBasket();
        });
        checkoutButton.setOnClickListener(v -> createOrderFromBasket());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBasket();
    }

    private void loadBasket() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        if (token.isEmpty()) {
            currentBasket = null;
            renderItems();
            return;
        }

        new BasketRepository().getCurrentBasket(token, new BasketRepository.BasketCallback() {
            @Override
            public void onSuccess(BasketRecordResponse basket) {
                runOnUiThread(() -> {
                    currentBasket = basket;
                    renderItems();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    currentBasket = null;
                    renderItems();
                });
            }
        });
    }

    private void renderItems() {
        itemsContainer.removeAllViews();
        List<BasketItemDto> items = currentBasket == null ? null : currentBasket.items;

        if (items == null || items.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            summaryLayout.setVisibility(View.GONE);
            checkoutButton.setEnabled(false);
            checkoutButton.setAlpha(0.5f);
            return;
        }

        emptyTextView.setVisibility(View.GONE);
        summaryLayout.setVisibility(View.VISIBLE);
        checkoutButton.setEnabled(true);
        checkoutButton.setAlpha(1f);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (BasketItemDto item : items) {
            View card = inflater.inflate(R.layout.item_basket_product, itemsContainer, false);
            ((TextView) card.findViewById(R.id.tvBasketItemTitle)).setText(safe(item.title));
            ((TextView) card.findViewById(R.id.tvBasketItemPrice)).setText(formatPrice(item.price));
            ((TextView) card.findViewById(R.id.tvBasketItemQuantity)).setText(formatQuantity(item.quantity));
            card.findViewById(R.id.btnRemoveBasketItem).setOnClickListener(v -> {
                updateItemQuantity(item.product_id, 0);
            });
            card.findViewById(R.id.btnBasketMinus).setOnClickListener(v -> {
                updateItemQuantity(item.product_id, item.quantity - 1);
            });
            card.findViewById(R.id.btnBasketPlus).setOnClickListener(v -> {
                updateItemQuantity(item.product_id, item.quantity + 1);
            });
            itemsContainer.addView(card);
        }

        totalTextView.setText(formatPrice(getBasketTotal(items)));
    }

    private void updateItemQuantity(String productId, int newQuantity) {
        if (currentBasket == null) {
            return;
        }

        List<BasketItemDto> updatedItems = copyItems(currentBasket.items);
        for (int i = 0; i < updatedItems.size(); i++) {
            BasketItemDto item = updatedItems.get(i);
            if (productId.equals(item.product_id)) {
                if (newQuantity <= 0) {
                    updatedItems.remove(i);
                } else {
                    item.quantity = newQuantity;
                }
                break;
            }
        }
        syncBasket(updatedItems);
    }

    private void syncBasket(List<BasketItemDto> items) {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        String userId = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_USER_ID, "");
        if (token.isEmpty() || currentBasket == null) {
            return;
        }

        if (items.isEmpty()) {
            clearBasket();
            return;
        }

        new BasketRepository().updateBasket(
                token,
                currentBasket.id,
                userId,
                items,
                getItemsCount(items),
                new BasketRepository.BasketCallback() {
                    @Override
                    public void onSuccess(BasketRecordResponse basket) {
                        runOnUiThread(() -> {
                            currentBasket = basket;
                            renderItems();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(
                                BasketActivity.this,
                                "Ошибка: " + error,
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }
        );
    }

    private void clearBasket() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        if (token.isEmpty() || currentBasket == null) {
            currentBasket = null;
            renderItems();
            return;
        }

        new BasketRepository().deleteBasket(token, currentBasket.id, new BasketRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    currentBasket = null;
                    renderItems();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(
                        BasketActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private void createOrderFromBasket() {
        String token = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_TOKEN, "");
        String userId = getSharedPreferences("app_settings", MODE_PRIVATE).getString(PREF_USER_ID, "");
        if (token.isEmpty() || userId.isEmpty() || currentBasket == null || currentBasket.items == null
                || currentBasket.items.isEmpty()) {
            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
            return;
        }

        new OrderRepository().createOrder(
                token,
                userId,
                currentBasket.items,
                getBasketTotal(currentBasket.items),
                new OrderRepository.OrderCallback() {
                    @Override
                    public void onSuccess(com.example.smartlabactivity.api.dto.OrderRecordResponse order) {
                        runOnUiThread(() -> {
                            Toast.makeText(BasketActivity.this,
                                    "Заказ создан", Toast.LENGTH_SHORT).show();
                            clearBasket();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(
                                BasketActivity.this,
                                "Ошибка: " + error,
                                Toast.LENGTH_LONG
                        ).show());
                    }
                }
        );
    }

    private List<BasketItemDto> copyItems(List<BasketItemDto> source) {
        java.util.ArrayList<BasketItemDto> items = new java.util.ArrayList<>();
        if (source == null) {
            return items;
        }
        for (BasketItemDto sourceItem : source) {
            BasketItemDto item = new BasketItemDto();
            item.product_id = sourceItem.product_id;
            item.title = sourceItem.title;
            item.price = sourceItem.price;
            item.quantity = sourceItem.quantity;
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

    private double getBasketTotal(List<BasketItemDto> items) {
        double total = 0;
        if (items == null) {
            return total;
        }
        for (BasketItemDto item : items) {
            total += item.price * item.quantity;
        }
        return total;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatQuantity(int quantity) {
        return quantity + " шт";
    }

    private String formatPrice(double price) {
        return String.format(Locale.getDefault(), "%.0f Р", price);
    }
}
