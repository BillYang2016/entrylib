package com.billyang.entrylib.MiraiCodeParser;

import com.billyang.entrylib.Config.UserIO;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.File;

/**
 * Mirai 码解析器
 * 负责将含有 Mirai 码的文本消息解码为包含多种消息内容的消息队列
 * 并可将后者编码成为前者
 * 构造本解析器时需要传递配置文件 UserIO
 * @see UserIO
 * @see FaceParser
 * @see ImageParser
 * @see AtParser
 * @see AtAllParser
 */
public class CodeParser {

    String path;
    UserIO uio;

    FaceParser faceParser;
    ImageParser imageParser;
    AtParser atParser;
    AtAllParser atAllParser;

    /**
     * 初始化
     * @param path 提供数据路径
     */
    public void init(String path, UserIO uio) {
        this.path = path;
        this.uio = uio;

        File file = new File(this.path);
        if(!file.exists()) {
            file.mkdirs();
        }

        faceParser = new FaceParser();
        imageParser = new ImageParser();
        atParser = new AtParser();
        atAllParser = new AtAllParser();
    }

    /**
     * 构造函数
     * 自动初始化
     */
    public CodeParser(UserIO uio) {
        init("data/EntryLib", uio);
    }

    /**
     * 将消息转义为 Mirai 码
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     */
    public MessageChain Encode(MessageChain msgChain) {
        msgChain = faceParser.Face2PlainText(msgChain);
        msgChain = imageParser.Image2PlainText(uio, msgChain);
        msgChain = atParser.At2PlainText(msgChain);
        msgChain = atAllParser.AtAll2PlainText(msgChain);

        return msgChain;
    }

    /**
     * 将 Mirai 码反转义为消息队列
     * @param g 原消息事件
     * @param text 纯文本
     * @return 反转义后的消息队列
     */
    public MessageChain Decode(GroupMessageEvent g, String text) {
        MessageChain msgChain = faceParser.PlainText2Face(g, text);
        msgChain = imageParser.PlainText2Image(g, msgChain);
        msgChain = atParser.PlainText2At(g, msgChain);
        if(uio.getAtAllPermission()) msgChain = atAllParser.PlainText2AtAll(g, msgChain); //查询配置文件是否允许解码

        return msgChain;
    }

}
