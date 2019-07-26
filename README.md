基于Mysql和Elasticsearch的数据同步
---------------------

## 原理

该同步是通过定时任务进行，通过读取记录的最后更新时间来实现全量同步和增量同步。
同时监听Entity的事件，将Entity实时同步到Elasticsearch。同步之后可以通过Es的Repository来做高性能的查询。

## 使用
需要给同步的Entity加@Table的name属性，同时需要@Entity具有updatedTime属性。

### 定义实体类
这里Es和JPA共享了相同的实体类，例如：
```java
@Entity
@Table(name = "tbl_device")
@Data
@Document(indexName = "tbl_device",type = "_doc",createIndex = false)
@EntityListeners({AuditingEntityListener.class,SyncEntityListener.class})
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @LastModifiedDate
    private Date updatedTime;
    @Text
    private String deviceName;
    private String deviceCode;
    private String gbId;
    private String deviceType;
    private BigDecimal lat;
    private BigDecimal lng;
}

```

### 定义同步
需要将JPA的Repository继承自SyncableRepository，注意请将jpa和es的包扫描分开，否则会报错
```java
public interface DeviceRepository extends JpaRepository<Device,Long>, SyncableRepository<Device> {
}
```

### 添加同步配置

```http request
POST /sync/add
{
    "name":"com.eappcat.sync.es.entity.Device"
}
```

### 查询全部同步

```http request
GET /sync/list

```

### 刷新字段映射

```http request
GET /sync/refresh

```

### 使用ES的DAO查询
一旦同步完成之后就可以用Elasticsearch的查询来查内容了，包括分词等都可以实现

Repository
```java
public interface EsDeviceRepository extends ElasticsearchCrudRepository<Device,String> {
}
```
控制器
```java
@RestController
@RequestMapping("device")
public class DeviceController {
    @Autowired
    private EsDeviceRepository repository;
    @GetMapping
    public List<Device> list(){
        return repository.findAll(PageRequest.of(0,1000)).getContent();
    }

}
```

* 注意请不要使用Es的Repository来做修改，保证只读，数据需要用JPA来修改。