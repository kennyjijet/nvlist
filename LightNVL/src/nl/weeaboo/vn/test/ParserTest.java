package nl.weeaboo.vn.test;

import java.io.FileInputStream;
import java.io.IOException;

import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.vn.parser.LVNParser;
import nl.weeaboo.vn.parser.ParseException;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

	@Test
	public void syntaxTest() throws ParseException, IOException {
		LVNParser parser = new LVNParser();
		
		String filename = "test/test";
		String contents;
		byte checkBytes[];
		
		FileInputStream in = new FileInputStream(filename + ".lvn");
		try {
			contents = parser.parseFile(filename, in).compile();
		} finally {
			in.close();
		}

		//System.err.println(contents);
		
		in = new FileInputStream(filename + ".lua");		
		try {
			checkBytes = StreamUtil.readFully(in);
		} finally {
			in.close();
		}
		
		/*
		String check = new String(checkBytes, "UTF-8");
		System.err.println(contents);
		System.out.println(check);
		
		for (int n = 0; n < Math.max(check.length(), contents.length()); n++) {
			char c0 = (n < check.length() ? check.charAt(n) : ' ');
			char c1 = (n < contents.length() ? contents.charAt(n) : ' ');
			System.out.println(c0 + " " + c1 + " " + (c0==c1));
		}
		*/
		
		Assert.assertArrayEquals(checkBytes, contents.getBytes("UTF-8"));
	}
	
}
