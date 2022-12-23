package com.sql.datachange.controller;

import com.sql.datachange.data.DataBaseUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


import java.util.*;

@RestController
public class DataIndex {

    public static Map<String,Map> sesson = new HashMap<>();

    @RequestMapping("/dataShow")
    public ModelAndView showDate() {
        ModelAndView model = new ModelAndView("dataShow.html");
        UUID uuid = UUID.randomUUID();
        //HttpSession session = request.getSession();
        sesson.put(uuid.toString(), new HashMap<>());
        //session.setAttribute(uuid.toString(), new HashMap<>());
        model.addObject("id", uuid.toString());
        return model;
    }

    /**
     * 获取表名
     * @param driverClassName
     * @param url
     * @param userName
     * @param password
     * @param id
     * @param num
     * @return
     */
    @RequestMapping("/getTable")
    public Map getSqlTable(@RequestParam String driverClassName, @RequestParam String url, @RequestParam String userName, @RequestParam String password, @RequestParam String id, @RequestParam Integer num) {
        Map map = new HashMap();
        Map map1 =  sesson.get(id);
        map1.put("driverClassName" + num, driverClassName);
        map1.put("url" + num, url);
        map1.put("userName" + num, userName);
        map1.put("password" + num, password);
        sesson.put(id, map1);
        DataBaseUtil dataBaseUtil = new DataBaseUtil(driverClassName, url, userName, password);
        List<String> tableNames = dataBaseUtil.getTableNames();
        map.put("tableNames", tableNames);
        map.put("msg", dataBaseUtil.getMsg());
        return map;
    }

    /**
     * 获取字段名
     * @param table
     * @param id
     * @return
     */
    @RequestMapping("/getTableName")
    public Map getTableName(@RequestParam String table, @RequestParam String id) {
        Map map = new HashMap();
        Map map1 = sesson.get(id);
        DataBaseUtil dataBaseUtil = new DataBaseUtil((String)map1.get("driverClassName1"), (String)map1.get("url1"), (String)map1.get("userName1"), (String)map1.get("password1"));
        List<String> tableNames = dataBaseUtil.getColumnNames("",table,true);
        map.put("tableNames", tableNames);
        map.put("msg", dataBaseUtil.getMsg());
        return map;
    }

    @RequestMapping("/getTableColumnName")
    public Map getTableColumnName(@RequestParam String sql, @RequestParam String tableName2, @RequestParam String id) {
        Map map = new HashMap();
        Map map1 = sesson.get(id);
        List<String> tableColumn1 = new ArrayList<>();
        List<String> tableColumn2 = new ArrayList<>();
        String msg = "success";
        DataBaseUtil dataBaseUtil = new DataBaseUtil((String)map1.get("driverClassName1"), (String)map1.get("url1"), (String)map1.get("userName1"), (String)map1.get("password1"));
        List<String> tableColumnNames = dataBaseUtil.getColumnNames(sql,"",false);
        List<String> tableColumnTypeNames = dataBaseUtil.getColumnTypes(sql,"",true);
        if (tableColumnNames.size()<1){
            msg = "sql查询出错";
        }else{
            for (int i=0;i<tableColumnNames.size();i++){
                String column = tableColumnNames.get(i)+"("+tableColumnTypeNames.get(i)+")";
                tableColumn1.add(column);
            }
        }

        DataBaseUtil dataBaseUti2 = new DataBaseUtil((String)map1.get("driverClassName2"), (String)map1.get("url2"), (String)map1.get("userName2"), (String)map1.get("password2"));
        List<String> tableColumnNames2 = dataBaseUti2.getColumnNames("",tableName2,false);
        List<String> tableColumnTypeNames2 = dataBaseUti2.getColumnTypes("",tableName2,true);
        if (tableColumnNames2.size()<1){
            msg = "目标库字段查询出错";
        }else{
            for (int i=0;i<tableColumnNames2.size();i++){
                String column = tableColumnNames2.get(i)+"("+tableColumnTypeNames2.get(i)+")";
                tableColumn2.add(column);
            }
        }
        map.put("tableColumn1",tableColumn1);
        map.put("tableColumn2",tableColumn2);
        map.put("msg",msg);
        return map;
    }
}