import FileHandlers.FileDriver;
import MySQLHandlers.MySQLDriver;
import MySQLHandlers.SQLConnection;
import Objects.Schema;
import Objects.Version;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static spark.Spark.*;


/*
 * This app creates an API REST to upload a CSV File. Then, the CSV file is processed
 * and it's content is upload into a remote MySQL Database.
 */


public class CSVToKafkaTopic {


    //THIS PART OF THE CODE HELPS HANDLING THE ARGUMENTS REQUIRED BY THE APPLICATION
    public static class Arguments {


        @Parameter(names = {"-db", "--database"}, description = "Database name", required = false)
        public String database;

        @Parameter(names = {"-ip", "--ip"}, description = "MySQL server ip", required = false)
        public String ip;

        @Parameter(names = {"-u", "--user","--username"}, description = "DB username", required = false)
        public String user = "confluent";


        @Parameter(names = {"-pw", "--password"}, description = "DB password", required = false)
        public String pw = "confluent";

        @Parameter(names={"-f","--file"}, description="Full path to the GTFS file to be loaded", required= false)
        public String filename="";

        @Parameter(names={"-sr","--schemaregistry","--schema-registry"}, description="Schema registry ip", required= false)
        public String schemaregistry="";

        @Parameter(names={"-F","--folder"}, description="Full path to the folder containing the GTFS files to be loaded", required= false)
        public String folderName="";

        @Parameter(names = "--help,-help", help = true)
        private boolean help;

    }



    private static final String uploadDirPath="upload";
    private static File uploadDir;
    static long timer;


    /* This code represents a form with
        - uploaded_file, a field to be filled with the file to be uploaded
        - tipic_name, a text field which indicates to which topic the file belongs to
        - selected_option, a two option selecto to choose between a bulk/stream way of uploading the file
     */
    static StringBuilder web_page_code= new StringBuilder();

    /*
    "<form method='post' enctype='multipart/form-data'>" // note the enctype
            + "    <input type='file' name='uploaded_file' accept='.csv'><br>"
            + "    <input type='text' name='topic_name' value='Choose topic name'>         "// make sure to call getPart using the same "name" in the post
            + "<select name='selected_option'>\n"
            +"  <option value=\"stream\" selected>Stream</option>\n"
            +"  <option value=\"bulk\">Bulk</option>\n"
            +"</select><br><br>"
            + "                                         <button>Upload CSV</button>"
            + "</form>";
     */


    static Map<String,Schema> schemas = new HashMap<>();

    private static void prepare() throws IOException {
        //Set the application PORT
        port(4568);

        //Prepare to receive files
        uploadDir = new File(uploadDirPath);
        uploadDir.mkdir(); // create the upload directory if it doesn't exist
        staticFiles.externalLocation(uploadDirPath);


        //Access defined schemas
        loadSchemasMap();

        //Create web page code
        buildHtml();

    }

    private static void buildHtml() {

        web_page_code.append("<html lang=\"en\"><head>\n"
                +"<link rel=\"stylesheet\" href=\"https://unpkg.com/purecss@1.0.0/build/pure-min.css\" integrity=\"sha384-nn4HPE8lTHyVtfCBi5yW9d20FjT8BJwUXyWZT9InLYax14RDjBj46LmSztkmNP9w\" crossorigin=\"anonymous\"><form method='post' enctype='multipart/form-data'>"
                +"</head><body>"// note the enctype
                + "<br><br><input type='file' name='uploaded_file' accept='.csv'>\n<br><br>").append(getRollDownMany()).append(""
                //+"<input type='text' name='topic_name' value='Choose topic name'>         "// make sure to call getPart using the same "name" in the post
                + "Choose load mode: <select name='selected_option'>\n"
                +"  <option value=\"stream\" selected>Stream</option>\n"
                +"  <option value=\"bulk\">Bulk</option>\n"
                +"</select><br><br>"
                + "                                         <button>Upload CSV</button>"
                + "</form>"
                + "</body></html>");






    }

    private static String getRollDownMany() {

        StringBuilder selectorCode  = new StringBuilder();

        selectorCode.append("Choose topic: <select name='schema_name'>\n");

        List<String> registeredSchemas = new ArrayList<>(schemas.keySet());
        Collections.sort(registeredSchemas);

        for(String schema_name: registeredSchemas)
        {
           // schema_name=schema_name.replaceAll("-value","");
            selectorCode.append("<option value=\""+schema_name+ "\">"+schema_name+"</option>\n");
        }
        selectorCode.append("</select>\n<br><br>");
        return selectorCode.toString();
    }

    private static void loadSchemasMap() throws IOException {

        System.out.println("Getting schema from http://"+arguments.schemaregistry+":8081/subjects");



        try {
            URL url = new URL("http://" + arguments.schemaregistry + ":8081/subjects");


            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    System.out.println(line);

                    //Gson gson = new Gson();
                    //String[] subjects = gson.fromJson(line,String[].class);

                    ObjectMapper mapper = new ObjectMapper();
                    String[] subjects = mapper.readValue(line, String[].class);


                    for (String schema_name : subjects) {
                        System.out.println(schema_name);
                        url = new URL("http://"+arguments.schemaregistry + ":8081/subjects/" + schema_name + "/versions/latest");
                        try (BufferedReader reader2 = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                            for (String versionString; (versionString = reader2.readLine()) != null; ) {
                                //System.out.println(versionString);

                                //org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
                                //org.apache.avro.Schema parse = parser.parse(versionString);




                                Version version = mapper.readValue(versionString, Version.class);
                                try {
                                    Schema schema1 = version.getSchema();
                                    schemas.put(schema1.getName(), schema1);
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Schema "+versionString+"couldn't be loaded.");
                                }
                            }
                        }

                    }

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            while(true){}
        }
    }

    static    Connection connection = null;

    static Arguments arguments = new Arguments();
    public static void main (String[] args) throws IOException, InterruptedException {

        //Handle the program arguments
        JCommander.newBuilder().addObject(arguments).build().parse(args);

        //Function to prepare the system to receive the files to be uploaded.
        prepare();


        // It creates a "web page" to upload a file (get) and then it handles it (post)
        get("/", (req, res) ->
               web_page_code
        );

        //It handles the uploaded file
        post("/", (req, res) -> {
            String result = handleFile(req);
            return result;
        });

    }

    private static String handleFile(Request req) throws IOException, SQLException, InterruptedException {
        Path pathToFile = Files.createTempFile(uploadDir.toPath(), "", "");

        //Retrive filename
        String rawFilename= FileDriver.getFileName(pathToFile,req);

        //Retrieve topicName
        //String topicName=req.queryParams("topic_name");

        //Retrive uploaded mode: either bulk o stream
        String selectedOption = req.queryParams("selected_option");
        timer = selectedOption.equals("stream")?1000:0;

        String topicName = req.queryParams("schema_name");




        System.out.println("File name: "+ rawFilename+" will be "+selectedOption+"ed ; topic name: "+ topicName );


        File f = pathToFile.toFile();

        String result = writeFileContentToMySQL(schemas.get(topicName),f.getAbsolutePath());
        Files.delete(pathToFile);
        return "<h1>Results:</h1>"+result;
    }


    //Writes the content of the file into a table called tableName of the database
    private static String writeFileContentToMySQL(Schema schema, String filename) throws IOException, InterruptedException {

        //Connect with the database
        connection = SQLConnection.getConnection(arguments.ip,arguments.database,arguments.user,arguments.pw);
        System.out.println("Connection to MYSQL established");


        String tableName=schema.getName();

        //Creates the table
        //String createdTable = MySQLDriver.createTable(connection,schemas.get(tableName));
        String createdTable = MySQLDriver.createTable(arguments.ip,"3306",arguments.user,arguments.pw,arguments.database,arguments.schemaregistry,tableName);
        System.out.println(createdTable);


        connection = SQLConnection.getConnection(arguments.ip,arguments.database,arguments.user,arguments.pw);

        //Insert records into the table
        String InsertsErrors= MySQLDriver.insertFileContent(connection,filename,schemas.get(tableName),timer);


        return "Table:<br>"+createdTable+"<br><br>"+InsertsErrors;


    }
}
