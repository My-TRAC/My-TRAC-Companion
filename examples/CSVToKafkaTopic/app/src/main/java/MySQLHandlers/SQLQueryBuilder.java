package MySQLHandlers;

import Objects.Schema;

public class SQLQueryBuilder {


    public static String createTable(Schema schema) {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS "+schema.getName()+"(");

        boolean first = true;

        boolean needId=true;
        boolean needTs=true;

        for(Schema.Field field : schema.getFields())
        {
            if (field.getName().equals("mytrac_id")) needId=false;
            if (field.getName().equals(("mytrac_last_modified"))) needTs=false;
        }

        if(needId) {
            sb.append("mytrac_id bigint NOT NULL AUTO_INCREMENT");
            first=false;
        }


        for(Schema.Field field : schema.getFields())
        {
            if (!first) sb.append(",\n");

            String sql_sentence = buildFieldDescriptor(field);
            sb.append(sql_sentence);
            first=false;
        }
        sb.append(")");

        if(needTs)
        {
            sb.append("mytrac_last_modified timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)");
        }

        return sb.toString();
    }

    private static String buildFieldDescriptor(Schema.Field field) {
        String field_name = field.getName();
        if(field_name.equals("mytrac_id")) return "mytrac_id serial NOT NULL PRIMARY KEY";
        if(field_name.equals("mytrac_last_modified")) return "mytrac_last_modified timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)";
        String type = field.getType();

        String mysqlType = convertToMysqlType(type);

        String query =  field_name+" "+mysqlType;

        return query;
    }

    private static String convertToMysqlType(String type) {
        switch (type.toLowerCase()) {
            case "int":
                return "INTEGER";
            case "float":
                return "FLOAT";
            case "long":
                return "BIGINT";
            case "double":
                return "DOUBLE";
            case "boolean":
                return "BIT(1)";
            default:
                System.out.println("[ERROR] It doesn't exist a mapping type for "+type);
        }
        return null;
    }


    public static String insert(String tableName,String header, String[] record)
    {
        StringBuilder sb = new StringBuilder("INSERT INTO "+tableName+header);
        sb.append("VALUES (");
        boolean first=true;
        for(String value : record)
        {
            value= value.replace("'","");
            if(!first) sb.append(", ");

            if (value.equalsIgnoreCase("NULL"))
                sb.append(value);
            else
                sb.append("'"+value+"'");
            first=false;
        }
        sb.append(")");
        return sb.toString();
    }


}
