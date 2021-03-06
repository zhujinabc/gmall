package com.kqkj.gmall.canal.app;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.kqkj.gmall.canal.handler.CanalHandler;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalApp {
    public static void main(String[] args) {
        //创建连接器
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("192.168.100.17", 11111), "example", "", "");
        while(true){
            //连接订阅抓取数据
            canalConnector.connect();
            canalConnector.subscribe("gmall.people");
            Message message = canalConnector.get(10);
            int size = message.getEntries().size();
            if(size == 0 ){
                try {
                    Thread.sleep(5000);
                    System.out.println("无数据");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                for (CanalEntry.Entry entry : message.getEntries()){
                    //判断事件类型。只处理行变化业务
                    if(entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)){
                        //把数据进行反序列化
                        ByteString storeValue = entry.getStoreValue();
                        CanalEntry.RowChange rowChange = null;
                        try {
                            rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        //获得行集
                        List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                        //操作类型
                        CanalEntry.EventType eventType = rowChange.getEventType();
                        //表名
                        String tableName = entry.getHeader().getTableName();
                        System.out.println(tableName);
                        CanalHandler.handle(tableName,eventType,rowDatasList);
                    }
                }
            }
        }

    }
}
