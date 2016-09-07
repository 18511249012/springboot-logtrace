# springboot-logtrace
&#8195;基于spring boot构建的微服务，服务之间调用日志跟踪，基于spring boot的配置日志输出模式，spring-cloud-starter-feign'的扩展日志输出，请求事件的解析和服务调用轨迹追踪

##搭建[spring boot](http://projects.spring.io/spring-boot/)工程##
&#8195;使用spring boot构建一个微服务

##spring cloud 依赖添加##
&#8195;参照[spring cloud](http://projects.spring.io/spring-cloud/)添加服务调用feign模块，如果想采用okHttp调用，则可选引入okHttp。
<pre><code>&lt;dependency&gt;
	&lt;groupId>org.springframework.cloud&lt;/groupId&gt;
	&lt;artifactId>spring-cloud-starter-feign&lt;/artifactId&gt;
&lt;/dependency&gt;

&lt;!--可选依赖--&gt;
&lt;dependency&gt;
	&lt;groupId&gt;com.netflix.feign&lt;/groupId&gt;
	&lt;artifactId&gt;feign-okhttp&lt;/artifactId&gt;
&lt;/dependency&gt;
</code></pre>
##配置##
&#8195;在spring boot规定的配置文件中添加配置，配置中可以指定日志格式、日志输出方式（支持kafka和file）、日志输出路径、是否使用okHttp，如果不添加配置，则日志默认输出到文件/var/log/hzcard目录下。以下以application.properties为配置文件样例:
'''properties
feign.httpclient.enabled=false&nbsp;&nbsp;	#使用okHttp调用服务*
logtrace.appenderType=KAFKA &nbsp;&nbsp;           *#使用KAFKA/FILE输出*
logtrace.kafkaTopic=eventpropertiestopic&nbsp;&nbsp; *#kafka输出使用的topic*
logtrace.kafkaProperty.bootstrapServers.name = bootstrap.servers&nbsp;&nbsp;*#kafka的属性配置属性名*
logtrace.kafkaProperty.bootstrapServers.value=${kafka.brokers:191.162.102.208:9092}&nbsp;&nbsp;*#kafka的属性配置属性值，跟上一条bootstrapServers.name配置组成 name=value的属性，跟上一条bootstrapServers可以随便定义，只要最终name=value给kafka配置就可以*
logtrace.kafkaProperty.maxrequestsize.name=max.request.size &nbsp;&nbsp; *#同上解释*
logtrace.kafkaProperty.maxrequestsize.value=2097152&nbsp;&nbsp;      *#同上解释*
...      &nbsp;&nbsp;*#kafka其他属性值配置*
logtrace.patter = %d %-5p [%t] %C{2} (%F:%L) - %m%n&nbsp;&nbsp;     *#使用的日志输出模式*
'''
##事件解析添加##
&#8195;编写一个类实现<code>com.hzcard.logtrace.event.EventTypeResolver</code>类，实现eventGen方法，自己定义把请求解析成事件。
根据spring boot的规范，在启动类中注入一个bean
<code>@Bean
	public EventTypeResolver defaultEventTypeResolver(){
		return new EventResolverDefault();    &nbsp;&nbsp;&nbsp;&nbsp;*//自定义创建的类*
	}</code>
&#8195;应用启动后，就会使用注入的bean对request做解析
##默认设备类型解析和扩展##
&#8195;logtrace默认会解析请求的设备，包括：iphone、ipad、mac、windows、linux、android，详细见<code>com.hzcard.logtrace.spring.boot.handle.interceptor.EquipmentTypeEnum</code>

&#8195;如果默认的请求无法辨认客户端设备类型，可以进行扩展。
&#8195;创建一个继承<code>com.hzcard.logtrace.spring.boot.handle.interceptor.EventHandlerInterceptor</code>的类，实现<code>EquipmentTypeEnum resolve(HttpServletRequest request)</code>方法。根据spring boot的mvc配置扩展规范，在自己工程目录下创建一个继承了<code>org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter</code>的类，覆写方法
'''java
    @Override
	public void addInterceptors(InterceptorRegistry registry) {
    &nbsp;&nbsp;&nbsp;&nbsp;registry.addInterceptor(new IPhoneEquipMentInterceptor(this.context));       *//继承了EventHandlerInterceptor的类*
}
'''
##获得客户访问设备类型##
&#8195;默认提供了工具类<code>com.hzcard.logtrace.spring.util.ClientTypeTools</code>，调用其方法<code>getClientType()</code>，获得设备默认对应的类型。
##日志输出与服务调用轨迹##
&#8195;logtrace会将所有服务调用的http header和body都输出。logtrace增加了几个定制头：X-Event-Platform（事件发生的平台）、X-Event-Type（事件类型）、X-Event-Id（事件id）、X-Event-Code（事件编码）、X-Event-Sequence（事件发生的顺序）
&#8195;从客户端过来一次请求，可能会调用到多个服务，但X-Event-Id只有一个，X-Event-Sequence会根据请求的顺序，依次增加。如client调用A服务，A再调用B，A再调用c。事件顺序就是：1-0，1-0-1，1-1
