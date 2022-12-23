package com.sql.datachange.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sql.datachange.Entity.WebSocketMsg;
import com.sql.datachange.data.DataBaseUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint(value = "/webSocket/{userId}")  //接收websocket请求路径
@Component  //注册到容器中
public class WebSocketServer {
    //当前连接（每个websocket连入都会创建一个WebSocket实例）
    private Session session;
    //存放对应的session
    private static ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap<>();
    //定义一个websocket容器存储session,即存放所有在线的socket连接
    private static CopyOnWriteArraySet<WebSocketServer> webSocketServerSet = new CopyOnWriteArraySet<>();

    //处理连接建立
    @OnOpen
    public void opOpen(Session session,@PathParam("userId") String userId) {
        System.out.println("WebSocket建立连接中，用户为："+userId);
        try {
            Session historySession = sessionPool.get(userId);
            //不为空说明已经有人登录账号，删除登录的webSocketServerSet
            if (historySession != null){
                webSocketServerSet.remove(historySession);
                historySession.close();
            }
        } catch (IOException e) {
            System.out.println("重复登陆异常");
            e.printStackTrace();
        }
        this.session = session;
        //log.info("【有新的客户端连接了】：{}",session.getId());
        webSocketServerSet.add(this);  //将新用户加入在线组
        sessionPool.put(userId,session);
        System.out.println("【websocket消息】有新的连接，总数：{}"+webSocketServerSet.size());
        //log.info("【websocket消息】有新的连接，总数：{}",webSocketSet.size());
    }
    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }

    //处理连接关闭
    @OnClose
    public void Onclose() {
        webSocketServerSet.remove(this);
        System.out.println("【websocket消息】连接断开，总数：{}"+webSocketServerSet.size());
        //log.info("【websocket消息】连接断开，总数：{}",webSocketSet.size());
    }

    //接受消息
    @OnMessage
    public void onMessage (String message) {
        Boolean flag = updateMessage(message);
        if (flag){
            JSONObject jsonObject = JSONObject.parseObject(message);
            Map map = DataIndex.sesson.get(jsonObject.getString("id"));
            JSONArray jsonArray = jsonObject.getJSONArray("conditionVal");
            String sql = jsonObject.getString("sql");
            String tableName = jsonObject.getString("tableName");
            String condition = jsonObject.getString("condition");
            WebSocketMsg msg = new WebSocketMsg();
            msg.setMessageType(1);
            msg.setCode(1);
            msg.setMessage("开始通过sql提取数据<br/>提取数据<span id ='selectDate'>0</span>条");
            sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
            //提取数据
            DataBaseUtil dataBaseUtil = new DataBaseUtil((String)map.get("driverClassName1"), (String)map.get("url1"), (String)map.get("userName1"), (String)map.get("password1"));
            List<String> types = dataBaseUtil.getColumnTypes(sql,"",false);
            List<String> names = dataBaseUtil.getColumnNames(sql,"",false);
            Map map1 = dataBaseUtil.getTableValue(sql,types,names,true,jsonObject.getString("id"));

            if ("查询成功".equals(map1.get("flag"))){
                //msg.setMessage("提取数据"+((List)map1.get("result")).size()+"条");
                //sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
                List list = (List)map1.get("result");
                DataBaseUtil dataBaseUtil2 = new DataBaseUtil((String)map.get("driverClassName2"), (String)map.get("url2"), (String)map.get("userName2"), (String)map.get("password2"));
                dataBaseUtil2.commitFalse();
                List<String> listCol = jsonArray.toJavaList(String.class);
                msg.setMessage("开始更新插入数据<br/>更新数据<span id = 'updateData'>0</span>条<br/>插入数据<span id = 'insertData'>0</span>条");
                sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));

                Boolean errCode = true;
                Integer insertNum = 1;
                Integer updateNum = 1;
                for (int i=0;i<list.size();i++){
                    Map map2 = dataBaseUtil2.updateTable(tableName,condition,listCol,(Map)list.get(i));
                    if ("插入成功".equals(map2.get("flag"))){
                        if ("insert".equals(map2.get("result"))){
                            msg.setMessageType(2);
                            msg.setMessage("insert:"+insertNum);
                            sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
                            insertNum += 1;
                        }else if ("update".equals(map2.get("result"))){
                            msg.setMessageType(2);
                            msg.setMessage("update:"+updateNum);
                            sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
                            updateNum += 1;
                        }
                    }else{
                        errCode = false;
                        msg.setCode(2);
                        msg.setMessage((String)map2.get("flag"));
                        sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
                        break;
                    }
                }
                if (errCode){
                    dataBaseUtil2.commitValue();
                }else{
                    dataBaseUtil2.commitRollbock();
                }
                dataBaseUtil2.closeConnection();
                msg.setMessageType(1);
                msg.setCode(2);
                msg.setMessage("迁移结束");
                sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
            }else{
                msg.setCode(2);
                msg.setMessage((String)map1.get("flag"));
                sendMessageByUser(jsonObject.getString("id"),JSONObject.toJSONString(msg));
            }

        }
        //log.info("【websoHcket消息】收到客户端发来的消息：{}",message);
    }

    /**
     * 推送消息给指定用户
     * @param userId
     * @param message
     */
    public static void sendMessageByUser(String userId,String message){
        System.out.println("发送消息"+message+"给："+userId);
        try {
            Session session = sessionPool.get(userId);
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            System.out.println("发送消息异常");
            e.printStackTrace();
        }

    }

    // 群发消息
    public static void sendMessage (String message){
        for (WebSocketServer webSocketServer : webSocketServerSet) {
            //log.info("【websocket消息】广播群发消息,message={}",message);
            try {
                webSocketServer.session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理心跳
     * @param message
     * @return
     */
    public Boolean updateMessage(String message){
        if (message.contains(":发送一次心跳")){
            System.out.println(message);
            sendMessage("处理一次心跳");
            return false;
        }else{
            return true;
        }
    }

}

