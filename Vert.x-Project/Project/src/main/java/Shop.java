import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import io.netty.util.concurrent.Future;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;


public class Shop extends AbstractVerticle {
	String canteen;
	String stall;
	String type;
	DatabaseUtil database;
	final String[] items={"Cup of milk 200ML","Biscuits (200g)","Cup of tea","Packed lunch","Mineral water bottle","Banana 1pc","Lunch meal"};
	Shop(String canteen,String stall,String type){     //Constructor
		this.canteen=canteen;
		this.stall=stall;
		this.type=type;
		database=new DatabaseUtil();
	}
	public void start() {
		if (this.type.equals("POS")){       //this will consume information when POS is called for transaction
			vertx.eventBus().consumer("POS", message -> {
				Random rand=new Random();
				String item=items[rand.nextInt(7)];
				//System.out.println("I am POS  "+this.canteen+" "+this.stall+" and I am purchasing item "+item+" for user "+message.body().toString());
				String[] purchaseDetail=database.purchaseItem(this.canteen+"_"+this.stall,item,Integer.valueOf(message.body().toString()));
				if (purchaseDetail[0].equals("Successful")){
					vertx.eventBus().publish("BroadcastCBRDetail",message.body().toString()+","+purchaseDetail[1]);
					vertx.eventBus().publish("BroadcastPurchaseDetail",message.body().toString()+","+purchaseDetail[1]+","+purchaseDetail[2]+","+purchaseDetail[3]+","+this.canteen+"_"+this.stall+","+purchaseDetail[4]);
					vertx.eventBus().publish("BackendServerCBRDetail",message.body().toString()+","+purchaseDetail[1]);
					vertx.eventBus().publish("BackendServerPurchaseDetail",message.body().toString()+","+purchaseDetail[1]+","+purchaseDetail[2]+","+purchaseDetail[3]+","+this.canteen+"_"+this.stall+","+purchaseDetail[4]);
				}
	        });
		}
		else{
			vertx.eventBus().consumer("kiosks", message -> {    //this will consume information when kiosks is called for transaction
				String[] array = message.body().toString().split(",");
				//System.out.println("I am kiosks and I will update user amount "+this.canteen+" "+this.stall);
	            int currentMoney = database.issueCard(this.canteen+"_"+this.stall,Integer.valueOf(array[0]),Integer.valueOf(array[1]));
	            vertx.eventBus().publish("BroadcastCBRDetail",array[0]+","+String.valueOf(currentMoney));
	            vertx.eventBus().publish("BackendServerCBRDetail",array[0]+","+String.valueOf(currentMoney));
	            Calendar calobj = Calendar.getInstance();
			    String time=calobj.getTime().toString();
	            vertx.eventBus().publish("BroadcastPurchaseDetail",array[0]+","+"1000"+","+"Amount Added In Card"+","+"1000"+","+this.canteen+"_"+this.stall+", "+time);
	            vertx.eventBus().publish("BackendServerPurchaseDetail",array[0]+","+"1000"+","+"Amount Added In Card"+","+"1000"+","+this.canteen+"_"+this.stall+", "+time);
	        });
		}
        vertx.eventBus().consumer("BroadcastCBRDetail", message -> {
        	String[] strArray = message.body().toString().split(",");
            database.updateCard(this.canteen+"_"+this.stall,Integer.parseInt(strArray[0]),Integer.parseInt(strArray[1]));
        });
        vertx.eventBus().consumer("BroadcastPurchaseDetail", message -> {
        	String[] strArray = message.body().toString().split(",");
        	database.updateUserPurchaseDetail(Integer.parseInt(strArray[0]),Integer.parseInt(strArray[1]),strArray[2],Integer.parseInt(strArray[3]),strArray[4],strArray[5],this.canteen+"_"+this.stall);
        });
    }
}
