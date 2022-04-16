// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.schema;

import net.groboclown.anhinga.schema.APIUsageTrace;
import org.junit.jupiter.api.Test;

public class APIUsageTraceTest {
    @Test
    void parseSimple() {
        APIUsageTrace value = ResourceLoader.loadFile("api-usage-1.json", APIUsageTrace.class);

    }
}
