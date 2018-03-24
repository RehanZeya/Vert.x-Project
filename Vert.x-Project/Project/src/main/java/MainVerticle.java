

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.Calendar;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class MainVerticle extends AbstractVerticle {
	int stallNo;
	public static ArrayList<NetSocket> netSocket;
	public static ArrayList<Shop> pos;
	public static ArrayList<Shop> kiosks;
	public static int canteen;
	public static int no_of_pos;
	public static int no_of_kiosks;
	public static int transaction_rate;
	public static int Users;
	DatabaseUtil database=new DatabaseUtil();
	
	MainVerticle(int can,int pos,int kiosks,int users,int rate) {
		canteen = can;
		no_of_pos=pos;
		no_of_kiosks = kiosks;
		transaction_rate = rate;
		Users = users;
	}
	public static ArrayList<Shop> getpos(){
		return pos;
	}
	
	@Override
	  public void start() {
		  
		  netSocket=new ArrayList<NetSocket>();
		  kiosks=new ArrayList<Shop>();
		  pos=new ArrayList<Shop>();
		  stallNo=0;
		vertx.deployVerticle(new BackendServer());
	    database.createTableOfBackendServer(Users);
	    for (int i=0;i<canteen;i++){
	    	for (int j=0,p=i;j<no_of_pos;j++){      //Creating POS shops
	    		Shop shop=new Shop("Canteen "+(p+1),"Stall "+((++stallNo)-p*(no_of_pos+no_of_kiosks)),"POS");
	    		pos.add(shop);
	    		vertx.deployVerticle(shop);
	    		database.createTable(shop.canteen+"_"+shop.stall+".db",Users,false);
	    	}
	    	
	    	for (int j=0,p=i;j<no_of_kiosks;j++){    //Creating Kiosks shop
	    		Shop shop=new Shop("Canteen "+(p+1),"Stall "+((++stallNo)-p*(no_of_pos+no_of_kiosks)),"kiosks");
	    		kiosks.add(shop);
	    		vertx.deployVerticle(shop);
	    		database.createTable(shop.canteen+"_"+shop.stall+".db",Users,true);
	    	}
		}
	    DatabaseUtil database=new DatabaseUtil();
	    vertx.setPeriodic((60000/transaction_rate), id -> {       //making transaction periodically
	    	Random rand=new Random();
	    	int user=rand.nextInt(Users)+1;
	    	int amount = database.getUserWallet(user);
	    	if (amount>=30)
	    		vertx.eventBus().send("POS",String.valueOf(user));
	    	else
	    		vertx.eventBus().send("kiosks",String.valueOf(user)+","+String.valueOf(amount));
	    	
	    });
	    vertx.setPeriodic((15000), id -> {      //Printing on console top selling items and POS sale today for each shop
	    	System.out.println("POS Sales Today\n          POS Name       Amount(Rs.)");
	    	for (int i=0;i<canteen*no_of_pos;i++){
	    		int total_sale = database.getTotalSale(pos.get(i).canteen+"_"+pos.get(i).stall);
	    		System.out.println((i+1)+".  "+pos.get(i).canteen+", "+pos.get(i).stall+" |   "+total_sale);
	    	}
	    	System.out.println();
	    	System.out.println("Top Selling Items Today\n         Item           Quantity");
	    	ArrayList<pair> topSelling_item = database.getTopSellingItems();
	    	if (topSelling_item.size()>0){
	    		for (int i=0;i<7;i++){
	    			System.out.println((i+1)+". "+topSelling_item.get(i).item+" |  "+topSelling_item.get(i).totalSale);
	    		}
	    		System.out.println();
	    	}
	    	
	    });
	  }
	
}
