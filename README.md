# irk 
easy to use HTTP benchmark tool written in scala.

Inspired by wrk (https://github.com/wg/wrk), this benchmark tool has out of the box extra functionality.

# Getting started with simple request

java -jar irk-0.1.jar -t 10 -c 50 -d 5s http://test.com:1234/test

this will run 5 parallel clients(threads) that shares 100 http connections
and run them for 1 minute

output:
run in sequence = false
number of connections = 50
number of threads per client = 10
running time = 5 seconds
requests file location = NA
api to call = http://test.com:1234/test
        
starting 10 threads

[200] occurred 4899 times

[Connection refused: localhost/0:0:0:0:0:0:0:1:1234] occurred 1 times

[http://localhost:1234] occurred 3 times

Total requests: 4977
995 REQ/SEC

## Command Line Options

usage: java -jar irk-0.1.jar [options] [<HTTP GET>...]

  -c, --connectionsNum <value>
                           number of clients
  -t, --threadsNum <value>
                           number of threads per irk.client
  -d, --duration <value>   duration (2s,15m,1h)
  -s, --sequenced <value>  run all requests in sequence
  -f, --requestsFile <value>
                           number of clients
  <HTTP GET>...            optional request url
  --help                   prints this usage text

## using request file

in order to send a sequence or a batch of specific http request
need to create a file with raw http requests.
all requests must be separated with ---END--- separator.

Tip: raw request can be easily created with postman

example:
```
GET /docs/index.html HTTP/1.1
Host: www.test1.com
Header: 1

---END---
PUT /docs/index.html HTTP/1.1
Host: www.test2.com
Header: 2

{body}
---END---
POST /docs/index.html HTTP/1.1
Host: www.test3.com
Header: 3

{body}
---END---
DELETE /docs/index.html HTTP/1.1
Host: www.test4.com
Header: 4

{body}
```
## building jar
sbt assembly

## upcoming roadmap
- improve performance by testing more clients (current is AsynApacheHttp)


#### link for execution jar
https://drive.google.com/open?id=1LyVWDhRZpMTLGtdby0UbaWv-0s6XKeF-

usage: java -jar irk-0.1.jar -t 4 -c 15 -d 2s http://localhost:12345/wrk
