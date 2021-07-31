# EntryLib

EntryLib 是一个基于 [Mirai-Console](https://github.com/mamoe/mirai-console) 的插件，用于实现群词条、自定义回复或更多功能。

![version](https://img.shields.io/badge/%E5%BD%93%E5%89%8D%E7%89%88%E6%9C%AC-v1.0.6-informational)

![GitHub issues](https://img.shields.io/github/issues/BillYang2016/entrylib)
![GitHub stars](https://img.shields.io/github/stars/BillYang2016/entrylib)
![GitHub downloads](https://img.shields.io/github/downloads/BillYang2016/entrylib/total)

# 目录
- [声明](#声明)
- [使用方法](#使用方法)
- [基本指令列表](#基本指令列表)
- [匹配顺序](匹配顺序)
- [群分组](#群分组)
- [额外说明](#额外说明)
- [配置项](#配置项)
- [控制台](#控制台)
- [FAQs](#faqs)
- [Mirai 码支持](#mirai-码支持)
- [数据库结构](#数据库结构)
- [插件依赖](#插件依赖)

# 声明
- 本插件仅作为学习交流等使用，请勿用于盈利，否则法律后果自负。
- [欢迎加入~~原神~~交流群：735209171](https://jq.qq.com/?_wv=1027&k=5cV7uEJ)
- 如果在使用插件过程中遇到任何问题，请发送 [Issues](https://github.com/BillYang2016/entrylib/issues) 询问
- 插件开发初期，有许多期待完善之处，欢迎提交代码风格相似的 [PR](https://github.com/BillYang2016/entrylib/pulls)

## 可用 console 版本

### 0.5.2
已停止维护，不保证可用性

### 1.0
已停止维护，不保证可用性

### 2.x 系列
- 2.6.x
- 2.7-M1
- 2.7-M2

# 使用方法
1. 请在 [Mirai-Console](https://github.com/mamoe/mirai-console) 框架下使用本插件
2. 从 [Release](https://github.com/BillYang2016/entrylib/releases) 下载最新版jar
3. 放置在 Mirai 目录的 plugins 文件夹下
4. [下载并安装](https://www.runoob.com/sqlite/sqlite-installation.html) [Sqlite3](https://www.sqlite.org/download.html) ，添加至环境变量
5. 运行 Mirai 并登录机器人
6. 在机器人所在群里发送`打开词条开关`，若获得回复`已启用词条库插件！`，则安装成功

# 基本指令列表

## "学习#[词条名]#[词条内容/回复项]#[匹配方式]"
**作用：学习一个新的词条，记入数据库中**  
*例：学习#词条库#欢迎使用词条库#精确*  

### 理想回复
![](/images/learn-reply.png)  
### 格式说明
1. 请使用`#`分割
2. 匹配方式为可选项，包含`精确`、`模糊`、`正则`，默认为`精确`
3. 如果使用正则，请在词条名处填写正则表达式，并可在内容中用`$1`,`$2`...代表分组截获
4. 正则示例：`学习#^.*机器人(.+)了.*$#机器人没$1#正则`，[调整`view-mode`](#配置项)后输入`机器人坏了`，获得回复`机器人没坏`
5. 使用`\`进行转义，详见[额外说明](#额外说明)
## "查看#[词条名]"
**作用：查看词条内容**  
*例：查看#词条库*  

### 理想回复
![](/images/view-reply.png)  
## "删除#[词条名]"
**作用：删除词条**  
*例：删除#词条库*  

### 理想回复
![](/images/delete-reply.png)  
## "历史#[词条名]#[页码]"
**作用：查看词条修改历史**  
*例：历史#词条库*  

### 理想回复
![](/images/history-reply.png)  
### 格式说明
1. 页码为可选项，只可填写数字，默认为`1`
2. 因为长度限制，因此一页所显示的数量有限，详见[配置项](#配置项)
## "搜索#[关键词]#[页码]"
**作用：检索与关键词有关的所有词条**  
*例：搜索#词条*  
### 理想回复
![](/images/search-reply.png)  

### 格式说明
1. 页码为可选项，只可填写数字，默认为`1`
## "全部#[页码]"
**作用：检索所有词条**  
*例：全部#2*  
### 理想回复
![](/images/all-reply.png)  

### 格式说明
1. 页码为可选项，只可填写数字，默认为`1`
2. 若不填写页码，也可以直接使用`全部`作为命令
## 打开/关闭词条开关
**作用：开启或关闭对应群聊的插件开关**  
*例：打开词条开关*  

# 匹配顺序
若存在多个满足条件的词条，查看/历史类指令优先返回规则如下：
1. 检测是否有存在的精确匹配，若存在返回最小编号
2. 检测是否有存在的模糊匹配，若存在返回最小编号
3. 检测是否有存在的正则匹配，若存在返回最小编号

-----

以上规则有待优化，并加入手动调整优先级的方法

# 群分组
群分组为机器人提供了 对不同群的操作处理使用同一个数据库 的解决方案  
在配置好了群分组文件后，在同一分组内的所有群共享一个数据库

可以通过编辑[配置项](#配置项)的`subgroup.json`文件，来配置群分组  
这个文件的格式样例可以参考[这里](https://github.com/BillYang2016/entrylib/blob/main/src/main/resources/subgroup-template.json)  
群分组配置文件由键值对构成，键为分组名，值为群组列表（群号字符串列表）  
## 注意事项
1. 群分组配置现已**支持热加载**，插件启动时从硬盘读取配置，也可以修改文件后从[控制台](#控制台)更新加载
2. 分组名不允许全为数字，原因请见[数据库结构](#数据库结构)
3. 不允许同一个群同时存在于多个分组中
4. 若群分组加载错误或失败，将在控制台发送错误信息，此时的群分组状态为空
5. 一旦群被纳入分组，其以前的数据库仍然保留但暂不使用；一旦群被移出分组，将会沿用以前的数据库
6. 若分组被删除，其对应的数据库并不会清空，需要手动删除

# 额外说明
1. 本插件仅适用于群聊，且每个群聊独立拥有自己的词条库
2. 本插件支持转义，可以使用`\#`来避免文本被解析为分隔符，也可以使用`\\`来避免右斜杠被识别为转义符
3. 本插件的指令与回复均可自定义，详见[配置项](#配置项)
4. 如果将回复配置为空，插件将不会发送消息
5. 拒绝以`__MAIN_TABLE`、开关指令、全部指令为词条名的修改与访问，插件将会检测并作出反馈
6. 删除命令的默认权限为管理员以上才可使用，因为不同于更新为空，一旦词条被删除，它的所有历史备份均被删除，且不会再出现在搜索结果中
7. 插件 24h 为周期执行一次数据库整理任务，目的是为了将数据库结构变得更方便管理。造成数据库需要整理的原因一般是删除了词条表造成编号不连续，或上次整理失败中途退出。整理状态会在控制台进行通知，请注意查看当前整理状态
8. 在插件周期执行数据库整理任务时，同时也会清理未使用的缓存的图片文件
9. 为什么偶尔会造成更新操作失败？是因为数据库正在执行其他工作，因此受到了并发性限制。该问题发生概率很低，一般重试一次即可解决。若数据库长时间被占用，会出现一直无法执行更新的现象，若发生需要请发送 [Issues](https://github.com/BillYang2016/entrylib/issues) 反馈

# 配置项
配置项位于`Mirai\data\EntryLib\`文件夹中

## `global.json`
本配置项提供对插件全局的控制，包含以下项目：
1. "view-mode"：`0`表示需要输入查看指令才可查看词条内容，修改为`1`表示可以直接输入词条名来查看词条内容（此时插件将不会再反馈查看指令错误信息）
2. "random-reply"：`0`表示回复词条最新版本内容，修改为`1`表示回复词条内容时从所有历史版本中随机选择
3. "default-switch"：`1`表示所有群都默认启用本插件，修改为`0`表示所有群都默认禁用本插件
4. "switch-permission"：`1`表示只有群管等级以上的成员才有权力修改插件开关（即使用命令`打开/关闭词条开关`），`0`表示所有成员都可以修改
5. "history-max-height"：`3`表示对于历史指令，每页仅返回3个记录，请确保本数值为正整数
6. "search-max-height"：`5`表示对于搜索指令，每页仅返回5个记录，请确保本数值为正整数
7. "reply-mode"：`0`表示机器人将普通回复，`1`表示机器人回复时会@发送指令的成员，`2`表示机器人回复时会引用指令消息
8. "at-all-permission"：`0`表示默认不允许机器人发送@全体成员，此时它将会被解析为 Mirai 码，修改为`1`表示允许机器人发送（机器人是管理员时才可生效）
9. "xxx-permission"：包含各类命令的执行权限，`1`为管理员表示只有群管等级以上的成员才可以使用，`0`表示所有成员都可以使用
10. "download-image"：`1`表示缓存接收到的图片（仅限于开关开启的群），修改为`0`表示不缓存。缓存图片可以保证您的词条图片不会丢失，但同时也会增加磁盘占用，若不缓存仅能保证在一个tx服务器缓存刷新周期内图片不会丢失（这个周期一般较长）

## `input.json`
本配置项提供用户键入指令的配置  
实质为键值对，键为用户输入，值为指令识别参数，请对照[基本指令列表](#基本指令列表)进行查看  
可以增加新的键值对来提供新的指令  
默认内容请查看[这里](https://github.com/BillYang2016/entrylib/blob/main/src/main/resources/input.json)

## `output.json`
本配置项提供插件回复项的配置  
实质为键值对，键为条件状况，值为实际回复项  
可以修改键值对来获得不同插件回复，但请勿增加或删除任意配置项  
默认内容请查看[这里](https://github.com/BillYang2016/entrylib/blob/main/src/main/resources/output.json)  
下面是一些参数解释：
1. "learn"中`$1`表示词条名
2. "view"中`$1`表示词条名，`$2`表示词条内容
3. "(history,reply)"中`$1`表示词条名，`$2`表示词条历史，`$3/$4`表示 页码/总页数
4. "(history,single)"表示单条历史格式，其中`$1`表示版本号，`$2`表示版本内容，`$3`表示修改时间

## `admin.json`
本配置项提供对管理员账号的配置
初始内容为空：
```json
[  ]
```
向内填入 QQ 账号，使其成为插件后台管理员，使用英文逗号分割
比如：
```json
[ 23333333, 114514 ]
```

插件后台管理员在插件功能上拥有与群主同样的权限

# 控制台
插件在 Windows 环境下运行时，将会创建系统托盘图标  
双击图标或点击菜单可以进入插件 GUI 控制台  
在控制台中可以方便地进行如下操作：
- 编辑全局配置
- 词条库导入导出
- 分组配置更新

## 词条库导入导出
导入导出会根据一个 json 文件进行，这个文件的格式样例可以参考[这里](https://github.com/BillYang2016/entrylib/blob/main/src/main/resources/datapackage-template.json)  
导出时将会生成上述格式的 json 文件  
导入时需要提供上述格式的 json 文件，同时可以选择三种覆盖方式：
1. 不覆盖相同词条
2. 合并相同词条
3. 覆盖相同词条

# FAQs
1. [#4. 请问如何才能让关键字触发的时候bot自动回复？](https://github.com/BillYang2016/entrylib/issues/4)

# Mirai 码支持
关于 Mirai 码的具体说明，请查看[Mirai 文档](https://github.com/mamoe/mirai/blob/dev/docs/Messages.md#mirai-%E7%A0%81)  

已适配的 Mirai 码：
- [x] 纯文本
- [x] 图片
- [x] At
- [x] AtAll
- [x] 表情
- [ ] 语音（需要等待 Mirai 接口稳定）
- [ ] 闪图（很可能不会适配）
- [ ] 戳一戳（很可能不会适配）
- [ ] Vip表情（Mirai 不支持发送，发送时会变成纯文本，因此不会适配）
- [ ] 富文本消息（很可能不会适配）
- [ ] 骰子
- [ ] 音乐分享（很可能不会适配）
- [ ] 文件信息（很可能不会适配）

# 数据库结构
插件采用 sqlite 作为数据库  
每一个群独立建立数据库，保存为`群号.db`文件  
每一个群分组独立建立数据库，保存为`群组名.db`文件  
每个数据库使用`__MAIN_TABLE`作为主表，储存了所有词条名  
每个词条单独建立`TABLE_[id]`表，储存该词条信息

# 插件依赖
本插件依赖于以下模块：
- mirai-console
- sqlite-jdbc
- fastjson

构建项目时请注意引用
