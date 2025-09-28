package com.cherry.velocityrpc.serializer;

import java.io.IOException;

/**
 * 序列化器接口
 *
 * java对象是存活在JVM虚拟机中的，如果想在其他位置存储并访问、或者在网络中进行传输，就需要进行序列化和反序列化。
 *
 * 主流序列化方式对比：
 *   1. JSON
 *      - 优点：● 易读性好，可读性强，便于人类理解和调试。
 *             ● 跨语言支特广泛，几乎所有编程语言都有JSON的解析和生成库。
 *      - 缺点：● 序列化后的数据量相对较大，因为JSON使用文本格式存诸数据，需要额外的字符表示键、值和数据结构。
 *             ● 不能很好地处理复杂的数据结构和循环引用，可能导致性能下降或者序列化失败。
 *   2. Hessian
 *      - 优点：● 二进制序列化，序列化后的数据量较小，网络传输效率高。
 *             ● 支持跨语言，适用于分布式系统中的服务调用。
 *      - 缺点：● 性能较JSON略低，因为需要将对象转换为二进制格式。
 *             ● 对象必须实现Serializable接口，限制了可序列化的对象范围。
 *   3. Kryo
 *      - 优点：● 高性能，序列化和反序列化速度快。
 *             ● 支持循环引用和自定义序列化器，适用于复杂的对象结构
 *             ● 无需实现Serializable接口，可以序列化任意对象。
 *      - 缺点：● 不跨语言，只适用于Java。
 *             ● 对象的序列化格式不够友好，不易读懂和调试。
 *   4. Protobuf
 *      - 优点：● 高效的二进制序列化，序列化后的数据量极小。
 *             ● 跨语言支持，并目提供了多种语言的实现库。
 *             ● 支持版本化和向前/向后兼容性。
 *      - 缺点：● 配置相对复杂，需要先定义数据结构的消息格式。
 *             ● 对象的序列化格式不易读懂，不便于调试。
 */
public interface Serializer {
    /**
     * 序列化
     * @param object
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws IOException;
}
