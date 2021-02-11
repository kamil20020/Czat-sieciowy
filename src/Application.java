
public class Application {

	public static void main(String[] args) {
		
		new MyServer();
		
		try{
			
			Thread.sleep(1000);
		} 
		catch(Exception e){
			
			
		}
		
		new MyClient("Adam");

	}

}
