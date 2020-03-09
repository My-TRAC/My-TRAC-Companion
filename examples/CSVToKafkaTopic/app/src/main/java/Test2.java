import MySQLHandlers.MySQLDriver;
import MySQLHandlers.SQLConnection;
import org.apache.avro.Schema;

import java.io.IOException;
import java.sql.Connection;

public class Test2 {


    public static void main (String[] args) throws InterruptedException, IOException {
        String filename = "/Users/joan/Downloads/user_chooses_route_small.csv";
        Connection connection  = null;// SQLConnection.getConnection("127.0.0.1:3306","connect_test","root","confluent");

        MySQLDriver.insertFileContent(connection,filename,"user_chooses_route",0);
    }



}
