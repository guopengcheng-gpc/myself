package com.sql.datachange.data;

import com.alibaba.fastjson.JSONObject;
import com.sql.datachange.Entity.WebSocketMsg;
import com.sql.datachange.controller.WebSocketServer;

import java.sql.*;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseUtil {
    //private static final Logger logger = LoggerFactory.getLogger(DatabaseTest.class);
    private  String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private  String DATABASE_URL = "jdbc:oracle:thin:@localhost:1521:orcl";//jdbc:mysql://152.136.246.135:3306/temp?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&allowMultiQueries=true
    private  String USERNAME = "RDSYSEDUV85210113";
    private  String PASSWORD = "Eplugger231";
    private static String SQL = "SELECT * FROM ";// 数据库操作
    private String MSG = "success";
    private Connection conn;

    public DataBaseUtil(String DRIVER, String DATABASE_URL, String USERNAME, String PASSWORD) {
        this.DRIVER = DRIVER;
        this.DATABASE_URL = DATABASE_URL;
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("can not load jdbc driver");
            MSG ="注册驱动错误";
            e.printStackTrace();
            //logger.error("can not load jdbc driver", e);
        }
        conn = getConnection();
    }
    /**
     * 获取数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("get connection failure");
            MSG = MSG+"数据库连接失败";
            e.printStackTrace();
            //logger.error("get connection failure", e);
        }
        return conn;
    }
    /**
     * 关闭数据库连接
     * @param
     */
    public void closeConnection() {
        if(conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                System.out.println("close connection failure");
                MSG = MSG+ "数据库连接关闭失败";
                //logger.error("close connection failure", e);
            }
        }
    }
    /**
     * 获取数据库下的所有表名
     */
    public List<String> getTableNames() {
        List<String> tableNames = new ArrayList<>();
        //Connection conn = getConnection();
        ResultSet rs = null;
        try {
            //获取数据库的元数据
            DatabaseMetaData db = conn.getMetaData();
            //从元数据中获取到所有的表名
            if (DATABASE_URL.contains("oracle")){
                rs = db.getTables(null, USERNAME, null, new String[] { "TABLE" });
            }else{
                rs = db.getTables(null, null, null, new String[] { "TABLE" });
            }
            while(rs.next()) {
                tableNames.add(rs.getString(3));
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("getTableNames failure");
            MSG = MSG+ "获取表名错误";
            e.printStackTrace();
            //logger.error("getTableNames failure", e);
        } finally {
            try {
                rs.close();
                closeConnection();
            } catch (Exception e) {
                System.out.println("close ResultSet failure");
                //logger.error("close ResultSet failure", e);
            }
        }
        return tableNames;
    }
    /**
     * 获取表中所有字段名称
     * @param tableName 表名
     * @param sql sql
     * @param flag 是否关闭连接
     * @return
     */
    public List<String> getColumnNames(String sql,String tableName,Boolean flag) {
        List<String> columnNames = new ArrayList<>();
        //与数据库的连接
        //Connection conn = getConnection();
        PreparedStatement pStemt = null;
        String tableSql;
        if ("".equals(sql) ||sql == null){
            tableSql = SQL + tableName;
        }else{
            tableSql = sql;
        }
        try {
            pStemt = conn.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            for (int i = 0; i < size; i++) {
                columnNames.add(rsmd.getColumnName(i + 1));
            }
        } catch (Exception e) {
            System.out.println("getColumnNames failure");
            MSG = MSG+ "获取表字段错误";
            //logger.error("getColumnNames failure", e);
        } finally {
            if (pStemt != null) {
                try {
                    pStemt.close();
                    if (flag){
                        closeConnection();
                    }
                } catch (SQLException e) {
                    System.out.println("getColumnNames close pstem and connection failure");
                    //logger.error("getColumnNames close pstem and connection failure", e);
                }
            }
        }
        return columnNames;
    }
    /**
     * 获取表中所有字段类型
     * @param tableName
     * @return
     */
    public List<String> getColumnTypes(String sql,String tableName,Boolean flag) {
        List<String> columnTypes = new ArrayList<>();
        //与数据库的连接
        //Connection conn = getConnection();
        PreparedStatement pStemt = null;
        String tableSql;
        if ("".equals(sql) ||sql == null){
            tableSql = SQL + tableName;
        }else{
            tableSql = sql;
        }
        try {
            pStemt = conn.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            for (int i = 0; i < size; i++) {
                columnTypes.add(rsmd.getColumnTypeName(i + 1));
            }
        } catch (Exception e) {
            System.out.println("getColumnTypes failure");
            MSG = MSG+ "获取表字段类型错误";
            //logger.error("getColumnTypes failure", e);
        } finally {
            if (pStemt != null) {
                try {
                    pStemt.close();
                    if (flag){
                        closeConnection();
                    }
                } catch (Exception e) {
                    System.out.println("getColumnTypes close pstem and connection failure");
                    //logger.error("getColumnTypes close pstem and connection failure", e);
                }
            }
        }
        return columnTypes;
    }
    /**
     * 获取表中字段的所有注释 sql server不可用
     * @param tableName
     * @return
     */
    public List<String> getColumnComments(String tableName,Boolean flag) {
        List<String> columnTypes = new ArrayList<>();
        //与数据库的连接
        Connection conn = getConnection();
        PreparedStatement pStemt = null;
        String tableSql = SQL + tableName;
        List<String> columnComments = new ArrayList<>();//列名注释集合
        ResultSet rs = null;
        try {
            pStemt = conn.prepareStatement(tableSql);
            rs = pStemt.executeQuery("show full columns from " + tableName);
            //rs = pStemt.executeQuery("sp_columns " + tableName);
            while (rs.next()) {
                columnComments.add(rs.getString("Comment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    if (flag){
                        closeConnection();
                    }
                } catch (SQLException e) {
                    System.out.println("getColumnComments close ResultSet and connection failure");
                    //logger.error("getColumnComments close ResultSet and connection failure", e);
                }
            }
        }
        return columnComments;
    }

    /**
     * 通过sql获取数据
     * @param sql
     * @return
     */
    public Map getTableValue(String sql, List<String> types,List<String> names,Boolean flag,String id){
        Map map = new HashMap();
        PreparedStatement pStemt = null;
        ResultSet resultSet = null;
        map.put("result",new ArrayList<>());
        map.put("flag","查询成功");
        try {
            WebSocketMsg msg = new WebSocketMsg();
            msg.setMessageType(2);
            msg.setCode(1);
            pStemt = conn.prepareStatement(sql);
            resultSet = pStemt.executeQuery();
            List list = new ArrayList();
            Integer selectDate = 1;
            while (resultSet.next()) {
                Map map1 = new HashMap();
                for (int i=0;i<types.size();i++){
                    map1.put(names.get(i),resultSet.getObject(names.get(i)));
                }
                list.add(map1);
                msg.setMessage("select:"+selectDate);
                WebSocketServer.sendMessageByUser(id, JSONObject.toJSONString(msg));
                selectDate += 1;
            }
            map.put("result",list);
        } catch (SQLException e) {
            //输出sql执行异常
            map.put("flag","sql执行出错");
            e.printStackTrace();
        }finally {
            if (pStemt != null){
                try {
                    pStemt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (flag){
                closeConnection();
            }
        }
        return map;
    }

    public Map updateTable(String tableName,String condition,List<String> tableColumn,Map tableVal){
        //System.out.println(tableVal.toString());
        Map map = new HashMap();
        PreparedStatement pStemt = null;
        map.put("result","");
        map.put("flag","插入成功");
        String updateSql = "UPDATE "+tableName +" SET ";
        for (String column:tableColumn){
            updateSql = updateSql + "\"" + column.split("=")[1]+"\" = ?,";
        }
        updateSql = updateSql.substring(0,updateSql.length()-1);
        updateSql = updateSql + " WHERE "+ "\"" +condition.split("=")[1]+"\" = ?";
        //System.out.println(updateSql);
        int updateNum = 0;
        try {
            pStemt = conn.prepareStatement(updateSql);
            if (pStemt!= null){
                for (int i=0;i<tableColumn.size();i++){
                    Object o = tableVal.get(tableColumn.get(i).split("=")[0]);
                    pStemt.setObject(i+1,o);
                }
                Object o1 = tableVal.get(condition.split("=")[0]);
                pStemt.setObject(tableColumn.size()+1,o1);
                updateNum = pStemt.executeUpdate();
                pStemt.close();
            }

            //更新为0执行插入
            if (updateNum == 0){
                String insertSql = "INSERT INTO "+ tableName +"(";
                for (String column:tableColumn){
                    insertSql = insertSql + "\"" + column.split("=")[1]+"\",";
                }
                insertSql = insertSql.substring(0,insertSql.length()-1) +") VALUES(";
                for (String column:tableColumn){
                    insertSql = insertSql + "?,";
                }
                insertSql = insertSql.substring(0,insertSql.length()-1) +")";
                //System.out.println(insertSql);
                try {
                    pStemt = conn.prepareStatement(insertSql);
                    for (int i=0;i<tableColumn.size();i++){
                        Object o = tableVal.get(tableColumn.get(i).split("=")[0]);
                        pStemt.setObject(i+1,o);
                    }
                    pStemt.executeUpdate();
                    pStemt.close();
                    map.put("result","insert");
                } catch (SQLException e) {
                    map.put("flag","执行插入失败,失败数据为:"+tableVal.toString()+"<br/>"+e.getMessage());
                    e.printStackTrace();
                }
            }else{
                map.put("result","update");
            }
        } catch (SQLException e) {
            map.put("flag","执行更新失败,失败数据为"+tableVal.toString()+"<br/>"+e.getMessage());
            e.printStackTrace();
        }finally {
            if (pStemt != null){
                try {
                    pStemt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
    //关闭默认提交
    public void commitFalse(){
        if (conn != null){
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //提交
    public void commitValue(){
        if (conn != null){
            try {
                conn.commit();
            } catch (SQLException e) {
                System.out.println("提交出错");
                e.printStackTrace();
            }
        }
    }

    public void commitRollbock(){
        if (conn != null){
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.out.println("提交出错");
                e.printStackTrace();
            }
        }
    }


    public String getMsg(){
        return MSG;
    }
    /*public static void main(String[] args) {
        List<String> tableNames = getTableNames();
        System.out.println("tableNames:" + tableNames);
        for (String tableName : tableNames) {
            System.out.println("================start==========================");
            System.out.println("==============================================");
            System.out.println("ColumnNames:" + getColumnNames(tableName));
            System.out.println("ColumnTypes:" + getColumnTypes(tableName));
            //System.out.println("ColumnComments:" + getColumnComments(tableName));
            System.out.println("==============================================");
            System.out.println("=================end=======================");
        }
    }*/

}
