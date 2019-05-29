package uk.gov.ons.datapreplambda.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WranglerRequestData {

    private String questionCode;

    private String response;

}
