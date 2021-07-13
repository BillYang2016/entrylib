package com.billyang.entrylib.EntryPackage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.billyang.entrylib.*;
import com.billyang.entrylib.Config.UserIO;
import com.billyang.entrylib.Database.*;
import com.billyang.entrylib.Matcher.MatchLoader;
import com.billyang.entrylib.Matcher.MatchValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PackageLoader 类
 * 负责对词条库进行导入与导出
 */
public class PackageLoader {

    Database db;

    /**
     * 初始化
     * @param db 提供数据库
     */
    public void init(Database db) {
        this.db = db;
    }

    /**
     * 从文件导入词条至目标群数据库
     * @param groupId 目标群号
     * @param file 词条包文件
     * @param overwrite 是否复写相同词条（0表示不覆写，1表示合并，2表示覆写）
     * @param ErrorInfo 传递错误信息
     * @return 导入状态
     */
    public boolean leadIn(long groupId, File file, int overwrite, StringBuilder ErrorInfo) {
        StringBuffer sb = UserIO.readFile(file);
        List<PackageValue> packageList;

        try {
            packageList = JSONArray.parseArray(sb.toString(), PackageValue.class);
        } catch (Exception e) {
            ErrorInfo.append("json 反序列化失败，或许是文件格式不正确？\n");
            e.printStackTrace();
            return false;
        }

        for(PackageValue pv: packageList) {
            if(!db.connect(groupId)) {
                ErrorInfo.append("无法连接至数据库！");
                return false;
            }

            int id = db.find_id(pv.getTitle());
            if(id > 0) { //词条已存在
                db.close();
                if(overwrite == 1) {
                    for(QueryValue qv: pv.getHistory())db.insert(groupId, pv.getTitle(), qv.getContent(), pv.getMode(), ErrorInfo);
                } else if(overwrite == 2) {
                    db.delete(groupId, pv.getTitle(), ErrorInfo);
                    for(QueryValue qv: pv.getHistory())db.insert(groupId, pv.getTitle(), qv.getContent(), pv.getMode(), ErrorInfo);
                }
            } else if(id == -3) {
                db.close();
                for(QueryValue qv: pv.getHistory())db.insert(groupId, pv.getTitle(), qv.getContent(), pv.getMode(), ErrorInfo);
            } else {
                db.close();
                ErrorInfo.append("导入 ").append(pv.getTitle()).append(" 词条时出错啦！");
                return false;
            }
        }

        return true;
    }

    /**
     * 从群数据库导出词条库至目标文件
     * @param ml 提供匹配器
     * @param groupId 目标群号
     * @param file 词条包文件
     * @param ErrorInfo 传递错误信息
     * @return 导出状态
     */
    public boolean leadOut(MatchLoader ml, long groupId, File file, StringBuilder ErrorInfo) {
        List<MatchValue> matchList = ml.all(groupId);
        List<PackageValue> packageList = new ArrayList<>();

        for(MatchValue mv: matchList) {
            List<QueryValue> history = db.history(groupId, mv.getId(), ErrorInfo);

            PackageValue pv = new PackageValue(mv.getTitle(), mv.getType(), history);

            packageList.add(pv);
        }

        UserIO.writeFile(file, JSON.toJSONString(packageList));

        return ErrorInfo.length() == 0;
    }

}
