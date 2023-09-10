package yh.rabbit.public_rabbitmq_starter.message;

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
// RabbitDataMsg作为一个RabbitData成员变量message的demo
public class RabbitDataMsg implements Serializable {
   private String data;
}
