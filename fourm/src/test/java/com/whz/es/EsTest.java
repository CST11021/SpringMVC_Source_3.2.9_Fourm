// package com.whz.es;
//
// import java.io.IOException;
// import java.net.InetAddress;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.LinkedHashMap;
// import java.util.Map;
//
// import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
// import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
// import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
// import org.elasticsearch.action.bulk.BulkResponse;
// import org.elasticsearch.action.delete.DeleteResponse;
// import org.elasticsearch.action.get.GetResponse;
// import org.elasticsearch.action.index.IndexRequest;
// import org.elasticsearch.action.index.IndexResponse;
// import org.elasticsearch.action.search.SearchResponse;
// import org.elasticsearch.client.Client;
// import org.elasticsearch.client.transport.TransportClient;
// import org.elasticsearch.common.settings.Settings;
// import org.elasticsearch.common.transport.TransportAddress;
// import org.elasticsearch.common.xcontent.XContentFactory;
// import org.elasticsearch.common.xcontent.XContentType;
// import org.elasticsearch.index.query.QueryBuilders;
// import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
// import org.elasticsearch.transport.client.PreBuiltTransportClient;
// import org.junit.After;
// import org.junit.Before;
// import org.junit.Test;
//
// /**
//  * ElasticSearch客户端连接服务器测试
//  *
//  * @author Administrator
//  */
// public class EsTest {
//
//     private static String HOST = "localhost"; // 服务器地址
//
//     private static int PORT = 9300; // 端口
//
//     private TransportClient client = null;
//
//     /**
//      * 获取连接
//      *
//      * @return
//      */
//     @SuppressWarnings({"unchecked", "resource"})
//     @Before
//     public void getCient() throws Exception {
//         Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
//         client = new PreBuiltTransportClient(settings)
//                 .addTransportAddresses(new TransportAddress(InetAddress.getByName(HOST), PORT));
//     }
//
//     /**
//      * 关闭连接
//      *
//      * @param
//      */
//     @After
//     public void close() {
//         if (client != null) {
//             client.close();
//         }
//     }
//
//     /**
//      * 添加索引
//      */
//     @Test
//     public void testIndex1() throws Exception {
//         IndexResponse response = client.prepareIndex("twitter", "tweet", "1")
//                 .setSource(XContentFactory.jsonBuilder()
//                         .startObject()
//                         .field("user", "kimchy")
//                         .field("postDate", new Date())
//                         .field("message", "trying out Elasticsearch")
//                         .endObject()
//                 )
//                 .get();
//         System.out.println("索引名称：" + response.getIndex());
//         System.out.println("类型：" + response.getType());
//         System.out.println("文档ID：" + response.getId()); // 第一次使用是1
//         System.out.println("当前实例状态：" + response.status());
//     }
//
//     /**
//      * 添加索引
//      */
//     @Test
//     public void testIndex2() throws Exception {
//         String json = "{" +
//                 "\"user\":\"kimchy\"," +
//                 "\"postDate\":\"2013-01-30\"," +
//                 "\"message\":\"trying out Elasticsearch\"" +
//                 "}";
//
//         IndexResponse response = client.prepareIndex("weibo", "tweet")
//                 .setSource(json, XContentType.JSON)
//                 .get();
//         System.out.println("索引名称：" + response.getIndex());
//         System.out.println("类型：" + response.getType());
//         System.out.println("文档ID：" + response.getId()); // 第一次使用是1
//         System.out.println("当前实例状态：" + response.status());
//     }
//
//     /**
//      * 添加索引
//      */
//     @Test
//     public void testIndex3() throws Exception {
//         Map<String, Object> json = new HashMap<String, Object>();
//         json.put("user", "kimchy");
//         json.put("postDate", new Date());
//         json.put("message", "trying out Elasticsearch");
//
//         IndexResponse response = client.prepareIndex("qq", "tweet")
//                 .setSource(json)
//                 .get();
//         System.out.println("索引名称：" + response.getIndex());
//         System.out.println("类型：" + response.getType());
//         System.out.println("文档ID：" + response.getId()); // 第一次使用是1
//         System.out.println("当前实例状态：" + response.status());
//     }
//
//
//
//     public static void main(String[] args) {
//
//     }
//
//     /**
//      * 创建索引，有则先删除
//      *
//      * @param client
//      */
//     private static void recreateIndex(Client client) {
//         if (client.admin().indices().prepareExists(index).execute().actionGet()
//                 .isExists()) {
//             DeleteIndexResponse deleteIndexResponse = client.admin().indices()
//                     .delete(new DeleteIndexRequest(index)).actionGet();
//             System.out.println("delete index :");
//             System.out.println(deleteIndexResponse);
//         }
//
//         CreateIndexResponse createIndexResponse = client.admin().indices()
//                 .prepareCreate(index).execute().actionGet();
//         System.out.println("create index :");
//         System.out.println(createIndexResponse);
//     }
//
//     /**
//      * 插入数据
//      *
//      * @param client
//      */
//     @SuppressWarnings({"rawtypes", "unchecked"})
//     private static void doIndex(final Client client) {
//         Map s11 = new LinkedHashMap();
//         s11.put("title", "Think in java");
//         s11.put("origin", "美国");
//         s11.put("description", "初级java开发人员必读的书");
//         s11.put("author", "Bruce Eckel");
//         s11.put("price", 108);
//
//         Map s12 = new LinkedHashMap();
//         s12.put("title", "Head First Java");
//         s12.put("origin", "英国");
//         s12.put("description", "java入门教材");
//         s12.put("author", "Kathy Sierra");
//         s12.put("price", 54);
//
//         Map s21 = new LinkedHashMap();
//         s21.put("title", "Design Pattern");
//         s21.put("origin", "法国");
//         s21.put("description", "程序员不得不读的设计模式");
//         s21.put("author", "Kathy Sierra");
//         s21.put("price", 89);
//
//         Map s22 = new LinkedHashMap();
//         s22.put("title", "黑客与画家");
//         s22.put("origin", "法国");
//         s22.put("description", "读完之后脑洞大开");
//         s22.put("author", "Paul Graham");
//         s22.put("price", 35);
//
//         BulkResponse bulkResponse = client
//                 .prepareBulk()
//                 .add(client.prepareIndex(index, type).setId("11").setSource(s11).setOpType(IndexRequest.OpType.INDEX).request())
//                 .add(client.prepareIndex(index, type).setId("12").setSource(s12).setOpType(IndexRequest.OpType.INDEX).request())
//                 .add(client.prepareIndex(index, type).setId("21").setSource(s21).setOpType(IndexRequest.OpType.INDEX).request())
//                 .add(client.prepareIndex(index, type).setId("22").setSource(s22).setOpType(IndexRequest.OpType.INDEX).request())
//                 .execute().actionGet();
//         if (bulkResponse.hasFailures()) {
//             System.err.println("index docs ERROR:" + bulkResponse.buildFailureMessage());
//         } else {
//             System.out.println("index docs SUCCESS:");
//             System.out.println(bulkResponse);
//         }
//     }
//
//     /**
//      * 关键词查询
//      *
//      * @param client
//      */
//     private static void searchKeyWord(Client client) {
//         SearchResponse response = client.prepareSearch(index)
//                 //查询所有字段匹配关键字
//                 .setQuery(QueryBuilders.matchQuery("_all", "法国"))
//                 //设置最小匹配程度
// //        .setQuery(QueryBuilders.matchQuery("_all", "法国").minimumShouldMatch("100%"))
//                 .execute().actionGet();
//         System.out.println("searchKeyWord : ");
//         System.out.println(response);
//     }
//
//     /**
//      * 高亮关键字
//      *
//      * @param client
//      */
//     private static void searchHightlight(Client client) {
//         //高亮多个字段
//         HighlightBuilder highlightBuilder = new HighlightBuilder();
//         highlightBuilder.field("title");
//         highlightBuilder.field("description");
//         SearchResponse response = client.prepareSearch(index)
//                 //单条件匹配，高亮时只能高亮该字段
// //        .setQuery(QueryBuilders.matchQuery("title", "java"))
//                 //多条件匹配，高亮时只能高亮多个字段
//                 .setQuery(QueryBuilders.multiMatchQuery("开发人员必读", "title", "description"))
//                 .highlighter(highlightBuilder)
//                 .execute()
//                 .actionGet();
//         System.out.println("searchHightlight : ");
//         System.out.println(response);
//     }
//
//     /**
//      * 根据id查找
//      *
//      * @param client
//      */
//     private static void findById(final Client client) {
//         String id = "12";
//         GetResponse response = client.prepareGet(index, type, id).get();
//         System.out.println("findById");
//         System.out.println(response);
//     }
//
//     /**
//      * 删除
//      *
//      * @param client
//      */
//     private static void deleteById(Client client) {
//         String id = "12";
//         DeleteResponse response = client.prepareDelete(index, type, id).get();
//     }
//
//
//     /**
//      * 更新
//      *
//      * @param client
//      */
//     private static void updateById(Client client) {
//         try {
//             String id = "11";
//             client.prepareUpdate(index, type, id)
//                     .setDoc(jsonBuilder()
//                             .startObject()
//                             .field("title", "白鹿原")
//                             .endObject())
//                     .get();
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }
//
// }