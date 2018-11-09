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
    body:{"parameterTypeNames":["int","int"],"arguments":["11","22"]}
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