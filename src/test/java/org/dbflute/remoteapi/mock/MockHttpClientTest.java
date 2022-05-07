package org.dbflute.remoteapi.mock;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.dbflute.remoteapi.FlutyRemoteApiRule;
import org.dbflute.remoteapi.mapping.FlVacantMappingPolicy;
import org.dbflute.remoteapi.sender.body.FlFormSender;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.4.7 (2022/05/03 Tuesday)
 */
public class MockHttpClientTest extends PlainTestCase {

    public void test_request_GET_query_asString() throws Exception {
        // ## Arrange ##
        String responseJson = "{productName=\"sea\", regularPrice=100}";
        MockHttpClient client = MockHttpClient.create(resopnse -> {
            resopnse.asJsonDirectly(responseJson, request -> {
                return request.getUrl().contains("/sea?hangar=mystic");
            });
        });
        HttpGet get = new HttpGet("http://docksidestage.org/sea?hangar=mystic");

        // ## Act ##
        CloseableHttpResponse response = client.execute(get);

        // ## Assert ##
        log(response);
    }

    public void test_request_POST_form_asString() throws Exception {
        // ## Arrange ##
        String responseJson = "{productName=\"sea\", regularPrice=100}";
        MockHttpClient client = MockHttpClient.create(resopnse -> {
            resopnse.asJsonDirectly(responseJson, request -> {
                String requestForm = "land=one%26man&sea=mys%2Ftic"; // fixed ordered?
                return request.getBody().get().contains(requestForm);
            });
        });
        HttpPost post = new HttpPost("http://docksidestage.org/sea");
        FlFormSender sender = new FlFormSender(new FlVacantMappingPolicy());
        sender.prepareEnclosingRequest(post, new MockForm("mys/tic", "one&man"), new FlutyRemoteApiRule());

        // ## Act ##
        CloseableHttpResponse response = client.execute(post);

        // ## Assert ##
        log(response);
        UrlEncodedFormEntity entity = (UrlEncodedFormEntity) post.getEntity();
        log(entity); // already URL-encoded string so cannot get form instance (or key-value object)
    }

    public static class MockForm {

        // #for_now jflute cannot be final by error? DfBeanDesc spec? (no research for now) (2022/05/03)
        // (maybe DfReflectionUtil.isInstanceVariableField() used in DfBeanDesc)
        public String sea;
        public String land;

        public MockForm(String sea, String land) {
            this.sea = sea;
            this.land = land;
        }
    }
}
