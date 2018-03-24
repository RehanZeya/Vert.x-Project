import io.vertx.core.AbstractVerticle;

import org.json.simple.*;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpServer;


public class MainServer extends AbstractVerticle{
	int canteen;           //No of canteen
	int no_of_pos;         //No of POS per canteen
	ArrayList<Shop> pos;   //ArrayList storing the name of the pos
	MainVerticle mai;
	public String topSellingItems(){       //fetch top selling items from database
		DatabaseUtil database=new DatabaseUtil();
		String ret="";
    	ArrayList<pair> topSelling_item = database.getTopSellingItems();      //get top selling items from database
    	if (topSelling_item.size()>0){          //getting the top 3 selling items
    		for (int i=0;i<3;i++){
    			ret+=((i+1)+". "+topSelling_item.get(i).item+" |  "+topSelling_item.get(i).totalSale)+"<br />";
    		}
    	}
    	return ret;
	}
	public String posSales(){          //fetching POS sales for each shop
		DatabaseUtil database=new DatabaseUtil();
		pos=mai.getpos();
		String ret="";
		for (int i=0;i<canteen*no_of_pos;i++){
    		int total_sale = database.getTotalSale(pos.get(i).canteen+"_"+pos.get(i).stall);       //calling database method to get total sale of passed shop
    		ret+=((i+1)+".  "+pos.get(i).canteen+", "+pos.get(i).stall+" |   "+total_sale)+"<br />";
    	}
    	
    	return ret;
	}
	public static void main(String[] args) throws IOException {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new MainServer());             //deploying the MainVerticle class
		System.out.println("localhost:8081 created");
	    
	}
	public int check=0;
	@Override
	 public void start(){
		
		vertx.createHttpServer().requestHandler(req -> {
		      String filename = null;
		      if (req.path().equals("/")) {     //It will be called when the localhost:8081 will be called first time
		        filename = "gg.html";
		      }
		      
		      
		      
		      //It will be called when the input of canteen,no of pos,no of kiosks,users and transaction rate if passed through html 
		      if (req.params().size()>0 && !req.getParam("Canteen").toString().equals("") && !req.getParam("POS").toString().equals("") && !req.getParam("kiosks").toString().equals("") && !req.getParam("users").toString().equals("") && !req.getParam("Rate").toString().equals("")){
		    	  int canteen1=Integer.valueOf(req.getParam("Canteen").toString());
		    	  int POS=Integer.valueOf(req.getParam("POS").toString());
		    	  int kiosks=Integer.valueOf(req.getParam("kiosks").toString());
		    	  int Rate=Integer.valueOf(req.getParam("Rate").toString());
		    	  int users=Integer.valueOf(req.getParam("users").toString());
		    	  mai=new MainVerticle(canteen1,POS,kiosks,users,Rate);
		    	  canteen=canteen1;
		    	  no_of_pos=POS;
		    	  vertx.deployVerticle(mai);       //calling the main server which handles all the task
		      }
		      if (filename != null) {
		          req.response().sendFile(filename);
		      }
		    }).listen(8081);
		
		
		//this port will be listened when query of top sales item is done
		vertx.createHttpServer().requestHandler(req -> {
				String ret=posSales();        //getting top sales item
				
		      req.response()
		      .putHeader("content-type", "text/plain")
		      .putHeader("Access-Control-Allow-Origin", "*")
		      .putHeader("Access-Control-Allow-Methods","GET, POST, OPTIONS")
		      .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
		      .end(ret);
		    }).listen(8082);
		
		//this port will be listened when query of pos sales today is done
		vertx.createHttpServer().requestHandler(req -> {
			String ret=topSellingItems();      //getting pos sales for today of all shops
			
	      req.response()
	      .putHeader("content-type", "text/plain")
	      .putHeader("Access-Control-Allow-Origin", "*")
	      .putHeader("Access-Control-Allow-Methods","GET, POST, OPTIONS")
	      .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
	      .end(ret);
	    }).listen(8083);
		
		
		//this port will be listened when stop query is done
		vertx.createHttpServer().requestHandler(req -> {
			System.exit(0);            //It will stop the program 
			
	      req.response()
	      .putHeader("content-type", "text/plain")
	      .putHeader("Access-Control-Allow-Origin", "*")
	      .putHeader("Access-Control-Allow-Methods","GET, POST, OPTIONS")
	      .putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
	      .end("Exited");
	    }).listen(8085);
		
	}
	
}
