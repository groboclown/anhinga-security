// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.schema;

import net.groboclown.anhinga.schema.ResourceAccessInventory;
import org.junit.jupiter.api.Test;

public class ResourceAccessInventoryTest {
    @Test
    void parseRai1() {
        ResourceAccessInventory value = ResourceLoader.loadFile(
                "resource-access-inventory-1.json", ResourceAccessInventory.class);
    }
}
