package uz.fb.customer;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uz.fb.amqp.RabbitMQMessageProducer;
import uz.fb.clients.fraud.FraudCheckResponse;
import uz.fb.clients.fraud.FraudClient;
import uz.fb.clients.notification.NotificationRequest;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void register(CustomerRegistrationRequest customerRegistrationRequest) {
        Customer customer = Customer.builder()
                .firstName(customerRegistrationRequest.firstName())
                .lastName(customerRegistrationRequest.lastName())
                .email(customerRegistrationRequest.email())
                .build();

        customerRepository.saveAndFlush(customer);

        FraudCheckResponse fraudCheckResponse =fraudClient.isFraudster(customer.getId());

        assert fraudCheckResponse != null;
        if (fraudCheckResponse.isFraudulent()){
            throw new IllegalStateException("fraudster");
        }

        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to Amigoscode...",
                        customer.getFirstName())
        );
        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}
