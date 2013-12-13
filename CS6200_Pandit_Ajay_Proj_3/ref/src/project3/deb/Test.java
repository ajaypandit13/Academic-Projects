package project3.deb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Test {

	public static void main(String[] args) throws IOException{
		RandomAccessFile raf = new RandomAccessFile("/Users/arijitdeb/Documents/IR/Project 3/test.txt", "rwd");
		//long start = raf.length();
		byte[] add = "add this".getBytes();
		
		raf.seek(raf.length());
		raf.write(add);
		
		raf.seek(raf.length());
		raf.write("second".getBytes());
		
		/*raf.seek(start);
		StringBuffer sb = new StringBuffer();
		byte[] output = new byte[add.length];
		raf.read(output);
		
		if(output != null){			
			for(int i=0;i<output.length;i++){
				sb.append((char)output[i]);
			}
		}*/
		
		raf.close();
		//System.out.println(sb.toString());
		
		//File file = new File("/Users/arijitdeb/Documents/IR/Project 3/test.txt");
		//System.out.println(file.getName());
	}
	
	
}
