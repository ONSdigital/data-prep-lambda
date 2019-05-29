package uk.gov.ons.datapreplambda.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandlerRequest {

    private String reference;

    private String period;

    private String survey;

}
