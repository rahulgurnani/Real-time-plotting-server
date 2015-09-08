 import java.io.*;  
 import java.net.*; 

 public class JavaClient {
	public static void main(String[] args) {  

		try{      
			Socket soc = new Socket("localhost",2004);  
			PrintWriter dout = new PrintWriter(new DataOutputStream(soc.getOutputStream()));
			dout.printf("Hello");
			dout.flush();
			for(int i = 0; i < 10; i++)
			{
				dout.printf("%d %d",i,i*2);
				dout.flush();
			}
			dout.printf("Bye");
			dout.flush();
			dout.close();
			soc.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
