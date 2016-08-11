spring quartz集群配置

************************************************
1、创建数据库表 
************************************************
见job.sql


************************************************
2、 配置数据库连接池，如果spring已经配置则不需要再另行配置，只需在后面配置的applicationContext-quartz.xml引入即可。 
************************************************

************************************************
3、 配置quartz.properties 
************************************************

#属性可为任何值，用在 JDBC JobStore 中来唯一标识实例，但是所有集群节点中必须相同。 
org.quartz.scheduler.instanceName  

#属性为 AUTO即可，基于主机名和时间戳来产生实例 ID。   
org.quartz.scheduler.instanceId　

#属性为 JobStoreTX，将任务持久化到数据中。因为集群中节点依赖于数据库来传播 Scheduler 实例的状态，你只能在使用 JDBC JobStore 时应用 Quartz 集群。
#这意味着你必须使用 JobStoreTX 或是 JobStoreCMT 作为 Job 存储；你不能在集群中使用 RAMJobStore。   
org.quartz.jobStore.class

#属性为 true，你就告诉了 Scheduler 实例要它参与到一个集群当中。这一属性会贯穿于调度框架的始终，用于修改集群环境中操作的默认行为。   
org.quartz.jobStore.isClustered 

#属性定义了Scheduler 实例检入到数据库中的频率(单位：毫秒)。Scheduler 检查是否其他的实例到了它们应当检入的时候未检入；
#这能指出一个失败的 Scheduler 实例，且当前 Scheduler 会以此来接管任何执行失败并可恢复的 Job。通过检入操作，Scheduler 也会更新自身的状态记录。
#clusterChedkinInterval 越小，Scheduler 节点检查失败的 Scheduler 实例就越频繁。默认值是 15000 (即15 秒)。  
org.quartz.jobStore.clusterCheckinInterval 


************************************************
4、 配置applicationContext-quartz.xml 
************************************************

************************************************
5、 配置Job任务注意
************************************************
用org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean指定类和方法，
但是直接使用会报java.io.NotSerializableException异常，一般用网上流传的
（需要将两个类copy到自己的工程下，要有springJAR包，Job需要持久化到数据库中，SimpleService必须实现Serializable）
frameworkx.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean，
可以参考: http://jira.springframework.org/browse/SPR-3797。

************************************************
6、启动测试 
************************************************
启动服务后，spring quartz会将trigger持久化到数据库中 

###############################################################
测试
##############################################################
非集群:
首先，测试没有集群的情况，把集群中每个note的quartz.properties文件的isClustered属性设置为false.
先启动AppNote1,启动信息如下,启动成功,Scheduler scheduler_$_NON_CLUSTERED started表面应用没有加入集群,系统每隔10秒调用一次远程service,打印相关信息。
2010-04-05 21:37:00 当前 APP NOTE 1 正在执行
2010-04-05 21:37:10 当前 APP NOTE 1 正在执行
2010-04-05 21:37:20 当前 APP NOTE 1 正在执行    
接着启动AppNote2,启动信息和之前一样,表明AppNote2也是没有加入集群，不过AppNote2并不会执行，因为quartz只要用集群的方式配置，不管Note是否加入集群，同一个job在同一时间只能有由一个Note执行.
接下来我们把AppNote1停掉，因为没有加入集群，所以AppNote2应该不会有任何反应，不会接过AppNote1正在执行的任务继续执行。结果也如我们想的一样，AppNote2没有任何反应。
我们看qrtz_scheduler_state表，没有任何集群实例信息被保存。
   

集群:
下面我们来测试加入集群的情况，把集群中每个note的quartz.properties文件的isClustered属性改为为true，然后启动AppNote1和AppNote2,启动信息中产生了一个集群实例kenny1270476046453
2010-04-05 22:00:50 当前 APP NOTE 1 正在执行
2010-04-05 22:01:00 当前 APP NOTE 1 正在执行
2010-04-05 22:01:10 当前 APP NOTE 1 正在执行
再来看qrtz_scheduler_state表，两个集群实例已经被保存下来，并且时刻每个7500毫秒检测一次。   
这次我们再次停掉AppNote1，再次看AppNote2，奇迹发生了，等待几秒AppNote2就会接着AppNote1的任务执行，至此，集群配置成功。


###############################################################
quartz集群原理
##############################################################
1.大家都清楚quartz最基本的概念就是job，在job内调用具体service完成具体功能，quartz需要把每个job存储起来，方便调 度，
quartz存储job方式就分三种，我们最常用的也是quartz默认的是RAMJobStore，RAMJobStore顾名思义就是把job的 相关信息存储在内存里，
如果用spring配置quartz的job信息的话，所有信息是配置在xml里，当spirng context启动的时候就把xml里的job信息装入内存。
这一性质就决定了一旦JVM挂掉或者容器挂掉，内存中的job信息就随之消失，无法持久化。
 另外两种方式是JobStoreTX和JobStoreCMT，暂时不讨论这两者的区别，使用这两种JobStore，quartz就会通过jdbc直连 或者应用服务器jndi连接数据库，
 读取配置在数据库里的job初始化信息，并且把job通过java序列化到数据库里，这样就使得每个job信息得到了 持久化，即使在jvm或者容器挂掉的情况下，
 也能通过数据库感知到其他job的状态和信息。 
    
2.quartz集群各节点之间是通过同一个数据库实例(准确的说是同一个数据库实例的同一套表)来感知彼此的。 





