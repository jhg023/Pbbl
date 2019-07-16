# Pbbl (pr. _pebble_, pɛbəl)
A thread-safe [ByteBuffer](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/nio/ByteBuffer.html) pool that allows for the automatic reuse of `ByteBuffer` objects, which can be over 30x faster than having to allocate a new `ByteBuffer` when needed.

# Maven/Gradle Dependency
Maven:
```xml
<dependency>
  <groupId>com.github.jhg023</groupId>
  <artifactId>Pbbl</artifactId>
  <version>1.0.1</version>
</dependency>
```
Gradle:
```groovy
implementation 'com.github.jhg023:Pbbl:1.0.1'
```

# How to Use
```java
// Create the HeapByteBufferPool (or DirectByteBufferPool).
var pool = new HeapByteBufferPool();

// Take a ByteBuffer (with 8 available bytes) from the pool.
// If the pool is empty, a new ByteBuffer will be created.
var buffer = pool.take(8);

// Do something with the ByteBuffer.
var array = buffer.putLong(42).array();

// Give the ByteBuffer back to the pool for re-use.
pool.give(buffer);
```
