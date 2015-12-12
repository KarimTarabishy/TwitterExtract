package gp.twitter.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class DataSet {
	
	String str[][];
	
	DataSet()
	{
		
	}
	
	public static int readfile(String fileName) throws FileNotFoundException
	{
		int count = 0;
		Scanner in = new Scanner(new FileReader(fileName));
		while(in.hasNext())
		{  
			if(in.next().equals(null))
				++count;
		}
		in.close();
		return count;
		
	}
	
	public int countLines(File f) throws IOException
	{
		BufferedReader reader =new BufferedReader(new FileReader (f));
		int lines = 0;
		String line = "";
		while(line != null)
		{
			line = reader.readLine();
		}
		if(line != null && line.trim().equals( "" ))
			lines++;
		
		return lines;
	}
	

public static void main(String[] args) throws IOException
{
	DataSet ds = new DataSet();
	File f = new File("daily.txt");
	
	
	System.out.print(ds.countLines(f));
}

}

