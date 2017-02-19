package mgmt;

import java.text.DecimalFormat;

/**
 * Utility class.
 * 
 * @author Carsten Stockloew
 *
 */
public class Util {

	private static DecimalFormat nf = new DecimalFormat();

	/**
	 * Converts a given number to byte units, i.e. KiloByte, MegaByte etc.
	 */
	public static String convertByteUnit(Long l) {
		String s = null;
		String[] unit = { "B", "KB", "MB", "GB", "TB" };
		int i = 0;
		if (l < 1024) {
			s = "" + l;
		} else {
			float del = 1024.0f;
			float f = l;

			while (f > del) {
				// System.out.println(i + " " + f);
				i++;
				f /= del;
			}
			// System.out.println(i + " " + f);

			DecimalFormat df = new DecimalFormat("0.00");
			s = df.format(f);
		}

		s += " " + unit[i];

		return s;
	}

	/**
	 * Converts a given number to a value in KiloByte. Also uses dots for 1000s.
	 */
	public static String convertDecimal(long l) {
		long l2 = l / 1024;
		if (l2 == 0 && l != 0)
			l2 = 1;
		return nf.format(l2) + " KB";
	}
}
