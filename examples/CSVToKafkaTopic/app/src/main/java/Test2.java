import org.apache.avro.Schema;

public class Test {


    public static void main (String[] args)
    {
        String schema_string = "{\"type\":\"record\",\"name\":\"ratings\",\"fields\":[{\"name\":\"id\",\"type\":[\"null\",\"long\"],\"default\":null},{\"name\":\"user_id\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"activity_id\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"rating\",\"type\":[\"null\",\"double\"],\"default\":null}],\"connect.name\":\"ratings\"}";
        org.apache.avro.Schema.Parser parser = new org.apache.avro.Schema.Parser();
        org.apache.avro.Schema parse = parser.parse(schema_string);
        System.out.println(parse);

        for(Schema.Field f: parse.getFields())
        {
            String name = f.name();
            System.out.println(name);
            Schema schema = f.schema();

            for (Schema subschema : schema.getTypes())
            {
                String type= subschema.getName();
                if (!type.equals("null"))
                    System.out.println("    "+type);
            }
        }


        String tmp = "\"{\"type\":\"record\",\"name\":\"ActivitiesSummary\",\"fields\":[{\"name\":\"id\",\"type\":\"long\"},{\"name\":\"num_rated_activities\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"best_rated_activity\",\"type\":[\"null\",\"int\"],\"default\":null},{\"name\":\"best_rating\",\"type\":[\"null\",\"double\"],\"default\":null}],\"connect.name\":\"ActivitiesSummary\"}\"";
    }



}
