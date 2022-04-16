// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.resources;

import net.groboclown.anhinga.analysis.problems.JsonProblem;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import net.groboclown.anhinga.schema.ResourceAccessInventory;

import javax.annotation.Nonnull;
import java.io.Reader;


/**
 * Utility class for loading resources as POJOs.
 */
public class ResourceLoader {
    /**
     * Load a ResourceAccessInventory schema file.
     *
     * @param name source name
     * @param reader streaming reader of the source
     * @return the inventory or problems.
     */
    @Nonnull
    public static RetVal<ResourceAccessInventory> loadResourceAccessInventory(
            @Nonnull String name,
            @Nonnull Reader reader) {
        return Ret.closeWith(reader, (r) -> {
            try {
                return RetVal.ok(new Gson().fromJson(r, ResourceAccessInventory.class));
            } catch (JsonParseException e) {
                return RetVal.fromProblem(JsonProblem.from(name, e));
            }
        });
    }
}
