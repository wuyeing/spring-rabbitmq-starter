package yh.rabbit.public_rabbitmq_starter.message;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author dengsd
 * @Date 2021/11/6 15:18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// RabbitData作为基础的模板传输数据格式，使用时直接套用即可
public class RabbitData<T> implements Serializable {
    // uuid用于配置CorrelationData，建议使用时自动生成
    @Nonnull
    private String uuid;
    // message是自定义的数据类型，装入自己实际的数据即可，注意对应的类型需要序列化
    // 根据消费者的判断流程，建议自定义message专用类型封装实际数据，不要使用通用类型
    private T message;
}
