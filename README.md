# EntryLib

EntryLib 是一个基于 [Mirai-Console](https://github.com/mamoe/mirai-console) 的插件，用于实现群词条、自定义回复或更多功能。

# 注意[Working In Progress]

## 项目仍在开发中

**目前版本：0.1.3**

开发模块：
- [x] Mirai通信
- [ ] 指令模块
- [x] 数据库模块
- [x] 用户OI
- [ ] GUI
- [x] 语义分析

# 声明

## 可用console版本

- 0.5.2
- 1.0
- 2.0
- 2.7-M1

# 使用方法

# 基本指令列表

## "学习#[词条名]#[词条内容/回复项]"
学习一个新的词条，记入数据库中
## "查看#[词条名]"
查看词条内容
## "历史#[词条名]"
查看词条修改历史
## "搜索#[关键字]"
搜索包含关键字的词条名

# 配置项

# 插件依赖
本插件依赖于以下模块：
- mirai-console
- sqlite-jdbc
- fastjson