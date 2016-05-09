import java.lang.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Stego {
	public static void main(String[] args) {
	
		BufferedImage src = null;
		try {
			src = ImageIO.read(new File("matthew.png"));
		} catch (IOException e) {
			System.out.println("Stego: could not open file");
			System.exit(1);	
		}
	
		BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		img.getGraphics().drawImage(src, 0, 0, null);

		String to_encrypt_string = "This is a test that is even longer so hopefully StegExpose will at least detect some kind of change in the file so we can start testing different forms of encrypting this shit into the images since right not it is a very basic task, we are just making the whole blue value the byte to encrypt... not a very good way of going about this tbh, but we need to be able to detect there is some change going on!";
		byte[] to_encrypt_bytes = to_encrypt_string.getBytes(Charset.forName("UTF-8"));
		int encrypt_length = to_encrypt_bytes.length;

		int width = img.getWidth();
		int height = img.getHeight();
		int block_size = 1024;
		int number_blocks = ((3*width*height)/block_size) - 1;
		int cur_byte = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (cur_byte < encrypt_length) {
					int rgb_int = img.getRGB(x, y);
					Color color = new Color(rgb_int, true);
					byte cur_encrypt_byte = to_encrypt_bytes[cur_byte];
					// first two bits in RED
					int red = color.getRed();
					int new_red = ((cur_encrypt_byte >>> 6) & 0x03) | 0xfc;
					red = (red | 0x03) & new_red;
					// second two bits in GREEN
					int green = color.getGreen();
					int new_green = (cur_encrypt_byte >>> 4) & 0x03 | 0xfc;
					green = (green | 0x03) & new_green;
					// third two bits in BLUE
					int blue = color.getBlue();
					int new_blue = (cur_encrypt_byte >>> 2) & 0x03 | 0xfc;
					blue = (blue | 0x03) & new_blue;
					// last two bits in ALPHA
					int alpha = color.getAlpha();
					int new_alpha = cur_encrypt_byte & 0x03 | 0xfc;
					alpha = (alpha | 0x03) & new_alpha;
					// make new color
					Color new_col = new Color(red, green, blue, alpha);
					img.setRGB(x, y, new_col.getRGB());
					cur_byte++;
				}
			}
		}

		try {
			File output = new File("output.png");
			ImageIO.write(img, "png", output);
		} catch (IOException e) {
			System.out.println("Stego: could not save file");
		}

		try {
			img = ImageIO.read(new File("output.png"));
		} catch (IOException e) {
			System.out.println("Stego: could not open file");
			System.exit(1);	
		}
		cur_byte = 0;
		byte[] decrypted_bytes = new byte[encrypt_length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (cur_byte < encrypt_length) {
					int rgb_int = img.getRGB(x, y);
					Color color = new Color(rgb_int, true);
					byte encrypted_info = 0x0;
					// first two bits in RED
					int red = color.getRed();
					encrypted_info = (byte) (encrypted_info | (red & 0x03) << 6);
					// second two bits in GREEN
					int green = color.getGreen();
					encrypted_info = (byte) (encrypted_info | ((green & 0x03) << 4));
					// third two bits in BLUE
					int blue = color.getBlue();
					encrypted_info = (byte) (encrypted_info | ((blue & 0x03) << 2));
					// last two bits in ALPHA
					int alpha = color.getAlpha();
					encrypted_info = (byte) (encrypted_info | (alpha & 0x03));
					// add byte
					decrypted_bytes[cur_byte] = encrypted_info;
					cur_byte++;
				}
			}
		}

		try {
			String decoded = new String(decrypted_bytes, "UTF-8");
			System.out.println(decoded);
		} catch (UnsupportedEncodingException e) {
		
		}
	}
}
