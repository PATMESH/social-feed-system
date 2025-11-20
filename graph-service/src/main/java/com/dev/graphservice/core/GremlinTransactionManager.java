package com.dev.gremlin_ogm.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GremlinTransactionManager {

    private final GraphTraversalSource g;

    public <T> T execute(TransactionCallback<T> callback) {
        try {
            return callback.doInTransaction(g);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeInNewTransaction(TransactionCallback<T> callback) {
        GraphTraversalSource gtx = g.tx().begin();

        try {
            T result = callback.doInTransaction(gtx);
            gtx.tx().commit();
            return result;
        } catch (Exception e) {
            try {
                if (gtx.tx().isOpen()) {
                    gtx.tx().rollback();
                }
            } catch (Exception rollbackEx) {
                log.error("Rollback failed", rollbackEx);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                if (gtx.tx().isOpen()) {
                    gtx.tx().close();
                }
            } catch (Exception closeEx) {
                log.error("Transaction close failed", closeEx);
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(GraphTraversalSource g);
    }
}