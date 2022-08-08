package com.github.royalflushdtd.pgsynchijacker.publisher;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import com.github.royalflushdtd.pgsynchijacker.model.Event;
import com.github.royalflushdtd.pgsynchijacker.model.InvokeContext;

/**
 * @author zlj
 * @version 1.0
 * @since  2022/07
 */
public interface IPublisher extends AutoCloseable {

    /**
     * 消费消息
     *
     * @param event    事件
     * @param callback 回调
     */
    void publish(Event event, Callback callback);


    /**
     * 消费消息
     *
     * @param context  上下文
     * @param callback 回调
     */
    default void publish(InvokeContext context, Callback callback) {
        context.getEvent().setLsn(context.getLsn());
        context.getEvent().setServerId(context.getServerId());
        context.getEvent().setSlotName(context.getSlotName());
        this.publish(context.getEvent(), callback);
    }

    /**
     * 关闭发布器
     */
    @Override
    default void close() {
    }

    /**
     * 批量发布
     *
     * @param contexts 上下文
     */
    default void publish(List<InvokeContext> contexts) {
        if (CollectionUtils.isEmpty(contexts)) {
            return;
        }
        contexts.forEach(ctx -> publish(ctx, null));
    }

    interface Callback {

        void onSuccess();

        void onFailure(Throwable t);

    }
}
