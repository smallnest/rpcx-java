var request = require('request');

var options = {
    url: 'http://10.231.72.221:8033/',
    headers: {
        'User-Agent': 'Easy_Go_NodeJs_Client',
        'X-RPCX-ServicePath':'com.colobu.rpcx.service.TestService',
        'X-RPCX-ServiceMethod':'sum',
        'connection':'close'
    },
    method: 'POST',
    json:true,
    body:{"className":"com.colobu.rpcx.service.TestService","methodName":"sum","group":"","parameterTypeNames":["int","int"],"arguments":["11","22"],"url":{"protocol":"rpcx","host":"","port":0,"path":"com.colobu.rpcx.service.TestService.sum(\"11\",\"22\")","parameters":{}},"timeOut":1000,"retryNum":1,"sendType":"sync","languageCode":"JAVA","serializeType":"SerializeNone","failType":"FailFast","selectMode":"RandomSelect"}
};

function callback(error, response, body) {
    if (!error && response.statusCode == 200) {
        console.log(body);
        console.log(body.payload)
        console.log(new Buffer(body.payload).toString('ascii'))
    } else {
        console.log(error)
    }
}

request(options, callback)