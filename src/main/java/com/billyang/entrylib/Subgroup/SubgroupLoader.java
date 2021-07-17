package com.billyang.entrylib.Subgroup;

import com.alibaba.fastjson.JSON;
import com.billyang.entrylib.Config.UserIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SubgroupLoader 类
 * 群分组加载器
 */
public class SubgroupLoader {
    List<Subgroup> list = new ArrayList<>();
    Map<Long, Subgroup> map = new HashMap<>();
    String path;

    /**
     * 初始化 subgroup.json
     */
    void initFile() {
        File file = new File(path,"subgroup.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("{\n" +
                        "}"
                );

                writer.close();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 加载 subgroup.json
     * 得到分组映射，并不再改变
     * @param path 配置路径
     * @param ErrorInfo 传递错误信息
     * @return 返回加载成功状态
     */
    public boolean load(String path, StringBuilder ErrorInfo) {
        this.path = path;

        File file = new File(path,"subgroup.json");
        if(!file.exists()) initFile();

        StringBuffer sb = UserIO.readFile(file);

        try {
            HashMap<String, List<String> > localMap = JSON.parseObject(sb.toString(), HashMap.class);

            for(Map.Entry<String, List<String>> entry : localMap.entrySet()) {
                String name = entry.getKey();
                List<Long> localList = parseLongList(entry.getValue());

                if(name.matches("[0-9]*")) {
                    ErrorInfo.append("分组名 ").append(name).append(" 不允许全为数字！\n");
                    clear();
                    return false;
                }

                Subgroup subgroup = new Subgroup(name, localList);
                list.add(subgroup);

                for(long groupId: localList) {
                    if(map.get(groupId) != null) {
                        ErrorInfo.append("群 ").append(groupId).append(" 存在重复分组，群分组加载失败！\n");
                        clear();
                        return false;
                    }
                    map.put(groupId, subgroup);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorInfo.append("subgroup.json 格式不符合要求！\n");
            clear();
            return false;
        }

        return true;
    }

    private List<Long> parseLongList(List<String> StringList) {
        List<Long> LongList = new ArrayList<>();
        for(String item: StringList) LongList.add(Long.parseLong(item));
        return LongList;
    }

    /**
     * 获取群所在的分组
     * 若群不存在于分组中，则返回 null
     * @param groupId 群号
     * @return 群所在分组
     */
    public Subgroup get(long groupId) {return map.get(groupId);}

    /**
     * 清空群分组配置
     */
    public void clear() {
        list.clear();
        map.clear();
    }
}
