import java.util.ArrayList;

import io.netty.util.concurrent.Future;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;


public class BackendServer extends AbstractVerticle {
	DatabaseUtil database;
	BackendServer(){
		database=new DatabaseUtil();
	}
	public void start() {
		vertx.eventBus().consumer("BackendServerCBRDetail", message -> {
        	String[] strArray = message.body().toString().split(",");   //Parsing the message got from other shops
            database.updateCard("BackendServer",Integer.parseInt(strArray[0]),Integer.parseInt(strArray[1]));     //updating its local copy of CBR detail
        });
        vertx.eventBus().consumer("BackendServerPurchaseDetail", message -> {
        	String[] strArray = message.body().toString().split(",");        //Parsing the message got from other shops
        	database.updateUserPurchaseDetail(Integer.parseInt(strArray[0]),Integer.parseInt(strArray[1]),strArray[2],Integer.parseInt(strArray[3]),strArray[4],strArray[5],"BackendServer");    //Updating its local copy of UserTransactionDetails
        });
    }
	
}
