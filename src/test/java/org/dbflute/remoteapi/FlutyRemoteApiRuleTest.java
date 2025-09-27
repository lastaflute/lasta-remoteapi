package org.dbflute.remoteapi;

import java.util.function.Function;

import org.dbflute.remoteapi.exception.control.RemoteApiExceptionOption;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.5.2 (2025/09/27 Saturday at ichihara)
 */
public class FlutyRemoteApiRuleTest extends PlainTestCase {

    public void test_adjustRemoteApiException_filterMessageRemoteApiUrl_basic() {
        // ## Arrange ##
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();

        // ## Act ##
        rule.adjustRemoteApiException(op -> {
            op.filterMessageRemoteApiUrl(url -> {
                return "sea:" + url;
            });
        });

        // ## Assert ##
        RemoteApiExceptionOption option = rule.getRemoteApiExceptionOption();
        Function<String, String> filter = option.getMessageRemoteApiUrlFilter().orElseThrow();
        assertEquals("sea:mystic", filter.apply("mystic"));
    }

    public void test_adjustRemoteApiException_basic() {
        // ## Arrange ##
        FlutyRemoteApiRule rule = new FlutyRemoteApiRule();

        // ## Act ##
        rule.adjustRemoteApiException(op -> {
            op.filterMessageRequestParameter(param -> {
                return "sea:" + param;
            });
        });

        // ## Assert ##
        RemoteApiExceptionOption option = rule.getRemoteApiExceptionOption();
        Function<Object, String> filter = option.getMessageRequestParameterFilter().orElseThrow();
        assertEquals("sea:927", filter.apply(new Integer(927)));
    }
}
