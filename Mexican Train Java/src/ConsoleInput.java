// Temporary file to emulate cin, for use in testing the console version of the java project
// Should be removed come final Android version

import java.io.IOException;

//----------------------------------------------------------------------------------------
//	Copyright © 2006 - 2021 Tangible Software Solutions, Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class provides the ability to convert basic C++ 'cin' behavior.
//----------------------------------------------------------------------------------------
public final class ConsoleInput
{
	private static boolean goodLastRead = false;
	public static boolean lastReadWasGood()
	{
		return goodLastRead;
	}

	public static String readToWhiteSpace(boolean skipLeadingWhiteSpace)
	{
		String input = "";
		char nextChar = ' ';
		try {
			while (Character.isWhitespace(nextChar = (char)System.in.read()))
			{
				//accumulate leading white space if skipLeadingWhiteSpace is false:
				if (!skipLeadingWhiteSpace)
				{
					input += nextChar;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//the first non white space character:
		input += nextChar;

		//accumulate characters until white space is reached:
		try {
			while (!Character.isWhitespace(nextChar = (char)System.in.read()))
			{
				input += nextChar;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		goodLastRead = input.length() > 0;
		return input;
	}

	public static String scanfRead()
	{
		return scanfRead(null, -1);
	}

	public static String scanfRead(String unwantedSequence)
	{
		return scanfRead(unwantedSequence, -1);
	}

	public static String scanfRead(String unwantedSequence, int maxFieldLength)
	{
		String input = "";

		char nextChar;
		if (unwantedSequence != null)
		{
			nextChar = '\0';
			for (int charIndex = 0; charIndex < unwantedSequence.length(); charIndex++)
			{
				if (Character.isWhitespace(unwantedSequence.charAt(charIndex)))
				{
					//ignore all subsequent white space:
					try {
						while (Character.isWhitespace(nextChar = (char)System.in.read()))
						{
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					//ensure each character matches the expected character in the sequence:
					try {
						nextChar = (char)System.in.read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (nextChar != unwantedSequence.charAt(charIndex))
						return null;
				}
			}

			input = (new Character(nextChar)).toString();
			if (maxFieldLength == 1)
				return input;
		}

		try {
			while (!Character.isWhitespace(nextChar = (char)System.in.read()))
			{
				input += nextChar;
				if (maxFieldLength == input.length())
					return input;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return input;
	}
}
