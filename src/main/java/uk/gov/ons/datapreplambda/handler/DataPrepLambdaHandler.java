package uk.gov.ons.datapreplambda.handler;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.datapreplambda.request.HandlerRequest;
import uk.gov.ons.datapreplambda.request.WranglerRequest;
import uk.gov.ons.datapreplambda.request.WranglerRequestData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.ons.datapreplambda.util.PropertiesUtil.*;

@Log4j2
public class DataPrepLambdaHandler implements RequestHandler<HandlerRequest, String> {

    private static final String SQL = "select questioncode, response from dev01.response where reference = '%s' " +
            "AND period = '%s' AND survey = '%s'";
    private static final String SEND_MESSAGE = "Attempting to invoke %s with the json string %s.";

    private static final int QUESTION_CODE_ELEMENT = 1;
    private static final int RESPONSE_ELEMENT = 2;

    public String handleRequest(HandlerRequest request, Context context) {
        try {
            sendDataToWrangler(getDataFromDatabase(request));
            return "Accepted";
        } catch (Exception e) {
            log.error("An exception was raised handling the request.", e);
            return "Failed";
        }
    }

    private List<WranglerRequestData> getDataFromDatabase(HandlerRequest request) throws Exception {
        List<WranglerRequestData> data = new ArrayList<>();

        try (final Connection conn = DriverManager.getConnection(getProperty(DATABASE_URL),
                getProperty(DATABASE_USER),
                getProperty(DATABASE_PASSWORD));
             final Statement stmt = conn.createStatement();) {
            ResultSet result = stmt.executeQuery(format(SQL, request.getReference(), request.getPeriod(),
                    request.getSurvey()));

            while(result.next()) {
                WranglerRequestData dataElement = WranglerRequestData.builder()
                        .questionCode(result.getString(QUESTION_CODE_ELEMENT))
                        .response(result.getString(RESPONSE_ELEMENT))
                        .build();

                data.add(dataElement);
            }
        } catch (final Exception e) {
            log.error("Attempt to read from database failed.", e);
            throw e;
        }

        return data;
    }

    private void sendDataToWrangler(List<WranglerRequestData> data) throws JsonProcessingException {
        try {
            AWSLambdaAsync client = AWSLambdaAsyncClient.asyncBuilder().withRegion(Regions.EU_WEST_2).build();

            WranglerRequest request = WranglerRequest.builder().responses(data).build();

            String requestJson = new ObjectMapper().writeValueAsString(request);
            String wranglerName = getProperty(WRANGLER_NAME);

            log.info(String.format(SEND_MESSAGE, wranglerName, requestJson));

            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.withFunctionName(wranglerName).withPayload(requestJson);

            client.invoke(invokeRequest);
        } catch (JsonProcessingException e) {
            log.error("An exception occured whilst attempting to prepare and send the request.", e);
            throw e;
        }
    }

}
