# IdsExportPlugin #
A plugin (to be more precise: set of plugins) for Solr allowing time-efficient export
of Ids of all found documents (or any DocValues-enabled field values) in comma-separated
format without sorting. Lack of result sorting results in significantly better
performance then Solr build-in `/export` endpoint.

Note: the plugin is developed and tested on standalone Solr instance, without any promises nor guarantees about Solr Cloud.

## Requirements
 * Solr version > 7.2 (tested with 7.2.1)
 * Solr running in standalone mode (Solr Cloud not supported) 

## Motivation
The initial motivation for creating this plugin was ability to produce output, which could be used as a direct input
for [Terms Query Parser](http://yonik.com/solr-terms-query/) in another Solr request. Example:
> First, search Car Brands index and give me IDs of all brands, which sell in Poland
>
>     http://localhost:8080/car_brands/select?availability:pl&fq={!ids field=brand_id}&wt=ids
>     Output: vw,opel,audi
> 
> Then, search Car Models index and give me models with electric engine:
>
>     http://localhost:8080/car_models/select?engine:electric&fq={!terms f=brand_id}vw,opel,audi     
  
Other possible use cases include:
 * simplifying batch jobs which do some calculation based on a full result set and doesn't require any
 document order (f.ex. _recalculate popularity for all product from Poland every day_) - removes necessity of paging    
 * creating reports - finding all documents matching criteria 
 * replacing `/export` endpoint when sorting is not required   

## Basic concepts
IdsExportPlugin consists of:
 * `IdsExportFilter`
 * `IdsExportSearchComponent`
 * `IdsExportResponseWriter`
 
The idea behind IdsExportPlugin is to use a post-filter (`IdsExportFilter`) as the last
filter during the request processing phase, which will collect all found Document Ids
in an optimized data structure. Then `IdsExportSearchComponent` will write those Ids
to the response, and `IdsExportResponseWriter` will output them in comma-separated format.

### `IdsExportFilter`
`IdsExportFilter` is a Solr post-filter. In Solr terminology, a filter is a piece of
code which decides, whether the document matches search criteria and should be included
in the response. A post-filter will be executed after regular filters, thanks to this it works on 
limited set of documents, already filtered by previous filters.

 `IdsExportFilter` implements the post-filter interface, but doesn't really decides if a
document matches search criteria or not - it accepts all documents - but instead it collects
certain field values from documents, and stores them in a data structure. The field name
is defined in the request [URL or configuration](#configuration).

This filter was initially designed to read values of the documents' unique key, but in fact
it can read values of any field, which has [DocValues](https://lucene.apache.org/solr/guide/7_2/docvalues.html)
enabled. **In this document we will refer to those values as `Ids`.**

Internally, Ids are stored in a data structure:
 * in case of fields with Numeric or Sorted Numeric DocValues, Ids (which are longs) are stored inside [`com.carrotsearch.hppc.LongArrayList`](http://carrotsearch.github.io/hppc/releases/0.7.3/api/com/carrotsearch/hppc/LongArrayList.html) (data structure based on array of primitive longs)
 * in case of fields with Binary, Sorted or SortedSet DocValues, Ids (which are Strings) are stored as an ArrayList of [`org.apache.lucene.util.BytesRef`](https://lucene.apache.org/core/7_2_1/core/org/apache/lucene/util/BytesRef.html) (lucene-optimized type for string binary values, mainly used for Strings)
 
Ids don't need to be unique - in case of repeated values, it will be stored a couple of times. 

### `IdsExportSearchComponent`
`IdsExportSearchComponent` is a search component (piece of code which executes after request processing, 
but before sending the response) which simply adds the collected Ids to the Solr response under the key defined
in the [configuration](#configuration). After this operation, response will contain additional list of Ids of all documents.

### `IdsExportResponseWriter`
The last component, `IdsExportResponseWriter`, transforms the Solr response into comma-separated list of Ids. All additional
response elements are skipped. The MIME type of the response is set to `text/plain`, encoding set to `UTF-8`.

Note: usage of `IdsExportResponseWriter` is optional. If you don't want a comma-separated format and you're fine
with standard Solr JSON/XML/etc. response - then you don't have to use `IdsExportResponseWriter`.

## Installation

1. Add JAR file to Solr's classpath ([https://lucene.apache.org/solr/guide/7_2/lib-directives-in-solrconfig.html](https://lucene.apache.org/solr/guide/7_2/lib-directives-in-solrconfig.html))

2. Add to `solrconfig.xml` following code

    ```xml
    <queryParser name="ids" class="pl.allegro.search.solr.ids.filter.IdsExportFilterParserPlugin">
        <int name="bufferInitialSize">100000</int>
    </queryParser>
    <searchComponent name="ids" class="pl.allegro.search.solr.ids.searchcomponent.IdsExportSearchComponent">
       <str name="responseKey">ids</str>
    </searchComponent>
    <queryResponseWriter name="ids" class="pl.allegro.search.solr.ids.responsewriter.IdsExportResponseWriter">
       <str name="responseKey">ids</str>
    </queryResponseWriter>
    ```
    The exact meaning of configuration parameters is described in [Configuration](#configuration)
    
    Each of those components may be registered under any valid name.
    * The name of the `IdsExportFilterParserPlugin` (which is a factory for `IdsExportFilter`) will be reflected in Solr URL (you will use it in requests
    to activate the plugin) - please give it some reasonable name. **In this document we will assume the name `ids`**
        <pre>http://localhost:8080/solr/core_name/select?q=*:*&fq={!<b>ids</b> field=product_id}</pre> 
    * We strongly recommend to give `IdsExportSearchComponent` the same name as in `IdsExportFilterParserPlugin` for simplicity.
    * The name of the `IdsExportResponseWriter` will be reflected in Solr URL (you will use it to change the output format) - please
    give it some reasonable name. We recommend the same name as in `IdsExportFilterParserPlugin` for simplicity. **In this document
    we will assume the name `ids`** 
        <pre>http://localhost:8080/solr/core_name/select?q=*:*&fq={!ids field=product_id}&wt=<b>ids</b></pre>

## Usage examples

```
http://localhost:8080/solr/core_name/select?q=*:*&fq={!ids field=product_id}&wt=ids

# Will output a comma-separated values of `product_id` field from all documents in the index.

Example response:
1,2,3,4,5,6
```


```
http://localhost:8080/solr/core_name/select?q=*:*&fq={!ids field=product_id}&rows=2

# Will output a list of values of `product_id` field as an additional Solr's response attribute.

Example response:
{
    "responseHeader": {
        "status": 0,
        "QTime": 2,
        "params": {
            "q": "*:*",
            "fq": "{!ids field=product_id}",
            "rows": "2"
        }
    },
    "response": {
        "numFound":6,
        "start": 0,
        "docs": [
            {
            "product_name": "Test 0",
            "product_id": "0"
            },
            {
            "product_name": "Test 1",
            "product_id": "1"
            }
        ]
    },
    "ids": [
        "0",
        "1",
        "2",
        "3",
        "4",
        "5",
        "6"
    ]
}

# Note: ids doesn't respect rows/start parameters - will always output everything found.
``` 

## Configuration

`IdsExportFilterParserPlugin` configuration options available in `solrconfig.xml`:
 * `bufferInitialSize` - initial size (in number of items) of the buffer for storing Ids. It should be a bit bigger than estimated average response size.
 Generally every number will work, however:
    - if set too low, the buffer will be extended a couple of times during request processing, resulting in increased
 CPU and memory consumption
    - if set too high, you will unnecessarily allocate a lot of memory
 Default value: *100 000*.
 * `defaultIndexField` - name of the field, where Ids are stored. This can be configured also on a per-request basics via URL parameter `field`,
 however in case of missing URL parameter the default configured here will be used. Default value: *doc_id*.

`IdsExportFilter` configuration options available in URL:
 * `field` - name of the field, where Ids are stored. Default value: configured in `defaultIndexField` in `solrconfig.xml`
   <pre>http://localhost:8080/solr/core_name/select?q=*:*&fq={!ids <b>field=product_id</b>}</pre> 
 
`IdsExportSearchComponent` configuration options available in `solrconfig.xml`:
 * `responseKey` - key in the Solr response where Ids should be stored. Default value: *ids*.
  
`IdsExportResponseWriter` configuration options available in `solrconfig.xml`:
 * `responseKey` - key in the Solr response where Ids are stored. The final Solr output will contains only comma-separated values from this field. Default value: *ids*.  
 * `separator` - a separator (char or String) used to separate values in the final Solr output. **In this document we will assume it is a comma**, 
 therefore we have used phrase "comma-separated" a couple of times, however it's possible to change it. Default value: *,* (comma)

## Performance

### Single query time comparison

In this test scenario, a single Solr instance was processing only a single request at once. Each request was sent three times
to Solr:
 1. To `/select` endpoint, with `rows=0`, and `IdsExportPlugin` enabled
 2. To `/export` endpoint, with sorting set to `Ids` (sorting was obligatory)
 3. To `/select` endpoint, with `rows` set to expected size od result set and sorting set to `Ids`, without `IdsExportPlugin` 

Note: given times are the total request time, including sending HTTP request, searching and downloading HTTP response. Technically,
times were measured using linux `time` command, which measured execution time of `curl` with a given query. Although this approach
is not a "clean" benchmark of the plugin itself, it also takes into account the overhead required to download a potentially large
response - and this also favors IdsExportPlugin, due to the very concise format of the output data - but it is also the closest
to the actual use cases of the plugin.
 
Results (times in seconds):

| numFound | IdsExportPlugin | /export | /select | 
|----------|-----------------|---------|---------| 
| 2        | 0.036           | 0.012   | 0.008   | 
| 1082     | 0.012           | 0.128   | 0.136   | 
| 12957    | 0.02            | 1.956   | 1.949   | 
| 225816   | 0.149           | 55.105  | 59.068  | 
| 1841320  | 0.681           | 393.532 | 396.918 | 
| 5971685  | 2.232           | 831.853 | 822.736 |

### Multi-threaded performance

In this test scenario, a single Solr instance was processing requests incoming via multiple connections concurrently.
Each request was sent to two endpoints:
 1. To `/select` endpoint, with `rows=0`, and `IdsExportPlugin` enabled
 2. To `/export` endpoint, with sorting set to `Ids` (sorting was obligatory)
 
The test scenario has been divided into three test cases. In each test case a set of unique phrases has been used,
selected to give the expected number of results:
 1. between 20 000 and 50 000 (phrases giving "small" result sets)
 2. between 50 000 and 280 000 (phrases giving "medium sized" result sets)
 3. between 280 000 and 3 100 000 (phrases giving "large" result sets)

This test scenario was carried out using Apache JMeter. All results presented below come from JMeter results.
 
Results:

|  concurrent connections | requests per connection | total request count | result set size per request | IdsExportPlugin&nbsp;RPS | IdsExportPlugin&nbsp;avg | IdsExportPlugin&nbsp;Max | /export&nbsp;RPS | /export&nbsp;avg | /export&nbsp;max |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
|  30 | 80 | 2400 | 20000-50000 | 489.50&nbsp;rps | 47.00&nbsp;ms | 190.00&nbsp;ms | 3.00&nbsp;rps | 9414.00&nbsp;ms | 26972.00&nbsp;ms |
|  30 | 80 | 2400 | 50000-280000 | 199.10&nbsp;rps | 127.00&nbsp;ms | 325.00&nbsp;ms | 0.80&nbsp;rps | 35663.00&nbsp;ms | 126313.00&nbsp;ms |
|  30 | 22 | 660 | 280000-3100000 | 30.00&nbsp;rps | 796.00&nbsp;ms | 2669.00&nbsp;ms | 0.10&nbsp;rps | 230305.00&nbsp;ms | 812294.00&nbsp;ms |

### Performance - summary

The presented results clearly show that the use of `IdsExportPlugin` highly speeds up Ids export from Solr - response time and throughput may be 
a **couple of hundred times better** than in case of Solr built-in `/export` or `/select` endpoints.

The largest performance killer `/export` and `/select` is result set sorting. `IdsExportPlugin`
does not perform any sorting, just outputs all found Ids in order they are processed by Solr. 

### Memory consumption

Memory consumption of `IdsExportPlugin` is not higher then memory consumption of the standard `/export` endpoint.

On the one hand, `IdsExportPlugin` require a data structure which size is proportional to the amount of found documents,
so the bigger result sets are found, the more memory is required for processing.

On the other hand, standard `/export` endpoint also require some data structure with size proportional
to the result set size for sorting purposes. Therefore the overall memory footprint of `IdsExportPlugin`
will not be higher then `/export`'s.

**Pro tip:**

Generally it's best to use `IdsExportPlugin` with fields, which have DocValues of type `Numeric` or `SortedNumeric` - in this case
the data structure is [`com.carrotsearch.hppc.LongArrayList`](http://carrotsearch.github.io/hppc/releases/0.7.3/api/com/carrotsearch/hppc/LongArrayList.html),
which internally relies on array of primitive longs.

All other field types will store it's Ids inside ArrayList of [`org.apache.lucene.util.BytesRef`](https://lucene.apache.org/core/7_2_1/core/org/apache/lucene/util/BytesRef.html) - 
an optimized way of storing Strings. 




## Build
`./gradlew clean build`

## License
This software is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).