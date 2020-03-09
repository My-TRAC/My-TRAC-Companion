package MySQLHandlers;

import FileHandlers.FileDriver;
import Objects.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.List;

import static java.lang.Thread.sleep;

public class MySQLDriver {

    //Create a table called following the defined schema
    public static String createTable(String mysql_host, String mysql_port, String user, String password, String database, String schemaregistry, String topics) throws InterruptedException {


        // call the Hello class
        Process theProcess=null;
        BufferedReader inStream = null;
        String command = "java -cp MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar MYSQLInitDataModel --username "+user+" --password "+password+"  --database "+ database +" --topic-names "+topics+" --schema-registry http://"+schemaregistry+":8081 --mysql "+mysql_host+":"+mysql_port;
        System.out.println("Executing: "+command);

        try
        {
            theProcess = Runtime.getRuntime().exec(command);
        }
        catch(IOException e)
        {
            System.err.println("Error on exec() method");
            e.printStackTrace();
        }

        // read from the called program's standard output stream
        try
        {
            inStream = new BufferedReader(
                    new InputStreamReader( theProcess.getInputStream() ));
            System.out.println(inStream.readLine());


           StringBuilder sb = new StringBuilder();

            String line;
            while ((line = inStream.readLine()) != null) {

                sb.append(line);
                sb.append(System.lineSeparator()+"<br>");
            }

            return sb.toString();
        }
        catch(IOException e)
        {
            System.err.println("Error on inStream.readLine()");
            e.printStackTrace();
            return  "error in \""+command+"\"";
        }
    }


    //Create a table called following the defined schema
    public static String createTable(Connection connection, Schema schema) {
        String createTable = SQLQueryBuilder.createTable(schema);

        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTable);
            return createTable;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return ("ERROR in SQL query: " + createTable);
        }


    }



    //Insert the content of a file, into  tableName. timer ms is the time between insertions.
    public static String insertFileContent(Connection connection, String filename, String tableName, long timer) throws InterruptedException, IOException {
        StringBuilder inserts = new StringBuilder("");
        StringBuilder errors = new StringBuilder("");


        boolean first = true;
        String tableAttributes=null;
        List<String[]> records = FileDriver.read(filename);

   //     waitTable(connection,tableName);

        for(String[] record : records) {

            if(first) {

                tableAttributes = buildHeader(record);
                first = false;
                continue;
            }

            Pair<String, String> InsertError = MySQLDriver.Insert(connection,tableName,tableAttributes,record);
            inserts.append(InsertError.getLeft());
            errors.append(InsertError.getRight());

            sleep(timer);
        }

        return errors.length()==0?"<br><br>Inserts:<br>"+inserts.toString():"<br><br>Inserts:<br>"+inserts.toString()+("<br><br>Errors:<br>")+errors.toString();

    }



    //Insert the content of a file, into  tableName. timer ms is the time between insertions.
    public static String insertFileContent(Connection connection, String filename, Schema schema, long timer) throws InterruptedException, IOException {
        StringBuilder inserts = new StringBuilder("");
        StringBuilder errors = new StringBuilder("");


        String tableName=schema.getName();

        boolean first = true;
       /* StringBuilder tableAttributes = new StringBuilder();
        tableAttributes.append("(");

        for (Schema.Field field : schema.getFields())
        {
            String attribute_name = field.getName();
            //if(attribute_name.equals("mytrac_id")) continue;
            //if(attribute_name.equals("mytrac_last_modified")) continue;

            if(!first) tableAttributes.append(",");
            tableAttributes.append(" "+attribute_name);

            first=false;
        }
        tableAttributes.append(")");
*/
        String tableAttributes=null;
        List<String[]> records = FileDriver.read(filename);

        waitTable(connection,tableName);

        for(String[] record : records) {

            if(first) {

                tableAttributes = buildHeader(record);
                first = false;
                continue;
            }

            Pair<String, String> InsertError = MySQLDriver.Insert(connection,tableName,tableAttributes,record);
            inserts.append(InsertError.getLeft());
            errors.append(InsertError.getRight());

            sleep(timer);
        }

        return errors.length()==0?"<br><br>Inserts:<br>"+inserts.toString():"<br><br>Inserts:<br>"+inserts.toString()+("<br><br>Errors:<br>")+errors.toString();

    }

    //Insert a record into the table tableName  with the form tableAttributes.
    public static Pair<String, String> Insert(Connection connection, String tableName, String tableAttributes, String[] record) {
        {
            String insert = SQLQueryBuilder.insert(tableName, tableAttributes, record); //We will need to extend that to adapt the inser to the table previously created
            String correct = "";
            String error = "";
            //try {
            //    Statement statement = connection.createStatement();
            //    statement.executeUpdate(insert);
                correct = insert + "<br>";
            //} //catch (SQLException e) {
                //System.out.println("Error in " + insert+"\n         "+e.getMessage());
                //error = insert + "<br>";
           // }
            return new ImmutablePair<String, String>(correct, error);


        }
    }


    private static String buildHeader(String[] record) {
        StringBuilder header = new StringBuilder(" (");
        boolean first= true;
        for(String field: record)
        {
            if(!first) header.append(", ");
            header.append(field);
            first = false;
        }
        return header.append(") ").toString();

    }


    public static void waitTable(Connection connection, String table_name) {
        System.out.println("Waiting from "+table_name+" to be created");
        boolean loop=true;
        while(loop)
        {
        Statement statement = null;
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs1 = meta.getTables(null, null, null,new String[] {"TABLE"});
            while (rs1.next())
            {
     //           System.out.println(rs1.getString("TABLE_NAME"));
                if(table_name.equals(rs1.getString("TABLE_NAME"))) {
                    loop = false;
                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }}
}
