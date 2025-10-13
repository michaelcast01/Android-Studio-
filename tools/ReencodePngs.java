import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ReencodePngs {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java ReencodePngs <file1> <file2> ...");
            System.exit(1);
        }
        for (String path : args) {
            File f = new File(path);
            if (!f.exists()) {
                System.err.println("Not found: " + path);
                continue;
            }
            File bak = new File(path + ".bak");
            if (!bak.exists()) {
                // try rename first
                boolean renamed = f.renameTo(bak);
                if (!renamed) {
                    // fallback to copy
                    try (InputStream in = new FileInputStream(f);
                         OutputStream out = new FileOutputStream(bak)) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
                    }
                }
            } else {
                System.out.println("Backup already exists: " + bak.getAbsolutePath());
            }

            BufferedImage img = ImageIO.read(bak);
            if (img == null) {
                System.err.println("Failed to read image (null) from " + bak.getAbsolutePath());
                continue;
            }
            boolean ok = ImageIO.write(img, "png", f);
            System.out.println((ok ? "Re-encoded: " : "Failed to write: ") + path);
        }
    }
}
