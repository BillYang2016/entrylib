package com.billyang.entrylib;

import com.alibaba.fastjson.JSONObject;

import java.io.*;

public class UserIO { //用户交互类

    String path;

    void init(String path) {

        this.path = path;

        File file = new File(path,"config.json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream fop = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");

                writer.append("{\n" +
                              "  \"learn\":{\n" +
                              "    \"done\":\"已更新 $1 词条！\",\n" +
                              "    \"fail\":\"更新 $1 词条失败！\"\n" +
                              "  },\n" +
                              "  \"view\":{\n" +
                              "    \"reply\":\"$1 的内容如下：\\n--------\\n$2\",\n" +
                              "    \"exist\":\"未找到 $1 相关的词条！\",\n" +
                              "    \"error\":\"查询 $1 时出错啦！\"\n" +
                              "  },\n" +
                              "  \"history\":{\n" +
                              "    \"reply\":\"$1 的历史情况如下：\\n--------\\n$2\",\n" +
                              "    \"single\":\"版本$1（修改时间：$3）：\\n-----\\n$2\\n-----\",\n" +
                              "    \"exist\":\"未找到 $1 相关的词条！\",\n" +
                              "    \"error\":\"查询 $1 时出错啦！\"\n" +
                              "  },\n" +
                              "  \"search\":{\n" +
                              "    \"reply\":\"根据 $1 搜索到如下词条：\\n$2\",\n" +
                              "    \"single\":\"- $1\",\n" +
                              "    \"exist\":\"未找到 $1 相关的词条！\",\n" +
                              "    \"error\":\"查询 $1 时出错啦！\"\n" +
                              "  }\n" +
                              "}"
                );

                writer.close();
                fop.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    String format(String Ftype,String Stype, String... args) {
        File file = new File(path,"config.json");
        if(!file.exists())init(path);

        StringBuffer sb = null;
        try {
            FileInputStream fip = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fip, "UTF-8");
            sb = new StringBuffer();

            while (reader.ready())sb.append((char) reader.read());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject configjson = JSONObject.parseObject(sb.toString());
            JSONObject Sjson = configjson.getJSONObject(Ftype);
            String answer = Sjson.getString(Stype);

            for(int i = 0; i < args.length; i++)answer = answer.replace("$"+String.valueOf(i+1),args[i]);

            return answer;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
