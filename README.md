# EntryLib

EntryLib 是一个基于 [Mirai-Console](https://github.com/mamoe/mirai-console) 的插件，用于实现群词条、自定义回复或更多功能。

![version](https://img.shields.io/badge/%E5%BD%93%E5%89%8D%E7%89%88%E6%9C%AC-v1.3.1-informational)

![GitHub issues](https://img.shields.io/github/issues/BillYang2016/entrylib)
![GitHub stars](https://img.shields.io/github/stars/BillYang2016/entrylib)
![GitHub downloads](https://img.shields.io/github/downloads/BillYang2016/entrylib/total)

# 说明文档
[基础用法文档（中文）](https://github.com/BillYang2016/entrylib/blob/main/docs/basic-CN.md)  
[进阶用法文档（中文）](https://github.com/BillYang2016/entrylib/blob/main/docs/advanced-CN.md)  

# 声明
- 本插件仅作为学习交流等使用，请勿用于盈利，否则法律后果自负。
- [欢迎加入~~原神~~交流群：735209171](https://jq.qq.com/?_wv=1027&k=5cV7uEJ)
- 如果在使用插件过程中遇到任何问题，请发送 [Issues](https://github.com/BillYang2016/entrylib/issues) 询问
- 插件开发初期，有许多期待完善之处，欢迎提交代码风格相似的 [PR](https://github.com/BillYang2016/entrylib/pulls)

# FAQs
1. [#4. 请问如何才能让关键字触发的时候bot自动回复？](https://github.com/BillYang2016/entrylib/issues/4)
2. 更多使用问题请查看 [Closed Issue](https://github.com/BillYang2016/entrylib/issues?q=is%3Aissue+is%3Aclosed)，这可以解决您 90% 的问题

# 版本更新说明
因为插件不会再加载默认的`input.json`即输入配置文件  
故若有新命令加入请手动更新`input.json`  
如：1.3.1加入帮助命令，需在文件中加入：
```diff
    "搜索":"search",
+   "别名":"alias",
    "全部":"all",
+   "帮助":"help"
```

# 可用 console 版本

## 0.5.2
已停止维护，不保证可用性

## 1.0
已停止维护，不保证可用性

### 2.x 系列
- 2.6.x
- 2.7-M1
- 2.7-M2

# 插件依赖
本插件依赖于以下模块：
- mirai-console
- sqlite-jdbc
- fastjson

构建项目时请注意引用
