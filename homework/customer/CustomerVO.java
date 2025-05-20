package homework.customer;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class CustomerVO {
    private String mode;
    private double rate;
}