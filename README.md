# JMerge

**JMerge** is a small Java library for merging JSON and YAML documents with flexible strategies.

Supported strategies:
* Object merging
  * `MERGE` - merge the objects (default)
  * `REPLACE` - replace the object
* Array merging
  * `APPEND` - add new elements to array (default)
  * `MERGE` - merge array elements by matching on element key field
  * `REPLACE` - replaces the array entirely

## Installation
Library can be found on [Maven Central][maven-central].
```xml
<dependency>
  <groupId>io.github.pstickney</groupId>
  <artifactId>jmerge</artifactId>
  <version>VESION</version>
</dependency>
```

You can also run a local build. 
```shell
mvn clean install
```

## Configuration
There is a couple different ways to configure the `MergeConfig` class.

### Defaults

```java
private Boolean prettyPrint = Boolean.FALSE;
private Strategy arrayStrategy = Strategy.APPEND;
private Strategy objectStrategy = Strategy.MERGE;
```

### Constructor
```java
// (prettyPrint, arrayStrategy, objectStrategy)
MergeConfig config = new MergeConfig(true, Strategy.APPEND, Strategy.REPLACE);
```

### Setters
```java
MergeConfig config = new MergeConfig();
config.setPrettyPrint(true);
config.setArrayStrategy(Strategy.APPEND);
config.setObjectStrategy(Strategy.REPLACE);
```

### Builder
```java
MergeConfig config = MergeConfig.builder()
    .prettyPrint(true)
    .arrayStrategy(Strategy.APPEND)
    .objectStrategy(Strategy.REPLACE)
    .build();
```

### Rules
By default, **jmerge** merges objects and appends array elements.
However, if we are trying to merge an array and the array contains objects (not primitives),
the library needs to know how to match these elements across the `base` and `overlay` in order 
to merge them correctly. 

This is where we can use `rules` to define how to match on array element merges.

#### Example

```yaml
# base
files:
  - name: a
    size: 1kb
  - name: b
    size: 4kb
  - name: c
    size: 10kb

# overlay
files:
  - name: b
    dir: /etc/conf/
  - name: c
    size: 5kb
  - name: d
    modified: today
```

Merge:
```java
MergeConfig config = MergeConfig.builder().build().addRule("files", "name");
YamlMerger merger = new YamlMerger(config);
String result = merger.merge(base, overlay);
```

Result:
```yaml
files:
  - name: "b"
    size: "4kb"
    dir: "/etc/conf/"
  - name: "c"
    size: "5kb"
  - name: "d"
    modified: "today"
```

## Usage

Basic Example
```java
public class Example {
    public static void main(String[] args) {
        String base = "{\"a\":1,\"b\":{\"x\":3}}";
        String overlay = "{\"b\":{\"y\":2},\"c\":4}";

        try {
            JsonMerger merger = new JsonMerger();
            String result = merger.merge(base, overlay);
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
// Output
// {"a":1,"b":{"x":3,"y":2},"c":4}
```

Rule Example
```java
public class Example {
    public static void main(String[] args) {
        String base = "{\"a\":1,\"b\":[{\"id\":1,\"x\":3},{\"id\":2,\"x\":1}]}";
        String overlay = "{\"b\":[{\"id\":2,\"y\":2},{\"id\":3,\"z\":4}],\"c\":4}";

        try {
            // Create a custom config and add a key to merge on in array 'b'
            MergeConfig config = MergeConfig.builder().build()
                .addRule("b", "id");

            JsonMerger merger = new JsonMerger(config);
            String result = merger.merge(base, overlay);
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

// Output - we can see id:1 was dropped, id:2 was merged, id:3 was added
// {"a":1,"b":[{"id":2,"x":1,"y":2},{"id":3,"z":4}],"c":4}
```

Customize Mapper Example
```java
public class Example {
    public static void main(String[] args) {
        String base = String.format("[{\"id\":1,\"time\":\"%s\"}]", Instant.now().minus(1, ChronoUnit.MINUTES));
        String overlay = String.format("[{\"id\":1},{\"id\":2,\"time\":\"%s\"}]", Instant.now().plus(1, ChronoUnit.MINUTES));

        try {
            // Create custom config that merges root array elements on key "id"
            MergeConfig config = MergeConfig.builder().build()
                .addRule("", "id");

            // Create a merger with the custom config and custom mapper config
            JsonMerger merger = new JsonMerger(config, mapper ->
                mapper.registerModule(new JavaTimeModule()));
            String result = merger.merge(base, overlay);
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
// Output
// [{"id":1,"time":"2025-09-05T12:01:00.00Z"},{"id":2,"time":"2025-09-05T12:03:0.000Z"}]
```

## Testing
Tests are defined as scenario directories under `src/test/resources/scenarios/{json,yaml}`.

Each directory contains:
* base.{json,yaml}
* overlay.{json,yaml}
* expected.{json,yaml}
* config.{json,yaml} (Optional)

The parameterized tests `JsonMergerTest` and `YamlMergerTest` will automatically discover and run them. 

[maven-central]: https://mvnrepository.com/artifact/io.github.pstickney/jmerge
