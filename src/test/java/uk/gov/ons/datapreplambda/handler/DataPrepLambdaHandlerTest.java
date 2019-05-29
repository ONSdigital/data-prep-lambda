package uk.gov.ons.datapreplambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.ons.datapreplambda.handler.DataPrepLambdaHandler;
import uk.gov.ons.datapreplambda.request.HandlerRequest;

public class DataPrepLambdaHandlerTest {

    @Test
    public void test() {
        new DataPrepLambdaHandler().handleRequest(HandlerRequest.builder().build(), Mockito.mock(Context.class));
    }

}
