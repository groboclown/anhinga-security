// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.schema;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceLoader {
    static <T> T loadFile(String name, Class<T> type) {
        InputStream resource = ResourceLoader.class.getResourceAsStream(name);
        if (resource == null) {
            throw new RuntimeException("No such resource: " + name);
        }
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(resource), type);
    }
}
