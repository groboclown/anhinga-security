// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Test resource helpers.
 */
public class ResourceUtil {
    @Nonnull
    public static InputStream getResourceAsStream(@Nonnull Class<?> base, @Nonnull String name) {
        final InputStream ret = base.getResourceAsStream(name);
        if (ret == null) {
            throw new IllegalStateException("No such resource " + name);
        }
        return ret;
    }

    @Nonnull
    public static byte[] getResourceAsBytes(@Nonnull Class<?> base, @Nonnull String name) {
        try (InputStream inp = getResourceAsStream(base, name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeTo(inp, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getResourceLines(@Nonnull Class<?> base, @Nonnull String name) {
        try (BufferedReader inp = new BufferedReader(new InputStreamReader(
                getResourceAsStream(base, name), StandardCharsets.UTF_8))) {
            final List<String> ret = new ArrayList<>();
            String line;
            while ((line = inp.readLine()) != null) {
                line = line.strip();
                if (! line.isEmpty()) {
                    ret.add(line);
                }
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveResourceAs(@Nonnull Class<?> base, @Nonnull String resourceName, @Nonnull File outputFile) {
        try (
                InputStream inp = getResourceAsStream(base, resourceName);
                OutputStream out = new FileOutputStream(outputFile);
        ) {
            writeTo(inp, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeTo(@Nonnull InputStream inp, @Nonnull OutputStream out)
            throws IOException {
        byte[] buff = new byte[4096];
        int len;
        while ((len = inp.read(buff, 0, 4096)) > 0) {
            out.write(buff, 0, len);
        }
    }
}