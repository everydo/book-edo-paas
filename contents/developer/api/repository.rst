---
title: 内容仓库
description: 系统首先是一个各种内容的存储仓库，都父子树状组织存放，有唯一的ID标识，支持版本，支持回收站
---

==================
内容仓库
==================

.. Contents::
.. sectnum::

系统主要管理各种文件、表单，通过各种文件夹、容器来组织管理。

树状对象关系
=====================

对象数据库中的主题对象，是一个树状的层次结构关系，如下::

    + 站点根
    |-----+容器（文件夹、项目、部门）
    |    |----+子容器1
    |    |   |--文件1
    |    |   |--文件2
    |    |----+子文件夹2
    | 	  |   |--….
    |-----+数据管理器
    |    |-- 流程单1
    |    |-- 流程单2

可以看到对象是父子包含关系。总体分为能包含其他对象的容器类型的对象，以及不能包含其他对象的非容器对象。

脚本中，可使用2个重要的内置变量：

- context: 是当前操作的对象
- container: 是当前对象context所在的容器对象，比如文件夹或者数据管理器。

对于任何一个对象，有如下方法得到其所在的容器信息：

- getParent(context)得到对象的父容器对象，也就是container
- getRoot()得到站点根对象

容器类对象
----------------
文件夹、流程、项目、部门、工作组等，都是容器类对象。

容器对象，是可以包含子对象的容器。可以用dict方法操作包含的子对象:

- container.values() 得到容器全部包含对象

  注意，如果数据量大，这个可能带来很严重的性能问题。因为所有数据会逐个从数据库加载到内存。如果是数据搜索过滤，应该采用搜索解决，通过索引来查找数据。具体见QuerySet查询接口。

- container.keys() 得到全部包含对象的名字
- container.items() 得到全部包含对象的列表 [(name, child_obj)]
- container [name]得到某个包含对象
- del contaner[name]删除某个包含对象

每个对象在容器中有一个唯一的名字，可以用getName得到::

  getName(context)

添加子对象
--------------
如果要添加一个对象，可以::

  container[name] = new_obj

注意每个container下的对象name必须唯一，可以使用INameChooser来自动生成name::

  new_name = INameChooser(container).chooseName(name)

对象路径
---------------
易度每个请求url，都是RESTful，由资源和操作2部分组成，比如::

 http://mycompanysite.com/files/folder_a/folder_b/@@view.html
 http://mycompanysite.com/files/folder_a/folder_b/@@@account.package.script

最后一个@@或@@@是一个视图标识符(表示可爱的眼睛)，将url分割为2部分：

- http://mycompanysite.com/files/folder_a/folder_b

  这个直接定位到一个资源，是资源的地址，这个资源就是context；站点域名后的每级名字，就是对象所在逐层父容器的名字。所以，要理解当前的context是什么，就要去看运行脚本的请求url是什么即可。

- view.html或account.package.script

  这个表示对资源的展示方法，是一个视图。

需要指出的例外情况：

- 如果url可能不包括@@，这表示使用了默认视图@@index.html
- 在流程单的添加表单，由于对象还没添加，按照前面的规则，context应该是数据管理器，但是从统一和简化的角度考虑，我们将context强制指定为None（表示正在创建），而用下面的container来表示数据管理器。

对象移动、复制
---------------------
可以使用"IObjectMover"接口移动对象或者改名::

    IObjectMover(context).moveTo(parent, new_name)
    IObjectMover(context).copyTo(parent, new_name)

具体的API包括：

- def moveTo(target, new_name=None): 移动对象到target 这个目录下，如果变量new_name 有值（非None）, 对象就改名为new_name。返回对象移动到target目录后的新名字。 需要注意的是target 是需要(implements) 实现IpasteTarget 这个接口.
- def moveable(): 如果这个对象允许移动， 就返回‘True‘, 否则返回‘False’
- def moveableTo(target, name=None): 如果对象允许移动到target 这个目录就返回‘True‘, 否则返回‘False’

对象的永久标识，以及快捷地址
======================================

数据库里面的对象，一旦发生移动或者改名，对象的路径就发生变化。这样用路径就不能来永久标识对象。

事实上，系统的所有对象，创建后，均会在一个全局的对象注册器intids中注册。一旦注册，系统会用一个长整数来永久标识这个对象。无论以后对象是否移动或者改名，都可以通过这个长整数快速找到对象自身::

  #通过长整数标识，找到对象
  intids.getObject(uid)

  #得到对象长整数标识
  uid = intids.getId(obj)

有了这个长整数标识，可在表单中记录这个标识来传递对象。

同时，我们也可以快速定位到这个对象::

   http://example.com/++intid++12312312

接口：对象的类型
====================

不同的对象，通过接口来标识其类型，比如文件、帖子、文件夹、批注等。

系统包括如下接口::

  接口	        说明	         完整标识
  IFile	        文件	         zopen.content.interfaces.IFile
  IFolder	文件夹	         zopen.content.interfaces.IFolder
  IApplet	应用	         zopen.apps.interfaces.IApplet
  IDataManager	数据/流程管理器	 zopen.flow.interfaces.IDataManager

判断一个对象是不是文件，可使用如下语句::

  IFile.providedBy(context)

版本管理
==================

版本管理是系统的一个基础功能，不仅文档可以保存多个版本，表单也支持多版本。

“IRevisionManager”: 版本管理

- save(comment='', metadata={}): 存为一个新版本
- retrieve(selector=None, preserve=()): 获得某一个版本
- getHistory(preserve=()): 得到版本历史清单信息
- remove(selector, comment="", metadata={}, countPurged=True): 删除某个版本 
- getWorkingVersionData(): 得到当前工作版本的版本信息，取出来后，在外部维护数据内容


回收站
============

回收站是系统的一个基础功能，以下对象删除，都将进入回收站：

- 文件
- 文件夹
- 流程单
- 流程
- 容器

一旦进入回收站，系统会定期对回收站的内容进行清理。删除历史已久的回收站内容。

查看回收站的内容
--------------------

TODO


从回收站收回一个对象
-----------------------------
TODO

从回收站里面永久删除
-----------------------
TODO

