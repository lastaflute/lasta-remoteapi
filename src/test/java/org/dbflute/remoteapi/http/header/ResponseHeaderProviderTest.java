package org.dbflute.remoteapi.http.header;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dbflute.optional.OptionalThing;
import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 */
public class ResponseHeaderProviderTest extends PlainTestCase {

    public void test_findPresentValueList_basic() {
        // ## Arrange ##
        List<ResponseHeader> headerList = new ArrayList<ResponseHeader>();
        headerList.add(new MockResponseHeader("sea", "mystic"));
        headerList.add(new MockResponseHeader("sea", "bbb"));
        headerList.add(new MockResponseHeader("land", "oneman"));
        headerList.add(new MockResponseHeader("piari", null));
        ResponseHeaderProvider provider = new ResponseHeaderProvider(headerList);

        // ## Act ##
        // ## Assert ##
        assertEquals(Arrays.asList("mystic", "bbb"), provider.findPresentValueList("sea"));
        assertEquals(Arrays.asList("oneman"), provider.findPresentValueList("land"));
        assertHasZeroElement(provider.findPresentValueList("piari"));
        assertHasZeroElement(provider.findPresentValueList("miraco"));
    }

    public static class MockResponseHeader implements ResponseHeader {

        private final String name;
        private final String value;

        public MockResponseHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public OptionalThing<String> getValue() {
            return OptionalThing.ofNullable(value, () -> {
                throw new IllegalStateException("Not found the value: " + name);
            });
        }
    }
}
