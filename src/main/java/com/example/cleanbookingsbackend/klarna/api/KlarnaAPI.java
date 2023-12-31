package com.example.cleanbookingsbackend.klarna.api;

import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.klarna.dto.KlarnaCreateOrderRequest;
import com.example.cleanbookingsbackend.klarna.dto.KlarnaCreateOrderResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

@Service
public class KlarnaAPI {
    private final RestTemplate restTemplate;

    public KlarnaAPI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Value("${KLARNA_USERNAME}")
    private String KLARNA_USERNAME;
    @Value("${KLARNA_PASSWORD}")
    private String KLARNA_PASSWORD;
    private static final KlarnaCreateOrderRequest.MerchantUrls merchantUrls =
            new KlarnaCreateOrderRequest.MerchantUrls(
                    "http://localhost:5173/terms",
                    "https://www.example.com/checkout.html",
                    "https://localhost:5173/checkout/confirmation",
                    "https://www.example.com/api/push"
            );
    private static final KlarnaCreateOrderRequest.Options options =
            new KlarnaCreateOrderRequest.Options(
                    "#594545",
                    "#594545",
                    "#594545"
            );


    public ResponseEntity<KlarnaCreateOrderResponse> createOrder(String jobType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString((KLARNA_USERNAME + ":" + KLARNA_PASSWORD).getBytes()));
        HttpEntity<KlarnaCreateOrderRequest> request = getNewOrderRequestHttpEntity(jobType, headers);
        ResponseEntity<KlarnaCreateOrderResponse> response = restTemplate.exchange(
                "https://api.playground.klarna.com/checkout/v3/orders",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                });
        return response;
    }

    private static HttpEntity<KlarnaCreateOrderRequest> getNewOrderRequestHttpEntity(String jobType, HttpHeaders headers) {
        KlarnaCreateOrderRequest request = null;
        switch (jobType) {
            case "BASIC" -> request = createNewBasicCleaning();
            case "TOPP" -> request = createNewToppCleaning();
            case "DIAMOND" -> request = createNewDiamondCleaning();
            case "WINDOW" -> request = createNewWindowCleaning();
        }
        return new HttpEntity<>(request, headers);
    }


    private static KlarnaCreateOrderRequest createNewBasicCleaning() {

        KlarnaCreateOrderRequest.KlarnaOrderProduct product = KlarnaCreateOrderRequest.KlarnaOrderProduct
                .builder()
                .type("physical")
                .reference("basic_cleaning")
                .name("Basic Städning")
                .quantity(1)
                .quantity_unit("St")
                .unit_price(79500)
                .tax_rate(2500)
                .total_amount(79500)
                .total_discount_amount(0)
                .total_tax_amount(15900)
                .build();

        return KlarnaCreateOrderRequest
                .builder()
                .purchase_country("SE")
                .purchase_currency("SEK")
                .locale("en-SE")
                .order_amount(79500)
                .order_tax_amount(15900)
                .order_lines(List.of(product))
                .merchant_urls(merchantUrls)
                .options(options)
                .build();
    }

    private static KlarnaCreateOrderRequest createNewToppCleaning() {

        KlarnaCreateOrderRequest.KlarnaOrderProduct product = KlarnaCreateOrderRequest.KlarnaOrderProduct
                .builder()
                .type("physical")
                .reference("topp_cleaning")
                .name("Topp Städning")
                .quantity(1)
                .quantity_unit("St")
                .unit_price(149500)
                .tax_rate(2500)
                .total_amount(149500)
                .total_discount_amount(0)
                .total_tax_amount(29900)
                .build();

        return KlarnaCreateOrderRequest
                .builder()
                .purchase_country("SE")
                .purchase_currency("SEK")
                .locale("en-SE")
                .order_amount(149500)
                .order_tax_amount(29900)
                .order_lines(List.of(product))
                .merchant_urls(merchantUrls)
                .options(options)
                .build();
    }

    private static KlarnaCreateOrderRequest createNewDiamondCleaning() {

        KlarnaCreateOrderRequest.KlarnaOrderProduct product = KlarnaCreateOrderRequest.KlarnaOrderProduct
                .builder()
                .type("physical")
                .reference("diamond_cleaning")
                .name("Diamant Städning")
                .quantity(1)
                .quantity_unit("St")
                .unit_price(249500)
                .tax_rate(2500)
                .total_amount(249500)
                .total_discount_amount(0)
                .total_tax_amount(49900)
                .build();

        return KlarnaCreateOrderRequest
                .builder()
                .purchase_country("SE")
                .purchase_currency("SEK")
                .locale("en-SE")
                .order_amount(249500)
                .order_tax_amount(49900)
                .order_lines(List.of(product))
                .merchant_urls(merchantUrls)
                .options(options)
                .build();
    }

    private static KlarnaCreateOrderRequest createNewWindowCleaning() {

        KlarnaCreateOrderRequest.KlarnaOrderProduct product = KlarnaCreateOrderRequest.KlarnaOrderProduct
                .builder()
                .type("physical")
                .reference("window_cleaning")
                .name("Fönsterputsning")
                .quantity(1)
                .quantity_unit("St")
                .unit_price(79500)
                .tax_rate(2500)
                .total_amount(79500)
                .total_discount_amount(0)
                .total_tax_amount(15900)
                .build();

        return KlarnaCreateOrderRequest
                .builder()
                .purchase_country("SE")
                .purchase_currency("SEK")
                .locale("en-SE")
                .order_amount(79500)
                .order_tax_amount(15900)
                .order_lines(List.of(product))
                .merchant_urls(merchantUrls)
                .options(options)
                .build();
    }
}
