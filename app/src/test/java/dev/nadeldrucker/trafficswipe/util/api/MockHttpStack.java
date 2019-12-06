package dev.nadeldrucker.trafficswipe.util.api;

import com.android.volley.Header;
import com.android.volley.Request;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonObject;
import dev.nadeldrucker.trafficswipe.util.api.mockfiles.MockFile;
import dev.nadeldrucker.trafficswipe.util.api.mockfiles.MockFileReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Mocked http stack providing responses predefined in json files.
 */
class MockHttpStack extends BaseHttpStack {

    @Override
    public HttpResponse executeRequest(Request<?> request, Map<String, String> additionalHeaders) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (request instanceof JsonObjectRequest) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("test", "hallo!");
            return createValidResponse(jsonObject);
        } else {
            throw new UnsupportedOperationException("Only JsonRequests are supported atm!");
        }
    }

    /**
     * Queues a new request using a provided mock file.
     *
     * @param mockFilePath path to mockfile
     */
    public void queueNextRequest(String mockFilePath) {
        MockFile mockFile = MockFileReader.getInstance().readApiMockFile(mockFilePath);
    }

    private HttpResponse createValidResponse(JsonObject responseBody) {
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd mmm yyyy HH:mm:ss zzz");
        List<Header> headers = Arrays.asList(new Header("Access-Control-Allow-Origin", "*"),
                new Header("Content-Type", "*"),
                new Header("Date", dateFormat.format(new Date())));

        byte[] bodyBytes = responseBody.toString().getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(bodyBytes);

        return new HttpResponse(200, headers, bodyBytes.length, stream);
    }
}
