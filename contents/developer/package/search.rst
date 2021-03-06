---
title: 索引和查询
description: 对象数据库和普通的关系数据库不一样，需要手工维护索引，目前还只支持内置的一组索引，不支持自定义索引。
---

=====================
对象索引和查询
=====================

.. contents::
.. sectnum::

对象的索引
============================================

有多少种索引，就能从多少角度搜索。
对象的所有属性和属性集都进入索引，另外还包括一组内置的、自动维护的属性:

- stati: 状态
- parent: 上一级对象的intid
- bytes: 大小
- content_type:   全文的内容类型，对于无法知道的内容类型，以 ``application/x-ext-{ext}`` 来替代
- metadata: 元数据
- acl_grant::

     {'Reader1': ['users.aa', 'groups.bb'],
      'Owner':['users.cc'],
     }

- acl_deny::

     {'Reader1': ['users.aa', 'groups.bb'],
      'Owner':['users.cc'],
     }

- object_types: 是什么类型的对象

  - 文件： File
  - 快捷方式：FileShortCut, FolderShortCut
  - 文件夹：Folder
  - 表单：DataItem 
  - 表单容器：DataContainer
  - 应用容器: AppContainer

  - 容器：Container
  - 条目：Item

- reference: 引用关系，表单里面字段引用出来的
- relations, 存放一个 name, ids 的嵌套表格::

      {'group', [123123, ],
       'children': [],
       'parent': [],
       'relate': [],
      }

下面的几个索引，是计算出来，方便快速搜索：

- path: 路径，值是所有父对象intid的集合
- file_content:     文件内包含的文本文件，用于全文搜索 
- allowed_principals:     授权的人
- disallowed_principals:  禁止的人

下面是分子字段的索引:

- fields: 各个字段的值
- settings: 设置信息::

     {"defaul_view": 'index',
      "item_schema": ('zopen.sales:changes', ),
      "test": {'a':'aa', 'b':'bb'}
     }

- mdsets: 属性集  

索引维护
===============

系统不会自动建立和更新索引。添加修改修改后重建索引

建立索引，recursive是否递归::

  obj.index(recursive=False)
  obj.unindex(recursive=False)

对fields字段更新索引,recursive是否递归::

  obj.reindex(recursive=False, fields=[])

对一个文件对象或文件夹对象，经行全文索引，以便可以通过文件里面的文字，搜索出这个文件对象 例子::

  obj.index_fulltext(recursive=False, include_history=False)

- recursive #如果obj是文件夹对象，则这个参数应该是True，让程序递归对文件夹对象下的文件对象做全文索引
- include_history #对文件对象的历史版本也做全文索引

搜索
============

搜索表达式
----------------------
搜索是对字段进行搜索，我们先看一个例子:::

  result = QuerySet().\ 
           anyof(path=[container]).\
           anyof(subjects=[‘aa’,’bb’]).
           range(created=[None, datetime.datetime.today()]).\
           parse('我爱北京', fields=['title']).\
           sort(‘-created’).limit(5)

QuerySet常用操作：

- eq: 等于
- anyof: 满足任何一个
- allof: 满足全部
- range: 一个区间范围
- exclude: 等于
- exclude_anyof: 不满足任何一个
- exclude_allof: 不满足全部
- exclude_range: 不在一个区间范围
- parse #搜索某字段
- sum(field) #统计某一个字段的和
- limit(x) #限制返回结果数 
- sort(Field) #按字段排序， 可已"+" 或"-"开头 , 以"-"开头时倒序排列
- ``exclude(**expression)`` #排除条件符合条件的结果

合并搜索
-----------
另外，可以将2个QuerySet相加，进行搜索合并::

 result = QuerySet().anyof(...) | QuerySet().allof(...).exclude(...)

如果2个QeurySet都有排序和sum操作，以第一个为准.

搜索属性集中的属性
-------------------------
调用filter或parse方法时，上面的field试用于 内置属性、基础属性和表单属性。
对于属性集中的字段，则需要增加一个 ``mdset`` 参数来指明属性集的名称。

下面的例子表示依据档案扩展属性中的档案编号进行检索::

   .anyof(number=['A101', 'C103'], mdset="archive")

如果属性集是在扩展软件包中定义的, 需要指明软件包的位置::

   .anyof(number=['A101', 'C103'], mdset="zopen.archive:archive")

搜索设置信息
-----------------
::

   .anyof(default_view=['index', 'tabular'], field="settings")
   .anyof(aa=['index', 'tabular'], field="settings.default_view")

dict字段
------------------------------
授权信息 acl_grant /acl_deny 等，存放为dict格式，这时候搜索自动名是::

   <主字段名>.<dict的key (点号替换为_)>

搜索给zhangsan授权Owner的内容::

   QuerySet().anyof(Owner=['users.pan', 'users.zhang'], field='acl_grant')

表单中的分用户存储字段，也是dict类型. 比如搜索属性集archive中的reviewer_comment字段::

   QuerySet().anyof(users_zhansan=['A101', 'C103'], field='review_comment', mdset="archive")

多行表格字段
--------------------------------
多行表格值 ``review_table`` 类似如下::

    [{'title':'aa', 'dept':['groups.121', 'groups.32']}, 
     {'title':'bb', 'dept':['groups.3212', 'groups.3212']}]

搜索表单中的动态表格reviewer_table中的dept字段::

   anyof(dept=['groups.1213', ], nested='review_table' )

搜索自定义属性集archive中的动态表格reviewer_table的dept字段::

   anyof(dept=['groups.1213', ], nested="review_table", mdset="archive")

全文搜索parse
------------------
默认所有字符串类型的字段，都支持全文搜索。

但是多值类型(list/tuple)中的字符串，不支持全文搜索，只能完全匹配:: 

   ('asd asd', 'fas', 'ssas')

如果搜索所有字段，可简单搜索::

   .parse('我北京')

如果要搜索多个字段::

   .parse('我北京', fields=['title', 'description'])

如果字段在属性集里面::

   .parse('我北京', fields=[{'archive.title', 'archive.description'])

如果字段在嵌套字段里面::

   .parse('我北京', fields=['.table.title', '.table.description'])
   .parse('我北京', fields=['archive.table.title', 'archive.table.description'])

如果需要搜索文件内容，需要使用 ``full`` 的全文索引::

   QuerySet('full').parse('北京', fields=['file_content'])

无权限和历史版本文档
-----------------------
这个搜索默认只搜索当前用户有权限查看的文件，以及最新版本的文件，可以调整改变::

  QuerySet(restricted=False， include_archive=True)

- ``restricted=False`` 表示仅仅搜索当前用户许可搜索的内容
- ``include_archive=True`` 表示可以搜索历史版本

直接采用JSON格式查询
----------------------------
TODO

搜索结果和分页
-------------------------------
搜索结果是一个list，len(result)可得到结果的数量。遍历搜索结果::

  for obj in result:
    do something

当你需要显示的东西（results） 太多了，一个页面放不下的时候，可以使用Batch.

下面例子，可以让results 每页只显示20个::

  # view.py
  batch = Batch(results, start=request.get(‘b_start’, 0), size=20)
  for obj in batch:
      ...

  batch_html = renderBatch(context, request, batch)

搜索流程工作项
-------------------------
评论和流程的工作项，是独立的索引，搜索方法为::

   QuerySet('workitem').anyof(path=[project])\
            .anyof(stati=('flowtask.active', 'flowtask.pending')).\
                    anyof(Responsible=(pid, ), field='acl_grant')

