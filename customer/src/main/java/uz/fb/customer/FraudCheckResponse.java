package uz.fb.customer;

import lombok.Builder;


@Builder
public record FraudCheckResponse(Boolean isFraudulent) {
}
