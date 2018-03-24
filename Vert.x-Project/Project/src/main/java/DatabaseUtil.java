import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;

public class DatabaseUtil {
	public static String dtb_path="C:\\Users\\mdrehanzeya1\\Desktop\\Database\\";   //Change this path
	public static Comparator<pair> comp=new Comparator<pair>(){
    	@Override
    	public int compare(pair ob1,pair ob2){	
    		return ob2.totalSale-ob1.totalSale;
    	}
    };
	public static int getUserWallet(int userId){
		String url = "jdbc:sqlite:"+dtb_path + "BackendServer"+".db";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Checking user amount"+" "+userId);
				 Statement stmt = conn.createStatement();
				 String userAmountQuery="SELECT Available_Amount_In_Rs FROM UserCBR WHERE UserId = "+userId+";";
				 int amount = stmt.executeQuery(userAmountQuery).getInt(1);
				 //System.out.println(amount);
				 return amount;
			 }
	      } catch (SQLException e) {
	      }
		return 0;
	}
	public static ArrayList<pair> getTopSellingItems(){
		ArrayList<pair> topSellingItem=new ArrayList<pair>();
		String url = "jdbc:sqlite:"+dtb_path + "BackendServer"+".db";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Checking topSellingItem"+" "+userId);
				 //String[] items={"Cup of milk 200ML   ","Biscuits (200g)     ","Cup of tea          ","Packed lunch        ","Mineral water bottle","Banana 1pc          ","Lunch meal          "};
				 String[] items={"Cup of milk 200ML","Biscuits (200g)","Cup of tea","Packed lunch","Mineral water bottle","Banana 1pc","Lunch meal"};
				 Statement stmt = conn.createStatement();
				 for (int i=0;i < 7 ;i++){
					 String top_selling_item="SELECT COUNT(Transaction_Amount_In_Rs) FROM UserTransactionDetails WHERE Purchase_Item = '"+items[i]+"';";
					 int amount = stmt.executeQuery(top_selling_item).getInt(1);
					 topSellingItem.add(new pair(items[i],amount));
					 //System.out.println(items[i]+" "+amount);
				 }
				 //System.out.println(amount);
				 Collections.sort(topSellingItem,comp);
				 return topSellingItem;
			 }
	      } catch (SQLException e) {
	      }
		return topSellingItem;
	}
	public static int getTotalSale(String databaseName){
		String url = "jdbc:sqlite:"+dtb_path +databaseName+".db";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Checking total sale"+" "+userId);
				 Statement stmt = conn.createStatement();
				 String totalSale="SELECT SUM(Total_Sale_In_Rs) FROM ItemsSales;";
				 int total_sale = stmt.executeQuery(totalSale).getInt(1);
				 //System.out.println(amount);
				 return total_sale;
			 }
	      } catch (SQLException e) {
	      }
		return 0;
	}
	public static void updateUserPurchaseDetail(int userId,int userAmount,String item,int itemPrice,String shopName,String time,String databaseName){
		String url = "jdbc:sqlite:"+dtb_path + databaseName+".db";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Updating User Transaction Detail for item "+item+" for user "+userId+" in database "+databaseName);
				 Statement stmt = conn.createStatement();
				 String updateuserTransactionDetail="INSERT INTO UserTransactionDetails (UserId, Transaction_Amount_In_Rs, Purchase_Item,Purchase_Shop,Time) "+
						 						"VALUES ("+userId+", "+itemPrice+", '"+item+"', '"+shopName+"', '"+time+"')";
				 stmt.execute(updateuserTransactionDetail);
			 }
	      } catch (SQLException e) {
	      }
	}
	public static String[] purchaseItem(String databaseName,String item,int userId){
		String url = "jdbc:sqlite:"+dtb_path + databaseName+".db";
		String[] purchaseDetails=new String[5];
		purchaseDetails[0]="NotSuccessful";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Purchasing Item"+" "+item);
				 String getDetails="SELECT Available_Quantity,Price,Total_Sale_In_Rs FROM ItemsSales WHERE Item_Name = '"+item+"';";
				 Statement stmt = conn.createStatement();
				 ResultSet detail=stmt.executeQuery(getDetails);
				 int quantity=detail.getInt(1);
				 int price=detail.getInt(2);
				 int sale = detail.getInt(3);
				 if (quantity==0){
					 return purchaseDetails;
				 }
				 quantity--;
				 sale=sale+price;
				 String updateQuantity="UPDATE ItemsSales SET Available_Quantity = "+quantity+", Total_Sale_In_Rs = "+sale+" WHERE Item_Name = '"+item+"';";
				 stmt.execute(updateQuantity);
				 int select=stmt.executeQuery("SELECT Available_Amount_In_Rs FROM UserCBR WHERE UserId = "+userId+";").getInt(1)-price;
				 String updateUserCBR = "UPDATE UserCBR SET Available_Amount_In_Rs = "+select+" WHERE UserId = "+userId+";";
				 stmt.execute(updateUserCBR);
				 Calendar calobj = Calendar.getInstance();
			     String time=calobj.getTime().toString();
				 String updateLastTransactionOfMyShop="INSERT INTO LastTransactionOfMyShop (UserId, Transaction_Amount_In_Rs, Purchase_Item,Time) "+
						 						"VALUES ("+userId+", "+price+", '"+item+"', '"+time+"')";
				 stmt.execute(updateLastTransactionOfMyShop);
				 purchaseDetails[0]="Successful";
				 purchaseDetails[1]=String.valueOf(select);
				 purchaseDetails[2]=item;
				 purchaseDetails[3]=String.valueOf(price);
				 purchaseDetails[4]=time;
				 return purchaseDetails;
				 
				 
			 }
	      } catch (SQLException e) {
	      }
		
		return purchaseDetails;
	}
	public static int issueCard(String databaseName,int userId,int prevAmount){
		String url = "jdbc:sqlite:"+dtb_path + databaseName+".db";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Issuing Card of user"+" "+userId);
				 int totalAmount=1000+prevAmount;
				 String update="UPDATE UserCBR SET Available_Amount_In_Rs = "+totalAmount+" WHERE UserId = "+userId+";";
				 Statement stmt = conn.createStatement();
				 stmt.execute(update);
				 Calendar calobj = Calendar.getInstance();
			     String time=calobj.getTime().toString();
				 String updateLastTransactionOfMyShop="INSERT INTO LastTransactionOfMyShop (UserId, Amount_Added_In_Rs, Time) "+
	 						"VALUES ("+userId+", "+1000+", '"+time+"')";
				 stmt.execute(updateLastTransactionOfMyShop);

				 return totalAmount;
			 }
	      } catch (SQLException e) {
	      }
		return 0;
	}
	
	public static void updateCard(String databaseName,int userId,int amount){
		String url = "jdbc:sqlite:"+dtb_path + databaseName+".db";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 //System.out.println("Updating Card");
				 String update="UPDATE UserCBR SET Available_Amount_In_Rs = "+amount+" WHERE UserId = "+userId+";";
				 Statement stmt = conn.createStatement();
				 stmt.execute(update);
			 }
	      } catch (SQLException e) {
	      }
	}
	public static void createTableOfBackendServer(int No_Of_Users){
		String url = "jdbc:sqlite:"+dtb_path + "BackendServer.db";
		String table2 = "CREATE TABLE UserCBR (\n"+
				 "UserId integer PRIMARY KEY,\n"+
	             "Available_Amount_In_Rs integer NOT NULL,\n"+
	             "FOREIGN KEY (UserId) REFERENCES UserTransactionDetails (UserId)\n"+
	             "ON DELETE CASCADE ON UPDATE CASCADE\n"+
	             ");";
		String table6 = "CREATE TABLE UserTransactionDetails (\n"+
				 "UserId integer NOT NULL,\n"+
				 "Transaction_Amount_In_Rs integer NOT NULL,\n"+
	             "Purchase_Item text NOT NULL,\n"+
	             "Purchase_Shop text NOT NULL,\n"+
	             "Time text\n"+
	             ");";
		String Users = "INSERT INTO UserCBR (UserId,Available_Amount_In_Rs) VALUES \n";
		for (int i=0;i<No_Of_Users;i++){
			if (i>0)
				Users=Users+",\n";
			Users=Users+"("+Integer.valueOf(i+1)+",0)";
		}
		Users=Users+";";
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 System.out.println("Connection Established");
				 Statement stmt = conn.createStatement();
				 stmt.execute(table6);
				 stmt.execute(table2);
				 stmt.execute(Users);
			 }
       } catch (SQLException e) {}
	}
	public static void createTable(String databaseName,int No_Of_Users,boolean kiosks){

        String url = "jdbc:sqlite:"+dtb_path + databaseName;
        
		String table1 = "CREATE TABLE ItemsSales (\n"+
						 "Item_Name text PRIMARY KEY,\n"+
			             "Available_Quantity integer NOT NULL,\n"+
			             "Price integer NOT NULL,\n"+
			             "Total_Sale_In_Rs integer,\n"+
			             "FOREIGN KEY (Item_Name) REFERENCES LastTransactionOfMyShop (Purchase_Item)\n"+
			             "ON DELETE CASCADE ON UPDATE CASCADE\n"+
			             ");";
		String table2 = "CREATE TABLE UserCBR (\n"+
						 "UserId integer PRIMARY KEY,\n"+
			             "Available_Amount_In_Rs integer NOT NULL,\n"+
			             "FOREIGN KEY (UserId) REFERENCES UserTransactionDetails (UserId)\n"+
			             "ON DELETE CASCADE ON UPDATE CASCADE\n"+
			             "FOREIGN KEY (UserId) REFERENCES LastTransactionOfMyShop (UserId)\n"+
			             "ON DELETE CASCADE ON UPDATE CASCADE\n"+
			             ");";
		String table3 = "CREATE TABLE LastTransactionOfMyShop (\n"+
						 "UserId integer NOT NULL,\n"+
			             "Transaction_Amount_In_Rs integer NOT NULL,\n"+
			             "Purchase_Item text NOT NULL,\n"+
			             "Time text\n"+
			             ");";
		String table4 = "CREATE TABLE LastTransactionOfMyShop (\n"+
						 "UserId integer NOT NULL,\n"+
			             "Amount_Added_In_Rs integer NOT NULL,\n"+
			             "Time text\n"+
			             ");";
		String table6 = "CREATE TABLE UserTransactionDetails (\n"+
						 "UserId integer NOT NULL,\n"+
						 "Transaction_Amount_In_Rs integer NOT NULL,\n"+
			             "Purchase_Item text NOT NULL,\n"+
			             "Purchase_Shop text NOT NULL,\n"+
			             "Time text\n"+
			             ");";
		String items = "INSERT INTO ItemsSales (Item_Name,Price,Available_Quantity,Total_Sale_In_Rs) VALUES \n"+
				 		"('Cup of milk 200ML',10,50000,0), \n"+
				 		"('Biscuits (200g)',10,25000,0), \n"+
				 		"('Cup of tea',5,60000,0), \n"+
				 		"('Packed lunch',25,30000,0), \n"+
				 		"('Mineral water bottle',12,50000,0), \n"+
				 		"('Banana 1pc',4,20000,0), \n"+
				 		"('Lunch meal',30,50000,0);";
		String Users = "INSERT INTO UserCBR (UserId,Available_Amount_In_Rs) VALUES \n";
		for (int i=0;i<No_Of_Users;i++){
			if (i>0)
				Users=Users+",\n";
			Users=Users+"("+Integer.valueOf(i+1)+",0)";
		}
		Users=Users+";";
		
				 		
		try (Connection conn = DriverManager.getConnection(url)) {
			 if (conn!=null){
				 System.out.println("Connection Established");
				 Statement stmt = conn.createStatement();
				 stmt.execute(table6);
				 if (kiosks==false){
					 stmt.execute(table3);
					 stmt.execute(table1);	
					 stmt.execute(items);
				 }
				 else{
					 stmt.execute(table4);
				 }
				 stmt.execute(table2);
				 stmt.execute(Users);
			 }
        } catch (SQLException e) {}
		
	}
}
