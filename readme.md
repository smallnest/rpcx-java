## rpcx-java

[![License](https://img.shields.io/:license-apache%202-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![GoDoc](https://godoc.org/github.com/smallnest/rpcx-java?status.png)](http://godoc.org/github.com/smallnest/rpcx-java)  [![travis](https://travis-ci.org/smallnest/rpcx-java.svg?branch=master)](https://travis-ci.org/smallnest/rpcx-java) [![codecov](https://codecov.io/gh/smallnest/rpcx-java/branch/master/graph/badge.svg)](https://codecov.io/gh/smallnest/rpcx-java)


rpcx-java is java implementation of [rpcx](https://github.com/smallnest/rpcx).

Until now it implements rpcx client with raw protocol.


### Why not use [rpcx-gateway](https://github.com/rpcx-ecosystem/rpcx-gateway) or direct http invoking?

Yes, you can write a http client to invoke rpcx services via [rpcx-gateway](https://github.com/rpcx-ecosystem/rpcx-gateway), event direct http invoking,

but http protocol is slower than direct TCP communications.

This client can access rpcx services via raw rpcx protocol sync ot async.


## roadmap

- 1.x: implement simple rpcx client
- 2.x: support Service discovery and service governance
- 3.x: support develop services in Java 