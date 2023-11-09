package com.example.cleanbookingsbackend.klarna.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record KlarnaCreateOrderRequest(String purchase_country, String purchase_currency, String locale,
                                       Integer order_amount, Integer order_tax_amount,
                                       List<KlarnaOrderProduct> order_lines, MerchantUrls merchant_urls) {
    @Builder
   public record KlarnaOrderProduct(String type, String reference, String name, Integer quantity, String quantity_unit,
                              Integer unit_price, Integer tax_rate, Integer total_amount, Integer total_discount_amount,
                              Integer total_tax_amount) {
    }

    @Builder
   public record MerchantUrls(String terms, String checkout, String confirmation, String push) {
    }
}
