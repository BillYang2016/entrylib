package com.billyang.entrylib.MiraiCodeParser;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FaceProcessor 类
 * 表情处理器
 * 实现对表情进行转义与反转义
 * @author Bill Yang
 */
public class FaceParser {

    /**
     * 将表情 ID 转化为 Mirai 码
     * @param Id 表情 ID
     * @return Mirai 码
     */
    public static String Id2MiraiCode(int Id) {return "[mirai:face:" + Id + "]";}

    /**
     * 将表情 Mirai 码转化为 表情 ID
     * @param code Mirai 码
     * @return 表情 ID
     */
    public static int MiraiCode2Id(String code) {return Integer.parseInt(code.replace("[mirai:face:", "").replace("]",""));}

    /**
     * 将消息队列中的表情转义为作为 Mirai 码的字符串
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     */
    public MessageChain Face2PlainText(MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage msg : msgChain) {
            if(msg instanceof Face) {
                Face face = (Face) msg;
                int faceId = face.getId();

                msg = new PlainText(Id2MiraiCode(faceId));
            }
            builder.append(msg);
        }

        return builder.build();
    }

    /**
     * 表情 Mirai 码的正则匹配式
     * @see Face
     */
    public static String regex = "\\[mirai:face:[0-9]*]";

    /**
     * 将纯文本中的表情 Mirai 码反转义为表情
     * @param g 原消息事件
     * @param msg 纯文本
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Face(GroupMessageEvent g, String msg) {
        MessageChainBuilder builder = new MessageChainBuilder();

        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(msg);

        int start, end = 0, lastEnd = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            if(start >= 1) builder.append(new PlainText(msg.substring(lastEnd, start)));

            int faceId = MiraiCode2Id(msg.substring(start, end));

            Face face = new Face(faceId);
            builder.append(face);

            lastEnd = end;
        }

        builder.append(new PlainText(msg.substring(end)));

        return builder.build();
    }

    /**
     * 将消息队列中的表情 Mirai 码反转义为表情
     * @param g 原消息事件
     * @param msgChain 消息队列
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Face(GroupMessageEvent g, MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage message: msgChain) {
            if(message instanceof PlainText) builder.append(PlainText2Face(g, message.contentToString()));
            else builder.append(message);
        }

        return builder.build();
    }
}
