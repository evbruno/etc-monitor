var i = 0;

function timedCount() {
    i = i + 1;
    postMessage(i);
    setTimeout("timedCount()",500);
}

self.addEventListener("message", function(e) {
    console.log("worker got " + e);
}, false);


timedCount();
//
// function FetchFromAJAX(url, timeout) {
// }
//
// function FetchFromAJAX(url, timeout) {
//
// }
//
// axios.get(url).then(response => {
//     self.sharedData.servers = response.data.servers;
// self.sharedData.refreshRate = response.data.refreshRate;
// });
//
//
