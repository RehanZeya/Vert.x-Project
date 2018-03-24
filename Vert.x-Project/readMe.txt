Md Rehan Zeya               2014CSB1059

Intructions to run the code:

1. Import the "Project" project in eclipse.
2. First of all create a directory and open "DatabaseUtil.java" and on 14th line of code change the dtb_path
   and point it to the created directory(i.e., give the path of the created directory).This is the directory
   where all the database for different shops will be created.
3. Now run the java file of "MainServer.java" to run the server.
4. Now open "http://localhost:8081" on the browser.It will open a UI.
5. To start the transaction fill the fields in the UI.
   Note: to start the transaction all the five values (Canteen,POS,Kiosks,Users,Rate) must
   be given.
6. Hit "start" button to start transaction.
7. To get top selling items hit "Refresh Top Selling Items Today".
   It will print today's top 3 selling items across all the shops.
8. To get amount of transaction/sale done by every shop,hit "Refresh POS Sales Today" button.
9. Point 6 and 7 can be done any number of times.It will give the updated one.
10. To stop the program,click "Stop" button. 


NOTE: Enter small values in all the fields,like, Canteen=3,POS=3,Kiosks=2,Users=20,Rate=15.Because
      entering larger values will decrease the response time of refreshing buttons. 