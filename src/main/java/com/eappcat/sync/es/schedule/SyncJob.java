package com.eappcat.sync.es.schedule;

import com.alibaba.fastjson.JSONObject;
import com.eappcat.sync.es.SyncConfigProperties;
import com.eappcat.sync.es.core.EntityChangeEvent;
import com.eappcat.sync.es.core.RefreshMappingEvent;
import com.eappcat.sync.es.core.SyncableRepository;
import com.eappcat.sync.es.core.Text;
import com.eappcat.sync.es.dao.DatabaseSyncRepository;
import com.eappcat.sync.es.entity.DatabaseSyncEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@Slf4j
public class SyncJob implements ApplicationRunner {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired(required = false)
    private List<JpaRepository> repositories;
    @Autowired
    private DatabaseSyncRepository databaseSyncDao;

    @Autowired
    private SyncConfigProperties syncConfigProperties;
    @Autowired
    private ApplicationContext applicationContext;

    private volatile boolean initialized=false;

    @Scheduled(fixedDelayString = "${sync.syncDelay}")
    @Transactional
    public void sync() throws Exception{
        if(initialized){
            List<DatabaseSyncEntity> data=databaseSyncDao.findAll();
            data.stream().forEach(this::processSync);
        }
    }

    private SyncableRepository findRepository(Class c){
        if(repositories!=null){
            for (JpaRepository repository:this.repositories){
                Class [] interfaces=repository.getClass().getInterfaces();
                for (Class clazz:interfaces){
                    Type[] test=clazz.getGenericInterfaces();
                    for (Type t :test){
                        if(t instanceof ParameterizedType){
                            ParameterizedType parameterizedType=(ParameterizedType)t;
                            if(parameterizedType.getRawType().equals(SyncableRepository.class)){
                                Type actualTypeArgument=parameterizedType.getActualTypeArguments()[0];
                                if(c.equals(actualTypeArgument)){
                                    return (SyncableRepository)repository;
                                }
                            }
                        }
                    }

                }
            }

        }
        return null;
    }

    private void processSync(DatabaseSyncEntity databaseSyncEntity) {
        long start=System.currentTimeMillis();
        Date startDate=new Date();
        Date lastSync=databaseSyncEntity.getSyncDate();
        if (lastSync==null){
            lastSync=new Date(0);
        }
        try {
            String clazz=databaseSyncEntity.getName();
            Class c=Class.forName(clazz);
            Table table= AnnotationUtils.findAnnotation(c,Table.class);

            List<UpdateQuery> updateQueries=new ArrayList<>();
            SyncableRepository repository=findRepository(c);
            if (repository!=null){
                Stream stream=repository.findByUpdatedTimeAfterAndUpdatedTimeBefore(lastSync,startDate);
                SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                log.info("sync {}, from {} to {}",table.name(), formatter.format(lastSync),formatter.format(startDate));
                AtomicLong count=new AtomicLong(0);
                stream.forEach(entity-> {
                    JSONObject jsonObject = prepareForEntityData(entity);
                    UpdateQuery updateQuery = createUpdateRequest(c, jsonObject);
                    if(updateQueries.size()>syncConfigProperties.getBatchSize()){
                        elasticsearchTemplate.bulkUpdate(updateQueries);
                        updateQueries.clear();
                    }else {
                        updateQueries.add(updateQuery);
                    }
                    count.incrementAndGet();
                    log.debug("=====> {}",jsonObject.toJSONString());
                });

                if(updateQueries.size()>0){
                    elasticsearchTemplate.bulkUpdate(updateQueries);
                    updateQueries.clear();
                }
                databaseSyncEntity.setSyncDate(startDate);
                databaseSyncEntity.setUpdatedTime(new Date());
                databaseSyncDao.save(databaseSyncEntity);
                log.info("sync {} in {}ms, count: {}",table.name(),System.currentTimeMillis()-start,count.get());
            }

        }catch (Exception e){
            log.error("{}",e);
        }

    }

    private UpdateQuery createUpdateRequest(Class c, JSONObject jsonObject) {
        Table table= AnnotationUtils.findAnnotation(c,Table.class);
        UpdateRequest request=new UpdateRequest();
        IndexRequest indexRequest=new IndexRequest();
        indexRequest.source(jsonObject.toJSONString(), XContentType.JSON);
        indexRequest.type("_doc");
        indexRequest.index(table.name());
        request.upsert(jsonObject.toJSONString(),XContentType.JSON);
        UpdateQueryBuilder builder=new UpdateQueryBuilder();
        builder.withDoUpsert(true).withId(jsonObject.getString("id"))
                .withType("_doc").withIndexName(table.name()).withUpdateRequest(request).withIndexRequest(indexRequest);
        return builder.build();
    }

    private JSONObject prepareForEntityData(Object entity) {
        JSONObject jsonObject=new JSONObject();
        List<Field> fieldList=new ArrayList<>();
        findFieldInClass(entity.getClass(),fieldList);
        for (Field field:fieldList){
            field.setAccessible(true);
            Object value= null;
            try {
                value = field.get(entity);
//                Column column= AnnotationUtils.findAnnotation(field,Column.class);
//                if(column==null){
//                    String name= CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(field.getName());
//                    jsonObject.put(name,value);
//                }else {
//                    jsonObject.put(column.name(),value);
//                }
                jsonObject.put(field.getName(),value);
            } catch (IllegalAccessException e) {
                log.error("{}",e.getMessage());
            }

        }
        return jsonObject;
    }

    private void createMappingForEntity(DatabaseSyncEntity entity) throws ClassNotFoundException, InterruptedException, java.util.concurrent.ExecutionException {
        String clazz=entity.getName();
        Class c=Class.forName(clazz);
        Table table= AnnotationUtils.findAnnotation(c,Table.class);
        JSONObject jsonObject = prepareForEntityMeta(c);
        JSONObject test=new JSONObject();
        test.put("properties",jsonObject);

        IndicesExistsResponse indicesExistsResponse=elasticsearchTemplate.getClient().admin().indices().prepareExists(table.name()).execute().get();
        if (!indicesExistsResponse.isExists()){
            CreateIndexResponse createIndexResponse=elasticsearchTemplate.getClient().admin().indices().prepareCreate(table.name()).execute().get();
            log.info("index created {}",createIndexResponse.isAcknowledged());
        }
        PutMappingResponse response=elasticsearchTemplate.getClient().admin().indices().preparePutMapping(table.name()).setType("_doc").setSource(test.toString(), XContentType.JSON).execute().get();
        log.info("index mapping created {}",response.isAcknowledged());
    }

    private JSONObject prepareForEntityMeta(Class c) {
        JSONObject jsonObject=new JSONObject();
        List<Field> fieldList=new ArrayList<>();
        findFieldInClass(c,fieldList);
        for (Field field:fieldList){
            field.setAccessible(true);
            Class type=field.getType();
            JSONObject object=new JSONObject();
//            Column column= AnnotationUtils.findAnnotation(field,Column.class);
            if (String.class.isAssignableFrom(type)){
                Text text=AnnotationUtils.findAnnotation(field,Text.class);
                if (text == null){
                    object.put("type","keyword");
                }else {
                    object.put("type","text");
                }

            }else if (Date.class.isAssignableFrom(type)){
                object.put("type","date");
            }else if (Double.class.isAssignableFrom(type)||Float.class.isAssignableFrom(type)|| BigDecimal.class.isAssignableFrom(type)){
                object.put("type","double");
            }else if (BigInteger.class.isAssignableFrom(type)||Long.class.isAssignableFrom(type)||Integer.class.isAssignableFrom(type)||Short.class.isAssignableFrom(type)){
                object.put("type","long");
            }else if (Boolean.class.isAssignableFrom(type)){
                object.put("type","boolean");
            }else {
                object.put("type","keyword");
            }
//            if(column!=null){
//                jsonObject.put(column.name(),object);
//            }else {
//                String name= CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(field.getName());
//                jsonObject.put(name,object);
//            }
            jsonObject.put(field.getName(),object);

        }
        return jsonObject;
    }

    private void findFieldInClass(Class c, List<Field> fieldList) {
        for (Field field:c.getDeclaredFields()){
            fieldList.add(field);
        }
        if(c.getSuperclass()!=null&&!c.getSuperclass().equals(Object.class)){
            findFieldInClass(c.getSuperclass(),fieldList);
        }
    }

    @Override
    public void run(ApplicationArguments args){
        applicationContext.publishEvent(new RefreshMappingEvent());
    }
    @EventListener
    public void refreshMapping(RefreshMappingEvent event){
        databaseSyncDao.findAll().stream().forEach(databaseSyncEntity->{
            try {
                createMappingForEntity(databaseSyncEntity);
            } catch (Exception e) {
                log.error("error to refresh mapping, {}",e);
            }

        });
        this.initialized=true;
    }
    @EventListener
    public void listen(EntityChangeEvent entityChangeEvent){
        //判断事务状态，在提交后更新
        if(TransactionSynchronizationManager.isSynchronizationActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
                public void afterCommit(){
                    onEntityChanged(entityChangeEvent);
                }
            });
        }else {
            onEntityChanged(entityChangeEvent);
        }
    }

    private void onEntityChanged(EntityChangeEvent entityChangeEvent) {
        log.info("{}", JSONObject.toJSONString(entityChangeEvent));
        switch (entityChangeEvent.getType()){
            case DELETE:
                Table table= AnnotationUtils.findAnnotation(entityChangeEvent.getClass(),Table.class);
                JSONObject jsonObject=JSONObject.parseObject(JSONObject.toJSONString(entityChangeEvent));
                DeleteQuery deleteQuery=new DeleteQuery();
                deleteQuery.setIndex(table.name());
                deleteQuery.setType("_doc");
                deleteQuery.setQuery(QueryBuilders.idsQuery(jsonObject.getString("id")));
                elasticsearchTemplate.delete(deleteQuery);
                break;
            default:
                JSONObject updateData = prepareForEntityData(entityChangeEvent.getEntity());
                UpdateQuery updateQuery=createUpdateRequest(entityChangeEvent.getEntity().getClass(),updateData);
                elasticsearchTemplate.update(updateQuery);

        }
    }
}
