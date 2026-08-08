package com.cyh.mallportal.es.repository;

import com.cyh.mallportal.es.entity.SpuIndex;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.List;

/**
 * ES 商品索引数据层
 * <p>
 * 封装 ES 索引的增删改查、批量操作、索引创建等底层操作。
 */
@Slf4j
@Repository
public class SpuIndexRepository {

    @Autowired
    private RestHighLevelClient client;

    @Value("${elasticsearch.index:mall_spu}")
    private String indexName;

    /**
     * 判断索引是否存在
     */
    public boolean existsIndex() throws IOException {
        return client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    /**
     * 创建索引（含 Mapping 和 Settings）
     */
    public boolean createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(SpuIndex.buildSettings());
        request.mapping(SpuIndex.buildMapping());
        return client.indices().create(request, RequestOptions.DEFAULT).isAcknowledged();
    }

    /**
     * 新增 / 覆盖文档
     */
    public void save(SpuIndex spuIndex) throws IOException {
        IndexRequest request = new IndexRequest(indexName)
                .id(spuIndex.getId().toString())
                .source(SpuIndexConverter.toMap(spuIndex), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
        log.debug("ES 索引保存文档: id={}", spuIndex.getId());
    }

    /**
     * 更新文档（部分字段）
     */
    public void update(SpuIndex spuIndex) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, spuIndex.getId().toString())
                .doc(SpuIndexConverter.toMap(spuIndex), XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
        client.update(request, RequestOptions.DEFAULT);
        log.debug("ES 索引更新文档: id={}", spuIndex.getId());
    }

    /**
     * 删除文档
     */
    public void delete(Long id) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, id.toString());
        client.delete(request, RequestOptions.DEFAULT);
        log.debug("ES 索引删除文档: id={}", id);
    }

    /**
     * 批量新增文档（用于全量同步）
     */
    public void bulkSave(List<SpuIndex> list) throws IOException {
        if (list == null || list.isEmpty()) {
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (SpuIndex spuIndex : list) {
            IndexRequest request = new IndexRequest(indexName)
                    .id(spuIndex.getId().toString())
                    .source(SpuIndexConverter.toMap(spuIndex), XContentType.JSON);
            bulkRequest.add(request);
        }
        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (response.hasFailures()) {
            log.error("ES 批量索引失败: {}", response.buildFailureMessage());
        } else {
            log.info("ES 批量索引成功: {} 条", list.size());
        }
    }

    /**
     * 搜索（使用 SearchSourceBuilder 自定义查询）
     */
    public SearchResponse search(SearchSourceBuilder sourceBuilder) throws IOException {
        SearchRequest request = new SearchRequest(indexName);
        request.source(sourceBuilder);
        return client.search(request, RequestOptions.DEFAULT);
    }

    /**
     * 搜索建议（Completion Suggester）
     */
    public SearchResponse suggest(String prefix, int size) throws IOException {
        SearchRequest request = new SearchRequest(indexName);
        // 添加 Completion 补全建议（输入前缀联想）
        SuggestionBuilder<?> suggestionBuilder = SuggestBuilders.completionSuggestion("suggest")
                .prefix(prefix)
                .size(size)
                .skipDuplicates(true);

        // cyhcanadd 还可以使用 Term ,进行添加 Term 纠错建议（单词拼写错误修正）
        request.source().suggest(new SuggestBuilder().addSuggestion("spu_suggest", suggestionBuilder));
        return client.search(request, RequestOptions.DEFAULT);
    }

    /**
     * 清空索引全部数据（通过 delete_by_query 匹配所有文档）
     */
    public void deleteAll() throws IOException {
        SearchRequest request = new SearchRequest(indexName);
        request.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));
        // 使用 delete_by_query 方式清空
        org.elasticsearch.index.reindex.DeleteByQueryRequest deleteRequest =
                new org.elasticsearch.index.reindex.DeleteByQueryRequest(indexName);
        deleteRequest.setQuery(QueryBuilders.matchAllQuery());
        client.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
        log.info("ES 索引已清空: {}", indexName);
    }
}