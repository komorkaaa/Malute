package com.example.smartlabactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartlabactivity.api.ShopRepository;
import com.example.smartlabactivity.api.dto.NewsListResponse;
import com.example.smartlabactivity.api.dto.NewsRecordResponse;
import com.example.smartlabactivity.api.dto.ProductRecordResponse;
import com.example.smartlabactivity.api.dto.ProductsListResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PageAnaliseActivity extends AppCompatActivity {
    private static final String PREF_TOKEN = "auth_token";

    private EditText searchEditText;
    private TextView chipAll;
    private TextView chipWomen;
    private TextView chipMen;
    private LinearLayout promoContainer;
    private LinearLayout productsContainer;
    private TextView emptyTextView;

    private final List<ProductRecordResponse> allProducts = new ArrayList<>();
    private final List<NewsRecordResponse> allNews = new ArrayList<>();
    private String selectedCategory = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_analise);

        searchEditText = findViewById(R.id.etHomeSearch);
        chipAll = findViewById(R.id.chipHomeAll);
        chipWomen = findViewById(R.id.chipHomeWomen);
        chipMen = findViewById(R.id.chipHomeMen);
        promoContainer = findViewById(R.id.promoContainer);
        productsContainer = findViewById(R.id.homeProductsContainer);
        emptyTextView = findViewById(R.id.tvHomeEmpty);

        chipAll.setOnClickListener(v -> setCategory("all"));
        chipWomen.setOnClickListener(v -> setCategory("women"));
        chipMen.setOnClickListener(v -> setCategory("men"));

        searchEditText.addTextChangedListener(new SimpleTextWatcher(this::renderProducts));

        BottomNavHelper.setup(this, BottomNavHelper.Screen.HOME);
        updateChips();
        loadProducts();
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
                        PageAnaliseActivity.this,
                        "Ошибка: " + error,
                        Toast.LENGTH_LONG
                ).show());
            }
        });

        new ShopRepository().getNews(token, new ShopRepository.NewsCallback() {
            @Override
            public void onSuccess(NewsListResponse response) {
                runOnUiThread(() -> {
                    allNews.clear();
                    if (response.items != null) {
                        allNews.addAll(response.items);
                    }
                    renderPromos();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(PageAnaliseActivity.this::renderPromos);
            }
        });
    }

    private void renderPromos() {
        promoContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (!allNews.isEmpty()) {
            for (int i = 0; i < Math.min(2, allNews.size()); i++) {
                NewsRecordResponse news = allNews.get(i);
                View card = inflater.inflate(R.layout.item_home_promo_card, promoContainer, false);
                bindNewsCard(card, news, i);
                promoContainer.addView(card);
            }
            return;
        }

        for (int i = 0; i < Math.min(2, allProducts.size()); i++) {
            ProductRecordResponse product = allProducts.get(i);
            View card = inflater.inflate(R.layout.item_home_promo_card, promoContainer, false);
            card.setBackgroundResource(i % 2 == 0
                    ? R.drawable.catalog_promo_background_a
                    : R.drawable.catalog_promo_background_b);

            ((TextView) card.findViewById(R.id.tvPromoTitle)).setText(safe(product.title));
            ((TextView) card.findViewById(R.id.tvPromoPrice)).setText(formatPrice(product.price));
            card.setOnClickListener(v -> openProduct(product));
            promoContainer.addView(card);
        }
    }

    private void bindNewsCard(View card, NewsRecordResponse news, int position) {
        android.widget.ImageView imageView = card.findViewById(R.id.ivPromoImage);

        String imageUrl = buildNewsImageUrl(news);
        if (imageUrl.isEmpty()) {
            imageView.setImageResource(position % 2 == 0
                    ? R.drawable.catalog_promo_background_a
                    : R.drawable.catalog_promo_background_b);
        } else {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(imageView);
        }

        card.setOnClickListener(v -> openCatalog());
    }

    private String buildNewsImageUrl(NewsRecordResponse news) {
        if (news == null
                || news.collectionId == null || news.collectionId.isEmpty()
                || news.id == null || news.id.isEmpty()
                || news.newsImage == null || news.newsImage.isEmpty()) {
            return "";
        }

        return "http://2.nntc.nnov.ru:8900/api/files/"
                + news.collectionId + "/"
                + news.id + "/"
                + news.newsImage;
    }

    private void renderProducts() {
        productsContainer.removeAllViews();

        List<ProductRecordResponse> filtered = filterProducts();
        if (filtered.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("Нет товаров");
            return;
        }

        emptyTextView.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < Math.min(3, filtered.size()); i++) {
            ProductRecordResponse product = filtered.get(i);
            View card = inflater.inflate(R.layout.item_catalog_product, productsContainer, false);
            bindProductCard(card, product);
            productsContainer.addView(card);
        }
    }

    private void bindProductCard(View card, ProductRecordResponse product) {
        ((TextView) card.findViewById(R.id.tvProductTitle)).setText(safe(product.title));
        ((TextView) card.findViewById(R.id.tvProductCategory)).setText(safeCategory(product));
        ((TextView) card.findViewById(R.id.tvProductPrice)).setText(formatPrice(product.price));

        MaterialButton button = card.findViewById(R.id.btnAddToCart);
        button.setText("Подробнее");
        button.setOnClickListener(v -> openProduct(product));

        card.setOnClickListener(v -> openProduct(product));
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

    private void openCatalog() {
        startActivity(new Intent(this, CatalogActivity.class));
    }

    private void openProduct(ProductRecordResponse product) {
        if (product == null || product.id == null || product.id.isEmpty()) {
            Toast.makeText(this, "Не удалось открыть товар", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(ProductDetailsActivity.createIntent(this, product.id));
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
